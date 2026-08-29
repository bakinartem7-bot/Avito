package org.example.ads.service;

import org.example.ads.entity.Role;
import org.example.ads.entity.User;
import org.example.ads.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Реализация {@link UserDetailsService} для загрузки данных пользователя при аутентификации.
 * <p>
 * Отвечает за поиск пользователя по email (который используется как username) и формирование
 * объекта {@link UserDetails} с соответствующими ролями. Является ключевым компонентом
 * механизма аутентификации в Spring Security для данного проекта.
 * <p>
 * Для дипломной работы демонстрирует интеграцию Spring Security с собственной моделью пользователей
 * и корректное преобразование ролей в GrantedAuthority.
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Конструктор для внедрения зависимости {@link UserRepository}.
     *
     * @param userRepository репозиторий для доступа к данным пользователей
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Загружает данные пользователя для аутентификации по имени пользователя (email).
     * <p>
     * Выполняет поиск пользователя в БД по email. Если пользователь найден, преобразует
     * сущность {@link User} в объект {@link UserDetails}. Если не найден — выбрасывает
     * {@link UsernameNotFoundException}. Логирует попытку поиска пользователя.
     *
     * @param username email пользователя, используемый в качестве username для аутентификации
     * @return объект {@link UserDetails}, содержащий данные пользователя и его роли
     * @throws UsernameNotFoundException если пользователь с указанным email не найден в БД
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Попытка загрузки пользователя по username: {}", username);
        return userRepository.findByEmail(username)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    /**
     * Преобразует сущность {@link User} в объект {@link UserDetails} для Spring Security.
     * <p>
     * Создаёт коллекцию полномочий (authorities) на основе роли пользователя, добавляя
     * префикс "ROLE_" в соответствии с требованиями Spring Security. Возвращает объект
     * UserDetails с email, хешем пароля и набором ролей.
     *
     * @param user сущность пользователя из БД
     * @return сконфигурированный объект {@link UserDetails}
     */
    private UserDetails toUserDetails(User user) {
        var authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }
}
