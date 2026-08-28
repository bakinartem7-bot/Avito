package org.example.ads.service;

import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.dto.AdUpdateDto;
import org.example.ads.entity.Ad;
import org.example.ads.entity.User;
import org.example.ads.mapper.AdMapper;
import org.example.ads.repository.AdRepository;
import org.example.ads.repository.UserRepository;
import org.example.ads.exception.AccessDeniedException; // <-- важно: импортируем нужное исключение
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;

    public AdService(AdRepository adRepository, UserRepository userRepository) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AdDto> getAllAds() {
        return adRepository.findAll().stream()
                .map(AdMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AdDto> findAdById(UUID id) {
        return adRepository.findById(id)
                .map(AdMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AdDto> findAllByAuthorId(UUID authorId) {
        return adRepository.findAllByAuthorIdOrderByCreatedAtDesc(authorId).stream()
                .map(AdMapper::toDto)
                .toList();
    }

    @Transactional
    public AdDto createAd(UUID authorId, AdCreateDto dto) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + authorId));

        Ad ad = new Ad();
        ad.setAuthor(author);
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        ad.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        ad.setActive(true);

        Ad saved = adRepository.save(ad);
        return AdMapper.toDto(saved);
    }

    @Transactional
    public Optional<AdDto> updateAd(UUID id, UUID currentUserId, AdUpdateDto dto) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found with id: " + id));

        if (!ad.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own ads"); // <-- меняем здесь
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

        if (!ad.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own ads"); // <-- и здесь
        }

        adRepository.delete(ad);
    }
}
