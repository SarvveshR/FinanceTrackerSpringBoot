package expenseTracker.demo.exception;

// Throw this from anywhere; GlobalExceptionHandler turns it into the JSON error response
public class ApiException extends RuntimeException {
    private final ErrorCode code;

    public ApiException(ErrorCode code) {
        this(code, code.getDefaultMessage());
    }

    public ApiException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
