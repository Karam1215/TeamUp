package com.karam.teamup.player.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.karam.teamup.player.enums.TeamRanking;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "teams")
@Schema(description = "Entity representing a sports team")
public class Team {

    @Id
    @Column(name = "team_id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Schema(description = "Unique identifier of the team", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "Team name cannot be blank")
    @Column(name = "name",unique = true, nullable = false)
    @Size(max = 255, message = "Team name must be less than 255 characters")
    @Schema(description = "Unique name of the team", example = "Dynamo Moscow")
    private String name;

    @OneToOne
    @JsonManagedReference
    @Schema(description = "Team leader reference")
    @JoinColumn(name = "leader_id", nullable = false)
    @NotNull(message = "Team leader must be specified")
    private Player leader;

    @Column(name = "capacity",nullable = false)
    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(description = "Maximum team capacity", example = "5")
    private int capacity;

    @Column(name = "ranking")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Ranking must be specified")
    @Schema(description = "Team skill level", example = "medium")
    private TeamRanking ranking;

    @NotNull(message = "Start time cannot be null")
    @Column(name = "preferredStartTime",nullable = false)
    @Schema(description = "Preferred match start time", example = "18:00:00")
    private LocalTime preferredStartTime;

    @NotNull(message = "End time cannot be null")
    @Column(name = "preferredEndTime",nullable = false)
    @Schema(description = "Preferred match end time", example = "21:00:00")
    private LocalTime preferredEndTime;

    @JsonManagedReference
    @Schema(description = "List of team members")
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Player> players = new ArrayList<>();

    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_venues", columnDefinition = "jsonb")
    private List<UUID> preferredVenues;

    private boolean isValidTimeRange() {
        return preferredEndTime.isAfter(preferredStartTime);
    }
}