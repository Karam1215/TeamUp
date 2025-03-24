package com.karam.teamup.match.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "match_requests")
public class MatchRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "Unique identifier of the match request", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID requestId;

    @Column(name = "team_id", nullable = false, unique = true)
    @NotNull(message = "Team ID cannot be null")
    @Schema(description = "ID of the team making the request", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID teamId;


    //TODO make enum
    //@Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    @NotNull(message = "Ranking cannot be null")
    @Schema(description = "Skill ranking of the team", allowableValues = {"beginner", "medium", "advanced", "world-class"})
    private String ranking;

    @Column(name = "preferred_start_time", nullable = false)
    @NotNull(message = "Preferred start time cannot be null")
    @Schema(description = "Preferred match start time", example = "18:00:00")
    private LocalTime preferredStartTime;

    @Column(name = "preferred_end_time", nullable = false)
    @NotNull(message = "Preferred end time cannot be null")
    @Schema(description = "Preferred match end time", example = "21:00:00")
    private LocalTime preferredEndTime;

    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    @Schema(description = "Current status of the request", allowableValues = {"pending", "matched", "expired"})
    private String status;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    @Schema(description = "Timestamp of request creation", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;

    @Column(name = "preferred_venues", nullable = false, columnDefinition = "jsonb")
    @NotEmpty(message = "Preferred venues cannot be empty")
    @Schema(description = "List of preferred venue UUIDs")
    @Convert(converter = UUIDListConverter.class)
    private List<UUID> preferredVenues = new ArrayList<>();

// Add this converter class
    @Converter
    public static class UUIDListConverter implements AttributeConverter<List<UUID>, String> {
        private static final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(List<UUID> attribute) {
            try {
                return mapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting UUID list to JSON", e);
            }
        }

        @Override
        public List<UUID> convertToEntityAttribute(String dbData) {
            try {
                return mapper.readValue(dbData, new TypeReference<List<UUID>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting JSON to UUID list", e);
            }
        }
    }
}