package org.example.ads.controller;

import org.example.ads.dto.CommentDto;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/ad/{adId}")
    public List<CommentDto> getCommentsForAd(@PathVariable UUID adId) {
        return commentService.getCommentsForAd(adId);
    }

    @PostMapping("/ad/{adId}")
    public ResponseEntity<CommentDto> createComment(@PathVariable UUID adId, @RequestBody String content) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        UUID authorId = (UUID) auth.getPrincipal();

        CommentDto dto = commentService.createComment(adId, authorId, content);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable UUID id, @RequestBody String content) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = (UUID) auth.getPrincipal();

        CommentDto dto = commentService.updateComment(id, currentUserId, content);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = (UUID) auth.getPrincipal();
        try {
            commentService.deleteComment(id, currentUserId);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
