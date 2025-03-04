package com.karam.teamup.authentication.dto;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String username,
        String email
) {
}
