package org.example.ads.controller;

import org.example.ads.dto.AuthRequest;
import org.example.ads.dto.AuthResponse;
import org.example.ads.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для операций аутентификации и регистрации.
 * <p>
 * В текущей реализации поддерживает только регистрацию пользователя.
 * Эндпоинт не возвращает токены (проект без JWT), но структура DTO подготовлена к расширению.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Регистрирует нового пользователя.
     * <p>
     * Открыт для всех (без авторизации). Проверяет уникальность email и сохраняет пользователя.
     * В ответе возвращает пустой AuthResponse (без токенов).
     * Важно: не возвращает чувствительные данные (например, хеш пароля).
     *
     * @param request DTO с email и паролем
     * @return ResponseEntity с пустым AuthResponse
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new AuthResponse("", ""));
    }
}
