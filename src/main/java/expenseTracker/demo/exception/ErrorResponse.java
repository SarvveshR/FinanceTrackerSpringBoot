package expenseTracker.demo.exception;

import java.time.Instant;

// The body of every error: {"code":"CARD_NOT_FOUND","message":"Card not found","status":404,"path":"/card/edit/7","timestamp":"..."}
public record ErrorResponse(String code, String message, int status, String path, String timestamp) {

    public static ErrorResponse of(ErrorCode code, String message, String path) {
        return new ErrorResponse(code.name(), message, code.getStatus().value(), path, Instant.now().toString());
    }

    // Hand-written JSON for places outside Spring MVC (the security filter chain)
    public String toJson() {
        return "{\"code\":\"" + code + "\",\"message\":\"" + escape(message) + "\",\"status\":" + status
                + ",\"path\":\"" + escape(path) + "\",\"timestamp\":\"" + timestamp + "\"}";
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
