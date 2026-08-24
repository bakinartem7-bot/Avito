package org.example.ads.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Schema(description = "Пароль пользователя", example = "secret123")
    private String password;
}
