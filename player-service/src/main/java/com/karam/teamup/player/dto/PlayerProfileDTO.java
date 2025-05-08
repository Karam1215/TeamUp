package com.karam.teamup.player.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "DTO for player profile")
public record PlayerProfileDTO(
        String playerId,
        String username,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String gender,
        String city,
        String profilePicture,
        LocalDateTime createdAt,
        String bio,
        String sport,
        String teamId
) {
}
