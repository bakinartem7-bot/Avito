package org.example.ads.controller;

import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.service.AdService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    @PostMapping
    public AdDto createAd(@RequestHeader("X-User-Id") UUID userId, @RequestBody AdCreateDto dto) {
        return adService.createAd(userId, dto);
    }

    @GetMapping
    public List<AdDto> getAllAds() {
        return adService.getAllAds();
    }

    @GetMapping("/{id}")
    public AdDto getAd(@PathVariable UUID id) {
        return adService.getAdById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteAd(@PathVariable UUID id, @RequestHeader("X-User-Id") UUID userId) {
        adService.deleteAd(id, userId);
    }

    @PostMapping
    public AdDto createAd(@RequestBody AdCreateDto dto) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = (UUID) auth.getPrincipal();
        return adService.createAd(currentUserId, dto);
    }

}
