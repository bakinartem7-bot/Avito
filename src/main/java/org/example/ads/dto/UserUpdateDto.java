package org.example.ads.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserUpdateDto {
    @Schema(description = "Отображаемое имя пользователя", example = "Иван Иванов")
    private String displayName;
}
