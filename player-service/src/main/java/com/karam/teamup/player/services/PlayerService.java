package com.karam.teamup.player.services;

import com.karam.teamup.player.DTO.*;
import com.karam.teamup.player.entities.ConfirmationToken;
import com.karam.teamup.player.entities.Player;
import com.karam.teamup.player.exceptions.*;
import com.karam.teamup.player.jwt.JWTService;
import com.karam.teamup.player.mappers.PlayerProfileMapper;
import com.karam.teamup.player.repositories.PlayerRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final PlayerProfileMapper playerProfileMapper;
    private final EmailService emailService;
    private final ConfirmationTokenService confirmationTokenService;

    private static final String UPLOAD_DIR = "/home/karam/IdeaProjects/TeamUp/player-service/uploads";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public ResponseEntity<String> createPlayer(PlayerRegistration playerRegistration) {
        log.info("Attempting to register player: {}", playerRegistration.userName());

        if (playerRepository.findPlayerByEmail(playerRegistration.email()).isPresent()) {
            log.warn("Email already exists: {}", playerRegistration.email());
            throw new EmailAlreadyExistException("Email already exists");
        }

        if (playerRepository.findPlayerByUserName(playerRegistration.userName()).isPresent()) {
            log.warn("Username already exists: {}", playerRegistration.userName());
            throw new UserNameAlreadyExist(playerRegistration.userName() + " already exists, please enter a unique name");
        }

        Player player = Player.builder()
                .userName(playerRegistration.userName())
                .email(playerRegistration.email())
                    .password(passwordEncoder.encode(playerRegistration.password()))
                .build();

        playerRepository.save(player);
        ConfirmationToken token = confirmationTokenService.createToken(player);

        emailService.sendVerificationMail(playerRegistration.email(),
                                            player.getUserName(),
                                            token.getToken());

        log.info("Player successfully registered: {}", player.getUserName());

        return new ResponseEntity<>("🎊 Welcome aboard! Your account is created." +
                " Please check your email to verify and start your journey! 🚀", HttpStatus.CREATED);
    }

    public ResponseEntity<String> verifyAccount(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token).orElseThrow(
                () -> new InvalidTokenException("Invalid token")
        );

        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new TokenExpiredException("Срок действия токена истёк. Пожалуйста, запросите новую ссылку для подтверждения.");
        }

        if (confirmationToken.getConfirmedAt() != null) {
            throw new AccountAlreadyVerifiedException("Аккаунт уже подтверждён.");
        }

        confirmationTokenService.setConfirmedAt(token);

        Player player = confirmationToken.getPlayer();
        player.setIsVerified(true);
        playerRepository.save(player);

        return ResponseEntity.ok("Account is verified!");
    }

    @Transactional
    public ResponseEntity<String> resendVerificationToken(String email){
        Player player = playerRepository.findPlayerByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        if (player.getIsVerified()) {
            throw new AccountAlreadyVerifiedException("Аккаунт уже подтверждён.");
        }
        confirmationTokenService.invalidateExistingTokens(player);
        ConfirmationToken newToken = confirmationTokenService.createToken(player);
        emailService.sendVerificationMail(player.getEmail(), player.getUserName(), newToken.getToken());
        return ResponseEntity.ok("we sent an email to verify your account.");
    }

    public ResponseEntity<?> login(PlayerLogin playerLogin) {
        log.info("Attempting login for email: {}", playerLogin.email());

        try {
            Player player = playerRepository.findPlayerByEmail(playerLogin.email())
                    .orElseThrow(() -> {
                        log.warn("Invalid login attempt: {}", playerLogin.email());
                        throw new InvalidCredentialsException("Invalid email or password");
                    });

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(player.getUserName(), playerLogin.password())
            );

            String token = jwtService.generateToken(player.getUserName());
            log.info("Login successful for user: {}", player.getUserName());

            return ResponseEntity.ok(Map.of("token", token));

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for email: {}", playerLogin.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "An unexpected error occurred"));
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<PlayerProfileDTO> getPlayerByUsername(String username) {
        log.info("Fetching player profile for username: {}", username);

        Player player = playerRepository.findPlayerByUserName(username).orElseThrow(() -> {
            log.warn("Player not found: {}", username);
            throw new PlayerNotFoundException("Player not found");
        });

        log.info("Player profile found: {}", username);
        return ResponseEntity.ok(playerProfileMapper.toDTO(player));
    }

    public ResponseEntity<String> deletePlayerByUsername(Authentication authentication) {
        String username = authentication.getName();
        log.info("Attempting to delete account for: {}", username);

        Player player = playerRepository.findPlayerByUserName(username).orElseThrow(() -> {
            log.warn("Player not found for deletion: {}", username);
            throw new PlayerNotFoundException("Player not found");
        });

        playerRepository.delete(player);
        log.info("Player account deleted: {}", username);

        return new ResponseEntity<>("Account deleted successfully", HttpStatus.OK);
    }

    public ResponseEntity<PlayerProfileDTO> getPlayerProfile(Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching profile for player: {}", username);

        Player player = playerRepository.findPlayerByUserName(username).orElseThrow(() -> {
            log.warn("Player not found: {}", username);
            throw new PlayerNotFoundException("Player not found");
        });

        log.info("Profile found for player: {}", username);
        return ResponseEntity.ok(playerProfileMapper.toDTO(player));
    }

    public ResponseEntity<String> updatePlayerProfile(UpdatePlayerProfileDTO updatePlayerProfileDTO,
                                                      Authentication authentication) {
        String username = authentication.getName();
        log.info("Updating profile for player: {}", username);

        Player player = playerRepository.findPlayerByUserName(username).orElseThrow(() -> {
            log.warn("Player not found for profile update: {}", username);
             throw new PlayerNotFoundException("Player not found");
        });

        playerProfileMapper.updatePlayerProfile(player, updatePlayerProfileDTO);
        playerRepository.save(player);

        log.info("Profile updated successfully for player: {}", username);
        return new ResponseEntity<>("Profile updated successfully", HttpStatus.OK);
    }

    public ResponseEntity<String> uploadProfilePicture(MultipartFile file, Authentication authentication) {
        String username = authentication.getName();
        log.info("Uploading profile picture for player: {}", username);

        Player player = playerRepository.findPlayerByUserName(username).orElseThrow(() -> {
            log.warn("Player not found for profile picture upload: {}", username);
            return new PlayerNotFoundException("Player not found");
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

        String filePath = UPLOAD_DIR + "/" + player.getPlayerId() + extension;
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

    public ResponseEntity<String> changePassword(Authentication authentication,
                                                 ChangePasswordRequest changePasswordRequest) {
        Player player = playerRepository.findPlayerByUserName(authentication.getName()).get();
        if (!passwordEncoder.matches(changePasswordRequest.currentPassword(), player.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        if (!changePasswordRequest.newPassword().equals(changePasswordRequest.passwordConfirmation())) {
            throw new ValidationException("Confirmed password does not match.");
        }
        player.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
        playerRepository.save(player);
        log.info("Password changed successfully");
        return ResponseEntity.ok("Password changed successfully.");
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
                .collect(Collectors.toList());
        return ResponseEntity.ok(playerProfileDTOList);
    }
}