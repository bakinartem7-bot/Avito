package org.example.ads.controller;

import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.service.AdService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с объявлениями.
 * Предоставляет REST-методы для создания, получения и удаления объявлений.
 */

@RestController
@RequestMapping("/api/ads")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    /**
     * Создаёт новое объявление от имени авторизованного пользователя.
     * UserId берётся из контекста безопасности (JWT).
     */

    @PostMapping
    public AdDto createAd(@RequestBody AdCreateDto dto) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = (UUID) auth.getPrincipal();
        return adService.createAd(currentUserId, dto);
    }

    /**
     * Возвращает список всех объявлений.
     */
    @GetMapping
    public List<AdDto> getAllAds() {
        return adService.getAllAds();
    }

    /**
     * Получает объявление по ID.
     */
    @GetMapping("/{id}")
    public AdDto getAd(@PathVariable UUID id) {
        return adService.getAdById(id);
    }

    /**
     * Удаляет объявление. Разрешено только владельцу объявления.
     */
    @DeleteMapping("/{id}")
    public void deleteAd(@PathVariable UUID id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = (UUID) auth.getPrincipal();
        adService.deleteAd(id, currentUserId);
    }
}
