package org.example.ads.controller;

import org.example.ads.dto.CommentDto;
import org.example.ads.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // ✅ ПРАВИЛЬНО: получаем сущности, маппим в DTO, возвращаем DTO
    @GetMapping("/ad/{adId}")
    public List<CommentDto> getCommentsForAd(@PathVariable UUID adId) {
        List<org.example.ads.entity.Comment> comments = commentService.getCommentsForAd(adId);

        return comments.stream()
                .map(org.example.ads.mapper.CommentMapper.TO_DTO)
                .collect(Collectors.toList());
    }

    // Пример создания комментария (тоже возвращаем DTO)
    @PostMapping("/ad/{adId}")
    public CommentDto createComment(
            @PathVariable UUID adId,
            @RequestHeader("X-User-Id") UUID authorId,
            @RequestBody String text) {

        org.example.ads.entity.Comment commentEntity = commentService.createComment(adId, authorId, text);

        // ✅ Маппим одну сущность в DTO перед возвратом
        return org.example.ads.mapper.CommentMapper.TO_DTO.apply(commentEntity);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID currentUserId) {
        commentService.deleteComment(id, currentUserId);
    }
}
