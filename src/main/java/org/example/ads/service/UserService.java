package org.example.ads.service;

import org.example.ads.entity.User;
import org.example.ads.entity.Role;
import org.example.ads.repository.UserRepository;
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
        // userRepository.save(user); // @Transactional сохранит автоматически
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

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

        return user; // save произойдёт автоматически благодаря @Transactional
    }
}
