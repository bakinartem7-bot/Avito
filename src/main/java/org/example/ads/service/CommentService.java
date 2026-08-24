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
 * Сервис для управления комментариями к объявлениям.
 * Обеспечивает создание, получение и удаление комментариев
 * с проверкой прав доступа (только автор может удалять свой комментарий).
 */
@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;

    /**
     * Создаёт экземпляр CommentService с внедрёнными зависимостями.
     *
     * @param commentRepository репозиторий для работы с комментариями
     * @param adRepository репозиторий для проверки существования объявления
     * @param userRepository репозиторий для проверки существования пользователя-автора
     */
    public CommentService(CommentRepository commentRepository,
                          AdRepository adRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.adRepository = adRepository;
        this.userRepository = userRepository;
    }

    /**
     * Получает список всех комментариев для указанного объявления.
     *
     * @param adId идентификатор объявления
     * @return список сущностей Comment
     */
    public List<Comment> getCommentsForAd(UUID adId) {
        return commentRepository.findByAdId(adId);
    }

    /**
     * Создаёт новый комментарий к объявлению.
     * <p>
     * Проверяет существование объявления и пользователя-автора,
     * затем сохраняет комментарий в БД.
     * </p>
     *
     * @param adId идентификатор объявления, к которому добавляется комментарий
     * @param authorId идентификатор пользователя, который оставляет комментарий
     * @param text текст комментария
     * @return сохранённая сущность Comment
     * @throws NotFoundException если объявление или пользователь не найдены
     */
    public Comment createComment(UUID adId, UUID authorId, String text) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Ad not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setAd(ad);
        comment.setAuthor(author);
        comment.setText(text);

        return commentRepository.save(comment);
    }

    /**
     * Удаляет комментарий, если текущий пользователь является его автором.
     * <p>
     * Гарантирует, что пользователь может удалять только собственные комментарии.
     * </p>
     *
     * @param id идентификатор комментария
     * @param currentUserId идентификатор текущего пользователя (проверяемый автор)
     * @throws NotFoundException если комментарий не найден
     * @throws AccessDeniedException если текущий пользователь не является автором комментария
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
