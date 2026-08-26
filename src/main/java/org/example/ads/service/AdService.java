package org.example.ads.service;

import lombok.extern.slf4j.Slf4j;
import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.dto.AdUpdateDto;
import org.example.ads.entity.Ad;
import org.example.ads.entity.User;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.repository.AdRepository;
import org.example.ads.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;

    public AdService(AdRepository adRepository, UserRepository userRepository) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
    }

    public AdDto createAd(UUID authorId, AdCreateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("AdCreateDto cannot be null");
        }
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + authorId));

        Ad ad = new Ad();
        ad.setAuthor(author);
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        // Защита от null: если цена не указана, ставим 0.0
        ad.setPrice(dto.getPrice() != null ? dto.getPrice().doubleValue() : 0.0);

        Ad saved = adRepository.save(ad);
        log.debug("Ad created: id={}, authorId={}", saved.getId(), saved.getAuthor().getId());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AdDto> getAllAds() {
        return adRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AdDto> findAdById(UUID id) {
        return adRepository.findById(id)
                .map(this::toDto);
    }

    /**
     * Получить все объявления конкретного автора (для эндпоинта /mine).
     */
    @Transactional(readOnly = true)
    public List<AdDto> findAllByAuthorId(UUID authorId) {
        List<Ad> ads = adRepository.findAllByAuthorIdOrderByCreatedAtDesc(authorId);
        return ads.stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<AdDto> updateAd(UUID id, UUID currentUserId, AdUpdateDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("AdUpdateDto cannot be null");
        }

        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ad not found with id: " + id));

        if (!ad.getAuthor().getId().equals(currentUserId)) {
            log.warn("Update denied: user {} tried to update ad {} which belongs to {}",
                    currentUserId, id, ad.getAuthor().getId());
            throw new AccessDeniedException("You can only update your own ads");
        }

        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        if (dto.getPrice() != null) {
            ad.setPrice(dto.getPrice().doubleValue());
        }

        Ad updated = adRepository.save(ad);
        log.debug("Ad updated: id={}", updated.getId());
        return Optional.of(toDto(updated));
    }

    public void deleteAd(UUID id, UUID currentUserId) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ad not found with id: " + id));

        if (!ad.getAuthor().getId().equals(currentUserId)) {
            log.warn("Delete denied: user {} tried to delete ad {} which belongs to {}",
                    currentUserId, id, ad.getAuthor().getId());
            throw new AccessDeniedException("You can only delete your own ads");
        }

        adRepository.delete(ad);
        log.info("Ad deleted: id={}", id);
    }

    private AdDto toDto(Ad ad) {
        AdDto dto = new AdDto();
        dto.setId(ad.getId());
        dto.setAuthorId(ad.getAuthor().getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setPrice(ad.getPrice());
        dto.setCreatedAt(ad.getCreatedAt());
        dto.setUpdatedAt(ad.getUpdatedAt());
        return dto;
    }
}
