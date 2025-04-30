package com.karam.teamup.player.dto;

import com.karam.teamup.player.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "DTO for Team Invitation")
public record TeamInvitationDTO(
        UUID invitationId,
        UUID teamId,
        String teamName,
        UUID invitedPlayerId,
        Status status,
        LocalDateTime createdAt
)
{}
