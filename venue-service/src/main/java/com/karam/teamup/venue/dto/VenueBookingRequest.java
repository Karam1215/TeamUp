package com.karam.teamup.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VenueBookingRequest(
        @NotNull
        @Schema(description = "List of venue IDs in order of preference")
        List<UUID> venueIds,

        @NotNull
        @FutureOrPresent
        @Schema(description = "Booking start time in UTC", example = "2024-03-30T14:00:00")
            LocalDateTime startTime,

        @NotNull
        @Future
        @Schema(description = "Booking end time in UTC", example = "2024-03-30T16:00:00")
        LocalDateTime endTime,

        @NotNull
        @Schema(description = "Associated match ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID matchId
) {
}
