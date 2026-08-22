package org.example.ads.mapper;

import org.example.ads.dto.CommentDto;
import org.example.ads.entity.Comment;
import java.util.function.Function;

public class CommentMapper {

    public static final Function<Comment, CommentDto> TO_DTO = c -> {
        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setAdId(c.getAd().getId());

        // 👇 Используем getAuthor() (поле author в Comment) и пишем в userId (поле в DTO)
        dto.setUserId(c.getAuthor().getId());

        // 👇 content в DTO <-> text в Comment
        dto.setContent(c.getText());

        dto.setCreatedAt(c.getCreatedAt());
        // Если в Comment нет updatedAt, то либо оставь null, либо не ставь вообще
        return dto;
    };
}
