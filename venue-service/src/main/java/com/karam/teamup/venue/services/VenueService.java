package com.karam.teamup.venue.services;

import com.karam.teamup.venue.dto.UpdateVenueProfileDTO;
import com.karam.teamup.venue.dto.UserCreatedEvent;
import com.karam.teamup.venue.entities.Field;
import com.karam.teamup.venue.entities.Venue;
import com.karam.teamup.venue.exceptions.VenueNotFoundException;
import com.karam.teamup.venue.mappers.VenueProfileMapper;
import com.karam.teamup.venue.repositories.VenueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final VenueProfileMapper venueProfileMapper;
    public static final String VENUE_NOT_FOUND = "Venue not found";


    public ResponseEntity<String> createVenue(UserCreatedEvent userCreatedEvent) {

        Venue venue = Venue.builder()
                .venueId(userCreatedEvent.userId())
                .name(userCreatedEvent.username())
                .email(userCreatedEvent.email())
                .build();

        venueRepository.save(venue);

        log.info("Venue created successfully for: {} 🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉", venue.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body("Venue created successfully");
    }

    public ResponseEntity<String> updateVenueProfile(UpdateVenueProfileDTO updateVenueProfileDTO,
                                                      String username) {

        log.info("Updating profile for Venue: {}", username);

        Venue venue = venueRepository.findVenueByName(username).orElseThrow(() -> {
            log.warn("Venue not found for profile update: {}", username);
            return new VenueNotFoundException(VENUE_NOT_FOUND);
        });

        venueProfileMapper.updateVenueProfile(venue,updateVenueProfileDTO);

        venueRepository.save(venue);

        log.info("Profile updated successfully for Venue: {} 🎉🎉🎉🎉🎉🎉", username);

        return new ResponseEntity<>("Profile updated successfully", HttpStatus.OK);
    }

    public ResponseEntity<String> deleteVenueByUsername(String username) {
        log.info("Attempting to delete account for: {}", username);

        Venue venue = venueRepository.findVenueByName(username).orElseThrow(() -> {
            log.warn("Player not found for deletion: {}", username);
            return new VenueNotFoundException(VENUE_NOT_FOUND);
        });

        venueRepository.delete(venue);

        log.info("Venue account deleted: {} 🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉", username);

        return new ResponseEntity<>("Account deleted successfully", HttpStatus.OK);
    }

    public ResponseEntity<List<Venue>> getAllVenues() {
        if (venueRepository.findAll().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok().body(venueRepository.findAll());
    }
}
