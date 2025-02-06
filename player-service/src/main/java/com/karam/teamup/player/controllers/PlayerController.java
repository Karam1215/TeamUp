package com.karam.teamup.player.controllers;

import com.karam.teamup.player.DTO.PlayerLogin;
import com.karam.teamup.player.DTO.PlayerProfileDTO;
import com.karam.teamup.player.DTO.PlayerRegistration;
import com.karam.teamup.player.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/player")
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/register")
    @Operation(summary = "Register a new player", description = "Registers a new player account with provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<String> registerPlayer(@Valid @RequestBody PlayerRegistration playerRegistration) {
        log.info("Received request to register player: {}", playerRegistration.userName());
        playerService.createPlayer(playerRegistration);
        log.info("Player {} registered successfully.", playerRegistration.userName());
        return ResponseEntity.ok("Player registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials")
    })
    public ResponseEntity<?> login(@Valid @RequestBody PlayerLogin playerLogin) {
        log.info("Received login request for player: {}", playerLogin.email());
        return playerService.login(playerLogin);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete player account", description = "Deletes the authenticated player's account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to delete this account")
    })
    public ResponseEntity<String> deletePlayer(Authentication authentication) {
        log.info("Received request to delete player: {}", authentication.getName());
        return playerService.deletePlayerByUsername(authentication);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get player details", description = "Fetches the profile details of a player by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<PlayerProfileDTO> getPlayerByUsername(@Valid @PathVariable("username") String username) {
        log.info("Received request to get player details for: {}", username);
        return playerService.getPlayerByUsername(username);
    }

    @GetMapping("/me")
    @Operation(summary = "Get own player profile", description = "Retrieves the profile of the authenticated player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    })
    public ResponseEntity<PlayerProfileDTO> getOwnPlayerProfile(Authentication authentication) {
        log.info("Received request to get own player profile");
        return playerService.getPlayerProfile(authentication);
    }

    @PatchMapping("/me")
    @Operation(summary = "Update player profile", description = "Updates the profile information of the authenticated player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update request")
    })
    public ResponseEntity<String> updatePlayer(@RequestBody PlayerProfileDTO dto, Authentication authentication) {
        log.info("Received request to update player profile: {}", authentication.getName());
        return playerService.updatePlayerProfile(dto, authentication);
    }
}
