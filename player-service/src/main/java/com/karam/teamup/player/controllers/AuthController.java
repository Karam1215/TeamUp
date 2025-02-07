package com.karam.teamup.player.controllers;

import com.karam.teamup.player.DTO.PlayerLogin;
import com.karam.teamup.player.DTO.PlayerRegistration;
import com.karam.teamup.player.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "getting Account API", description = "operations connected with user register/login")
public class AuthController {
        private final PlayerService playerService;

    @PostMapping("/register")
    @Operation(summary = "Register a new player", description = "Creates a new player account. Requires unique email and username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Player registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email or username already exists", content = @Content)
    })
    public ResponseEntity<String> registerPlayer(@Valid @RequestBody PlayerRegistration playerRegistration) {
        log.info("Registering player: {}", playerRegistration.userName());
        return playerService.createPlayer(playerRegistration);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates the player and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class),
                    examples = @ExampleObject(value = "{\"token\": \"jwt-token-string\"}")
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content)
    })
    public ResponseEntity<?> login(@Valid @RequestBody PlayerLogin playerLogin) {
        log.info("Login attempt for: {}", playerLogin.email());
        return playerService.login(playerLogin);
    }
}
