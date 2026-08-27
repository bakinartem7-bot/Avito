package org.example.ads.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Сущность объявления.
 * Соответствует таблице ads в базе данных (PostgreSQL / H2).
 * <p>
 * ВАЖНО: Для стабильной работы в двух БД (H2 и PostgreSQL) используется
 * GenerationType.UUID — это гарантирует генерацию ID на стороне приложения.
 * <p>
 * Для корректной работы с коллекцией comments в сервисах/DTO необходимо:
 * - либо загружать коллекцию через EntityGraph / JOIN FETCH в репозитории,
 * - либо не обращаться к ней вне транзакции (иначе LazyInitializationException).
 */
@Entity
@Table(name = "ads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ad {

    /**
     * Уникальный идентификатор объявления.
     * GenerationType.UUID предпочтительнее AUTO для UUID-ключей в Hibernate 6.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "published_at", updatable = false)
    private Instant publishedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    /**
     * Коллекция комментариев.
     * - LAZY: не загружается автоматически, чтобы не делать лишних SELECT.
     * - CascadeType.ALL + orphanRemoval=true: при удалении объявления удаляются и его комментарии.
     * <p>
     * Инициализация new ArrayList() здесь безопасна: Lombok @Data не переопределяет конструктор,
     * и при загрузке из БД Hibernate заменит коллекцию на свою прокси-версию.
     */
    @OneToMany(mappedBy = "ad", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onPrePersist() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.publishedAt == null) {
            this.publishedAt = now;
        }
        if (this.price == null) {
            this.price = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.updatedAt = Instant.now();
    }
}
