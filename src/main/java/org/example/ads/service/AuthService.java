package org.example.ads.service;

import org.example.ads.entity.Role;
import org.example.ads.entity.User;
import org.example.ads.repository.UserRepository;
import org.example.ads.dto.AuthRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.logging.Logger;

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
        String email = request.email();

        if (email == null || email.isBlank()) {
            logger.warning("Попытка регистрации с пустым email");
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            logger.warning("Попытка повторной регистрации для email: " + email);
            throw new IllegalStateException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);

        int atIndex = email.indexOf('@');
        String username = (atIndex > 0) ? email.substring(0, atIndex) : email;
        user.setUsername(username);

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        logger.info("Сохранение нового пользователя: email=" + email + ", id=" + user.getId());
        userRepository.save(user); // <-- это реально пишет в SQL
    }
}
