package org.example.ads.service;

import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.entity.Ad;
import org.example.ads.entity.User;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.example.ads.repository.AdRepository;
import org.example.ads.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;

    public AdService(AdRepository adRepository, UserRepository userRepository) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
    }

    public AdDto createAd(UUID authorId, AdCreateDto dto) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Ad ad = new Ad();
        ad.setAuthor(author);
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        ad.setPrice(dto.getPrice());

        Ad saved = adRepository.save(ad);
        return toDto(saved);
    }

    public List<AdDto> getAllAds() {
        return adRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public AdDto getAdById(UUID id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ad not found"));
        return toDto(ad);
    }

    public void deleteAd(UUID id, UUID currentUserId) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ad not found"));

        if (!ad.getAuthor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own ads");
        }
        adRepository.delete(ad);
    }

    private AdDto toDto(Ad ad) {
        AdDto dto = new AdDto();
        dto.setId(ad.getId());
        dto.setAuthorId(ad.getAuthor().getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setPrice(ad.getPrice());
        dto.setCreatedAt(ad.getCreatedAt());
        return dto;
    }
}
