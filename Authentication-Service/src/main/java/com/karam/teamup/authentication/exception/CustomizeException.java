package com.karam.teamup.authentication.exception;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public record CustomizeException(String message, HttpStatus httpStatus, ZonedDateTime timestamp) {

}
