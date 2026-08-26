package org.example.ads.service;

import lombok.extern.slf4j.Slf4j;
import org.example.ads.dto.ChangePasswordDto;
import org.example.ads.dto.UserDto;
import org.example.ads.dto.UserProfileUpdateDto;
import org.example.ads.entity.User;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Consumer;

@Service
@Transactional
@Slf4j // Нужен для log.info/log.error
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Если здесь будет ошибка "No qualifying bean of type 'PasswordEncoder'",
    // значит, у тебя нет @Bean в SecurityConfig. См. пояснение ниже.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return toDto(user);
    }

    @Transactional(readOnly = false) // Явное указание, что тут пишем в БД
    public UserDto updateProfile(UUID userId, UserProfileUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        applyIfNotNull(dto.getDisplayName(), user::setDisplayName);
        applyIfNotNull(dto.getPhone(), user::setPhone);
        applyIfNotNull(dto.getCity(), user::setCity);

        // updatedAt обновится автоматически через @PreUpdate в сущности
        User saved = userRepository.save(user);
        log.debug("Profile updated for user: {}", userId);
        return toDto(saved);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordDto dto) {
        if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()) {
            throw new AccessDeniedException("Current password cannot be empty");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new AccessDeniedException("New password cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed: incorrect current password for user {}", userId);
            throw new AccessDeniedException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Password successfully changed for user: {}", userId);
    }

    private UserDto toDto(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setDisplayName(u.getDisplayName());
        dto.setPhone(u.getPhone());
        dto.setCity(u.getCity());
        dto.setRole(u.getRole());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setUpdatedAt(u.getUpdatedAt());
        return dto;
    }

    private static <T> void applyIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
