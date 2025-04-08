package com.karam.teamup.venue.controllers;

import com.karam.teamup.venue.dto.VenueBookingRequest;
import com.karam.teamup.venue.dto.VenueBookingResponse;
import com.karam.teamup.venue.services.VenueBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/venue")
public class VenueBookingController {
    private final VenueBookingService venueBookingService;

    @PostMapping("/booking-request")
    @Operation(summary = "Request a booking at preferred venues")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Booking created successfully"),
        @ApiResponse(responseCode = "404", description = "Venue not found"),
        @ApiResponse(responseCode = "409", description = "No available fields"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<VenueBookingResponse> bookVenue(
            @Valid @RequestBody VenueBookingRequest request) {
        return venueBookingService.bookAvailableField(request);
    }
}
