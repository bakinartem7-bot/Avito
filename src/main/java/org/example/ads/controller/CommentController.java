package org.example.ads.controller;

import org.example.ads.dto.CommentCreateDto;
import org.example.ads.dto.CommentDto;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.service.CommentService;
import org.example.ads.security.AppPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с комментариями объявлений.
 * Предоставляет REST API для получения, создания, обновления и удаления комментариев.
 * Все методы, кроме GET списка комментариев по объявлению, требуют авторизации.
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Получает список всех комментариев для указанного объявления.
     * Доступен без авторизации (публичный эндпоинт).
     *
     * @param adId идентификатор объявления
     * @return список DTO комментариев
     */
    @GetMapping("/ad/{adId}")
    public List<CommentDto> getCommentsForAd(@PathVariable UUID adId) {
        return commentService.getCommentsForAd(adId);
    }

    /**
     * Создаёт новый комментарий к объявлению.
     * Требует авторизации. Автор комментария определяется из контекста безопасности.
     *
     * @param adId    идентификатор объявления
     * @param dto     DTO с данными комментария (содержит поле content)
     * @return DTO созданного комментария с HTTP 201 Created
     */
    @PostMapping("/ad/{adId}")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable UUID adId,
            @RequestBody CommentCreateDto dto,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        UUID authorId = principal.getUserId();
        CommentDto result = commentService.createComment(adId, authorId, dto.getContent());
        return ResponseEntity.status(201).body(result);
    }

    /**
     * Обновляет существующий комментарий.
     * Требует авторизации. Пользователь может обновлять только свои комментарии.
     *
     * @param id      идентификатор комментария
     * @param dto     DTO с новыми данными комментария
     * @param principal объект принципала из контекста безопасности
     * @return обновлённый DTO комментария
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable UUID id,
            @RequestBody CommentCreateDto dto,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        UUID currentUserId = principal.getUserId();
        CommentDto result = commentService.updateComment(id, currentUserId, dto.getContent());
        return ResponseEntity.ok(result);
    }

    /**
     * Удаляет комментарий.
     * Требует авторизации. Пользователь может удалять только свои комментарии.
     * Возвращает 404, если комментарий не найден, и 403, если у пользователя нет прав.
     *
     * @param id идентификатор комментария
     * @param principal объект принципала из контекста безопасности
     * @return пустой ответ с соответствующим HTTP-кодом
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        UUID currentUserId = principal.getUserId();

        try {
            commentService.deleteComment(id, currentUserId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();   // 404 Not Found
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }
    }
}
