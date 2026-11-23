package expenseTracker.demo.service;

import expenseTracker.demo.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

// A session = one login on one device = one refresh_token_entity row
@Service
public class SessionService {
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    public boolean isActive(long sessionId) {
        return refreshTokenRepository.findById(sessionId)
                .map(session -> !session.isRevoked() && session.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }
}