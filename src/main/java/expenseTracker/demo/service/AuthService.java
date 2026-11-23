package expenseTracker.demo.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import expenseTracker.demo.entities.RefreshTokenEntity;
import expenseTracker.demo.entities.UserEntity;
import expenseTracker.demo.exception.ApiException;
import expenseTracker.demo.exception.ErrorCode;
import expenseTracker.demo.repository.RefreshTokenRepository;
import expenseTracker.demo.repository.UserRepository;
import expenseTracker.demo.responseDTOs.ResponseAuthDTO;
import expenseTracker.demo.responseDTOs.ResponseUserDTO;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    GoogleTokenVerifierService googleTokenVerifierService;
    @Autowired
    JwtService jwtService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-ttl-days}")
    long refreshTtlDays;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ResponseAuthDTO googleLogin(String idToken) {
        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(idToken);

        // First login creates the user; later logins refresh name/photo in case they changed on Google
        UserEntity user = userRepository.findByGoogleSub(payload.getSubject()).orElseGet(() -> {
            log.debug("googleLogin: first login, creating new user");
            UserEntity newUser = new UserEntity();
            newUser.setGoogleSub(payload.getSubject());
            newUser.setCreatedAt(Instant.now());
            return newUser;
        });
        user.setEmail(payload.getEmail());
        user.setName((String) payload.get("name"));
        user.setPictureUrl((String) payload.get("picture"));
        UserEntity savedUser = userRepository.save(user);
        log.debug("googleLogin: userId={} logged in", savedUser.getId());

        return issueTokens(savedUser);
    }

    // Swaps a refresh token for a new pair. The old refresh token is revoked, so each one works once.
    @Transactional
    public ResponseAuthDTO refresh(String refreshToken) {
        RefreshTokenEntity session = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_EXPIRED, "Unknown refresh token"));
        if (session.isRevoked() || session.getExpiresAt().isBefore(Instant.now())) {
            log.debug("refresh: session {} is logged out or expired", session.getId());
            throw new ApiException(ErrorCode.SESSION_EXPIRED);
        }
        session.setRevoked(true);
        refreshTokenRepository.save(session);

        UserEntity user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_EXPIRED, "User no longer exists"));
        log.debug("refresh: userId={} session {} rotated", user.getId(), session.getId());
        return issueTokens(user);
    }

    // Ends the session: its refresh token and every access token issued for it stop working at once
    // Takes the refresh token (not the access token) so logout still works after the access token expired
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(hash(refreshToken)).ifPresentOrElse(session -> {
            session.setRevoked(true);
            refreshTokenRepository.save(session);
            log.debug("logout: userId={} session {} revoked", session.getUserId(), session.getId());
        }, () -> log.debug("logout: unknown refresh token, nothing to revoke"));
    }

    public ResponseUserDTO getUser(long userId) {
        return userRepository.findById(userId).map(this::toUserDTO)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    private ResponseAuthDTO issueTokens(UserEntity user) {
        String refreshToken = generateRefreshToken();

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setUserId(user.getId());
        refreshTokenEntity.setTokenHash(hash(refreshToken));
        refreshTokenEntity.setExpiresAt(Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS));
        refreshTokenEntity.setRevoked(false);
        RefreshTokenEntity session = refreshTokenRepository.save(refreshTokenEntity);

        ResponseAuthDTO responseAuthDTO = new ResponseAuthDTO();
        responseAuthDTO.setAccessToken(jwtService.generateAccessToken(user.getId(), session.getId()));
        responseAuthDTO.setRefreshToken(refreshToken);
        responseAuthDTO.setExpiresIn(jwtService.getAccessTtlSeconds());
        responseAuthDTO.setUser(toUserDTO(user));
        return responseAuthDTO;
    }

    public ResponseUserDTO toUserDTO(UserEntity user) {
        ResponseUserDTO responseUserDTO = new ResponseUserDTO();
        responseUserDTO.setId(user.getId());
        responseUserDTO.setEmail(user.getEmail());
        responseUserDTO.setName(user.getName());
        responseUserDTO.setPictureUrl(user.getPictureUrl());
        return responseUserDTO;
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // Only the hash is stored, so a leaked database can't be used to log in
    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
