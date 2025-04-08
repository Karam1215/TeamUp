package com.karam.teamup.authentication.kafka;

import com.karam.teamup.authentication.dto.UserCreatedEvent;
import com.karam.teamup.authentication.entities.FailedEvent;
import com.karam.teamup.authentication.entities.Role;
import com.karam.teamup.authentication.repositories.FailedEventRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventService {

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
    private final FailedEventRepository failedEventRepository;

    @Value("${player.kafka.topic.name}")
    private String playerTopicName;

    @Value("${venue.kafka.topic.name}")
    private String venueTopicName;


    @Async
    public void sendUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        String targetTopic = userCreatedEvent.role().equals(Role.USER)
                ? playerTopicName : venueTopicName;

        CompletableFuture<Void> sendFuture = CompletableFuture.runAsync(() -> {
            kafkaTemplate.send(targetTopic, userCreatedEvent);
            log.info("Sending user event to Kafka: {}", userCreatedEvent);
        });

        try {
            sendFuture.get(8, TimeUnit.SECONDS);
            log.info("User event successfully sent to Kafka: {}", userCreatedEvent);
        } catch (Exception e) {
            log.error("Error sending user event to Kafka within timeout, storing event for retry: {}", userCreatedEvent, e);

            FailedEvent failedEvent = FailedEvent.builder()
                    .userId(userCreatedEvent.userId())
                    .username(userCreatedEvent.username())
                    .email(userCreatedEvent.email())
                    .role(userCreatedEvent.role())
                    .build();
            failedEventRepository.save(failedEvent);

            log.info("Stored failed event for later retry: {}", failedEvent);
        }
    }
}
