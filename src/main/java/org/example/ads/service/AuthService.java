package org.example.ads.service;

import lombok.RequiredArgsConstructor;
import org.example.ads.dto.AuthRequest;
import org.example.ads.dto.AuthResponse;
import org.example.ads.entity.User;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.repository.UserRepository;
import org.example.ads.entity.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Регистрация нового пользователя.
     * Для email admin@test.com автоматически назначается роль ADMIN.
     */
    public AuthResponse register(AuthRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists with this email");
        }

        User user = new User();
        user.setEmail(request.getEmail());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPasswordHash(encodedPassword);

        String[] parts = request.getEmail().split("@");
        String displayName = parts.length > 0 ? parts[0] : request.getEmail();

        user.setDisplayName(displayName);

        if ("admin@test.com".equalsIgnoreCase(request.getEmail())) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

        user = userRepository.save(user);

        return new AuthResponse(null, null);
    }

    /**
     * Аутентификация пользователя (логин + пароль).
     * В текущей конфигурации без JWT этот метод фактически просто проверяет валидность пароля.
     * Реальная аутентификация будет происходить через стандартный механизм Spring Security (Basic Auth).
     */
    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found with this email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AccessDeniedException("Invalid password");
        }

        return new AuthResponse(null, null);
    }
}
