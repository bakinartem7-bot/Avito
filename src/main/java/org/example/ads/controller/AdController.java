package org.example.ads.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.dto.AdUpdateDto;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.service.AdService;
import org.example.ads.service.CurrentUserService; // <-- Новый сервис
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

    @PostMapping
    public ResponseEntity<AdDto> createAd(@RequestBody @Valid AdCreateDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(adService.createAd(currentUserId, dto));
    }

    @GetMapping
    public List<AdDto> getAllAds() {
        return adService.getAllAds();
    }

    @GetMapping("/mine")
    public ResponseEntity<List<AdDto>> getMyAds() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(adService.findAllByAuthorId(currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdDto> getAd(@PathVariable UUID id) {
        Optional<AdDto> result = adService.findAdById(id);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdDto> updateAd(@PathVariable UUID id, @RequestBody @Valid AdUpdateDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Optional<AdDto> result = adService.updateAd(id, currentUserId, dto);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable UUID id) {
        UUID currentUserId = currentUserService.getCurrentUserId();
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
