package com.karam.teamup.player.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "players")
@Schema(description = "пользователь Entity")
public class Player {
    @Id
    @Column(name = "player_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(name = "player_id", example = "72bde859-d59e-4a61-b060-d5fa60426203", required = true, description = "Уникальный идентификатор пользователя.")
    UUID playerId;

    @NotNull
    @NotBlank
    @Column(name = "user_name",nullable = false, unique = true)
    @Size(min = 1, max = 100, message = "Имя должно быть от 1 до 100 символов")
    @Schema(name = "name", example = "Карам", required = true, description = "Имя пользователя. Должно быть от 1 до 100 символов.")
    private String userName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Email(message = "Введите действительный адрес электронной почты")
    @Column(name = "email")
    @Schema(name = "email", example = "2002@hotmail.com", required = true,
            description = "Электронная почта пользователя. Должна быть уникальной и в правильном формате.")
    private String email;

    @Column(name = "phone_number")
    @Size(max = 18, message = "Номер телефона не должен превышать 18 символов")
    @Pattern(regexp = "(^\\+7|7|8)[0-9]{10}$|^\\+7\\s?\\(\\d{3}\\)\\s?\\d{3}[-\\s]?\\d{2}[-\\s]?\\d{2}$",
            message = "Неверный формат номера телефона. Используйте формат +7 (XXX) XXX-XX-XX или 7XXXXXXXXXX.")
    @Schema(name = "phoneNumber", example = "+7 (123) 456-78-90",
            description = "Номер телефона пользователя. Должен быть в формате: +7 (XXX) XXX-XX-XX.")
    private String phoneNumber;

    @NotBlank
    @NotNull
    @Column(name = "password")
    @Size(min = 7, max = 255, message = "Пароль должна быть от 7 символов")
    @Schema(name = "password", example = "pass123123", required = true,
            description = "Пароль пользователя. Должен быть от 7 до 255 символов.")
    private String password;

    @Past
    @Column(name = "date_of_birth")
    @Schema(name = "birthday", example = "2002-11-04", description = "Дата рождения пользователя. Формат: yyyy-MM-dd.")
    private LocalDate birthDate;

    @Column(name = "gender")
    private String gender;

    @Column(name = "city")
    private String city;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "bio")
    private String bio;

    @Column(name = "sport")
    private String sport;
}
