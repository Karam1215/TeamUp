package com.karam.teamup.player.kafka;

import com.karam.teamup.player.dto.UserCreatedEvent;
import com.karam.teamup.player.services.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaListeners {

    private final PlayerService playerService;

    @KafkaListener(
            topics = "player-created-topic",
            groupId = "player-service-group"
    )
     public void handleUserCreatedEvent(UserCreatedEvent event) {
      log.info("Received data: {}", event);
      playerService.createPlayer(event);
      log.info("Player created: {}", event);
    } 
}
