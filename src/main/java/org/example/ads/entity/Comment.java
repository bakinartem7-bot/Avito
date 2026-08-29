package org.example.ads.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.UUID;

/**
 * Сущность комментария к объявлению.
 * Соответствует таблице comments в базе данных (PostgreSQL / H2).
 * <p>
 * ВАЖНО: Для корректной работы в сервисах (особенно при обращении к ad/author)
 * необходимо использовать методы репозитория с JOIN FETCH.
 * Иначе при ленивой загрузке (FetchType.LAZY) будет LazyInitializationException.
 */
@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    /**
     * Уникальный идентификатор комментария.
     * GenerationType.UUID гарантирует генерацию UUID на стороне приложения
     * и стабильную работу как в PostgreSQL, так и в H2.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    /**
     * Объявление, к которому относится комментарий.
     * Связь ManyToOne с ленивой загрузкой (LAZY).
     * <p>
     * Для использования в DTO без ошибок LazyInitializationException
     * в репозитории должен быть метод с JOIN FETCH, например:
     * SELECT c FROM Comment c JOIN FETCH c.ad JOIN FETCH c.author ...
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    /**
     * Автор комментария (пользователь).
     * Связь ManyToOne с ленивой загрузкой (LAZY).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Текст комментария. Обязательное поле.
     * columnDefinition = "TEXT" оптимален для PostgreSQL (без ограничения длины).
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Дата и время создания комментария.
     * Заполняется автоматически при первом сохранении сущности (onPrePersist).
     * Поле не подлежит обновлению (updatable = false).
     */
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /**
     * Дата и время последнего обновления комментария.
     * Автоматически обновляется при каждом изменении записи (onPreUpdate).
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }

        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.updatedAt = Instant.now();
    }
}
