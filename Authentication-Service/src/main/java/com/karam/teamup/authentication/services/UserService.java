package com.karam.teamup.authentication.services;

import com.karam.teamup.authentication.dto.UserRegistrationDTO;
import com.karam.teamup.authentication.entities.User;
import com.karam.teamup.authentication.exception.EmailAlreadyExistException;
import com.karam.teamup.authentication.exception.UserNameAlreadyExist;
import com.karam.teamup.authentication.jwt.JWTService;
import com.karam.teamup.authentication.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        if (userRepository.findPlayerByEmail(registrationDTO.email()).isPresent()) {
            log.warn("Email already exists: {}", registrationDTO.email());
            throw new EmailAlreadyExistException("Email already exists");
        }

        if (userRepository.findPlayerByUsername(registrationDTO.username()).isPresent()) {
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
}
