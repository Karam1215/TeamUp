package com.karam.teamup.player.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record MatchRequest(
        UUID teamId,                 // Requesting team's ID
        LocalDate matchDate,         // Preferred match date
        LocalTime startTime,         // Preferred start time
        LocalTime endTime,           // Preferred end time
        List<UUID> preferredVenues,  // Optional list of venue IDs
        String opponentRanking     // Desired opponent skill level
) {}
