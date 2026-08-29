package org.example.ads.controller;

import org.example.ads.dto.AuthRequest;
import org.example.ads.dto.AuthResponse;
import org.example.ads.service.AuthService;
import org.example.ads.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для операций аутентификации и регистрации.
 * <p>
 * Поддерживает:
 * - регистрацию новых пользователей через {@link #register(AuthRequest)};
 * - аутентификацию и выдачу JWT-токена через {@link #login(AuthRequest)}.
 * <p>
 * Эндпоинты в группе /api/auth открыты для анонимных запросов (без токена),
 * что позволяет выполнить вход и получить токен до авторизации.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param authService               сервис регистрации пользователей
     * @param authenticationManager     менеджер аутентификации Spring Security
     * @param jwtService                сервис генерации JWT-токенов
     */
    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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

    /**
     * Выполняет аутентификацию пользователя и возвращает JWT-токен.
     * <p>
     * Принимает email и пароль в теле запроса. Использует {@link AuthenticationManager}
     * для проверки учётных данных. При успешной аутентификации извлекает данные пользователя,
     * генерирует JWT-токен через {@link JwtService} и возвращает его в ответе.
     * При ошибке аутентификации (неверный пароль или пользователь не найден) выбрасывается
     * исключение, которое Spring Security преобразует в ответ 401 Unauthorized.
     *
     * @param request DTO с email и паролем для входа
     * @return ResponseEntity с токеном и сообщением об успехе
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token, "Успешная авторизация"));
    }
}
