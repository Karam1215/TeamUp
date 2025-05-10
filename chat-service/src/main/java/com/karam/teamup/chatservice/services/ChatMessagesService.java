package com.karam.teamup.chatservice.services;

import com.karam.teamup.chatservice.dto.ChatMessageDTO;
import com.karam.teamup.chatservice.entities.ChatMessage;
import com.karam.teamup.chatservice.entities.ChatRoom;
import com.karam.teamup.chatservice.repositories.ChatMessageRepository;
import com.karam.teamup.chatservice.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessagesService {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;

    public ChatMessage saveMessage(String roomId, ChatMessageDTO dto) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(UUID.fromString(roomId));
        if (chatRoom.isEmpty()) {
            throw new IllegalArgumentException("Chat room not found");
        }

        UUID chatRoomId = UUID.fromString(roomId);
        ChatMessage message = new ChatMessage();
        message.setChatRoomId(chatRoomId);
        message.setSenderTeamId(dto.getSenderTeamId());
        message.setSenderUsername(dto.getSenderUsername());
        message.setMessage(dto.getMessage());
        message.setSentAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public ResponseEntity<List<ChatMessage>> getMessages(UUID matchId) {
        UUID room = chatRoomService.getChatRoomByMatchId(matchId).getBody().getId();
        List<ChatMessage> messages = messageRepository.findByChatRoomIdOrderBySentAtAsc(room);
        return ResponseEntity.ok(messages);
    }
}
