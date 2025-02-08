package com.karam.teamup.player.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

@Schema(description = "DTO for updating players profile")
public record UpdatePlayerProfileDTO(

        String firstName,

        String lastName,

        @Past(message = "Invalid date")
        LocalDate birthDate,

        String gender,

        String city,

        String bio,

        String sport
) {
}
