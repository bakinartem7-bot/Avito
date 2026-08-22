package org.example.ads.controller;

import org.example.ads.dto.CommentDto;
import org.example.ads.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/ad/{adId}")
    @Operation(summary = "Добавить комментарий к объявлению")
    @ApiResponse(responseCode = "201", description = "Комментарий создан")
    public ResponseEntity<CommentDto> create(@PathVariable UUID adId,
                                             @RequestParam UUID userId,
                                             @NotBlank @RequestParam String content) {
        return ResponseEntity.status(201).body(
                commentService.createComment(adId, userId, content)
        );
    }

    @GetMapping("/ad/{adId}")
    @Operation(summary = "Список комментариев к объявлению")
    public ResponseEntity<java.util.List<CommentDto>> list(@PathVariable UUID adId) {
        return ResponseEntity.ok(commentService.getCommentsForAd(adId));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Обновить комментарий (только свой)")
    public ResponseEntity<CommentDto> update(@PathVariable UUID commentId,
                                             @RequestParam UUID currentUserId,
                                             @NotBlank @RequestParam String content) {
        return ResponseEntity.ok(
                commentService.updateComment(commentId, currentUserId, content)
        );
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Удалить комментарий (только свой)")
    public ResponseEntity<Void> delete(@PathVariable UUID commentId,
                                       @RequestParam UUID currentUserId) {
        commentService.deleteComment(commentId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
