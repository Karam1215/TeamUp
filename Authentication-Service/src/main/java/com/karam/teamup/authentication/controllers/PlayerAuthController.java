package com.karam.teamup.authentication.controllers;

import com.karam.teamup.authentication.dto.ChangePasswordRequest;
import com.karam.teamup.authentication.dto.UserLoginDTO;
import com.karam.teamup.authentication.dto.UserRegistrationDTO;
import com.karam.teamup.authentication.jwt.JWTService;
import com.karam.teamup.authentication.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/player")
@Tag(name = "getting Account API", description = "operations connected with user register/login")
public class PlayerAuthController {

    private final PlayerService playerService;
    private final JWTService jwtService;
    @PostMapping("/register")
    @Operation(summary = "Register a new player", description = "Creates a new player account. Requires unique email and username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Player registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email or username already exists", content = @Content)
    })
    public ResponseEntity<String> registerPlayer(@Valid @RequestBody UserRegistrationDTO dto) {
        log.info("Registering player: {}", dto.username());
        return playerService.createPlayer(dto);
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
    public ResponseEntity<Map<String,String>> login(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletResponse response) {
        log.info("Login attempt for: {}", userLoginDTO.email());
        return playerService.login(userLoginDTO, response);
    }

    @GetMapping("/validate")
    public void validateToken(@RequestParam("token") String token) {
        jwtService.validateToken(token);
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Change the authenticated user's password",
               description = "This endpoint allows the authenticated user to change their password. The user needs to provide the current password and the new password. The current password will be validated before the password change operation proceeds.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request, e.g. invalid current password or weak new password"),
        @ApiResponse(responseCode = "401", description = "Unauthorized, user is not authenticated or password validation failed"),
        @ApiResponse(responseCode = "500", description = "Internal server error, unexpected issue")
    })
    public ResponseEntity<String> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest passwordRequest) {
        log.info("Changing password for authenticated user: {}", authentication.getName());
        return playerService.changePassword(authentication,passwordRequest);
    }
}