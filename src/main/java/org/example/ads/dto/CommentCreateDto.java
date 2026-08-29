package org.example.ads.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания комментария к объявлению.
 * Содержит валидацию входных данных и метаданные для Swagger UI.
 */
@Schema(description = "DTO для создания комментария к объявлению")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CommentCreateDto {

    @Schema(
            description = "Текст комментария",
            example = "Отличный товар! Быстро доставили.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 1, max = 1000, message = "Длина комментария должна быть от 1 до 1000 символов")
    private String content;
}
