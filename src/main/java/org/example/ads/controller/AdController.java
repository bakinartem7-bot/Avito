package org.example.ads.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.dto.AdUpdateDto;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.service.AdService;
import org.example.ads.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST-контроллер для управления объявлениями.
 * <p>
 * Предоставляет API для создания, чтения, обновления и удаления объявлений.
 * Все операции, кроме публичных GET, требуют авторизации.
 * Права на изменение/удаление проверяются по совпадению authorId.
 */
@RestController
@RequestMapping("/api/ads")
@Validated
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;
    private final CurrentUserService currentUserService;

    /**
     * Создаёт новое объявление.
     * <p>
     * Требует авторизации. Автор объявления определяется из контекста текущего пользователя.
     * Возвращает созданный объект AdDto с HTTP-кодом 201.
     *
     * @param dto DTO с данными для создания объявления
     * @return ResponseEntity с созданным AdDto
     */
    @PostMapping
    public ResponseEntity<AdDto> createAd(@RequestBody @Valid AdCreateDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return ResponseEntity.status(201).body(adService.createAd(currentUserId, dto));
    }

    /**
     * Получает список всех объявлений.
     * <p>
     * Публичный эндпоинт (не требует авторизации).
     * Возвращает список AdDto.
     *
     * @return список объявлений в формате AdDto
     */
    @GetMapping
    public List<AdDto> getAllAds() {
        return adService.getAllAds();
    }

    /**
     * Получает объявления текущего пользователя.
     * <p>
     * Требует авторизации. Возвращает список объявлений, принадлежащих текущему пользователю.
     *
     * @return ResponseEntity со списком AdDto
     */
    @GetMapping("/mine")
    public ResponseEntity<List<AdDto>> getMyAds() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(adService.findAllByAuthorId(currentUserId));
    }

    /**
     * Получает одно объявление по идентификатору.
     * <p>
     * Публичный эндпоинт. Возвращает AdDto, если найдено, или 404, если нет.
     *
     * @param id UUID объявления
     * @return ResponseEntity с AdDto или статусом 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdDto> getAd(@PathVariable UUID id) {
        Optional<AdDto> result = adService.findAdById(id);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Обновляет существующее объявление.
     * <p>
     * Требует авторизации и проверки прав: пользователь может обновлять только свои объявления.
     * Возвращает обновлённый AdDto или 404/403 при ошибках.
     *
     * @param id  UUID объявления
     * @param dto DTO с новыми данными
     * @return ResponseEntity с обновлённым AdDto или соответствующим статусом ошибки
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdDto> updateAd(@PathVariable UUID id, @RequestBody @Valid AdUpdateDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Optional<AdDto> result = adService.updateAd(id, currentUserId, dto);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Удаляет объявление.
     * <p>
     * Требует авторизации и проверки прав. Возвращает 204 при успехе, 404 если не найдено, 403 если нет прав.
     *
     * @param id UUID объявления
     * @return ResponseEntity без тела с соответствующим HTTP-статусом
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable UUID id) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        try {
            adService.deleteAd(id, currentUserId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();  // 404
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build(); // 403
        }
    }
}
