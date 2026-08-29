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

/**
 * REST-контроллер для работы с комментариями к объявлениям.
 * <p>
 * Позволяет получать, создавать, обновлять и удалять комментарии.
 * Операции изменения требуют авторизации и проверки прав пользователя:
 * пользователь может управлять только своими комментариями.
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final CurrentUserService currentUserService;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param commentService сервис бизнес-логики для работы с комментариями
     * @param currentUserService сервис для получения данных текущего авторизованного пользователя
     */
    public CommentController(CommentService commentService, CurrentUserService currentUserService) {
        this.commentService = commentService;
        this.currentUserService = currentUserService;
    }

    /**
     * Получает все комментарии для указанного объявления.
     * <p>
     * Публичный эндпоинт (не требует авторизации). Возвращает список комментариев
     * в формате CommentDto. Если объявление не найдено, выбрасывается NotFoundException.
     *
     * @param adId UUID объявления, к которому привязаны комментарии
     * @return список комментариев в формате CommentDto
     */
    @GetMapping("/ad/{adId}")
    public List<CommentDto> getCommentsForAd(@PathVariable UUID adId) {
        return commentService.getCommentsForAd(adId);
    }

    /**
     * Создаёт новый комментарий к объявлению.
     * <p>
     * Требует авторизации. Автор комментария определяется автоматически из контекста
     * текущего пользователя. Возвращает созданный CommentDto с HTTP-кодом 201.
     *
     * @param adId  UUID объявления, к которому добавляется комментарий
     * @param dto   DTO с текстом комментария (содержит валидацию длины и непустоты)
     * @return ResponseEntity с созданным CommentDto и статусом 201
     */
    @PostMapping("/ad/{adId}")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable UUID adId,
            @RequestBody CommentCreateDto dto
    ) {
        UUID authorId = currentUserService.getCurrentUserId();
        CommentDto result = commentService.createComment(adId, authorId, dto.getContent());
        return ResponseEntity.status(201).body(result);
    }

    /**
     * Обновляет существующий комментарий.
     * <p>
     * Требует авторизации и проверки прав: пользователь может обновлять только
     * свои комментарии. Возвращает обновлённый CommentDto или 403 при отсутствии прав.
     *
     * @param id    UUID комментария, который нужно обновить
     * @param dto   DTO с новым текстом комментария
     * @return ResponseEntity с обновлённым CommentDto
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable UUID id,
            @RequestBody CommentCreateDto dto
    ) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        CommentDto result = commentService.updateComment(id, currentUserId, dto.getContent());
        return ResponseEntity.ok(result);
    }

    /**
     * Удаляет комментарий.
     * <p>
     * Требует авторизации и проверки прав: пользователь может удалять только
     * свои комментарии. Возвращает 204 при успехе, 404 если комментарий не найден,
     * 403 если у пользователя нет прав на удаление.
     *
     * @param id UUID комментария для удаления
     * @return ResponseEntity без тела с соответствующим HTTP-статусом
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id
    ) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        try {
            commentService.deleteComment(id, currentUserId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();  // 404
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build(); // 403
        }
    }
}
