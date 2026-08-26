package org.example.ads.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class CommentDto {
    @Schema(description = "ID комментария", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID id;

    @Schema(description = "ID объявления", example = "a1b2c3d4-e5f6-7890-1234-56789abcdef0")
    private UUID adId;

    @Schema(description = "ID пользователя", example = "09876543-210f-edcba-9876-543210fedcba")
    private UUID userId;

    @Schema(description = "Текст комментария", example = "Отличное объявление, спасибо!")
    private String content; // тоже content

    @Schema(description = "Дата создания", example = "2026-08-22T12:34:56Z")
    private Instant createdAt;

    @Schema(description = "Дата последнего обновления", example = "2026-08-22T12:40:11Z")
    private Instant updatedAt;
}
