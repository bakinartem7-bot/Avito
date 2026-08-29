package org.example.ads.service;

import org.example.ads.entity.User;
import org.example.ads.repository.UserRepository;
import org.example.ads.dto.AuthRequest;
import org.example.ads.dto.ChangePasswordDto;
import org.example.ads.dto.UserProfileUpdateDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрация пользователя через DTO.
     */
    @Transactional
    public User register(AuthRequest request) {
        Assert.hasText(request.email(), "Email must not be empty");
        Assert.hasText(request.password(), "Password must not be empty");

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID()); // если ID не генерируется автоматически
        user.setEmail(request.email());

        // username можно сделать из email (до @) или оставить null, если не используется
        int atIndex = request.email().indexOf('@');
        String username = (atIndex > 0) ? request.email().substring(0, atIndex) : request.email();
        user.setUsername(username);

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(org.example.ads.entity.Role.USER); // если есть enum Role

        return userRepository.save(user);
    }

    /**
     * Поиск по email — КРИТИЧНО для CustomUserDetailsService.
     * Spring Security будет искать пользователя именно по этому методу.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Устаревший метод поиска по username (оставлен для совместимости).
     */
    @Deprecated
    public Optional<User> findByUsername(String username) {
        return Optional.empty();
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordDto dto) {
        Assert.notNull(userId, "userId must not be null");
        Assert.notNull(dto, "dto must not be null");
        Assert.hasText(dto.getCurrentPassword(), "currentPassword must not be empty");
        Assert.hasText(dto.getNewPassword(), "newPassword must not be empty");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setPasswordHash(encodedNewPassword);
        // save не нужен: в транзакционном методе Spring Data сам сделает flush при выходе
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateProfile(UUID id, UserProfileUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (dto.getDisplayName() != null) {
            user.setDisplayName(dto.getDisplayName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getCity() != null) {
            user.setCity(dto.getCity());
        }

        return userRepository.save(user); // обязательно сохраняем изменения
    }
}
