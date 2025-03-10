package com.karam.teamup.authentication.kafka;

import com.karam.teamup.authentication.dto.UserCreatedEvent;
import com.karam.teamup.authentication.entities.FailedEvent;
import com.karam.teamup.authentication.repositories.FailedEventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class FailedEventProcessor {
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
    private final String topicName = "user-creation-topic";
    private final FailedEventRepository failedEventRepository;

    /**
     * Retry sending failed events to Kafka
     */
    @Transactional
    public void processFailedEvents() {
        Iterable<FailedEvent> failedEvents = failedEventRepository.findAll();

        for (FailedEvent failedEvent : failedEvents) {
            try {
                UserCreatedEvent userCreatedEvent = new UserCreatedEvent(
                        failedEvent.getUserId(),
                        failedEvent.getUsername(),
                        failedEvent.getEmail(),
                        failedEvent.getRole()
                );
                kafkaTemplate.send(topicName, userCreatedEvent);
                log.info("Successfully sent failed event to Kafka: {}", userCreatedEvent);

                failedEventRepository.delete(failedEvent);
            } catch (Exception e) {
                log.error("Failed to send failed event to Kafka: {}", failedEvent, e);
            }
        }
    }
}
