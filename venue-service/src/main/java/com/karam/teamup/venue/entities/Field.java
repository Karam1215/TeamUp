package com.karam.teamup.venue.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a field within a venue.
 * Each field can be rented by the hour for matches or events.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fields")
@Schema(description = "Entity representing a field within a venue.")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "field_id", nullable = false, updatable = false, unique = true)
    @Schema(
        description = "Unique identifier for the field.",
        example = "550e8400-e29b-41d4-a716-446655440000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID fieldId;

    @ManyToOne
    @JoinColumn(name = "venue_id", nullable = false)
    @Schema(
        description = "The venue to which this field belongs.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Venue venue;

    @NotBlank(message = "Field name cannot be blank")
    @Size(max = 255, message = "Field name must not exceed 255 characters")
    @Column(name = "name", nullable = false)
    @Schema(
        description = "Name of the field.",
        example = "Field 1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @PositiveOrZero(message = "Price per hour must be a positive number or zero")
    @Column(name = "price_per_hour", nullable = false)
    @Schema(
        description = "Price per hour to rent the field.",
        example = "2000.00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal pricePerHour;

    @Positive(message = "Capacity must be a positive number")
    @Column(name = "capacity", nullable = false)
    @Schema(
        description = "Maximum number of players allowed on the field.",
        example = "20",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer capacity;

    @Column(name = "created_at", updatable = false)
    @Schema(
        description = "Timestamp when the field was created.",
        example = "2024-03-20T12:00:00",
        requiredMode = Schema.RequiredMode.AUTO
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}