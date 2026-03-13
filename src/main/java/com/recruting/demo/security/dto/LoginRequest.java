package com.recruting.demo.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Логин или email обязателен")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    private String password;
}
