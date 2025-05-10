package com.karam.teamup.chatservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_message")
public class ChatMessage {
    @Id
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

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();
}
