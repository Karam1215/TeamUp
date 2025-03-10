package com.karam.teamup.venue.controllers;

import com.karam.teamup.venue.dto.UpdateVenueProfileDTO;
import com.karam.teamup.venue.services.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/venue")
public class VenueController {

    private final VenueService venueService;

    @PatchMapping("/me")
    @Operation(summary = "Update player profile", description = "Updates profile information of the authenticated player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update request")
    })
    public ResponseEntity<String> updatePlayer(@RequestHeader("X-Username") String username,@RequestBody UpdateVenueProfileDTO dto) {
        log.info("Updating player profile: {}", username);
        return venueService.updateVenueProfile(dto, username);
    }


    @DeleteMapping("/me")
    @Operation(summary = "Delete player account", description = "Deletes the authenticated player's account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to delete this account"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<String> deletePlayer(@RequestHeader("X-Username") String username) {
        log.info("Deleting player: {}", username);
        return venueService.deleteVenueByUsername(username);
    }
}