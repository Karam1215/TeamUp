package com.karam.teamup.authentication.dto;

import com.karam.teamup.authentication.entities.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String username,
        String email,

        @Enumerated(EnumType.STRING)
        Role role
) {
}
