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
public class VenueService {
    private final JWTService jwtService;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaEventService kafkaEventService;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<String> createVenue(UserRegistrationDTO registrationDTO) {
        log.info("Attempting to register venue: {}", registrationDTO.username());

        if (userRepository.findUserByEmail(registrationDTO.email()).isPresent()) {
            log.warn("Email already exists: {}", registrationDTO.email());
            throw new EmailAlreadyExistException("Email already exists");
        }

        if (userRepository.findUserByUsername(registrationDTO.username()).isPresent()) {
            log.warn("Username already exists: {}", registrationDTO.username());
            throw new UserNameAlreadyExist(registrationDTO.username() + " already exists, please enter a unique name");
        }

        User venueToBeSaved = User.builder()
                .username(registrationDTO.username())
                .email(registrationDTO.email())
                .password(passwordEncoder.encode(registrationDTO.password()))
                .role(Role.VENUE)
                .build();

        userRepository.save(venueToBeSaved);

        log.info("Venue successfully registered: {}", venueToBeSaved.getUsername());

        UserCreatedEvent venueToBeSendByKafkaToVenueService = new UserCreatedEvent(
                venueToBeSaved.getUserId(),
                venueToBeSaved.getUsername(),
                venueToBeSaved.getEmail(),
                venueToBeSaved.getRole()
        );
        kafkaEventService.sendUserCreatedEvent(venueToBeSendByKafkaToVenueService);
        log.info("Sending venue event: {}", venueToBeSendByKafkaToVenueService);

        return new ResponseEntity<>("🎊 Welcome aboard! Your venue account is created." +
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

        String token = jwtService.generateAccessToken(user.getUsername(), String.valueOf(Role.VENUE));
        log.info("Login successful for venue: {}", user.getUsername());

        cookieUtil.addAuthTokenCookie(response, token);

        return ResponseEntity.ok(token);
    }

    public ResponseEntity<String> changePassword(Authentication authentication,
                                                 ChangePasswordRequest changePasswordRequest) {
        User venue = userRepository.findUserByUsername(authentication.getName()).get();
        if (!passwordEncoder.matches(changePasswordRequest.currentPassword(), venue.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        if (!changePasswordRequest.newPassword().equals(changePasswordRequest.passwordConfirmation())) {
            throw new ValidationException("Confirmed password does not match.");
        }
        venue.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
        userRepository.save(venue);
        log.info("Password changed successfully");
        return ResponseEntity.ok("Password changed successfully.");
    }
}
