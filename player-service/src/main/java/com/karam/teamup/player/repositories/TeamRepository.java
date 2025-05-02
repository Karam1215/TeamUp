package com.karam.teamup.player.repositories;

import com.karam.teamup.player.entities.Player;
import com.karam.teamup.player.entities.Team;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByName(String name);
    List<Team> findByName(String name, Sort sort);
    Optional<Team> findByLeader(@NotNull(message = "Team leader must be specified") Player leader);
    Optional<Team> findTeamById(UUID id);

    Optional<Team> findTeamByLeader(@NotNull(message = "Team leader must be specified") Player leader);
}
