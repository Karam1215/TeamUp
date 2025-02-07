package com.karam.teamup.player.DTO;

import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UpdatePlayerProfileDTO(

        String firstName,

        String lastName,

        @Past
        LocalDate birthDate,

        String gender,

        String city,

        String bio,

        String sport
) {
}
