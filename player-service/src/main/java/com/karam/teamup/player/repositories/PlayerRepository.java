package com.karam.teamup.player.repositories;

import com.karam.teamup.player.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    Optional<Player> findPlayerByUsername(String username);
    Optional<Player> findPlayerByEmail(String email);
    List<Player> findAllByCity(String city);

    Optional<Player> findPlayerByPlayerId(UUID playerId);
}
