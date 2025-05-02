package com.karam.teamup.player.dto;

import com.karam.teamup.player.enums.TeamRanking;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "DTO for updating teams profile")
public record UpdateTeamsProfileDTO(
        @Min(value = 1, message = "Capacity must be at least 1")
        int capacity,

        TeamRanking ranking,

        LocalTime preferredStartTime,

        LocalTime preferredEndTime,

        List<UUID> preferredVenues
) {
}
