package com.karam.teamup.player.entities;

import com.karam.teamup.player.enums.TeamRanking;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

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
    @Schema(description = "Team leader reference")
    @JoinColumn(name = "leader_id", nullable = false)
    @NotNull(message = "Team leader must be specified")
    private Player leader;

    @Column(name = "capacity",nullable = false)
    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(description = "Maximum team capacity", example = "5")
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "ranking")
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

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @Schema(description = "List of team members")
    private List<Player> players = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "team_preferred_venues", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "venue_id")
    private List<UUID> preferredVenues = new ArrayList<>();


    @AssertTrue(message = "End time must be after start time")
    private boolean isValidTimeRange() {
        return preferredEndTime.isAfter(preferredStartTime);
    }
}