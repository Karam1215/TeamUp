package com.karam.teamup.chatservice.services;

import com.karam.teamup.chatservice.dto.ChatRoomRequest;
import com.karam.teamup.chatservice.entities.ChatRoom;
import com.karam.teamup.chatservice.repositories.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRepository;

    public ResponseEntity<String> createChat(ChatRoomRequest chatRoomRequest) {
        ChatRoom chatRoom = ChatRoom.builder()
                .matchId(chatRoomRequest.getMatchId())
                .teamAId(chatRoomRequest.getTeamAId())
                .teamBId(chatRoomRequest.getTeamBId())
                .day(chatRoomRequest.getDay())
                .startTime(chatRoomRequest.getStart_time())
                .endTime(chatRoomRequest.getEnd_time())
                .build();
        log.info("Creating chat room {}", chatRoom);
        chatRepository.save(chatRoom);
        log.info("Successfully saved chat room {}", chatRoom);
        return ResponseEntity.ok("chat room created");
    }

    public ResponseEntity<ChatRoom> getChatRoomByMatchId(UUID matchId) {
        ChatRoom room = chatRepository.findByMatchId(matchId).orElseThrow(
                () -> new IllegalArgumentException("Chat room not found")
        );
        return ResponseEntity.ok().body(room);
    }
}
