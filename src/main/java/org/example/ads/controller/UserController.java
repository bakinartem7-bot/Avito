package org.example.ads.controller;

import org.example.ads.dto.ChangePasswordDto;
import org.example.ads.dto.UserDto;
import org.example.ads.dto.UserProfileUpdateDto;
import org.example.ads.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users/{id}
     * Возвращает пользователя в виде DTO.
     * ИСПРАВЛЕНО: Обработка Optional и создание UserDto (убирает ошибку incompatible types)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        var userOpt = userService.getUserById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserDto dto = new UserDto(userOpt.get());
        return ResponseEntity.ok(dto);
    }

    /**
     * PUT /api/users/{id}/profile
     * Обновляет профиль (город, телефон, имя).
     * ИСПРАВЛЕНО: Вызывает метод updateProfile, который мы добавили в сервис.
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<UserDto> updateProfile(@PathVariable UUID id,
                                                 @RequestBody UserProfileUpdateDto dto) {
        var updatedUser = userService.updateProfile(id, dto);
        return ResponseEntity.ok(new UserDto(updatedUser));
    }

    /**
     * PUT /api/users/{id}/password
     * Смена пароля.
     * ИСПРАВЛЕНО: Вызывает метод changePassword, который проверяет старый пароль и хеширует новый.
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id,
                                               @RequestBody ChangePasswordDto dto) {
        userService.changePassword(id, dto);
        return ResponseEntity.ok().build();
    }
}
