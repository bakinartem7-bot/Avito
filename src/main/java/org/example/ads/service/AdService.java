package org.example.ads.service;

import org.example.ads.dto.*;
import org.example.ads.entity.Ad;
import org.example.ads.entity.User;
import org.example.ads.repository.AdRepository;
import org.example.ads.mapper.AdMapper;
import org.example.ads.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

@Service
@Transactional
public class AdService {

    private final AdRepository adRepository;

    public AdService(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    @Transactional(readOnly = true)
    public List<AdDto> getAllAds() {
        // Важно: здесь мы загружаем только сущности Ad.
        // Если понадобится author.id — лучше добавить метод с JOIN FETCH в репозиторий.
        List<Ad> ads = adRepository.findAll();
        return ads.stream()
                .map(AdMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AdDto> findAdById(UUID id) {
        Optional<Ad> adOpt = adRepository.findById(id);
        return adOpt.map(AdMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AdDto> findAllByAuthorId(UUID authorId) {
        List<Ad> ads = adRepository.findAllByAuthorIdOrderByCreatedAtDesc(authorId);
        return ads.stream()
                .map(AdMapper::toDto)
                .toList();
    }

    @Transactional
    public AdDto createAd(UUID authorId, AdCreateDto dto) {
        User author = new User();
        author.setId(authorId);

        Ad ad = new Ad();
        ad.setAuthor(author);
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        ad.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        ad.setActive(true);

        // Поля createdAt/updatedAt/publishedAt заполнятся в @PrePersist
        Ad saved = adRepository.save(ad);
        return AdMapper.toDto(saved);
    }

    @Transactional
    public Optional<AdDto> updateAd(UUID id, UUID currentUserId, AdUpdateDto dto) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found with id: " + id));

        // Проверка владельца
        if (!ad.getAuthor().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only update your own ads");
        }

        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
            ad.setPrice(dto.getPrice());
        }
        ad.setUpdatedAt(Instant.now());

        Ad updated = adRepository.save(ad);
        return Optional.of(AdMapper.toDto(updated));
    }

    @Transactional
    public void deleteAd(UUID id, UUID currentUserId) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found with id: " + id));

        if (!ad.getAuthor().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only delete your own ads");
        }

        adRepository.delete(ad);
    }

    @Service
    @Transactional
    public static class UserService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional(readOnly = true)
        public Optional<User> getUserById(UUID id) {
            return userRepository.findById(id);
        }

        /**
         * Обновление профиля пользователя (город, телефон, отображаемое имя).
         */
        @Transactional
        public User updateProfile(UUID id, UserProfileUpdateDto dto) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

            if (dto.getDisplayName() != null) {
                user.setDisplayName(dto.getDisplayName());
            }
            if (dto.getPhone() != null) {
                user.setPhone(dto.getPhone());
            }
            if (dto.getCity() != null) {
                user.setCity(dto.getCity());
            }

            return user; // сохранение происходит автоматически благодаря @Transactional
        }

        /**
         * Смена пароля: проверка старого, хеширование нового.
         */
        @Transactional
        public void changePassword(UUID userId, ChangePasswordDto dto) {
            Assert.notNull(userId, "userId must not be null");
            Assert.notNull(dto, "dto must not be null");
            Assert.hasText(dto.getCurrentPassword(), "currentPassword must not be empty");
            Assert.hasText(dto.getNewPassword(), "newPassword must not be empty");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }

            String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
            user.setPasswordHash(encodedNewPassword);
        }
    }
}
