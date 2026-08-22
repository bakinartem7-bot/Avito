package org.example.ads.mapper;

import org.example.ads.dto.CommentDto;
import org.example.ads.entity.Comment;
import java.util.function.Function;

public class CommentMapper {
    public static Function<Comment, CommentDto> toDto = c -> {
        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setAdId(c.getAd().getId());
        dto.setUserId(c.getUser().getId());
        dto.setContent(c.getContent());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    };
}
