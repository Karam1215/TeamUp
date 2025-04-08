package com.karam.teamup.venue.services;


import com.karam.teamup.venue.dto.VenueBookingRequest;
import com.karam.teamup.venue.dto.VenueBookingResponse;
import com.karam.teamup.venue.entities.Field;
import com.karam.teamup.venue.entities.FieldAvailability;
import com.karam.teamup.venue.entities.Venue;
import com.karam.teamup.venue.repositories.FieldAvailabilityRepository;
import com.karam.teamup.venue.repositories.FieldRepository;
import com.karam.teamup.venue.repositories.VenueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class VenueBookingService {

    private final FieldRepository fieldRepository;
    private final VenueRepository venueRepository;
    private final FieldAvailabilityRepository fieldAvailabilityRepository;

    public ResponseEntity<VenueBookingResponse> bookAvailableField(VenueBookingRequest venueBookingRequest) {
        List<Venue> venues = venueRepository.findAllById(venueBookingRequest.venueIds());

        if (venues.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(VenueBookingResponse.failure("None of the requested venues exist"));
        }

        for(UUID venueId : venueBookingRequest.venueIds()) {
            Optional<Field> availableField = fieldRepository
                    .findAvailableFieldsInVenue(venueId,venueBookingRequest.startTime(),venueBookingRequest.endTime())
                    .stream()
                    .findFirst();
            if (availableField.isPresent()) {
                Field field = availableField.get();

                FieldAvailability booking = FieldAvailability.builder()
                        .field(field)
                        .startTime(venueBookingRequest.startTime())
                        .endTime(venueBookingRequest.endTime())
                        .match_id(venueBookingRequest.matchId())
                        .status("CONFIRMED")
                        .build();
                FieldAvailability savedBooking = fieldAvailabilityRepository.save(booking);

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(VenueBookingResponse.success(savedBooking));
            }
        }
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(VenueBookingResponse.failure("No available fields in requested venues"));
    }
}
