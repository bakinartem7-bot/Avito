package org.example.ads.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false) // TEXT лучше подходит для длинных описаний в PostgreSQL
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant updatedAt;

    /**
     * Слушатель Hibernate: срабатывает перед каждым обновлением сущности.
     * Гарантирует, что updatedAt всегда актуален.
     */
    @PreUpdate
    protected void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Слушатель Hibernate: срабатывает при первом сохранении сущности.
     * Устанавливает createdAt, если оно не было задано вручную.
     */
    @PrePersist
    protected void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
