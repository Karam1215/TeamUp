package com.karam.teamup.venue.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Represents the availability and booking status of a field for a specific time slot.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "field_availability")
@Schema(description = "Entity representing the availability of a field for a specific time slot.")
public class FieldAvailability {

    @Id
    @GeneratedValue
    @Column(name = "availability_id", nullable = false, updatable = false)
    @Schema(
        description = "Unique identifier for the availability slot.",
        example = "550e8400-e29b-41d4-a716-446655440000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID availabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    @NotNull(message = "Field must be specified")
    @Schema(
        description = "The field to which this availability slot belongs.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Field field;

    @Column(nullable = false)
    @NotNull(message = "Date cannot be null")
    @FutureOrPresent(message = "Date must be today or in the future")
    @Schema(
        description = "Date of the availability slot (format: yyyy-MM-dd).",
        example = "2024-03-20",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "Start time cannot be null")
    @Schema(
        description = "Start time of the slot (format: HH:mm:ss).",
        example = "09:00:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "End time cannot be null")
    @Schema(
        description = "End time of the slot (format: HH:mm:ss).",
        example = "10:00:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalTime endTime;

    @Column(nullable = false)
    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "AVAILABLE|BOOKED", message = "Status must be 'AVAILABLE' or 'BOOKED'")
    @Schema(
        description = "Current status of the slot.",
        allowableValues = {"AVAILABLE", "BOOKED"},
        example = "AVAILABLE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String status;

    @Column(name = "match_id")
    @Schema(
        description = "ID of the match associated with this booking (if status is BOOKED).",
        example = "550e8400-e29b-41d4-a716-446655440000",
        requiredMode = Schema.RequiredMode.AUTO
    )
    private UUID matchId;

    @AssertTrue(message = "Match ID is required when status is BOOKED")
    private boolean isValidMatchId() {
        return !"BOOKED".equals(status) || (matchId != null);
    }

    @AssertTrue(message = "End time must be after start time")
    private boolean isValidTimeRange() {
        return endTime.isAfter(startTime);
    }
}