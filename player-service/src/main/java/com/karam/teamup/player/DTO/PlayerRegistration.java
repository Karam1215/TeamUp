package com.karam.teamup.player.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlayerRegistration(

        @NotBlank(message = "Username cannot be empty")
        @Size(min = 1, max = 100, message = "Имя должно быть от 1 до 100 символов")
        String userName,

        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Введите действительный адрес электронной почты")
        String email,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 7, max = 255, message = "Пароль должна быть от 7 символов")
        String password
) {
}