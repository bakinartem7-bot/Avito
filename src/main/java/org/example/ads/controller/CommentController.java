package org.example.ads.controller;

import org.example.ads.dto.CommentCreateDto;
import org.example.ads.dto.CommentDto;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.service.CommentService;
import org.example.ads.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final CurrentUserService currentUserService;

    public CommentController(CommentService commentService, CurrentUserService currentUserService) {
        this.commentService = commentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/ad/{adId}")
    public List<CommentDto> getCommentsForAd(@PathVariable UUID adId) {
        return commentService.getCommentsForAd(adId);
    }

    @PostMapping("/ad/{adId}")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable UUID adId,
            @RequestBody CommentCreateDto dto
    ) {
        UUID authorId = currentUserService.getCurrentUserId();
        CommentDto result = commentService.createComment(adId, authorId, dto.getContent());
        return ResponseEntity.status(201).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable UUID id,
            @RequestBody CommentCreateDto dto
    ) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        CommentDto result = commentService.updateComment(id, currentUserId, dto.getContent());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id
    ) {
        UUID currentUserId = currentUserService.getCurrentUserId();

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
