package com.karam.teamup.player.controllers;

import com.karam.teamup.player.dto.CreateTeamRequest;
import com.karam.teamup.player.entities.Team;
import com.karam.teamup.player.services.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.ws.rs.HeaderParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/player/team")
public class TeamController {

    private final TeamService teamService;


    /**
     * Create a new team and set a leader.
     *
     * @param username the username of the player who will be the leader of the team
     * @param requestDTO the details of the team to be created
     * @return ResponseEntity with created team details
     */
    @Operation(summary = "Create a new team", description = "Creates a new team, assigns a leader, and sets preferences such as ranking, capacity, and playing time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Team created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or team name already in use"),
            @ApiResponse(responseCode = "404", description = "Player (leader) not found")
    })
    @PostMapping
    public ResponseEntity<String> createTeam(
            @HeaderParam(value = "X-Username") @RequestHeader("X-Username") String username,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details of the team to be created") @RequestBody @Valid CreateTeamRequest requestDTO) {

        return teamService.createTeam(username, requestDTO);
    }
}
