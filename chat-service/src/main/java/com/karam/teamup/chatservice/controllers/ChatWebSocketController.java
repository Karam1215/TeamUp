package com.karam.teamup.chatservice.controllers;

import com.karam.teamup.chatservice.dto.ChatMessageDTO;
import com.karam.teamup.chatservice.entities.ChatMessage;
import com.karam.teamup.chatservice.repositories.ChatMessageRepository;
import com.karam.teamup.chatservice.services.ChatMessagesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessagesService chatMessagesService;

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/send/{roomId}")
    public void sendMessage(@Payload ChatMessageDTO message,
                            @DestinationVariable("roomId") String roomId) {

        log.info("Received message for roomId={}", roomId);
        chatMessagesService.saveMessage(roomId, message);
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, message);
    }
}
