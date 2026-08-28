package org.example.ads.security;

import java.util.UUID;

// В схеме БЕЗ JWT этот класс можно даже не использовать напрямую.
// Но если ты хочешь хранить в нём данные — оставляем.
public class AppPrincipal {
    private final UUID userId;
    private final String role;

    public AppPrincipal(UUID userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() { return userId; }
    public String getRole() { return role; }
}
