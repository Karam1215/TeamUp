package com.karam.teamup.authentication.services;

import com.karam.teamup.authentication.cookie.CookieUtil;
import com.karam.teamup.authentication.dto.ChangePasswordRequest;
import com.karam.teamup.authentication.dto.UserCreatedEvent;
import com.karam.teamup.authentication.dto.UserLoginDTO;
import com.karam.teamup.authentication.dto.UserRegistrationDTO;
import com.karam.teamup.authentication.entities.Role;
import com.karam.teamup.authentication.entities.User;
import com.karam.teamup.authentication.exception.EmailAlreadyExistException;
import com.karam.teamup.authentication.exception.InvalidCredentialsException;
import com.karam.teamup.authentication.exception.UserNameAlreadyExist;
import com.karam.teamup.authentication.jwt.JWTService;
import com.karam.teamup.authentication.kafka.KafkaEventService;
import com.karam.teamup.authentication.repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final JWTService jwtService;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaEventService kafkaEventService;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<String> createPlayer(UserRegistrationDTO registrationDTO) {
        log.info("Attempting to register player: {}", registrationDTO.username());

        if (userRepository.findUserByEmail(registrationDTO.email()).isPresent()) {
            log.warn("Email already exists: {}", registrationDTO.email());
            throw new EmailAlreadyExistException("Email already exists");
        }

        if (userRepository.findUserByUsername(registrationDTO.username()).isPresent()) {
            log.warn("Username already exists: {}", registrationDTO.username());
            throw new UserNameAlreadyExist(registrationDTO.username() + " already exists, please enter a unique name");
        }

        User userToBeSaved = User.builder()
                .username(registrationDTO.username())
                .email(registrationDTO.email())
                .password(passwordEncoder.encode(registrationDTO.password()))
                .role(Role.USER)
                .build();

        userRepository.save(userToBeSaved);

        log.info("Player successfully registered: {}", userToBeSaved.getUsername());

        UserCreatedEvent userToBeSendByKafkaToPlayerService = new UserCreatedEvent(userToBeSaved.getUserId(),
                userToBeSaved.getUsername(),
                userToBeSaved.getEmail(),
                userToBeSaved.getRole());

        kafkaEventService.sendUserCreatedEvent(userToBeSendByKafkaToPlayerService);
        log.info("Sending user event: {}", userToBeSendByKafkaToPlayerService);

        return new ResponseEntity<>("🎊 Welcome aboard! Your account is created." +
                " Please check your email to verify and start your journey! 🚀", HttpStatus.CREATED);
    }

        public ResponseEntity<String> login(UserLoginDTO userLoginDTO, HttpServletResponse response) {
        log.info("Attempting login for email: {}", userLoginDTO.email());

        User user = userRepository.findUserByEmail(userLoginDTO.email())
                    .orElseThrow(() -> {
                        log.warn("Invalid login attempt: {}", userLoginDTO.email());
                        return new InvalidCredentialsException("Invalid email or password");
                    });

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), userLoginDTO.password())
        );

        String token = jwtService.generateAccessToken(user.getUsername(), String.valueOf(Role.USER));
        log.info("Login successful for user: {}", user.getUsername());

        cookieUtil.addAuthTokenCookie(response, token);

        return ResponseEntity.ok("Login successful. Token is stored in cookie.");
    }

    public ResponseEntity<String> changePassword(Authentication authentication,
                                                 ChangePasswordRequest changePasswordRequest) {

        User player = userRepository.findUserByUsername(authentication.getName()).get();

        if (!passwordEncoder.matches(changePasswordRequest.currentPassword(), player.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        if (!changePasswordRequest.newPassword().equals(changePasswordRequest.passwordConfirmation())) {
            throw new ValidationException("Confirmed password does not match.");
        }
        player.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
        userRepository.save(player);
        log.info("Password changed successfully");
        return ResponseEntity.ok("Password changed successfully.");
    }
}
