package expenseTracker.demo.security;

import expenseTracker.demo.exception.ErrorCode;
import expenseTracker.demo.exception.ErrorResponse;
import expenseTracker.demo.service.JwtService;
import expenseTracker.demo.service.SessionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService, SessionService sessionService)
            throws Exception {
        http
                // No cookies or server sessions: every request proves itself with its own token
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // These carry their own proof (Google ID token / refresh token) in the body
                        .requestMatchers("/auth/google", "/auth/refresh", "/auth/logout", "/status", "/error").permitAll()
                        .anyRequest().authenticated())
                // Runs before any controller, so it writes the standard error JSON itself
                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write(ErrorResponse.of(ErrorCode.UNAUTHORIZED,
                            ErrorCode.UNAUTHORIZED.getDefaultMessage(), request.getRequestURI()).toJson());
                }))
                .addFilterBefore(new JwtAuthFilter(jwtService, sessionService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Stops Spring Boot from creating its default "user" account and printing a generated password
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}