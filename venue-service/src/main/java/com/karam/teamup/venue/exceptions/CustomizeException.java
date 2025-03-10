package com.karam.teamup.venue.exceptions;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public record CustomizeException(String message, HttpStatus httpStatus, ZonedDateTime timestamp) {
}
