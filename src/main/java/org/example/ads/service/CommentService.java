package org.example.ads.service;

import org.example.ads.dto.CommentDto;
import org.example.ads.entity.Ad;
import org.example.ads.entity.Comment;
import org.example.ads.entity.User; // <-- Добавлен импорт сущности User
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.repository.AdRepository;
import org.example.ads.repository.CommentRepository;
import org.example.ads.repository.UserRepository; // <-- Добавлен репозиторий
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с комментариями.
 * <p>
 * ВАЖНО: Для корректной работы DTO без LazyInitializationException
 * используется метод репозитория с JOIN FETCH.
 */
@Service
@Transactional
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

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForAd(UUID adId) {
        if (!adRepository.existsById(adId)) {
            throw new NotFoundException("Ad not found");
        }

        return commentRepository.findByAdIdWithAssociations(adId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Создаёт комментарий.
     * <p>
     * ВАЖНО: Мы загружаем реального пользователя из БД.
     * Это гарантирует:
     * 1. Что пользователь существует.
     * 2. Что внешний ключ в БД будет валидным.
     * 3. Отсутствие ошибок типов.
     */
    @Transactional
    public CommentDto createComment(UUID adId, UUID authorId, String content) {
        Assert.hasText(content, "Comment content cannot be null or empty");
        Assert.notNull(adId, "Ad ID cannot be null");
        Assert.notNull(authorId, "Author ID cannot be null");

        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Ad not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found: " + authorId));

        Comment comment = new Comment();
        comment.setAd(ad);
        comment.setAuthor(author);
        comment.setContent(content);

        return toDto(commentRepository.save(comment));
    }

    public CommentDto updateComment(UUID id, UUID currentUserId, String content) {
        Assert.hasText(content, "Comment content cannot be empty");

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (comment.getAuthor() == null) {
            throw new AccessDeniedException("Author information is missing — check Hibernate fetch strategy");
        }

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own comments");
        }

        comment.setContent(content);
        return toDto(commentRepository.save(comment));
    }

    public void deleteComment(UUID id, UUID currentUserId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (comment.getAuthor() == null) {
            throw new AccessDeniedException("Author information is missing — check Hibernate fetch strategy");
        }

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private CommentDto toDto(Comment c) {
        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setAdId(c.getAd() != null ? c.getAd().getId() : null);
        dto.setUserId(c.getAuthor() != null ? c.getAuthor().getId() : null);

        dto.setContent(c.getContent());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }
}
