package com.karam.teamup.player.exceptions;

public class PlayerIsNotLeaderException extends RuntimeException {
    public PlayerIsNotLeaderException(String message) {
        super(message);
    }
}
