package org.example.ads.security;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.ads.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Объект принципала (пользователя) для Spring Security.
 * Хранит идентификатор пользователя и его роль, используемые для авторизации запросов.
 * Интегрируется с JWT-фильтром и SecurityContextHolder.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppPrincipal {

    /**
     * Уникальный идентификатор пользователя (UUID).
     */
    private UUID userId;

    /**
     * Роль пользователя в системе (например, USER, ADMIN).
     */
    private Role role;

    /**
     * Получает список полномочий (GrantedAuthority) на основе роли пользователя.
     * Преобразует enum Role в формат Spring Security: "ROLE_USER", "ROLE_ADMIN" и т.д.
     *
     * @return Список полномочий для аутентифицированного пользователя.
     */
    public List<GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Вспомогательный метод для получения ID пользователя.
     * Необходим для контроллеров, где требуется извлечь UUID из контекста безопасности.
     *
     * @return UUID текущего пользователя.
     */
    public UUID getUserId() {
        return userId;
    }
}
