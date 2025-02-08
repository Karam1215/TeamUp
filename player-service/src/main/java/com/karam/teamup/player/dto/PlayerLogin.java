package com.karam.teamup.player.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "DTO for player login")
public record PlayerLogin(

        @NotBlank(message = "Email can't be empty")
        @Email(message = "Invalid email format")
        @Schema(description = "Player's email", example = "karam@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 7, max = 255, message = "Password must be 7-255 characters")
        @Schema(description = "Player's password", example = "pass12345", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
