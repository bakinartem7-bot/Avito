package org.example.ads.controller;

import lombok.RequiredArgsConstructor;
import org.example.ads.service.AdminAdService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST-контроллер для административных операций над объявлениями.
 * <p>
 * Содержит операции, доступные только пользователям с ролью ADMIN.
 * Проверка прав осуществляется через аннотацию @PreAuthorize.
 */
@RestController
@RequestMapping("/api/admin/ads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // <-- гарантия, что сюда попадёт только админ
public class AdminAdController {

    private final AdminAdService adminAdService;

    /**
     * Удаляет любое объявление по идентификатору.
     * <p>
     * Доступно только администраторам. Проверка роли выполнена на уровне контроллера.
     * Не проверяет принадлежность объявления конкретному пользователю.
     *
     * @param id UUID объявления
     * @return ResponseEntity со статусом 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable UUID id) {
        adminAdService.deleteAnyAd(id); // безопасно: проверка роли уже выполнена
        return ResponseEntity.noContent().build();
    }
}
