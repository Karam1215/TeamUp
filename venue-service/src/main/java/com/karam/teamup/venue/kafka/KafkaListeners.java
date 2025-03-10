package com.karam.teamup.venue.kafka;

import com.karam.teamup.venue.dto.UserCreatedEvent;
import com.karam.teamup.venue.services.VenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaListeners {
    private final VenueService venueService;

    @KafkaListener(
            topics = "venue-created-topic",
            groupId = "venue-service-group"
    )
    public void handleVenueCreatedEvent(UserCreatedEvent event) {
        log.info("Received data: {}", event);
        venueService.createVenue(event);
        log.info("Venue created: {}", event);
    }
}
