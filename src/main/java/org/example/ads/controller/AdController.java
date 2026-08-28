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

@RestController
@RequestMapping("/api/ads")
@Validated
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;
    private final CurrentUserService currentUserService;

    /**
     * Создать объявление.
     * Требует авторизации (HTTP Basic). Автор определяется из контекста.
     */
    @PostMapping
    public ResponseEntity<AdDto> createAd(@RequestBody @Valid AdCreateDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return ResponseEntity.status(201).body(adService.createAd(currentUserId, dto));
    }

    /**
     * Получить все объявления (публичный GET).
     */
    @GetMapping
    public List<AdDto> getAllAds() {
        return adService.getAllAds();
    }

    /**
     * Получить свои объявления.
     * Требует авторизации.
     */
    @GetMapping("/mine")
    public ResponseEntity<List<AdDto>> getMyAds() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(adService.findAllByAuthorId(currentUserId));
    }

    /**
     * Получить одно объявление по ID.
     * Публичный GET.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdDto> getAd(@PathVariable UUID id) {
        Optional<AdDto> result = adService.findAdById(id);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Обновить объявление.
     * Требует авторизации и проверки прав (только своё объявление).
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdDto> updateAd(@PathVariable UUID id, @RequestBody @Valid AdUpdateDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Optional<AdDto> result = adService.updateAd(id, currentUserId, dto);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Удалить объявление.
     * Требует авторизации и проверки прав.
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
