package expenseTracker.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// Turns every exception from a controller into the same JSON shape (see ErrorResponse)
@RestControllerAdvice
public class    GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Errors we threw on purpose: expected, so no stack trace
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException e, HttpServletRequest request) {
        log.debug("{} {} -> {} {}", request.getMethod(), request.getRequestURI(), e.getCode(), e.getMessage());
        return respond(e.getCode(), e.getMessage(), request);
    }

    // Body isn't valid JSON, or a field has the wrong type
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.debug("{} {} -> unreadable body: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return respond(ErrorCode.BAD_REQUEST, "Request body is missing or not valid JSON", request);
    }

    // e.g. /card/delete/abc where a number is expected
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        return respond(ErrorCode.BAD_REQUEST, "Invalid value for '" + e.getName() + "'", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoRoute(NoResourceFoundException e, HttpServletRequest request) {
        return respond(ErrorCode.NOT_FOUND, "No endpoint " + request.getMethod() + " " + request.getRequestURI(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethod(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return respond(ErrorCode.METHOD_NOT_ALLOWED, e.getMessage(), request);
    }

    // Anything else is a bug: log the full stack trace, but don't leak internals to the app
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("Unhandled error on {} {}", request.getMethod(), request.getRequestURI(), e);
        return respond(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), request);
    }

    private ResponseEntity<ErrorResponse> respond(ErrorCode code, String message, HttpServletRequest request) {
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code, message, request.getRequestURI()));
    }
}
