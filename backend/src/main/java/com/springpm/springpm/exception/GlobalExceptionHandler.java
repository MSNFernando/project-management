package com.springpm.springpm.exception;

import com.springpm.springpm.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

/**
 * One place that turns exceptions into API responses.
 *
 * Messages sent to the client are ones we wrote. Database, framework and library
 * messages (which can include SQL, table names or stack details) are logged with a
 * reference ID and never sent; the client gets the reference so a report can be
 * matched to the log line.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> unauthorized(UnauthorizedException e) {
        return respond(HttpStatus.UNAUTHORIZED, e.getMessage(), "UNAUTHORIZED");
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> forbidden(RuntimeException e) {
        String message = e instanceof ForbiddenException ? e.getMessage() : "You don't have access to this.";
        return respond(HttpStatus.FORBIDDEN, message, "FORBIDDEN");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(ResourceNotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage(), "NOT_FOUND");
    }

    /** A NOT NULL, UNIQUE or foreign-key constraint failed. The SQL stays in the log. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> dataIntegrity(DataIntegrityViolationException e) {
        String ref = reference();
        log.warn("[{}] Data integrity violation: {}", ref, e.getMostSpecificCause().getMessage());
        return respond(HttpStatus.BAD_REQUEST,
                "Some required information is missing, or it conflicts with existing data. Reference: " + ref,
                "DATA_CONFLICT");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> unreadable(HttpMessageNotReadableException e) {
        return respond(HttpStatus.BAD_REQUEST, "The request body isn't valid JSON, or a value has the wrong type.", "BAD_REQUEST");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> typeMismatch(MethodArgumentTypeMismatchException e) {
        return respond(HttpStatus.BAD_REQUEST, "Invalid value for " + e.getName() + ".", "BAD_REQUEST");
    }

    /** Anything else: Spring MVC's own errors keep their status; the rest is our bug. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unexpected(Exception e) {
        if (e instanceof ErrorResponse errorResponse) {
            // Unknown route, wrong method, missing parameter...: a client error, not a 500
            HttpStatusCode status = errorResponse.getStatusCode();
            HttpStatus known = HttpStatus.resolve(status.value());
            String message = known != null ? known.getReasonPhrase() : "Request failed";
            return ResponseEntity.status(status).body(ApiResponse.error(message, "REQUEST_FAILED"));
        }
        String ref = reference();
        log.error("[{}] Unhandled exception", ref, e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on our side. Reference: " + ref, "INTERNAL_ERROR");
    }

    private static ResponseEntity<ApiResponse<Void>> respond(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status).body(ApiResponse.error(message, code));
    }

    private static String reference() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
