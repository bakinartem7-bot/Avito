package org.example.ads.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdUpdateDto {
    @NotBlank(message = "Заголовок обязателен")
    @Schema(description = "Заголовок объявления", example = "Продам велосипед")
    private String title;

    @NotBlank(message = "Описание обязательно")
    @Schema(description = "Описание", example = "Велосипед в отличном состоянии, почти не использовался.")
    private String description;

    @Positive(message = "Цена должна быть положительной")
    @Schema(description = "Цена", example = "15000.00")
    private BigDecimal price;
}
