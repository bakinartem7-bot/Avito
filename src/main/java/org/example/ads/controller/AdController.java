package org.example.ads.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.dto.AdUpdateDto;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.security.AppPrincipal;
import org.example.ads.service.AdService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /**
     * Создать объявление (автором становится текущий пользователь).
     */
    @PostMapping
    public ResponseEntity<AdDto> createAd(
            @AuthenticationPrincipal AppPrincipal principal,
            @RequestBody @Valid AdCreateDto dto
    ) {
        UUID currentUserId = principal.getUserId();
        return ResponseEntity.ok(adService.createAd(currentUserId, dto));
    }

    /**
     * Получить ВСЕ объявления (для админки или общего списка).
     */
    @GetMapping
    public List<AdDto> getAllAds() {
        return adService.getAllAds();
    }

    /**
     * Получить СВОИ объявления (для профиля пользователя).
     * Это реализует требование: «в профиле отображаются только его объявления».
     */
    @GetMapping("/mine")
    public ResponseEntity<List<AdDto>> getMyAds(@AuthenticationPrincipal AppPrincipal principal) {
        UUID currentUserId = principal.getUserId();
        List<AdDto> myAds = adService.findAllByAuthorId(currentUserId);
        return ResponseEntity.ok(myAds);
    }

    /**
     * Получить одно объявление по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdDto> getAd(@PathVariable UUID id) {
        Optional<AdDto> result = adService.findAdById(id);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Обновить объявление (только если автор совпадает).
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdDto> updateAd(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppPrincipal principal,
            @RequestBody @Valid AdUpdateDto dto
    ) {
        UUID currentUserId = principal.getUserId();
        Optional<AdDto> result = adService.updateAd(id, currentUserId, dto);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Удалить объявление (только если автор совпадает).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        UUID currentUserId = principal.getUserId();
        try {
            adService.deleteAd(id, currentUserId);
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
