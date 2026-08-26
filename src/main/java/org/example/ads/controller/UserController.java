package org.example.ads.controller;

import lombok.RequiredArgsConstructor;
import org.example.ads.dto.ChangePasswordDto;
import org.example.ads.dto.UserDto;
import org.example.ads.dto.UserProfileUpdateDto;
import org.example.ads.security.AppPrincipal;
import org.example.ads.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Получить свой профиль (автоматически подставляется текущий пользователь).
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(@AuthenticationPrincipal AppPrincipal principal) {
        return ResponseEntity.ok(userService.getUserById(principal.getUserId()));
    }

    /**
     * Обновить свой профиль.
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMyProfile(
            @AuthenticationPrincipal AppPrincipal principal,
            @RequestBody UserProfileUpdateDto dto
    ) {
        return ResponseEntity.ok(userService.updateProfile(principal.getUserId(), dto));
    }

    /**
     * Сменить свой пароль.
     * POST /api/users/me/password
     */
    @PostMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            @AuthenticationPrincipal AppPrincipal principal,
            @RequestBody ChangePasswordDto dto
    ) {
        userService.changePassword(principal.getUserId(), dto);
        return ResponseEntity.noContent().build();
    }
}
