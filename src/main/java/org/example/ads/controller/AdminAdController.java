package org.example.ads.controller;

import lombok.RequiredArgsConstructor;
import org.example.ads.service.AdminAdService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/ads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // <-- гарантия, что сюда попадёт только админ
public class AdminAdController {

    private final AdminAdService adminAdService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable UUID id) {
        adminAdService.deleteAnyAd(id); // безопасно: проверка роли уже выполнена
        return ResponseEntity.noContent().build();
    }
}
