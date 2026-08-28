package org.example.ads.mapper;

import org.example.ads.dto.AdDto;
import org.example.ads.entity.Ad;

import java.math.BigDecimal;

public class AdMapper {

    public static AdDto toDto(Ad ad) {
        if (ad == null) {
            return null;
        }

        AdDto dto = new AdDto();
        dto.setId(ad.getId());

        // ИСПРАВЛЕНИЕ: берём ID из связи author, а не из authorId.
        // authorId заполняется только при чтении из БД, а сразу после save() он null.
        dto.setAuthorId(ad.getAuthor() != null ? ad.getAuthor().getId() : null);

        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setPrice(ad.getPrice() != null ? ad.getPrice() : BigDecimal.ZERO);
        dto.setImageUrl(ad.getImageUrl());
        dto.setCreatedAt(ad.getCreatedAt());
        dto.setUpdatedAt(ad.getUpdatedAt());
        dto.setPublishedAt(ad.getPublishedAt());
        dto.setActive(ad.isActive());

        return dto;
    }

    public static void updateFromDto(Ad ad, AdDto dto) {
        if (ad == null || dto == null) {
            return;
        }

        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());

        if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
            ad.setPrice(dto.getPrice());
        }
    }
}
