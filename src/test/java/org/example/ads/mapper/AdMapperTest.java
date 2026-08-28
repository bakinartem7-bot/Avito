package org.example.ads.mapper;

import org.example.ads.dto.AdDto;
import org.example.ads.entity.Ad;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdMapperTest {

    @Test
    void toDto_returnsNull_whenInputIsNull() {
        AdDto result = AdMapper.toDto(null);
        assertThat(result).isNull();
    }

    @Test
    void toDto_handlesNullAuthor() {
        Ad ad = new Ad();
        UUID testId = UUID.randomUUID();
        ad.setId(testId);
        ad.setTitle("Test Title");
        ad.setDescription("Test Desc");
        ad.setPrice(new BigDecimal("100.00"));
        // Автор намеренно не устанавливается (null)

        AdDto result = AdMapper.toDto(ad);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getTitle()).isEqualTo("Test Title");
        assertThat(result.getDescription()).isEqualTo("Test Desc");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.getAuthorId()).isNull();
    }

    @Test
    void toDto_handlesNullPrice_and_usesZero() {
        Ad ad = new Ad();
        UUID testId = UUID.randomUUID();
        ad.setId(testId);
        ad.setPrice(null);

        AdDto result = AdMapper.toDto(ad);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getPrice()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void toDto_mapsAllFields_correctly() {
        Ad ad = new Ad();
        UUID testId = UUID.randomUUID();
        ad.setId(testId);
        ad.setTitle("Test Title");
        ad.setDescription("Test Desc");
        ad.setPrice(new BigDecimal("100.50"));
        ad.setActive(true);

        AdDto result = AdMapper.toDto(ad);

        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getTitle()).isEqualTo("Test Title");
        assertThat(result.getDescription()).isEqualTo("Test Desc");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("100.50"));
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void updateFromDto_doesNothing_whenAdIsNull() {
        Ad ad = null;
        AdDto dto = new AdDto();
        dto.setTitle("New Title");

        // Метод не должен выбрасывать NullPointerException
        AdMapper.updateFromDto(ad, dto);

        assertThat(true).isTrue(); // Тест пройден, если код не упал
    }

    @Test
    void updateFromDto_doesNothing_whenDtoIsNull() {
        Ad ad = new Ad();
        AdDto dto = null;

        // Метод не должен выбрасывать NullPointerException
        AdMapper.updateFromDto(ad, dto);

        assertThat(true).isTrue(); // Тест пройден, если код не упал
    }

    @Test
    void updateFromDto_updatesFields_correctly() {
        Ad ad = new Ad();
        AdDto dto = new AdDto();
        dto.setTitle("Updated Title");
        dto.setDescription("Updated Desc");
        dto.setPrice(new BigDecimal("500.00"));

        AdMapper.updateFromDto(ad, dto);

        assertThat(ad.getTitle()).isEqualTo("Updated Title");
        assertThat(ad.getDescription()).isEqualTo("Updated Desc");
        assertThat(ad.getPrice()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void updateFromDto_ignoresNegativePrice() {
        Ad ad = new Ad();
        AdDto dto = new AdDto();
        dto.setPrice(new BigDecimal("-100.00"));

        AdMapper.updateFromDto(ad, dto);

        // Ожидаем, что цена не установится в отрицательное значение, а останется null
        assertThat(ad.getPrice()).isNull();
    }
}
