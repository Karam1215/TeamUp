package com.karam.teamup.authentication.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @Column(name = "user_id", nullable = false, updatable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(name = "user_id", example = "72bde859-d59e-4a61-b060-d5fa60426203", requiredMode = Schema.RequiredMode.REQUIRED, description = "Unique user identifier.")
    private UUID userId;

    @NotBlank(message = "username can't be empty")
    @Column(name = "user_name", nullable = false, unique = true)
    @Size(min = 1, max = 100, message = "Username should be between 1 and 100 characters.")
    @Schema(name = "name", example = "Karam", requiredMode = Schema.RequiredMode.REQUIRED, description = "Username. Should be between 1 and 100 characters.")
    private String username;

    @NotBlank(message = "Email can't be empty")
    @Email(message = "Please enter a valid email address.")
    @Column(name = "email", unique = true)
    @Schema(name = "email", example = "2002@hotmail.com", requiredMode = Schema.RequiredMode.REQUIRED, description = "users email. Should be unique and in a valid format.")
    private String email;

    @NotBlank(message = "Password can't be empty")
    @Column(name = "password")
    @Size(min = 7, max = 255, message = "Password should be between 7 and 255 characters.")
    @Schema(name = "password", example = "pass123123", requiredMode = Schema.RequiredMode.REQUIRED, description = "User's password. Should be between 7 and 255 characters.")
    private String password;
}
