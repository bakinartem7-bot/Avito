package org.example.ads.repository;

import org.example.ads.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с комментариями.
 * <p>
 * ВАЖНО: Метод findByAdIdWithAssociations использует JOIN FETCH, чтобы загрузить
 * связанные сущности Ad и User в одном запросе. Это предотвращает LazyInitializationException
 * при обращении к c.getAd() и c.getAuthor() вне транзакции (например, в DTO-маппере).
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Базовый метод — возвращает комментарии без загрузки ассоциаций.
     * Подходит только для случаев, когда ассоциации не нужны или загружаются явно.
     */
    List<Comment> findByAdId(UUID adId);

    /**
     * Метод с JOIN FETCH — загружает ad и author вместе с комментариями.
     * Используется в CommentService для безопасного маппинга в CommentDto.
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.ad JOIN FETCH c.author WHERE c.ad.id = :adId")
    List<Comment> findByAdIdWithAssociations(@Param("adId") UUID adId);
}
