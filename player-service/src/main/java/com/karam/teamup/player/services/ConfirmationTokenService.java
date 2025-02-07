package com.karam.teamup.player.services;

import com.karam.teamup.player.entities.ConfirmationToken;
import com.karam.teamup.player.entities.Player;
import com.karam.teamup.player.repositories.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationToken createToken(Player player) {
        log.info("Creating confirmation token for player: {}", player.getUserName());
        String token = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusMinutes(30);

        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(token)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();
        confirmationTokenRepository.save(confirmationToken);
        log.info("Confirmation token created: {}", confirmationToken);
        return confirmationToken;
    }

    public Optional<ConfirmationToken> getToken(String token){
        return confirmationTokenRepository.findByToken(token);
    }

    public void setConfirmedAt(String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalid"));
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        log.info("Confirmation token confirmed: {}", confirmationToken.getConfirmedAt());
        confirmationTokenRepository.save(confirmationToken);
    }

    public void invalidateExistingTokens(Player player) {
        confirmationTokenRepository.findAllByPlayer(player).forEach(token -> {
            token.setExpiresAt(LocalDateTime.now().minusSeconds(1));
            confirmationTokenRepository.save(token);
        });
    }
}
