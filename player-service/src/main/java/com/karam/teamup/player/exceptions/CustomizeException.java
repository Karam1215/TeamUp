package com.karam.teamup.player.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.time.ZonedDateTime;

public record CustomizeException(String message, HttpStatus httpStatus, ZonedDateTime timestamp) {
}
