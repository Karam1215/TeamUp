package com.karam.teamup.player.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO for player registration")
public record PlayerRegistration(
        @NotBlank(message = "Username can't be empty")
        @Size(min = 1, max = 100, message = "Username must be between 1 and 100 characters")
        @Schema(description = "userName", example = "karam", requiredMode = Schema.RequiredMode.REQUIRED)
        String userName,

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