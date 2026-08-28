package org.example.ads.controller;

import lombok.RequiredArgsConstructor;
import org.example.ads.dto.AuthRequest;
import org.example.ads.dto.AuthResponse;
import org.example.ads.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Регистрация пользователя.
     * Возвращаем пустой AuthResponse (без токенов, т.к. работаем без JWT).
     * Главное — НЕ возвращать объект User с хешем пароля!
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {

        authService.register(request);
        return ResponseEntity.ok(new AuthResponse("", ""));
    }

    /**
     * Логин.
     * В схеме БЕЗ JWT этот эндпоинт просто проверяет пароль.
     * Сессия создаётся автоматически фильтром Spring Security при POST на /login,
     * но если ты вызываешь этот метод явно — тоже возвращаем пустой ответ.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authService.authenticate(request);

        return ResponseEntity.ok(new AuthResponse("", ""));
    }
}
