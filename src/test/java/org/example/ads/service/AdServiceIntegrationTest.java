package org.example.ads.service;

import org.example.ads.dto.AdCreateDto;
import org.example.ads.entity.User;
import org.example.ads.repository.UserRepository;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdServiceIntegrationTest {

    @Autowired
    private AdService adService;

    @Autowired
    private UserRepository userRepository;

    private UUID testUserId;
    private UUID otherUserId;

    @BeforeEach
    void setUp() {
        var user = new User();
        user.setEmail("test-author@example.com");
        user.setPasswordHash("hashed-pass");
        testUserId = userRepository.save(user).getId();

        var otherUser = new User();
        otherUser.setEmail("other-author@example.com");
        otherUser.setPasswordHash("hashed-pass-2");
        otherUserId = userRepository.save(otherUser).getId();
    }

    @Test
    void createAd_success() {
        AdCreateDto dto = new AdCreateDto();
        dto.setTitle("Велосипед");
        dto.setDescription("В отличном состоянии");
        dto.setPrice(15000.0);

        var result = adService.createAd(testUserId, dto);

        assertNotNull(result);
        assertEquals("Велосипед", result.getTitle());
        assertEquals(testUserId, result.getAuthorId());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void getAllAds_returns_list() {
        AdCreateDto dto1 = new AdCreateDto();
        dto1.setTitle("Товар 1");
        dto1.setPrice(100.0);
        adService.createAd(testUserId, dto1);

        AdCreateDto dto2 = new AdCreateDto();
        dto2.setTitle("Товар 2");
        dto2.setPrice(200.0);
        adService.createAd(otherUserId, dto2);

        var list = adService.getAllAds();

        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(a -> "Товар 1".equals(a.getTitle())));
        assertTrue(list.stream().anyMatch(a -> "Товар 2".equals(a.getTitle())));
    }

    @Test
    void getAdById_success() {
        AdCreateDto dto = new AdCreateDto();
        dto.setTitle("Смартфон");
        dto.setPrice(30000.0);
        var created = adService.createAd(testUserId, dto);

        var found = adService.getAdById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Смартфон", found.getTitle());
    }

    @Test
    void getAdById_notFound_throws() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(NotFoundException.class, () -> {
            adService.getAdById(nonExistentId);
        });
    }

    @Test
    void deleteAd_own_success() {
        AdCreateDto dto = new AdCreateDto();
        dto.setTitle("Удаляемое объявление");
        dto.setPrice(50.0);
        var ad = adService.createAd(testUserId, dto);

        adService.deleteAd(ad.getId(), testUserId);

        assertThrows(NotFoundException.class, () -> {
            adService.getAdById(ad.getId());
        });
    }

    @Test
    void deleteAd_other_user_throws() {
        AdCreateDto dto = new AdCreateDto();
        dto.setTitle("Чужое объявление");
        dto.setPrice(70.0);
        var ad = adService.createAd(otherUserId, dto);

        assertThrows(AccessDeniedException.class, () -> {
            adService.deleteAd(ad.getId(), testUserId); // пытаемся удалить чужое
        });
    }
}
