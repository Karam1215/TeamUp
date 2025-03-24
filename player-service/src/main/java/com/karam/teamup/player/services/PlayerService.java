package com.karam.teamup.player.services;

import com.karam.teamup.player.dto.PlayerProfileDTO;
import com.karam.teamup.player.dto.UpdatePlayerProfileDTO;
import com.karam.teamup.player.dto.UserCreatedEvent;
import com.karam.teamup.player.entities.Player;
import com.karam.teamup.player.exceptions.PlayerNotFoundException;
import com.karam.teamup.player.exceptions.ResourceNotFoundException;
import com.karam.teamup.player.mappers.PlayerProfileMapper;
import com.karam.teamup.player.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerProfileMapper playerProfileMapper;
    private static final long MAX_FILE_SIZE = 5 * 1024L * 1024; // 5MB
    public static final String PLAYER_NOT_FOUND = "Player not found";
    private static final String UPLOAD_DIR = "/home/karam/IdeaProjects/TeamUp/player-service/uploads";


    @Transactional(readOnly = true)
    public ResponseEntity<PlayerProfileDTO> getPlayerByUsername(String username) {
        log.info("Fetching player profile for username: {}", username);

        Player player = playerRepository.findPlayerByUsername(username).orElseThrow(() -> {
            log.warn("Player not found: {}", username);
            throw new PlayerNotFoundException(PLAYER_NOT_FOUND);
        });

        log.info("Player profile found: {} 🎉🎉🎉🎉🎉🎉", username);
        return ResponseEntity.ok(playerProfileMapper.toDTO(player));
    }

    public ResponseEntity<String> deletePlayerByUsername(String username) {
        log.info("Attempting to delete account for: {}", username);

        Player player = playerRepository.findPlayerByUsername(username).orElseThrow(() -> {
            log.warn("Player not found for deletion: {}", username);
            throw new PlayerNotFoundException(PLAYER_NOT_FOUND);
        });

        playerRepository.delete(player);
        log.info("Player account deleted: {} 🎉🎉🎉🎉🎉🎉", username);

        return new ResponseEntity<>("Account deleted successfully", HttpStatus.OK);
    }

    public ResponseEntity<PlayerProfileDTO> getPlayerProfile(String username) {
        //String username = authentication.getName();
        log.info("Fetching profile for player: {}", username);

        Player player = playerRepository.findPlayerByUsername(username).orElseThrow(() -> {
            log.warn("Player not found: {}", username);
            throw new PlayerNotFoundException(PLAYER_NOT_FOUND);
        });

        log.info("Profile found for player: {} 🎉🎉🎉🎉🎉🎉", username);
        return ResponseEntity.ok(playerProfileMapper.toDTO(player));
    }

    public ResponseEntity<String> updatePlayerProfile(UpdatePlayerProfileDTO updatePlayerProfileDTO,
                                                      String username) {
        log.info("Updating profile for player: {}", username);

        Player player = playerRepository.findPlayerByUsername(username).orElseThrow(() -> {
            log.warn("Player not found for profile update: {}", username);
             throw new PlayerNotFoundException(PLAYER_NOT_FOUND);
        });

        playerProfileMapper.updatePlayerProfile(player, updatePlayerProfileDTO);
        playerRepository.save(player);

        log.info("Profile updated successfully for player: {} 🎉🎉🎉🎉🎉🎉", username);
        return new ResponseEntity<>("Profile updated successfully", HttpStatus.OK);
    }

    public ResponseEntity<String> uploadProfilePicture(MultipartFile file, String username) {
        log.info("Uploading profile picture for player: {}", username);

        Player player = playerRepository.findPlayerByUsername(username).orElseThrow(() -> {
            log.warn("Player not found for profile picture upload: {}", username);
            return new PlayerNotFoundException(PLAYER_NOT_FOUND);
        });

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            log.warn("Invalid file type for {}: {}", username, contentType);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type. Only JPEG and PNG are allowed.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File too large for {}: {} bytes", username, file.getSize());
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File size exceeds the maximum limit of 5MB.");
        }

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            log.error("Failed to create upload directory: {}", UPLOAD_DIR);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not create upload directory.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

        String filePath = String.valueOf(Paths.get(UPLOAD_DIR, player.getPlayerId() + extension));
        File destinationFile = new File(filePath);

        try {
            file.transferTo(destinationFile);
            log.info("Profile picture uploaded for {}: {}", username, filePath);
        } catch (IOException e) {
            log.error("Failed to save profile picture for {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save the profile picture.");
        }

        player.setProfilePicture(filePath);
        playerRepository.save(player);

        return ResponseEntity.ok("Profile picture uploaded successfully: " + filePath);
    }

    //TODO make controller for this future
    public ResponseEntity<List<PlayerProfileDTO>> getAllPlayerByCity(String city) {
        List<Player> players = playerRepository.findAllByCity(city);
        log.info("Found {} players", players.size());
        if (players.isEmpty()) {
            log.warn("No players found for city: {}", city);
            throw new ResourceNotFoundException("No players found for city: " + city);
        }
        List<PlayerProfileDTO> playerProfileDTOList = players.stream()
                .map(playerProfileMapper::toDTO)
                .toList();
        return ResponseEntity.ok(playerProfileDTOList);
    }

    public ResponseEntity<String> createPlayer(UserCreatedEvent userCreatedEvent) {
        Player player = Player.builder()
                .playerId(userCreatedEvent.userId())
                .username(userCreatedEvent.username())
                .email(userCreatedEvent.email())
                .build();
        playerRepository.save(player);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}