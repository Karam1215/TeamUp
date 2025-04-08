package com.karam.teamup.venue.services;

import com.karam.teamup.venue.dto.CreateFieldDTO;
import com.karam.teamup.venue.entities.Field;
import com.karam.teamup.venue.entities.Venue;
import com.karam.teamup.venue.exceptions.VenueNotFoundException;
import com.karam.teamup.venue.repositories.FieldRepository;
import com.karam.teamup.venue.repositories.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldService {

    private final FieldRepository fieldRepository;
    private final VenueRepository venueRepository;
    public static final String VENUE_NOT_FOUND = "Venue not found";

    public ResponseEntity<String> createField(CreateFieldDTO createFieldDTO, String username) {

        Venue venue = venueRepository.findVenueByName(username).orElseThrow(
        () -> {
            log.warn("Venue not found: {}", username);
            return new VenueNotFoundException(VENUE_NOT_FOUND);
        });

        Field field = Field.builder()
                .name(createFieldDTO.name())
                .pricePerHour(createFieldDTO.pricePerHour())
                .capacity(createFieldDTO.capacity())
                .venue(venue)
                .build();

        log.info("Creating field: {}", field);

        fieldRepository.save(field);

        log.info("Created field: {}🎉🎉🎉🎉", field);

        return ResponseEntity.status(HttpStatus.CREATED).body("Field created successfully");
    }

    public ResponseEntity<List<Field>> getFieldsByVenue(UUID venueId) {
        log.info("Get fields by venue: {}", venueId);
        return ResponseEntity.ok(fieldRepository.findByVenue_VenueId(venueId));
    }

}
