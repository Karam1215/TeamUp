package com.karam.teamup.authentication.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${player.kafka.topic.name}")
    private String playerTopicName;

    @Value("${venue.kafka.topic.name}")
    private String venueTopicName;

    @Bean
    public NewTopic playerTopic() {
        return TopicBuilder
                .name(playerTopicName)
                .build();
    }

    @Bean
    public NewTopic venueTopic() {
        return TopicBuilder
                .name(venueTopicName)
                .build();
    }
}
