package com.karam.teamup.venue.dto;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String username,
        String email
) {
}
