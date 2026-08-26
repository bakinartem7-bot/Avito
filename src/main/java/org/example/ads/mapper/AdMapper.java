package org.example.ads.mapper;

import org.example.ads.dto.AdDto;
import org.example.ads.entity.Ad;
import java.math.BigDecimal;

public class AdMapper {

    public static AdDto toDto(Ad ad) {
        AdDto dto = new AdDto();
        dto.setId(ad.getId());
        dto.setAuthorId(ad.getAuthor().getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setPrice(ad.getPrice());          // BigDecimal → BigDecimal
        dto.setImageUrl(ad.getImageUrl());    // Добавлено
        dto.setCreatedAt(ad.getCreatedAt());
        dto.setUpdatedAt(ad.getUpdatedAt());
        return dto;
    }

    public static void updateFromDto(Ad ad, AdDto dto) {
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        if (dto.getPrice() != null) {
            ad.setPrice(dto.getPrice());
        }
    }
}
