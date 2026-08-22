package org.example.ads.service;

import org.example.ads.dto.CommentDto;
import org.example.ads.entity.Ad;
import org.example.ads.entity.Comment;
import org.example.ads.entity.User;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.mapper.CommentMapper;
import org.example.ads.repository.CommentRepository;
import org.example.ads.repository.AdRepository;
import org.example.ads.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public CommentDto createComment(UUID adId, UUID userId, String content) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Comment comment = new Comment();
        comment.setAd(ad);
        comment.setUser(user);
        comment.setContent(content);
        // createdAt/updatedAt заполняются через @PrePersist

        Comment saved = commentRepository.save(comment);
        return commentMapper.toDto(saved);
    }

    public List<CommentDto> getCommentsForAd(UUID adId) {
        return commentRepository.findByAdIdOrderByCreatedAtDesc(adId).stream()
                .map(commentMapper::toDto)
                .toList();
    }

    public CommentDto updateComment(UUID commentId, UUID currentUserId, String newContent) {
        if (!commentRepository.existsByIdAndUserId(commentId, currentUserId)) {
            throw new AccessDeniedException("Нельзя редактировать чужой комментарий");
        }

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Комментарий не найден")
        );
        comment.setContent(newContent);
        // updatedAt обновится через @PreUpdate

        return commentMapper.toDto(commentRepository.save(comment));
    }

    public void deleteComment(UUID commentId, UUID currentUserId) {
        if (!commentRepository.existsByIdAndUserId(commentId, currentUserId)) {
            throw new AccessDeniedException("Нельзя удалять чужой комментарий");
        }
        commentRepository.deleteById(commentId);
    }
}
