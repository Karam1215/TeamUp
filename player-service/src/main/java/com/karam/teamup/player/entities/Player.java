package com.karam.teamup.player.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Schema(name = "player_id", example = "72bde859-d59e-4a61-b060-d5fa60426203", requiredMode = Schema.RequiredMode.REQUIRED, description = "Unique player identifier.")
    UUID playerId;

    @NotBlank(message = "username can't be empty")
    @Column(name = "user_name", nullable = false, unique = true)
    @Size(min = 1, max = 100, message = "Username should be between 1 and 100 characters.")
    @Schema(name = "name", example = "Karam", requiredMode = Schema.RequiredMode.REQUIRED, description = "Username. Should be between 1 and 100 characters.")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @NotBlank(message = "Email can't be empty")
    @Email(message = "Please enter a valid email address.")
    @Column(name = "email", unique = true)
    @Schema(name = "email", example = "2002@hotmail.com", requiredMode = Schema.RequiredMode.REQUIRED, description = "Player's email. Should be unique and in a valid format.")
    private String email;

    @Column(name = "phone_number")
    @Size(max = 18, message = "Phone number should not exceed 18 characters.")
    @Pattern(regexp = "^([+78])\\d{10}$", message = "Phone number must be in the format +7XXXXXXXXXX or 7XXXXXXXXXX.")
    @Pattern(regexp = "^\\+7\\s?\\(\\d{3}\\)\\s?\\d{3}[-\\s]?\\d{2}[-\\s]?\\d{2}$", message = "Phone number must be in the format +7 (XXX) XXX-XX-XX.")
    @Schema(name = "phoneNumber", example = "+7 (123) 456-78-90", description = "Player's phone number. Should be in the format: +7 (XXX) XXX-XX-XX.")
    private String phoneNumber;

    @Past
    @Column(name = "date_of_birth")
    @Schema(name = "birthday", example = "2002-11-04", description = "User's birth date. Format: yyyy-MM-dd.")
    private LocalDate birthDate;

    //TODO @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    @Schema(name = "gender", example = "MALE", description = "Player's gender. Examples: 'MALE', 'FEMALE'.")
    private String gender;

    @Column(name = "city")
    @Schema(name = "gender", example = "MALE", description = "Player's city. Examples: 'Kazan', 'Moscow'.")
    private String city;

    @Column(name = "profile_picture")
    @Schema(name = "gender", example = "MALE", description = "Player's url for profile picture")
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

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "team_id")
    private Team team;


}