package org.example.ads.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class AdCreateDto {
    @NotBlank(message = "Заголовок обязателен")
    private String title;
    @NotBlank(message = "Описание обязательно")
    private String description;
    @Positive(message = "Цена должна быть положительной")
    private BigDecimal price;
}
