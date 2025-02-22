package com.karam.teamup.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "DTO for changing a player's password")
public record ChangePasswordRequest(
    @NotBlank(message = "Current password is required.")
    @Size(min = 7, max = 255, message = "Current password should be between 7 and 255 characters.")
    @Schema(description = "The current password of the player", example = "oldPass123")
    String currentPassword,

    @NotBlank(message = "New password is required.")
    @Size(min = 7, max = 255, message = "New password should be between 7 and 255 characters.")
    @Schema(description = "The new password that the player wants to set", example = "newPass456")
    String newPassword,

    @NotBlank(message = "Password confirmation is required.")
    @Size(min = 7, max = 255, message = "Password confirmation should be between 7 and 255 characters.")
    @Schema(description = "Confirmation of the new password to ensure they match", example = "newPass456")
    String passwordConfirmation
) {
}
