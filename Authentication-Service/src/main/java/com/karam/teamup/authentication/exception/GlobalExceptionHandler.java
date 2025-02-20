package com.karam.teamup.authentication.exception;

import io.jsonwebtoken.security.SignatureException;
import jakarta.xml.bind.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {} fields failed validation", ex.getBindingResult().getFieldErrors().size());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            log.warn("Validation failed for field '{}': {}", error.getField(), error.getDefaultMessage());
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = EmailAlreadyExistException.class)
    public ResponseEntity<Object> emailAlreadyExist(EmailAlreadyExistException e) {
        log.warn("Email already exists: {}", e.getMessage());

        CustomizeException customizeException = new CustomizeException(
                e.getMessage(),
                HttpStatus.CONFLICT,
                ZonedDateTime.now()
        );
        return new ResponseEntity<>(customizeException, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = UserNameAlreadyExist.class)
    public ResponseEntity<Object> nameAlreadyExist(UserNameAlreadyExist e) {
        log.warn("Username already exists: {}", e.getMessage());

        CustomizeException customizeException = new CustomizeException(
                e.getMessage(),
                HttpStatus.CONFLICT,
                ZonedDateTime.now()
        );
        return new ResponseEntity<>(customizeException, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(value = InvalidCredentialsException.class)
    public ResponseEntity<Object> invalidCredentials(InvalidCredentialsException e) {
        log.warn("Invalid login attempt: {}", e.getMessage());

        CustomizeException customizeException = new CustomizeException(
                e.getMessage(),
                HttpStatus.UNAUTHORIZED,
                ZonedDateTime.now()
        );
        return new ResponseEntity<>(customizeException, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> handleInvalidJwtSignature(SignatureException ex) {
        log.warn("JWT signature validation failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT signature. Please log in again.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);

        CustomizeException customizeException = new CustomizeException(
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now()
        );
        return new ResponseEntity<>(customizeException, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException e) {
        log.warn("Validation error: {}", e.getMessage());

        CustomizeException customizeException = new CustomizeException(
                e.getMessage(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now()
        );
        return new ResponseEntity<>(customizeException, HttpStatus.BAD_REQUEST);
    }
}