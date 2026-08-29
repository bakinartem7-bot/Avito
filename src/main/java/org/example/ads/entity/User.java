package org.example.ads.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность пользователя для таблицы users.
 * <p>
 * Соответствует схеме БД:
 * - id: UUID, первичный ключ
 * - username, email: уникальные поля
 * - password_hash: колонка для хранения хеша пароля (BCrypt)
 * - role: роль пользователя (USER, ADMIN и т.п.)
 * - registered_at, updated_at: временные метки
 * </p>
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Уникальный идентификатор пользователя. Генерируется автоматически как UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Отображаемое имя пользователя (никнейм). Должно быть уникальным.
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Хеш пароля пользователя.
     * <p>
     * В базе данных колонка называется password_hash.
     * Для хранения пароля следует использовать кодирование (например, BCrypt).
     * </p>
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Email пользователя. Используется как логин. Должен быть уникальным.
     */
    @Column(unique = true, nullable = false, length = 255)
    private String email;

    /**
     * Роль пользователя (USER, ADMIN, MODERATOR и т.д.).
     * По умолчанию при создании устанавливается значение USER.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'USER'")
    private Role role;

    /**
     * Дата и время регистрации пользователя. Заполняется автоматически при создании.
     */
    @Column(name = "registered_at", updatable = false)
    private Instant registeredAt;

    /**
     * Дата и время последнего обновления записи.
     * Автоматически обновляется при каждом изменении сущности.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Город проживания пользователя (опционально).
     */
    @Column(length = 100)
    private String city;

    /**
     * Контактный телефон пользователя (опционально).
     */
    @Column(length = 20)
    private String phone;

    /**
     * Список объявлений, созданных пользователем.
     * Связь: один ко многим (OneToMany), ленивая загрузка.
     */
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Ad> ads = new ArrayList<>();

    /**
     * Список комментариев, оставленных пользователем.
     * Связь: один ко многим (OneToMany), ленивая загрузка.
     */
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    /**
     * Метод-перехватчик, вызываемый перед сохранением сущности в БД.
     * Устанавливает значения по умолчанию, если они не заданы явно.
     */
    @PrePersist
    protected void onPrePersist() {
        if (this.registeredAt == null) {
            this.registeredAt = Instant.now();
        }
        if (this.role == null) {
            this.role = Role.USER;
        }
    }

    /**
     * Позволяет установить отображаемое имя (username) через отдельный метод.
     * Удобно для сценариев, где имя задаётся отдельно от остальных полей.
     *
     * @param displayName новое значение для username
     */
    public void setDisplayName(String displayName) {
        this.username = displayName;
    }
}
