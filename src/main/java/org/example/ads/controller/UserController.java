package org.example.ads.controller;

import org.example.ads.dto.ChangePasswordDto;
import org.example.ads.dto.UserDto;
import org.example.ads.dto.UserProfileUpdateDto;
import org.example.ads.service.CurrentUserService;
import org.example.ads.service.UserService; // <-- Правильный сервис, отдельный класс
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    /**
     * GET /api/users/me
     * Возвращает текущего авторизованного пользователя.
     * Это безопаснее, чем GET /api/users/{id}, который открывает ID всех пользователей.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        var userOpt = userService.getUserById(currentUserId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new UserDto(userOpt.get()));
    }

    /**
     * PUT /api/users/me/profile
     * Обновляет профиль текущего пользователя (город, телефон, имя).
     * Защита: нельзя передать чужой ID.
     */
    @PutMapping("/me/profile")
    public ResponseEntity<UserDto> updateMyProfile(@RequestBody UserProfileUpdateDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        var updatedUser = userService.updateProfile(currentUserId, dto);
        return ResponseEntity.ok(new UserDto(updatedUser));
    }

    /**
     * PUT /api/users/me/password
     * Смена пароля текущего пользователя.
     * Защита: нельзя сменить пароль другому пользователю.
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(@RequestBody ChangePasswordDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        userService.changePassword(currentUserId, dto);
        return ResponseEntity.ok().build();
    }
}
