package expenseTracker.demo.exception;

import org.springframework.http.HttpStatus;

// Every error the API can return. The app switches on the name ("code" in the JSON), so don't rename them.
public enum ErrorCode {

    // 400: the request itself is wrong
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "The request is invalid"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Some fields are missing or invalid"),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "Unknown category"),

    // 401: not logged in, or the proof of login is no good
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Not logged in or session expired"),
    INVALID_GOOGLE_TOKEN(HttpStatus.UNAUTHORIZED, "Google sign-in could not be verified"),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "Session expired, please sign in again"),

    // 404: doesn't exist, or belongs to another user (same answer, so IDs can't be probed)
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not found"),
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "Card not found"),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Transaction not found"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "This endpoint doesn't support that method"),

    // 409: valid request, but the card's money rules don't allow it
    INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "Not enough balance on this card"),
    CREDIT_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "This would go over the card's credit limit"),
    CREDIT_LIMIT_BELOW_USED(HttpStatus.CONFLICT, "Credit limit can't be lower than the amount already used"),
    PAYMENT_EXCEEDS_USED(HttpStatus.CONFLICT, "Payment is more than the amount used on this card"),

    // 5xx: our fault
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on the server"),
    GOOGLE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Couldn't reach Google to verify the sign-in");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
