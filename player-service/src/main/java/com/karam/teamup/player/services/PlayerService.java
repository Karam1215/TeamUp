package com.karam.teamup.player.services;

import com.karam.teamup.player.DTO.PlayerLogin;
import com.karam.teamup.player.DTO.PlayerProfileDTO;
import com.karam.teamup.player.DTO.PlayerRegistration;
import com.karam.teamup.player.entities.Player;
import com.karam.teamup.player.exceptions.EmailAlreadyExistException;
import com.karam.teamup.player.exceptions.InvalidCredentialsException;
import com.karam.teamup.player.exceptions.PlayerNotFoundException;
import com.karam.teamup.player.exceptions.UserNameAlreadyExist;
import com.karam.teamup.player.jwt.JWTService;
import com.karam.teamup.player.mappers.PlayerProfileMapper;
import com.karam.teamup.player.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final PlayerProfileMapper playerProfileMapper;

    public ResponseEntity<String> createPlayer(PlayerRegistration playerRegistration) {

        if(playerRepository.findPlayerByEmail(playerRegistration.email()).isPresent()) {
            throw new EmailAlreadyExistException("Email already exist");
        }
        if (playerRepository.findPlayerByUserName(playerRegistration.userName()).isPresent()) {
            throw new UserNameAlreadyExist(playerRegistration.userName() +" is already exist, please enter a unique name");
        }

        Player player = Player.builder()
                .userName(playerRegistration.userName())
                .email(playerRegistration.email())
                .password(passwordEncoder.encode(playerRegistration.password()))
                .build();

        playerRepository.save(player);

        return new ResponseEntity<>("success", HttpStatus.CREATED);
    }

    public ResponseEntity<?> login(PlayerLogin playerLogin) {
        try {
            Player player = playerRepository.findPlayerByEmail(playerLogin.email())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(player.getUserName(), playerLogin.password())
            );

            String token = jwtService.generateToken(player.getUserName());

            return ResponseEntity.ok(Map.of("token", token));

        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<PlayerProfileDTO> getPlayerByUsername(String username) {
        Player player = playerRepository.findPlayerByUserName(username).orElseThrow(
                () -> new PlayerNotFoundException("Player not found")
        );
        return ResponseEntity.ok(playerProfileMapper.toDTO(player));
    }

    public ResponseEntity<String> deletePlayerByUsername(Authentication authentication) {

        Player player = playerRepository.findPlayerByUserName(authentication.getName()).orElseThrow(
                () -> new PlayerNotFoundException("Player not found")
        );

        playerRepository.delete(player);

        return new ResponseEntity<>("Account deleted successfully", HttpStatus.OK);
    }

        public ResponseEntity<PlayerProfileDTO> getPlayerProfile(Authentication authentication) {

        String username = authentication.getName();

        Player player = playerRepository.findPlayerByUserName(username).orElseThrow(
                () -> new PlayerNotFoundException("Player not found")
        );

        return ResponseEntity.ok(playerProfileMapper.toDTO(player));
    }


    public ResponseEntity<String> updatePlayerProfile(PlayerProfileDTO playerProfileDTO, Authentication authentication) {

        Player player = playerRepository.findPlayerByUserName(authentication.getName()).orElseThrow(
                () -> new PlayerNotFoundException("Player not found")
        );

        playerProfileMapper.updatePlayerProfile(player, playerProfileDTO);

        playerRepository.save(player);

        return new ResponseEntity<>("profile updated successfully", HttpStatus.OK);
    }
}