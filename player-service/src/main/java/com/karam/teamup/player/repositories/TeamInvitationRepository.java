package com.karam.teamup.player.repositories;

import com.karam.teamup.player.entities.TeamInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, UUID> {
        Optional<TeamInvitation> findByTeamIdAndInvitedPlayerId(UUID teamId, UUID invitedPlayerId);
}
