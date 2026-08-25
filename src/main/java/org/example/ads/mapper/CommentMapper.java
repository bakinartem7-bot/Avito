package org.example.ads.mapper;

import org.example.ads.dto.CommentDto;
import org.example.ads.entity.Comment;
import java.util.function.Function;

public class CommentMapper {
    public static final Function<Comment, CommentDto> TO_DTO = c -> {
        if (c == null || c.getAuthor() == null) {
            throw new IllegalArgumentException("Comment or its author is null");
        }
        if (c.getAd() == null) {
            throw new IllegalArgumentException("Comment must have an associated Ad");
        }

        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setAdId(c.getAd().getId());
        dto.setUserId(c.getAuthor().getId());
        dto.setContent(c.getContent());
        dto.setCreatedAt(c.getCreatedAt());

        return dto;
    };
}
