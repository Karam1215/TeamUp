package com.karam.teamup.chatservice.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    @GeneratedValue
    private UUID id;

    @Column(name = "chat_room_id", nullable = false)
    private UUID chatRoomId;

    @Column(name = "sender_team_id", nullable = false)
    private UUID senderTeamId;

    @Column(name = "sender_username", nullable = false)
    private String senderUsername;

    @Column(nullable = false)
    private String message;

}
