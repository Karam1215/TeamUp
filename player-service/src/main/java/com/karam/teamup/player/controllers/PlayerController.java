package com.karam.teamup.player.controllers;

import com.karam.teamup.player.DTO.LoginResponse;
import com.karam.teamup.player.DTO.PlayerLogin;
import com.karam.teamup.player.DTO.PlayerRegistration;
import com.karam.teamup.player.entities.Player;
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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody PlayerLogin playerLogin) {
        log.info("Received login request for player: {}", playerLogin.email());
        return playerService.login(playerLogin);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<String> deletePlayer(@Valid @PathVariable("username") String username, Authentication authentication) {
        log.info("Received request to delete player: {} by user: {}", username, authentication.getName());
        return playerService.deletePlayerByUsername(username, authentication);
    }

    @GetMapping("/{username}")
    public Player getPlayerByUsername(@Valid @PathVariable("username") String username) {
        log.info("Received request to get player details for: {}", username);
        return playerService.getPlayerByUsername(username);
    }
}
