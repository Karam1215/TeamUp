package com.karam.teamup.player.exceptions;

public class TeamAlreadyExistsException extends RuntimeException {
    public TeamAlreadyExistsException(String teamName) {
        super("Team with name " + teamName + " already exists.");
    }
}
