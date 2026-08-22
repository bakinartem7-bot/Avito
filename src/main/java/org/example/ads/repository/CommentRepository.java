package org.example.ads.repository;

import org.example.ads.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Получить все комментарии к объявлению
    List<Comment> findByAdIdOrderByCreatedAtDesc(UUID adId);

    // Проверить, что комментарий принадлежит пользователю (для удаления/редактирования)
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
