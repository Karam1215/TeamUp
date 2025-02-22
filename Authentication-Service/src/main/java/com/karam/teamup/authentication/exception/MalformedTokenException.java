package com.karam.teamup.authentication.exception;

public class MalformedTokenException extends RuntimeException {
    public MalformedTokenException(String message) {
        super(message);
    }

    public MalformedTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
