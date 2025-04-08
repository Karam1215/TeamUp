package com.karam.teamup.venue.dto;

import com.karam.teamup.venue.entities.FieldAvailability;

import java.time.LocalDateTime;
import java.util.UUID;

public record VenueBookingResponse(
        boolean success,
        String message,
        UUID bookingId,
        UUID venueId,
        UUID fieldId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    public static VenueBookingResponse success(FieldAvailability booking) {
        return new VenueBookingResponse(
                true,
                "Booking successful",
                booking.getBookingId(),
                booking.getField().getVenue().getVenueId(),
                booking.getField().getFieldId(),
                booking.getStartTime(),
                booking.getEndTime()
        );
    }
    public static VenueBookingResponse failure(String message) {
        return new VenueBookingResponse(false, message, null,
                null, null, null, null);
    }
}
