package com.karam.teamup.player.services;

import com.karam.teamup.player.dto.CreateTeamRequest;
import com.karam.teamup.player.dto.UpdatePlayerProfileDTO;
import com.karam.teamup.player.dto.UpdateTeamsProfileDTO;
import com.karam.teamup.player.entities.Player;
import com.karam.teamup.player.entities.Team;
import com.karam.teamup.player.exceptions.*;
import com.karam.teamup.player.mappers.TeamProfileMapper;
import com.karam.teamup.player.repositories.PlayerRepository;
import com.karam.teamup.player.repositories.TeamRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.karam.teamup.player.services.PlayerService.PLAYER_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "Endpoints for managing sports teams")
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamProfileMapper teamProfileMapper;
    protected static final String TEAM_NOT_FOUND = "Team not found";

    @Transactional
    public ResponseEntity<String> createTeam(String username,CreateTeamRequest requestDTO) {

        Player leader = playerRepository.findPlayerByUsername(username).orElseThrow(
                () -> {
                    log.info("Player not found with name {}", username);
                    return new PlayerNotFoundException(username);
                }
        );

        if (teamRepository.findByLeader(leader).isPresent()) {
            throw new AlreadyATeamLeaderException("you already have a team");
        }

        teamRepository.findByName(requestDTO.name()).ifPresent(team -> {
            log.info("Team already exists with name {}", team.getName());
            throw new TeamAlreadyExistsException("Team name is already in use.");
        });


        Team team = Team.builder()
                .leader(leader)
                .name(requestDTO.name())
                .ranking(requestDTO.ranking())
                .capacity(requestDTO.capacity())
                .preferredStartTime(requestDTO.preferredStartTime())
                .preferredEndTime(requestDTO.preferredEndTime())
                .build();

        teamRepository.save(team);
        leader.setTeam(team);
        log.info("Team created🎉");
        return new ResponseEntity<>("Team created successfully",HttpStatus.CREATED);
    }

    public ResponseEntity<Team> getPlayersTeam(String username) {
        Player player = playerRepository.findPlayerByUsername(username).orElseThrow(
                () -> {
                    log.info("Player not found with name {}", username);
                    return new PlayerNotFoundException(username);
                }
        );

        if (player.getTeam() == null) {
            throw new PlayerDontHaveTeamException("you are not in a team");
        }

        Team team = teamRepository.findTeamById(player.getTeam().getId()).orElseThrow(
                () -> {
                    log.info("Team not found with id {}", player.getTeam().getId());
                    return new TeamNotFoundException("Team not found with id " + player.getTeam().getId());
                }
        );

        return new ResponseEntity<>(team, HttpStatus.OK);
    }

    public ResponseEntity<List<Team>> getAllTeams(String username){
        playerRepository.findPlayerByUsername(username).orElseThrow(
                () -> {
                    log.info("Player not found with name {}", username);
                    return new PlayerNotFoundException(username);
                }
        );
        log.info("All teams found");
        return ResponseEntity.ok(teamRepository.findAll());
    }

    public ResponseEntity<String> updateTeamsProfile(UpdateTeamsProfileDTO updateTeamsProfileDTO,
                                                     String username) {
        log.info("Request for updating teams profile: {}", username);

        Player player = playerRepository.findPlayerByUsername(username).orElseThrow(() -> {
            log.warn("Player not found for profile update: {}", username);
             throw new PlayerNotFoundException(PLAYER_NOT_FOUND);
        });

        Team teamToBeUpdated = teamRepository.findTeamByLeader(player).orElseThrow(() -> {
            throw new TeamNotFoundException("Team not found with id");
                }
        );

        if (!player.getPlayerId().equals(teamToBeUpdated.getLeader().getPlayerId())){
            log.info(player.getPlayerId() + " and also" + player.getTeam().getLeader());
            throw new AccessDeniedException("You are not allowed to update teams profile");
        }

        teamProfileMapper.updateTeamsProfile(teamToBeUpdated,updateTeamsProfileDTO);
        log.info("before saving team profile");
        teamRepository.save(teamToBeUpdated);

        log.info("Profile updated successfully for team: {} 🎉🎉🎉🎉🎉🎉", teamToBeUpdated.getName());
        return new ResponseEntity<>("Team's profile updated successfully", HttpStatus.OK);
    }
}
