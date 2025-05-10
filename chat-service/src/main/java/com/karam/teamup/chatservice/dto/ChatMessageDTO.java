package com.karam.teamup.chatservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    @NotNull
    private UUID senderTeamId;

    @NotBlank
    @Size(min = 1, max = 100)
    private String senderUsername;

    @NotBlank
    @Size(min = 1, max = 500)
    private String message;
}
