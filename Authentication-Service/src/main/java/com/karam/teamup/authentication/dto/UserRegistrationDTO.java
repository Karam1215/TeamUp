package com.karam.teamup.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationDTO(

        @NotBlank(message = "username can't be empty")
        @Size(min = 1, max = 100, message = "Username should be between 1 and 100 characters.")
        @Schema(name = "username", example = "Karam", requiredMode = Schema.RequiredMode.REQUIRED, description = "Username. Should be between 1 and 100 characters.")
        String username,

        @NotBlank(message = "Email can't be empty")
        @Email(message = "Please enter a valid email address.")
        @Schema(name = "email", example = "2002@hotmail.com", requiredMode = Schema.RequiredMode.REQUIRED, description = "users email. Should be unique and in a valid format.")
        String email,

        @NotBlank(message = "Password can't be empty")
        @Column(name = "password")
        @Size(min = 7, max = 255, message = "Password should be between 7 and 255 characters.")
        @Schema(name = "password", example = "pass123123", requiredMode = Schema.RequiredMode.REQUIRED, description = "User's password. Should be between 7 and 255 characters.")
        String password
) {
}
