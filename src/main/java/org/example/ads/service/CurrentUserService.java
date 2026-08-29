package org.example.ads.service;

import org.example.ads.repository.UserRepository;
import org.example.ads.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Сервис для получения данных текущего авторизованного пользователя.
 * <p>
 * Определяет UUID пользователя по email из SecurityContext (Spring Security).
 * Используется во всех сервисах, где требуется проверка прав (авторство объявлений, комментариев и т.п.).
 * Для диплома это ключевой компонент реализации RBAC (Role-Based Access Control).
 */
@Service
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Получает UUID текущего авторизованного пользователя.
     * <p>
     * Извлекает email из SecurityContextHolder, затем ищет пользователя в БД по email
     * и возвращает его UUID. Если пользователь не найден — выбрасывает исключение
     * (в реальном проекте можно обработать иначе, но для MVP допустимо).
     *
     * @return UUID текущего пользователя
     * @throws IllegalArgumentException если пользователь не найден в БД
     */
    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Пользователь не авторизован");
        }
        String email = auth.getName(); // в Spring Security getName() возвращает username (email)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return user.getId();
    }
}
