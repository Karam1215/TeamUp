package com.karam.teamup.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomRequest {
    private UUID matchId;
    private UUID teamAId;
    private UUID teamBId;
    private LocalDate day;
    private LocalTime start_time;
    private LocalTime end_time;
}
