package com.karam.teamup.player.controllers;

import com.karam.teamup.player.dto.CreateTeamRequest;
import com.karam.teamup.player.dto.UpdatePlayerProfileDTO;
import com.karam.teamup.player.dto.UpdateTeamsProfileDTO;
import com.karam.teamup.player.entities.Team;
import com.karam.teamup.player.exceptions.AccessDeniedException;
import com.karam.teamup.player.exceptions.PlayerNotFoundException;
import com.karam.teamup.player.exceptions.TeamAlreadyExistsException;
import com.karam.teamup.player.exceptions.TeamNotFoundException;
import com.karam.teamup.player.services.TeamInvitationService;
import com.karam.teamup.player.services.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/player/team")
public class TeamController {

    private final TeamService teamService;
    private final TeamInvitationService teamInvitationService;

    /**
     * Create a new team and set a leader.
     *
     * @param username the username of the player who will be the leader of the team
     * @param requestDTO the details of the team to be created
     * @return ResponseEntity with created team details
     * @throws PlayerNotFoundException if the player who is trying to create the team is not found
     * @throws TeamAlreadyExistsException if the team exists
     */
    @Operation(summary = "Create a new team", description = "Creates a new team, assigns a leader, and sets preferences such as ranking, capacity, and playing time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Team created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or team name already in use"),
            @ApiResponse(responseCode = "404", description = "Player (leader) not found")
    })
    @PostMapping
    public ResponseEntity<String> createTeam(
            @Parameter(in = ParameterIn.HEADER, description = "Username of the team leader")
            @RequestHeader("X-Username") String username,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details of the team to be created")
            @RequestBody @Valid CreateTeamRequest requestDTO) {

        return teamService.createTeam(username, requestDTO);
    }

    /**
     * Sends an invitation to a player to join a team.
     *
     * @param username the username of the team leader sending the invitation
     * @param invitedPlayerId the UUID of the player being invited
     * @return ResponseEntity indicating the success or failure of the operation
     * @throws com.karam.teamup.player.exceptions.PlayerIsNotLeaderException if the player is not a team leader
     * @throws PlayerNotFoundException if the invited player is not found
     */
    @Operation(summary = "Send a team invitation", description = "Sends an invitation from the current user to another player to join their team.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation sent successfully"),
            @ApiResponse(responseCode = "400", description = "Player is not a team leader or player already in a team"),
            @ApiResponse(responseCode = "404", description = "Invited player not found")
    })
    @PostMapping("/send")
    public ResponseEntity<String> addPlayerToTeam(
            @Parameter(in = ParameterIn.HEADER, description = "Username of the team leader")
            @RequestHeader("X-Username") String username,

            @Parameter(description = "UUID of the player to invite")
            @RequestParam(name = "invitedPlayerId") UUID invitedPlayerId) {

        return teamInvitationService.sendInvitation(username, invitedPlayerId);
    }

    /**
     * Accepts or declines a team invitation.
     *
     * @param username the username of the player responding to the invitation
     * @param invitationId the UUID of the team invitation
     * @param response the response ("accept" or "decline") from the invited player
     * @return ResponseEntity indicating the success or failure of the operation
     * @throws AccessDeniedException if Player tried to accept someone else's invitation
     * @throws TeamNotFoundException if the team is not found or deleted
     */
    @Operation(summary = "Respond to a team invitation", description = "Accept or decline a team invitation by the invited player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid response or already responded"),
            @ApiResponse(responseCode = "403", description = "Not allowed to respond to this invitation"),
            @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @PostMapping("/response")
    public ResponseEntity<String> responseToInvitation(
            @Parameter(in = ParameterIn.HEADER, description = "Username of the responding player")
            @RequestHeader("X-Username") String username,

            @Parameter(description = "UUID of the invitation")
            @RequestParam(name = "invitationId") UUID invitationId,

            @Parameter(description = "Response value: 'accept' or 'decline'")
            @RequestParam("response") String response) {

        return teamInvitationService.respondToInvitation(username, invitationId, response);
    }

    @Operation(summary = "Get Player's team", description = "Get Team info for specific player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid response or already responded"),
            @ApiResponse(responseCode = "403", description = "Not allowed to respond to this invitation"),
            @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @GetMapping()
    public ResponseEntity<Team> getPlayersTeam(
            @Parameter(in = ParameterIn.HEADER, description = "Username of the responding player")
            @RequestHeader("X-Username") String username) {

        return teamService.getPlayersTeam(username);
    }

    @Operation(summary = "Get all teams", description = "Get all teams as a list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "list of team or empty"),
            @ApiResponse(responseCode = "404", description = "not found")
    })
    @GetMapping("/all")
    public ResponseEntity<List<Team>> getAllTeams(
            @Parameter(in = ParameterIn.HEADER, description = "Username of the responding player")
            @RequestHeader("X-Username") String username) {

        return teamService.getAllTeams(username);
    }

    @PatchMapping("/update")
    @Operation(summary = "Update team's profile", description = "Updates profile information of the team.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update request")
    })
    public ResponseEntity<String> updateTeamProfile(@RequestHeader("X-Username") String username,@RequestBody UpdateTeamsProfileDTO dto) {
        log.info("Profile updating (controller): {}", dto);
        return teamService.updateTeamsProfile(dto, username);
    }
}
