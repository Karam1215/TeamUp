package com.karam.teamup.authentication.exception;

import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String GENERIC_AUTH_ERROR = "Authentication failed. Please check your credentials.";
    private static final String GENERIC_JWT_ERROR = "Invalid authentication token. Please log in again.";
    private static final String GENERIC_SERVER_ERROR = "An unexpected error occurred. Please try again later.";

    /**
     * Handle validation errors for request bodies
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {} fields failed validation", ex.getBindingResult().getFieldErrorCount());

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing + ", " + replacement
            ));

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handle duplicate email registration attempts
     */
    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<CustomizeException> handleEmailConflict(EmailAlreadyExistException ex) {
        log.warn("Email conflict: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * Handle duplicate username registration attempts
     */
    @ExceptionHandler(UserNameAlreadyExist.class)
    public ResponseEntity<CustomizeException> handleUsernameConflict(UserNameAlreadyExist ex) {
        log.warn("Registration conflict: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * Handle invalid login credentials (generic message for security)
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<CustomizeException> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Authentication failure: {}", ex.getMessage());
        return buildErrorResponse(GENERIC_AUTH_ERROR, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle JWT-related exceptions
     */
    @ExceptionHandler({SignatureException.class, InvalidTokenException.class, MalformedTokenException.class})
    public ResponseEntity<CustomizeException> handleJwtExceptions(RuntimeException ex) {
        log.warn("JWT validation failed: {}", ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.set("WWW-Authenticate", "Bearer error=\"invalid_token\"");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .headers(headers)
                .body(new CustomizeException(
                        GENERIC_JWT_ERROR,
                        HttpStatus.UNAUTHORIZED,
                        ZonedDateTime.now()
                ));
    }

    /**
     * Handle expired tokens
     */
    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<CustomizeException> handleExpiredToken(ExpiredTokenException ex) {
        log.warn("Expired token: {}", ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.set("WWW-Authenticate", "Bearer error=\"invalid_token\", error_description=\"Token expired\"");

        // Build the response with headers
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .headers(headers)
                .body(new CustomizeException(
                        GENERIC_JWT_ERROR,
                        HttpStatus.UNAUTHORIZED,
                        ZonedDateTime.now()
                ));
    }

    /**
     * Handle all unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomizeException> handleGenericException(Exception ex) {
        log.error("Unexpected error: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return buildErrorResponse(GENERIC_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Build standardized error response
     */
    private ResponseEntity<CustomizeException> buildErrorResponse(String message, HttpStatus status) {
        return ResponseEntity
            .status(status)
            .body(new CustomizeException(message, status, ZonedDateTime.now()));
    }

    /**
    * Handle all JWT exceptions
    */
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<CustomizeException> handleJwtException(JwtAuthenticationException ex, HttpStatus status) {
        log.warn("Authentication failure JwtAuthenticationException: {}", ex.getMessage());

        return ResponseEntity
                .status(status)
                .body(new CustomizeException(
                        ex.getMessage(),
                        status,
                        ZonedDateTime.now()
                )
        );
    }

}