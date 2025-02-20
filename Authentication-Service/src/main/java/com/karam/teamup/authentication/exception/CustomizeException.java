package com.karam.teamup.authentication.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Getter
public class CustomizeException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final ZonedDateTime timestamp;

    public CustomizeException(String message, HttpStatus httpStatus, ZonedDateTime timestamp) {
        super(message);
        this.httpStatus = httpStatus;
        this.timestamp = timestamp;
    }
}
