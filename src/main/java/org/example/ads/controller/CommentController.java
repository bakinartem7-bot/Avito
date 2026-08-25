package org.example.ads.controller;

import org.example.ads.dto.CommentDto;
import org.example.ads.service.CommentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с комментариями к объявлениям.
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Возвращает все комментарии к объявлению.
     */
    @GetMapping("/ad/{adId}")
    public List<CommentDto> getCommentsForAd(@PathVariable UUID adId) {
        return commentService.getCommentsForAd(adId).stream()
                .map(c -> {
                    CommentDto dto = new CommentDto();
                    dto.setId(c.getId());
                    dto.setAdId(c.getAd().getId());
                    dto.setUserId(c.getAuthor().getId());
                    dto.setContent(c.getContent());
                    dto.setCreatedAt(c.getCreatedAt());
                    dto.setUpdatedAt(c.getUpdatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Добавляет комментарий к объявлению от имени авторизованного пользователя.
     */
    @PostMapping("/ad/{adId}")
    public CommentDto createComment(
            @PathVariable UUID adId,
            @RequestBody String text) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        UUID authorId = (UUID) auth.getPrincipal();

        var comment = commentService.createComment(adId, authorId, text);

        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setAdId(comment.getAd().getId());
        dto.setUserId(comment.getAuthor().getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }

    /**
     * Удаляет комментарий. Разрешено только автору комментария.
     */
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable UUID id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = (UUID) auth.getPrincipal();
        commentService.deleteComment(id, currentUserId);
    }
}
