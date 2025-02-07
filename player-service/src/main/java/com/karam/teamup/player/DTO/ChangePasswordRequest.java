package com.karam.teamup.player.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "Current password is required.")
    @Size(min = 7, max = 255, message = "Current password should be between 7 and 255 characters.")
    String currentPassword,

    @NotBlank(message = "New password is required.")
    @Size(min = 7, max = 255, message = "New password should be between 7 and 255 characters.")
    String newPassword,

    @NotBlank(message = "Password confirmation is required.")
    @Size(min = 7, max = 255, message = "Password confirmation should be between 7 and 255 characters.")
    String confirmPassword
) {
}