package com.karam.teamup.chatservice.controllers;

import com.karam.teamup.chatservice.dto.ChatRoomRequest;
import com.karam.teamup.chatservice.entities.ChatRoom;
import com.karam.teamup.chatservice.services.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/create")
    public ResponseEntity<String> createChatRoom(@RequestBody ChatRoomRequest chatRoom) {
        return chatRoomService.createChat(chatRoom);
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<ChatRoom> getChatRoomByMatchId(@PathVariable UUID matchId) {
        return chatRoomService.getChatRoomByMatchId(matchId);
    }
}