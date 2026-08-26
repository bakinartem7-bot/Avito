package org.example.ads.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.dto.AdUpdateDto;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.service.AdService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/ads")
// Эта аннотация говорит Swagger: для доступа к этим эндпоинтам нужен bearer-токен
@SecurityRequirement(name = "bearerAuth")
@Validated
public class AdminAdController {

    private final AdService adService;

    public AdminAdController(AdService adService) {
        this.adService = adService;
    }

    /**
     * Удаляет любое объявление (только для ADMIN).
     * В Swagger это будет видно как защищённый эндпоинт.
     * Проверка роли делается на уровне SecurityConfig / сервиса.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnyAd(@PathVariable UUID id) {
        adService.deleteAnyAd(id); // метод только для админа
        return ResponseEntity.noContent().build();
    }

    // Сюда можно добавить другие админские операции, например:
    // - модерацию объявлений
    // - принудительное обновление статуса
}
