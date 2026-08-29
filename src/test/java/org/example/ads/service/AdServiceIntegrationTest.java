package org.example.ads.service;

import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.entity.User;
import org.example.ads.repository.UserRepository;
import org.example.ads.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdServiceIntegrationTest {

    @Autowired
    private AdService adService;

    @Autowired
    private UserRepository userRepository;

    // Если у тебя есть AdRepository, лучше внедрить его сюда для очистки.
    // Но если нет, можно чистить через сервис или оставить как есть,
    // главное - добавить очистку в тест.
    // Предположим, что AdRepository есть, так как это интеграционный тест.
    @Autowired
    private org.example.ads.repository.AdRepository adRepository;

    private UUID testUserId;
    private UUID otherUserId;

    @BeforeEach
    void setUp() {
        testUserId = createUser("test-author-" + UUID.randomUUID() + "@example.com");
        otherUserId = createUser("other-author-" + UUID.randomUUID() + "@example.com");
    }

    private UUID createUser(String email) {
        var user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-pass");
        return userRepository.save(user).getId();
    }

    @Test
    void getAllAds_returns_list() {
        // ✅ ГЛАВНОЕ ИСПРАВЛЕНИЕ: Явно очищаем базу перед тестом.
        // Это гарантирует, что мы начинаем с чистого листа, даже если DirtiesContext не сработал идеально.
        adRepository.deleteAll();

        // Создаём ровно 2 объявления для теста
        AdCreateDto dto1 = new AdCreateDto();
        dto1.setTitle("Товар 1");
        dto1.setDescription("Desc 1");
        dto1.setPrice(new BigDecimal("100.00"));
        adService.createAd(testUserId, dto1);

        AdCreateDto dto2 = new AdCreateDto();
        dto2.setTitle("Товар 2");
        dto2.setDescription("Desc 2");
        dto2.setPrice(new BigDecimal("200.50"));
        adService.createAd(otherUserId, dto2);

        List<AdDto> list = adService.getAllAds();

        // Теперь здесь точно будет 2
        assertThat(list).hasSize(2);
        assertThat(list)
                .extracting(AdDto::getTitle)
                .containsExactlyInAnyOrder("Товар 1", "Товар 2");

        assertThat(list)
                .extracting(AdDto::getAuthorId)
                .containsExactlyInAnyOrder(testUserId, otherUserId);
    }

    @Test
    void createAd_success() {
        AdCreateDto dto = new AdCreateDto();
        dto.setTitle("Велосипед");
        dto.setDescription("В отличном состоянии");
        dto.setPrice(new BigDecimal("15000.00"));

        AdDto result = adService.createAd(testUserId, dto);

        assertNotNull(result);
        assertEquals("Велосипед", result.getTitle());
        assertEquals(new BigDecimal("15000.00"), result.getPrice());
        assertEquals(testUserId, result.getAuthorId());
    }

    @Test
    void getAdById_success() {
        AdCreateDto dto = new AdCreateDto();
        dto.setTitle("Смартфон");
        dto.setDescription("Новый");
        dto.setPrice(new BigDecimal("30000.99"));

        AdDto created = adService.createAd(testUserId, dto);

        Optional<AdDto> foundOptional = adService.findAdById(created.getId());
        assertTrue(foundOptional.isPresent());
        AdDto found = foundOptional.get();

        assertEquals(created.getId(), found.getId());
        assertEquals("Смартфон", found.getTitle());
        assertEquals(testUserId, found.getAuthorId());
    }

    @Test
    void deleteAd_own_success() {
        AdCreateDto dto = new AdCreateDto();
        dto.setTitle("Удаляемое объявление");
        dto.setDescription("Тест");
        dto.setPrice(new BigDecimal("50.00"));
        AdDto ad = adService.createAd(testUserId, dto);

        adService.deleteAd(ad.getId(), testUserId);

        Optional<AdDto> remaining = adService.findAdById(ad.getId());
        assertFalse(remaining.isPresent());
    }

    @Test
    void deleteAd_other_user_throws() {
        AdCreateDto dto = new AdCreateDto();
        dto.setTitle("Чужое объявление");
        dto.setDescription("Тест");
        dto.setPrice(new BigDecimal("70.00"));
        AdDto ad = adService.createAd(otherUserId, dto);

        assertThrows(AccessDeniedException.class, () -> {
            adService.deleteAd(ad.getId(), testUserId);
        });
    }
}
