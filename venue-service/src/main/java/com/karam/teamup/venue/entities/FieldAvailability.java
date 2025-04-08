package com.karam.teamup.venue.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "field_availability")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entity representing a field booking time slot")
public class FieldAvailability {

    @Id
    @GeneratedValue
    @Column(name = "booking_id", updatable = false)
    @Schema(description = "Unique identifier for the booking",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    @NotNull(message = "Field must be specified")
    @Schema(description = "The field being booked", requiredMode = Schema.RequiredMode.REQUIRED)
    private Field field;

    @Column(name = "match_id")
    @Schema(description = "User who made the booking")
    private UUID match_id;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "Start time cannot be null")
    @FutureOrPresent(message = "Start time must be in the future")
    @Schema(description = "Booking start time (UTC)",
            example = "2024-03-20T09:00:00")
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "End time cannot be null")
    @Future(message = "End time must be in the future")
    @Schema(description = "Booking end time (UTC)",
            example = "2024-03-20T10:00:00")
    private LocalDateTime endTime;

    @Column(nullable = false)
    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "CONFIRMED|PENDING|CANCELLED",
            message = "Status must be CONFIRMED, PENDING, or CANCELLED")
    @Schema(description = "Booking status",
            allowableValues = {"CONFIRMED", "PENDING", "CANCELLED"},
            example = "PENDING")
    private String status;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    @Schema(description = "Timestamp of booking creation")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Schema(description = "Timestamp of last update")
    private LocalDateTime updatedAt;

    @AssertTrue(message = "End time must be after start time")
    private boolean isValidTimeRange() {
        return endTime.isAfter(startTime);
    }

    @AssertTrue(message = "match_id must be specified for confirmed bookings")
    private boolean isValidMatchAssociation() {
        return !"CONFIRMED".equals(status) || (match_id != null);
    }
}