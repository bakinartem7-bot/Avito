package org.example.ads.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.dto.AdUpdateDto;
import org.example.ads.entity.Ad;
import org.example.ads.entity.User;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.mapper.AdMapper;
import org.example.ads.repository.AdRepository;
import org.example.ads.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;

    public AdDto createAd(UUID authorId, AdCreateDto dto) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + authorId));
        BigDecimal maxPrice = new BigDecimal("10000000"); // 10 млн
        if (dto.getPrice().compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Price cannot exceed 10 million");
        }

        Ad ad = new Ad();
        ad.setAuthor(author);
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        ad.setPrice(dto.getPrice()); // BigDecimal

        Ad saved = adRepository.save(ad);
        log.debug("Ad created: id={}", saved.getId());
        return AdMapper.toDto(saved);
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
        List<Ad> ads = adRepository.findAllByAuthorIdOrderByCreatedAtDesc(authorId);
        return ads.stream()
                .map(AdMapper::toDto)
                .toList();
    }

    public Optional<AdDto> updateAd(UUID id, UUID currentUserId, AdUpdateDto dto) {
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
            BigDecimal maxPrice = new BigDecimal("10000000");
            if (dto.getPrice().compareTo(maxPrice) > 0) {
                throw new IllegalArgumentException("Price cannot exceed 10 million");
            }
            ad.setPrice(dto.getPrice());
        }

        Ad updated = adRepository.save(ad);
        return Optional.of(AdMapper.toDto(updated));
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
}
