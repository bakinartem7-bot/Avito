package org.example.ads.service;

import lombok.extern.slf4j.Slf4j;
import org.example.ads.dto.AdDto;
import org.example.ads.entity.Ad;
import org.example.ads.exception.NotFoundException;
import org.example.ads.repository.AdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@Slf4j
public class AdminAdService {

    private final AdRepository adRepository;

    public AdminAdService(AdRepository adRepository) {
        this.adRepository = adRepository;
    }

    /**
     * Админ может удалить ЛЮБОЕ объявление без проверки автора.
     * Вызывать ТОЛЬКО после проверки роли (через @PreAuthorize или вручную).
     */
    public void deleteAnyAd(UUID adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Ad not found with id: " + adId));

        adRepository.delete(ad);
        log.info("[ADMIN] Ad deleted: id={}", adId);
    }

    // Сюда можно добавить другие админские методы: бан объявления, принудительное обновление и т.п.
}
