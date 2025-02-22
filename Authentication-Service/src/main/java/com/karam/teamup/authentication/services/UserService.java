package com.karam.teamup.authentication.services;

import com.karam.teamup.authentication.dto.ChangePasswordRequest;
import com.karam.teamup.authentication.dto.UserLoginDTO;
import com.karam.teamup.authentication.dto.UserRegistrationDTO;
import com.karam.teamup.authentication.entities.User;
import com.karam.teamup.authentication.exception.EmailAlreadyExistException;
import com.karam.teamup.authentication.exception.ExpiredTokenException;
import com.karam.teamup.authentication.exception.InvalidCredentialsException;
import com.karam.teamup.authentication.exception.UserNameAlreadyExist;
import com.karam.teamup.authentication.jwt.JWTService;
import com.karam.teamup.authentication.repositories.UserRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
        
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    
    
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
                .build();

        userRepository.save(userToBeSaved);

        log.info("Player successfully registered: {}", userToBeSaved.getUsername());

        return new ResponseEntity<>("🎊 Welcome aboard! Your account is created." +
                " Please check your email to verify and start your journey! 🚀", HttpStatus.CREATED);
    }

    public ResponseEntity<String> login(UserLoginDTO userLoginDTO) {
        log.info("Attempting login for email: {}", userLoginDTO.email());

        User user = userRepository.findUserByEmail(userLoginDTO.email())
                    .orElseThrow(() -> {
                        log.warn("Invalid login attempt: {}", userLoginDTO.email());
                        return new InvalidCredentialsException("Invalid email or password");
                    });

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), userLoginDTO.password())
        );

        String token = jwtService.generateToken(user.getUsername());
        log.info("Login successful for user: {}", user.getUsername());

        return ResponseEntity.ok(token);
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
