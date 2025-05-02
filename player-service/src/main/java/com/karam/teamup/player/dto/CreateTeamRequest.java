package com.karam.teamup.player.dto;

import com.karam.teamup.player.enums.TeamRanking;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

@Schema(description = "Request object for creating a new team")
public record CreateTeamRequest(
    @Schema(description = "Unique name of the team", example = "Dynamo Moscow", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Team name cannot be blank")
    @Size(max = 255, message = "Team name must be less than 255 characters")
    String name,

    @Schema(description = "Skill ranking of the team", example = "medium", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Ranking must be specified")
    TeamRanking ranking,

    @Schema(description = "Maximum number of players in the team", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive(message = "Capacity must be a positive number")
    int capacity,

    @Schema(description = "Preferred start time for matches (HH:mm:ss)", example = "18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Preferred start time cannot be null")
    LocalTime preferredStartTime,

    @Schema(description = "Preferred end time for matches (HH:mm:ss)", example = "21:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Preferred end time cannot be null")
    LocalTime preferredEndTime
) {
    public CreateTeamRequest {
        if (preferredEndTime.isBefore(preferredStartTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}