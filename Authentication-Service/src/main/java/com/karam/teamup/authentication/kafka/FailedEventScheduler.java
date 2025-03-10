package com.karam.teamup.authentication.kafka;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FailedEventScheduler {

    private final FailedEventProcessor failedEventProcessor;

    public FailedEventScheduler(FailedEventProcessor failedEventProcessor) {
        this.failedEventProcessor = failedEventProcessor;
    }

    @Scheduled(fixedRate = 60000)
    public void retryFailedEvents() {
        failedEventProcessor.processFailedEvents();
    }
}
