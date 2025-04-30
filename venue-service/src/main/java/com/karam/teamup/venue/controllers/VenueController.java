package com.karam.teamup.venue.controllers;

import com.karam.teamup.venue.dto.UpdateVenueProfileDTO;
import com.karam.teamup.venue.entities.Field;
import com.karam.teamup.venue.entities.Venue;
import com.karam.teamup.venue.services.FieldService;
import com.karam.teamup.venue.services.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/venue")
public class VenueController {

    private final VenueService venueService;
    private final FieldService fieldService;

    @Operation(summary = "get all venues", description = "Getting all venues so the players can choose were to play etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfully"),
            @ApiResponse(responseCode = "400", description = "error")
    })
    @GetMapping("/all")
    public ResponseEntity<List<Venue>> getAllVenues() {
        log.info("getAllVenues");
        return venueService.getAllVenues();
    }

    @PatchMapping("/me")
    @Operation(summary = "Update venue profile", description = "Updates profile information of the authenticated venue.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update request")
    })
    public ResponseEntity<String> updateVenue(@RequestHeader("X-Username") String username,@RequestBody UpdateVenueProfileDTO dto) {
        log.info("Updating venue profile: {}", username);
        return venueService.updateVenueProfile(dto, username);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete venue account", description = "Deletes the authenticated venue's account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to delete this account"),
            @ApiResponse(responseCode = "404", description = "Player not found")
    })
    public ResponseEntity<String> deleteVenue(@RequestHeader("X-Username") String username) {
        log.info("Deleting venue: {}", username);
        return venueService.deleteVenueByUsername(username);
    }

    @GetMapping("/{venue_id}/fields")
    @Operation(summary = "Get all fields for a specific venue", description = "Retrieve a list of fields associated with a venue by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of fields"),
        @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    public ResponseEntity<List<Field>> getFieldsByVenue(@PathVariable("venue_id") UUID venueId) {
        return fieldService.getFieldsByVenue(venueId);
    }
}