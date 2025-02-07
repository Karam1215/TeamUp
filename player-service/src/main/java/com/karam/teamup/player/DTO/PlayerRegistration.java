package com.karam.teamup.player.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlayerRegistration(

        @NotBlank(message = "Username is required")
    String userName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 7, max = 255, message = "Password must be 7-255 characters")
    String password
) {
}