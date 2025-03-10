package com.karam.teamup.venue.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "venues")
@Schema(description = "Represents a venue, including location details and contact information.")
public class Venue {

    @Id
    @Column(name = "venue_id", unique = true, nullable = false)
    @Schema(description = "Unique identifier for the venue, same as the owner ID from auth-service.", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID venueId;

    @NotBlank(message = "Venue name is required")
    @Size(max = 255, message = "Venue name must not exceed 255 characters")
    @Column(name = "name", unique = true, nullable = false)
    @Schema(description = "The name of the venue.", example = "Moscow Sports Arena")
    private String name;

    @Column(name = "address")
    @Schema(description = "The street address of the venue.", example = "Tverskaya St, 10, Moscow, Russia")
    private String address;

    @Column(name = "region")
    @Schema(description = "The Moscow region where the venue is located.", example = "Arbat")
    private String region;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Column(name = "latitude", precision = 9, scale = 6)
    @Schema(description = "Latitude coordinate of the venue for mapping.", example = "55.7558")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Column(name = "longitude", precision = 9, scale = 6)
    @Schema(description = "Longitude coordinate of the venue for mapping.", example = "37.6173")
    private BigDecimal longitude;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description")
    @Schema(description = "A short description of the venue.", example = "A modern sports complex with multiple football fields.")
    private String description;

    @Pattern(regexp = "^\\+?[0-9\\- ]{7,20}$", message = "Invalid phone number format")
    @Column(name = "phone_number")
    @Schema(description = "Contact phone number for the venue.", example = "+7 926 123 4567")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "email", unique = true, nullable = false)
    @Schema(description = "Contact email for the venue.", example = "contact@moscowarena.com")
    private String email;

    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @PrePersist
    protected void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
