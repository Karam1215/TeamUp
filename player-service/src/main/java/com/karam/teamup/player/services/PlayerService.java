package com.karam.teamup.player.services;

import com.karam.teamup.player.DTO.LoginResponse;
import com.karam.teamup.player.DTO.PlayerLogin;
import com.karam.teamup.player.DTO.PlayerRegistration;
import com.karam.teamup.player.entities.Player;
import com.karam.teamup.player.exceptions.EmailAlreadyExistException;
import com.karam.teamup.player.exceptions.InvalidCredentialsException;
import com.karam.teamup.player.exceptions.PlayerNotFoundException;
import com.karam.teamup.player.exceptions.UserNameAlreadyExist;
import com.karam.teamup.player.jwt.JwtTokenUtil;
import com.karam.teamup.player.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public ResponseEntity<String> createPlayer(PlayerRegistration playerRegistration) {

        if(playerRepository.findPlayerByEmail(playerRegistration.email()).isPresent()) {
            throw new EmailAlreadyExistException("Email already exist");
        }
        if (playerRepository.findByUserName(playerRegistration.userName()).isPresent()) {
            throw new UserNameAlreadyExist(playerRegistration.userName() +" is already exist, please enter a unique name");
        }
        String hashedPassword = passwordEncoder.encode(playerRegistration.password());

        Player player = Player.builder()
                .userName(playerRegistration.userName())
                .email(playerRegistration.email())
                .password(hashedPassword)
                .build();

        playerRepository.save(player);

        return new ResponseEntity<>("success", HttpStatus.CREATED);
    }

    public ResponseEntity<LoginResponse> login(PlayerLogin playerLogin) {
        Player player = playerRepository.findPlayerByEmail(playerLogin.email()).orElseThrow(
                () -> new InvalidCredentialsException("Неверные учетные данные")
        );

        if (!passwordEncoder.matches(playerLogin.password(), player.getPassword())) {
            throw new InvalidCredentialsException("wrong data inputs");
        }
        return ResponseEntity.ok(new LoginResponse(jwtTokenUtil.generateToken(player.getEmail()),
                "Login successful"));
    }

    public Player getPlayerByUsername(String username) {
        return playerRepository.findByUserName(username).orElseThrow(
                () -> new PlayerNotFoundException("Player not found")
        );
    }

    public ResponseEntity<String> deletePlayerByUsername(String username, Authentication authentication) {
        String loggedInUserEmail = authentication.getName();
        System.out.println(loggedInUserEmail);

        Player player = getPlayerByUsername(username);

        if (!loggedInUserEmail.equals(player.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        playerRepository.delete(getPlayerByUsername(username));

        return new ResponseEntity<>("Account deleted successfully", HttpStatus.OK);
    }
}