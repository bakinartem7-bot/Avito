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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid current password");
        }

        String newEncoded = passwordEncoder.encode(dto.getNewPassword());
        user.setPasswordHash(newEncoded);

    }

    @Transactional
    public User updateProfile(UUID userId, UserProfileUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (dto.getDisplayName() != null) {
            user.setUsername(dto.getDisplayName()); // setDisplayName меняет username
        }
        if (dto.getCity() != null) {
            user.setCity(dto.getCity());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        return user;
    }

    public User registerUser(String username, String email, String rawPassword, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }
}
