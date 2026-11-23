package expenseTracker.demo.controllers;

import expenseTracker.demo.exception.ApiException;
import expenseTracker.demo.exception.ErrorCode;
import expenseTracker.demo.requestDtos.RequestGoogleAuthDTO;
import expenseTracker.demo.requestDtos.RequestRefreshDTO;
import expenseTracker.demo.responseDTOs.ResponseAuthDTO;
import expenseTracker.demo.responseDTOs.ResponseUserDTO;
import expenseTracker.demo.security.CurrentUser;
import expenseTracker.demo.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthService authService;

    @PostMapping("/google")
    public ResponseAuthDTO googleLogin(@RequestBody RequestGoogleAuthDTO request) {
        log.debug("POST /auth/google");
        requireNotBlank(request.getIdToken(), "idToken is required");
        ResponseAuthDTO responseAuthDTO = authService.googleLogin(request.getIdToken());
        log.debug("POST /auth/google ok, userId={}", responseAuthDTO.getUser().getId());
        return responseAuthDTO;
    }

    @PostMapping("/refresh")
    public ResponseAuthDTO refresh(@RequestBody RequestRefreshDTO request) {
        log.debug("POST /auth/refresh");
        requireNotBlank(request.getRefreshToken(), "refreshToken is required");
        return authService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RequestRefreshDTO request) {
        log.debug("POST /auth/logout");
        requireNotBlank(request.getRefreshToken(), "refreshToken is required");
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseUserDTO me(@AuthenticationPrincipal CurrentUser user) {
        log.debug("GET /auth/me userId={}", user.userId());
        return authService.getUser(user.userId());
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, message);
        }
    }
}
