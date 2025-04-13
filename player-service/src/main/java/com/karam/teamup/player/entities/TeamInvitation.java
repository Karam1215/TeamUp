package com.karam.teamup.player.entities;

import com.karam.teamup.player.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_invitations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "invited_player_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamInvitation {
    @Id
    @GeneratedValue
    @Column(name = "invitation_id", nullable = false)
    private UUID invitationId;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "invited_player_id", nullable = false)
    private UUID invitedPlayerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}