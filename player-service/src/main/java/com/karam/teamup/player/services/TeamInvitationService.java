package com.karam.teamup.player.services;

import com.karam.teamup.player.entities.Player;
import com.karam.teamup.player.entities.Team;
import com.karam.teamup.player.entities.TeamInvitation;
import com.karam.teamup.player.enums.Status;
import com.karam.teamup.player.exceptions.*;
import com.karam.teamup.player.repositories.PlayerRepository;
import com.karam.teamup.player.repositories.TeamInvitationRepository;
import com.karam.teamup.player.repositories.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamInvitationService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamInvitationRepository teamInvitationRepository;
    public static final String TEAM_INVITATION_ALREADY_EXIST = "Team Invitation already exists";

    public ResponseEntity<String> sendInvitation(String username, UUID invitedPlayerId) {
        log.info("getting started");

        Player teamLeader = playerRepository.findPlayerByUsername(username).orElseThrow(
                () -> new PlayerNotFoundException(username)
        );

        Team team = teamRepository.findByLeader(teamLeader).orElseThrow(
                () -> new PlayerIsNotLeaderException(PlayerService.PLAYER_NOT_FOUND)
        );

        Optional<TeamInvitation> teamInvitationOptional = teamInvitationRepository.
                findByTeamIdAndInvitedPlayerId(team.getId(),invitedPlayerId);

        if (teamInvitationOptional.isPresent()) {
            throw new DuplicateInvitationException(TEAM_INVITATION_ALREADY_EXIST);
        }

        TeamInvitation teamInvitation = TeamInvitation.builder()
                .teamId(team.getId())
                .invitedPlayerId(invitedPlayerId)
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        teamInvitationRepository.save(teamInvitation);

        log.info("Team Invitation has been sent successfully 🎉🎉🎉");

        return ResponseEntity.ok("Team Invitation sent");
    }

    public ResponseEntity<String> respondToInvitation(String username, UUID invitationId, String response){

        Optional<TeamInvitation> optionalTeamInvitation = teamInvitationRepository.findById(invitationId);

        Player player = playerRepository.findPlayerByUsername(username).orElseThrow(
                () -> new PlayerNotFoundException(PlayerService.PLAYER_NOT_FOUND)
        );

        if (!player.getPlayerId().equals(optionalTeamInvitation.get().getInvitedPlayerId())) {
            throw new AccessDeniedException("Player tried to accept someone else's invitation");
        }

        Team team = teamRepository.findById(optionalTeamInvitation.get().getTeamId()).orElseThrow(
                () -> new TeamNotFoundException(TeamService.TEAM_NOT_FOUND)
        );

        TeamInvitation teamInvitation = optionalTeamInvitation.get();

        if (!teamInvitation.getStatus().equals(Status.PENDING)) {
            log.info("Team Invitation already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invitation already responded to");
        }

        if (response.equalsIgnoreCase("accept")) {

            teamInvitation.setStatus(Status.ACCEPTED);
            log.info("Team Invitation accepted");

            player.setTeam(team);

            playerRepository.save(player);
            log.info("Player has been added to the team 🎉🎉🎉🎉");
        } else if (response.equalsIgnoreCase("decline")) {

            log.info("Team Invitation declined");
            teamInvitation.setStatus(Status.DECLINED);

            log.info("Deleting team Invitation with id " + teamInvitation.getInvitedPlayerId());
            teamInvitationRepository.delete(teamInvitation);
            log.info("Team Invitation has been removed");

        } else {
            return ResponseEntity.badRequest().body("Invalid response. Use 'accept' or 'decline'");
        }

        return ResponseEntity.ok("Team Invitation has been responded to " + response);
    }
}