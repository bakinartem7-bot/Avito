package org.example.ads.service;

import org.example.ads.dto.AuthRequest;
import org.example.ads.dto.AuthResponse;
import org.example.ads.entity.User;
import org.example.ads.repository.UserRepository;
import org.example.ads.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Сервис аутентификации и регистрации пользователей.
 * Реализует логику создания учётных записей, проверки учётных данных
 * и выдачи JWT-токенов доступа.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Создаёт экземпляр AuthService с внедрёнными зависимостями.
     *
     * @param userRepository репозиторий для работы с сущностью User
     * @param passwordEncoder компонент для хеширования паролей
     * @param jwtService сервис для генерации и валидации JWT-токенов
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Регистрирует нового пользователя в системе.
     * <p>
     * Проверяет уникальность email, хеширует пароль и сохраняет пользователя.
     * Возвращает токен доступа сразу после успешной регистрации.
     * </p>
     *
     * @param request DTO с данными для регистрации (email и пароль)
     * @return объект AuthResponse, содержащий accessToken (и при необходимости refreshToken)
     * @throws IllegalStateException если пользователь с указанным email уже существует
     */
    public AuthResponse register(AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("User already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        return response;
    }

    /**
     * Выполняет аутентификацию пользователя по учётным данным.
     * <p>
     * Находит пользователя по email, проверяет соответствие пароля,
     * затем генерирует и возвращает JWT-токен.
     * </p>
     *
     * @param request DTO с учётными данными (email и пароль)
     * @return объект AuthResponse с accessToken
     * @throws IllegalStateException если пользователь не найден или пароль неверен
     */
    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalStateException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        return response;
    }
}
