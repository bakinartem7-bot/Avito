package org.example.ads.mapper;

import org.example.ads.dto.CommentDto;
import org.example.ads.entity.Comment;
import java.util.function.Function;

/**
 * Маппер для конвертации сущности Comment в DTO.
 */
public class CommentMapper {

    /**
     * Функция для преобразования Comment в CommentDto.
     * Выполняет проверку на null и гарантирует, что у комментария есть автор и объявление.
     */
    public static final Function<Comment, CommentDto> TO_DTO = c -> {
        if (c == null || c.getAuthor() == null) {
            throw new IllegalArgumentException("Comment or its author is null");
        }
        // Дополнительно можно проверить ad, если хочешь быть максимально строгим
        if (c.getAd() == null) {
            throw new IllegalArgumentException("Comment must have an associated Ad");
        }

        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setAdId(c.getAd().getId());
        dto.setUserId(c.getAuthor().getId());
        dto.setContent(c.getText());
        dto.setCreatedAt(c.getCreatedAt());

        // Если в Comment нет updatedAt, DTO получит null — это нормально
        return dto;
    };
}
