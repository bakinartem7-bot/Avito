package org.example.ads.controller;

import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.dto.AdUpdateDto;
import org.example.ads.entity.User;
import org.example.ads.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для AdController.
 * Проверяют создание, чтение, обновление и удаление объявлений, а также контроль доступа.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;

    private UUID ownerId;
    private String ownerEmail;
    private UUID otherId;
    private String otherEmail;

    /**
     * Подготовка тестовых пользователей перед каждым тестом.
     * Пароли хешируются через PasswordEncoder для соответствия реальной логике приложения.
     */
    @BeforeEach
    void setUp() {
        ownerEmail = "owner-" + UUID.randomUUID() + "@example.com";
        var owner = new User();
        owner.setEmail(ownerEmail);
        owner.setUsername(ownerEmail.split("@")[0]);
        owner.setPasswordHash(passwordEncoder.encode("123456"));
        owner.setRole(org.example.ads.entity.Role.USER);
        ownerId = userRepository.save(owner).getId();

        otherEmail = "other-" + UUID.randomUUID() + "@example.com";
        var other = new User();
        other.setEmail(otherEmail);
        other.setUsername(otherEmail.split("@")[0]);
        other.setPasswordHash(passwordEncoder.encode("123456"));
        other.setRole(org.example.ads.entity.Role.USER);
        otherId = userRepository.save(other).getId();
    }

    @Test
    void createAd_success() throws Exception {
        AdCreateDto dto = new AdCreateDto();
        dto.setTitle("Велосипед");
        dto.setDescription("В отличном состоянии");
        dto.setPrice(new BigDecimal("15000.00"));

        MvcResult result = mockMvc.perform(post("/api/ads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(ownerEmail))
                        .header("X-User-Id", ownerId.toString())
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        assertThat(body).isNotEmpty();

        AdDto responseDto = objectMapper.readValue(body, AdDto.class);
        assertThat(responseDto.getTitle()).isEqualTo("Велосипед");
        assertThat(responseDto.getAuthorId()).isEqualTo(ownerId);
    }

    @Test
    void getAllAds_returns_list() throws Exception {
        createAd("Товар 1", new BigDecimal("100.00"), ownerId);
        createAd("Товар 2", new BigDecimal("200.50"), otherId);

        mockMvc.perform(get("/api/ads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(ownerEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").isArray());
    }

    @Test
    void getAdById_success() throws Exception {
        AdDto adDto = createAd("Смартфон", new BigDecimal("30000.99"), ownerId);
        UUID adId = adDto.getId();

        MvcResult result = mockMvc.perform(get("/api/ads/" + adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(ownerEmail)))
                .andExpect(status().isOk())
                .andReturn();

        String body = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        AdDto responseDto = objectMapper.readValue(body, AdDto.class);

        assertThat(responseDto.getTitle()).isEqualTo("Смартфон");
        assertThat(responseDto.getAuthorId()).isEqualTo(ownerId);
    }

    @Test
    void getAdById_notFound_returns_404() throws Exception {
        UUID nonExistent = UUID.randomUUID();

        mockMvc.perform(get("/api/ads/" + nonExistent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(ownerEmail)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAd_own_success() throws Exception {
        AdDto adDto = createAd("Старый заголовок", new BigDecimal("100.00"), ownerId);
        UUID adId = adDto.getId();

        AdUpdateDto dto = new AdUpdateDto();
        dto.setTitle("Новый заголовок");
        dto.setDescription("Описание обновлено");
        dto.setPrice(new BigDecimal("200.00"));

        MvcResult result = mockMvc.perform(put("/api/ads/" + adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId.toString())
                        .with(user(ownerEmail))
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        String body = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        AdDto responseDto = objectMapper.readValue(body, AdDto.class);
        assertThat(responseDto.getTitle()).isEqualTo("Новый заголовок");
    }

    @Test
    void updateAd_other_user_returns_403() throws Exception {
        AdDto adDto = createAd("Чужое объявление", new BigDecimal("50.00"), otherId);
        UUID adId = adDto.getId();

        AdUpdateDto dto = new AdUpdateDto();
        dto.setTitle("Попытка изменить чужое");
        dto.setDescription("Валидное описание для теста");

        mockMvc.perform(put("/api/ads/" + adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId.toString())
                        .with(user(ownerEmail))
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAd_own_success() throws Exception {
        AdDto adDto = createAd("Удаляемое", new BigDecimal("99.00"), ownerId);
        UUID adId = adDto.getId();

        mockMvc.perform(delete("/api/ads/" + adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId.toString())
                        .with(user(ownerEmail)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/ads/" + adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(ownerEmail)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAd_other_user_returns_403() throws Exception {
        AdDto adDto = createAd("Чужое для удаления", new BigDecimal("70.00"), otherId);
        UUID adId = adDto.getId();

        mockMvc.perform(delete("/api/ads/" + adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId.toString())
                        .with(user(ownerEmail)))
                .andExpect(status().isForbidden());
    }

    /**
     * Вспомогательный метод для создания объявления в тестах.
     *
     * @param title  Заголовок объявления
     * @param price  Цена
     * @param authorId ID автора объявления
     * @return DTO созданного объявления
     */
    private AdDto createAd(String title, BigDecimal price, UUID authorId) {
        try {
            AdCreateDto dto = new AdCreateDto();
            dto.setTitle(title);
            dto.setDescription(title + " description");
            dto.setPrice(price);

            String emailToUse = (authorId.equals(ownerId)) ? ownerEmail : otherEmail;
            String content = objectMapper.writeValueAsString(dto);

            MvcResult result = mockMvc.perform(post("/api/ads")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-User-Id", authorId.toString())
                            .with(user(emailToUse))
                            .content(content))
                    .andExpect(status().isCreated())
                    .andReturn();

            String body = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
            return objectMapper.readValue(body, AdDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ad in test", e);
        }
    }
}
