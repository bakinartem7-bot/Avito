package org.example.ads.service;

import org.example.ads.dto.CommentDto;
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
        adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Ad not found"));
        return commentRepository.findByAdId(adId).stream()
                .map(this::toDto)
                .toList();
    }

    public CommentDto createComment(UUID adId, UUID authorId, String content) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Ad not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setAd(ad);
        comment.setAuthor(author);
        comment.setContent(content);

        return toDto(commentRepository.save(comment));
    }

    public CommentDto updateComment(UUID id, UUID currentUserId, String content) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own comments");
        }

        comment.setContent(content);

        return toDto(commentRepository.save(comment));
    }

    public void deleteComment(UUID id, UUID currentUserId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    private CommentDto toDto(Comment c) {
        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setAdId(c.getAd().getId());
        dto.setUserId(c.getAuthor().getId());
        dto.setContent(c.getContent());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }
}
