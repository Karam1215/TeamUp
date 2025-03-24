package com.karam.teamup.venue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "DTO representing creating field blank within a venue.")
public record CreateFieldDTO(

        @NotBlank(message = "Field name cannot be blank")
        @Size(max = 255, message = "Field name must not exceed 255 characters")
        @Schema(
            description = "Name of the field.",
            example = "Field 1",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name,

        @PositiveOrZero(message = "Price per hour must be a positive number or zero")
        @Schema(
            description = "Price per hour to rent the field.",
            example = "2000.00",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        BigDecimal pricePerHour,

        @Positive(message = "Capacity must be a positive number")
        @Schema(
            description = "Maximum number of players allowed on the field.",
            example = "20",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
         Integer capacity
) {
}
