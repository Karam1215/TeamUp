package com.karam.teamup.player.repositories;

import com.karam.teamup.player.entities.ConfirmationToken;
import com.karam.teamup.player.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByToken(String token);

    List<ConfirmationToken> findAllByPlayer(Player player);
}