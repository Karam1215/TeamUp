package com.karam.teamup.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalTime;

@Schema(description = "DTO for updating venue's profile")
public record UpdateVenueProfileDTO(

        String address,

        String region,

        BigDecimal latitude,

        BigDecimal longitude,

        String description,

        String phoneNumber,

        LocalTime openingTime,

        LocalTime closingTime
) {
}
