package com.karam.teamup.player.controllers;

import com.karam.teamup.player.dto.PlayerLogin;
import com.karam.teamup.player.dto.PlayerRegistration;
import com.karam.teamup.player.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "getting Account API", description = "operations connected with user register/login")
public class AuthController {
        private final PlayerService playerService;

    @PostMapping("/register")
    @Operation(summary = "Register a new player", description = "Creates a new player account. Requires unique email and username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Player registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email or username already exists", content = @Content)
    })
    public ResponseEntity<String> registerPlayer(@Valid @RequestBody PlayerRegistration playerRegistration) {
        log.info("Registering player: {}", playerRegistration.userName());
        return playerService.createPlayer(playerRegistration);
    }

    @GetMapping("/verify")
    @Operation(
            summary = "Подтвердить учетную запись по токену",
            description = "Этот эндпоинт позволяет пользователю подтвердить свою учетную" +
                    " запись с помощью токена, который был отправлен на почту."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учетная запись успешно подтверждена."),
            @ApiResponse(responseCode = "400", description = "Токен неверен или срок его действия истек."),
            @ApiResponse(responseCode = "404", description = "Пользователь с таким токеном не найден."),
    })
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        log.info("Verifying account token: {}", token);
        return playerService.verifyAccount(token);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates the player and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class),
                    examples = @ExampleObject(value = "{\"token\": \"jwt-token-string\"}")
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content)
    })
    public ResponseEntity<Map<String,String>> login(@Valid @RequestBody PlayerLogin playerLogin) {
        log.info("Login attempt for: {}", playerLogin.email());
        return playerService.login(playerLogin);
    }

    @PostMapping("/resend-token")
    @Operation(
            summary = "Запросить новый токен для подтверждения почты",
            description = "Этот эндпоинт позволяет пользователю запросить новый токен для подтверждения учетной записи."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Новый токен подтверждения отправлен на вашу почту."),
            @ApiResponse(responseCode = "400", description = "Неверный email или другие ошибки при запросе токена."),
            @ApiResponse(responseCode = "404", description = "Пользователь с таким email не найден."),
    })

    //TODO make it public or authenticated in security config
    public ResponseEntity<String> resendVerificationToken(@RequestParam("email") String email) {
        return playerService.resendVerificationToken(email);
    }
}