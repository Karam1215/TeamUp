package com.karam.teamup.chatservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(nullable = false, unique = true,name = "match_id")
    private UUID matchId;

    @Column(nullable = false, name = "team_a_id")
    private UUID teamAId;

    @Column(nullable = false, name = "team_b_id")
    private UUID teamBId;

    @Column(nullable = false, name = "day")
    private LocalDate day;

    @Column(nullable = false, name = "start_time")
    private LocalTime startTime;

    @Column(nullable = false,name = "end_time")
    private LocalTime endTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
