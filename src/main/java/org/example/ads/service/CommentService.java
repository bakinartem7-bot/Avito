package org.example.ads.service;

import org.example.ads.entity.Ad;
import org.example.ads.entity.Comment;
import org.example.ads.entity.User;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.repository.AdRepository;
import org.example.ads.repository.CommentRepository;
import org.example.ads.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * Сервис комментариев. Реализует бизнес-логику для работы с комментариями:
 * получение списка, создание, удаление с обязательной проверкой прав доступа.
 */
@Service
@Transactional(readOnly = false)
public class CommentService {

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            AdRepository adRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.adRepository = adRepository;
        this.userRepository = userRepository;
    }

    /**
     * Возвращает список всех комментариев к объявлению.
     *
     * @param adId ID объявления
     * @return список сущностей Comment
     * @throws NotFoundException если объявление не найдено (для согласованности поведения API)
     */
    @Transactional(readOnly = true)
    public List<Comment> getCommentsForAd(UUID adId) {
        // Проверка существования объявления — чтобы API возвращал 404, если объявления нет
        adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Ad not found"));
        return commentRepository.findByAdId(adId);
    }

    /**
     * Создаёт новый комментарий к объявлению от имени пользователя.
     *
     * @param adId      ID объявления
     * @param authorId  ID автора комментария (пользователя)
     * @param text      Текст комментария
     * @return сохранённая сущность Comment
     * @throws NotFoundException если не найдено объявление или пользователь
     */
    public Comment createComment(UUID adId, UUID authorId, String text) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Ad not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setAd(ad);
        comment.setAuthor(author);
        comment.setContent(text);
        // createdAt и updatedAt заполняются автоматически через @PrePersist / @PreUpdate

        return commentRepository.save(comment);
    }

    /**
     * Удаляет комментарий, если текущий пользователь является его автором.
     *
     * @param id            ID комментария
     * @param currentUserId ID текущего пользователя (из JWT)
     * @throws AccessDeniedException если пользователь пытается удалить чужой комментарий
     * @throws NotFoundException    если комментарий не найден
     */
    public void deleteComment(UUID id, UUID currentUserId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }
}
