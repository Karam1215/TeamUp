package com.karam.teamup.player.controllers;

import com.karam.teamup.player.DTO.*;
import com.karam.teamup.player.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/player")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/{username}")
    @Operation(summary = "Get player details", description = "Fetches profile details of a player by username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<PlayerProfileDTO> getPlayerByUsername(
            @Parameter(description = "Username of the player to fetch", example = "john_doe")
            @PathVariable("username") String username) {
        log.info("Fetching player profile for: {}", username);
        return playerService.getPlayerByUsername(username);
    }

    @GetMapping("/me")
    @Operation(summary = "Get authenticated player's profile", description = "Retrieves profile information of the logged-in player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    })
    public ResponseEntity<PlayerProfileDTO> getOwnPlayerProfile(Authentication authentication) {
        log.info("Fetching profile for authenticated user: {}", authentication.getName());
        return playerService.getPlayerProfile(authentication);
    }

    @PatchMapping("/me")
    @Operation(summary = "Update player profile", description = "Updates profile information of the authenticated player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update request")
    })
    public ResponseEntity<String> updatePlayer(@RequestBody UpdatePlayerProfileDTO dto, Authentication authentication) {
        log.info("Updating player profile: {}", authentication.getName());
        return playerService.updatePlayerProfile(dto, authentication);
    }

    @PostMapping("/me/upload_picture")
    @Operation(summary = "Upload profile picture", description = "Uploads a profile picture for the authenticated player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
            @ApiResponse(responseCode = "500", description = "Server error while processing the file")
    })
    public ResponseEntity<String> uploadProfilePicture(
            Authentication authentication,
            @Parameter(description = "Profile picture file (JPEG, PNG)", required = true)
            @RequestParam("profilePicture") MultipartFile file) throws IOException {
        log.info("Uploading profile picture for: {}", authentication.getName());
        return playerService.uploadProfilePicture(file, authentication);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete player account", description = "Deletes the authenticated player's account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to delete this account"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<String> deletePlayer(Authentication authentication) {
        log.info("Deleting player: {}", authentication.getName());
        return playerService.deletePlayerByUsername(authentication);
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
