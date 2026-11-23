package expenseTracker.demo.security;

import expenseTracker.demo.exception.ApiException;
import expenseTracker.demo.service.JwtService;
import expenseTracker.demo.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Reads "Authorization: Bearer <token>". If the token is valid and its session is still active,
// the request runs as that user. Otherwise it continues anonymous and SecurityConfig answers 401.
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final SessionService sessionService;

    public JwtAuthFilter(JwtService jwtService, SessionService sessionService) {
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                JwtService.AccessToken token = jwtService.parseAccessToken(header.substring(7));
                // A logged-out session kills its access tokens immediately, not when they expire
                if (sessionService.isActive(token.sessionId())) {
                    CurrentUser user = new CurrentUser(token.userId(), token.sessionId());
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(user, null, List.of()));
                } else {
                    log.debug("{} {}: session {} is logged out or expired", request.getMethod(), request.getRequestURI(), token.sessionId());
                }
            } catch (ApiException e) {
                log.debug("{} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}