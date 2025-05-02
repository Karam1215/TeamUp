package com.karam.teamup.player.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO for updating players profile")
public record UpdatePlayerProfileDTO(

        UUID playerId,

        String username,

        String lastName,

        @Past(message = "Invalid date")
        LocalDate birthDate,

        String gender,

        String city,

        String bio,

        String sport
) {
}
