package com.karam.teamup.player.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "players")
@Schema(description = "Player Entity")
public class Player {

    @Id
    @Column(name = "player_id", nullable = false, updatable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(name = "player_id", example = "72bde859-d59e-4a61-b060-d5fa60426203", required = true, description = "Unique player identifier.")
    UUID playerId;

    @NotBlank
    @Column(name = "user_name", nullable = false, unique = true)
    @Size(min = 1, max = 100, message = "Username should be between 1 and 100 characters.")
    @Schema(name = "name", example = "Karam", required = true, description = "Username. Should be between 1 and 100 characters.")
    private String userName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Email(message = "Please enter a valid email address.")
    @Column(name = "email", unique = true)
    @Schema(name = "email", example = "2002@hotmail.com", required = true, description = "User's email. Should be unique and in a valid format.")
    private String email;

    @Column(name = "phone_number")
    @Size(max = 18, message = "Phone number should not exceed 18 characters.")
    @Pattern(regexp = "(^\\+7|7|8)[0-9]{10}$|^\\+7\\s?\\(\\d{3}\\)\\s?\\d{3}[-\\s]?\\d{2}[-\\s]?\\d{2}$",
            message = "Invalid phone number format. Use the format +7 (XXX) XXX-XX-XX or 7XXXXXXXXXX.")
    @Schema(name = "phoneNumber", example = "+7 (123) 456-78-90", description = "User's phone number. Should be in the format: +7 (XXX) XXX-XX-XX.")
    private String phoneNumber;

    @NotBlank
    @Column(name = "password")
    @Size(min = 7, max = 255, message = "Password should be between 7 and 255 characters.")
    @Schema(name = "password", example = "pass123123", required = true, description = "User's password. Should be between 7 and 255 characters.")
    private String password;

    @Past
    @Column(name = "date_of_birth")
    @Schema(name = "birthday", example = "2002-11-04", description = "User's birth date. Format: yyyy-MM-dd.")
    private LocalDate birthDate;

    //TODO @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    @Schema(name = "gender", example = "MALE", description = "User's gender. Examples: 'MALE', 'FEMALE'.")
    private String gender;  //TODO Enum for gender

    @Column(name = "city")
    private String city;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "created_at", updatable = false)
    @Schema(name = "createdAt", example = "2025-02-07T14:30:00", description = "Date and time when the player was created.")
    private LocalDateTime createdAt;

    @Column(name = "bio")
    @Size(max = 500, message = "Bio should not exceed 500 characters.")
    @Schema(name = "bio", example = "Football and travel enthusiast.", description = "Short bio of the player.")
    private String bio;

    @Column(name = "sport")
    @Schema(name = "sport", example = "football", description = "Sport that the user is interested in.")
    private String sport;

    @Column(name = "is_verified")
    @Schema(name = "isVerified", example = "false",
            description = "verification for email address")
    private Boolean isVerified = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
