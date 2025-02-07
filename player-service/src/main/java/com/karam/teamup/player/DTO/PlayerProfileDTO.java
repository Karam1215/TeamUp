package com.karam.teamup.player.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(name = "DTO for player profile")
public record PlayerProfileDTO(
        String userName,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String gender,
        String city,
        String profilePicture,
        LocalDateTime createdAt,
        String bio,
        String sport
) {
}
