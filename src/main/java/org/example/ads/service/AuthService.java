package org.example.ads.service;

import org.example.ads.entity.User;
import org.example.ads.entity.Role;
import org.example.ads.repository.UserRepository;
import org.example.ads.dto.AuthRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.logging.Logger; // Используем стандартный логгер, чтобы не зависеть от Lombok

@Service
@Transactional
public class AuthService {

    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(AuthRequest request) {
        String email = request.getEmail();

        // 1. Проверка на null и пустоту
        if (email == null || email.isBlank()) {
            logger.severe("Попытка регистрации с пустым email");
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        // 2. Проверка на существование пользователя
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warning("Попытка повторной регистрации для email: " + email);
            throw new IllegalArgumentException("User with this email already exists");
        }

        // 3. Создание пользователя
        User user = new User();

        // Генерируем ID вручную
        user.setId(UUID.randomUUID());

        user.setEmail(email);

        // ИСПРАВЛЕННАЯ ЛОГИКА: безопасное получение имени до символа @
        String usernamePart;
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            usernamePart = email.substring(0, atIndex);
        } else {
            // Если символа @ нет (невалидный email), берем весь email или дефолтное значение
            usernamePart = email;
        }
        user.setUsername(usernamePart);

        // Хеширование пароля
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Установка роли по умолчанию
        user.setRole(Role.USER);

        // 4. Сохранение
        userRepository.save(user);
        logger.info("Пользователь успешно зарегистрирован: id=" + user.getId() + ", email=" + user.getEmail());
    }
}
