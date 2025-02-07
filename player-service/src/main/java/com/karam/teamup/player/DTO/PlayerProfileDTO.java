    package com.karam.teamup.player.DTO;

    import java.time.LocalDate;
    import java.time.LocalDateTime;

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
