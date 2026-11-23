package expenseTracker.demo.service;

import expenseTracker.demo.exception.ApiException;
import expenseTracker.demo.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    // Claim holding the login session (refresh_token_entity row) this access token belongs to
    private static final String SESSION_CLAIM = "sid";

    private final SecretKey key;
    private final long accessTtlSeconds;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access-ttl-seconds}") long accessTtlSeconds) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTtlSeconds = accessTtlSeconds;
    }

    public String generateAccessToken(long userId, long sessionId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(SESSION_CLAIM, sessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .signWith(key)
                .compact();
    }

    // Checks signature and expiry; throws ApiException(UNAUTHORIZED) if either fails
    public AccessToken parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            // Number, not Long: the Gson parser reads JSON numbers back as Double
            Number sessionId = claims.get(SESSION_CLAIM, Number.class);
            if (sessionId == null) {
                throw new ApiException(ErrorCode.UNAUTHORIZED, "Access token has no session");
            }
            return new AccessToken(Long.parseLong(claims.getSubject()), sessionId.longValue());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid or expired access token");
        }
    }

    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    public record AccessToken(long userId, long sessionId) {
    }
}