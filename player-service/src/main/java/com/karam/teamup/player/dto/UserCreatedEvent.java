package com.karam.teamup.player.dto;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String username,
        String email
) {
}
