package com.karam.teamup.player.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DTO for login response")
public record LoginResponse(
        String token,
        String message
) {
}
