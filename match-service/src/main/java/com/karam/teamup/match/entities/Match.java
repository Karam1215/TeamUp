package com.karam.teamup.match.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID matchId;

    @Column(name = "team1_id" ,nullable = false)
    private UUID team1Id;

    @Column(name = "team2_id",nullable = false)
    private UUID team2Id;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "venue")
    private UUID venue;

    @Column(name = "created_at" , nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
