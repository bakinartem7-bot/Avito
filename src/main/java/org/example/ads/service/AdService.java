package org.example.ads.service;

import org.example.ads.dto.AdCreateDto;
import org.example.ads.dto.AdDto;
import org.example.ads.dto.AdUpdateDto;
import org.example.ads.entity.Ad;
import org.example.ads.entity.User;
import org.example.ads.mapper.AdMapper;
import org.example.ads.repository.AdRepository;
import org.example.ads.repository.UserRepository;
import org.example.ads.exception.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис бизнес-логики для управления объявлениями в проекте сайта объявлений.
 * <p>
 * Реализует CRUD-операции над сущностью {@link Ad} с учётом правил разграничения прав:
 * пользователь может создавать, обновлять и удалять только свои объявления.
 * Для дипломной работы демонстрирует корректную обработку прав доступа, работу с транзакциями
 * и интеграцию слоёв (DTO → Entity → Repository → Mapper).
 */
@Service
@Transactional
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param adRepository репозиторий для работы с объявлениями
     * @param userRepository репозиторий для проверки существования автора
     */
    public AdService(AdRepository adRepository, UserRepository userRepository) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
    }

    /**
     * Получает список всех объявлений.
     * <p>
     * Выполняется в режиме readOnly. Преобразует найденные сущности {@link Ad} в DTO
     * с помощью {@link AdMapper} и возвращает список.
     *
     * @return список всех объявлений в формате {@link AdDto}
     */
    @Transactional(readOnly = true)
    public List<AdDto> getAllAds() {
        return adRepository.findAll().stream()
                .map(AdMapper::toDto)
                .toList();
    }

    /**
     * Находит объявление по уникальному идентификатору.
     * <p>
     * Выполняется в режиме readOnly. Возвращает {@link Optional} с DTO, если объявление найдено,
     * или пустой Optional в противном случае.
     *
     * @param id UUID объявления
     * @return Optional с {@link AdDto}, если найдено
     */
    @Transactional(readOnly = true)
    public Optional<AdDto> findAdById(UUID id) {
        return adRepository.findById(id)
                .map(AdMapper::toDto);
    }

    /**
     * Получает все объявления указанного автора, отсортированные по дате создания (новые сверху).
     * <p>
     * Выполняется в режиме readOnly. Используется для эндпоинта «мои объявления».
     *
     * @param authorId UUID автора объявлений
     * @return список объявлений автора в формате {@link AdDto}
     */
    @Transactional(readOnly = true)
    public List<AdDto> findAllByAuthorId(UUID authorId) {
        return adRepository.findAllByAuthorIdOrderByCreatedAtDesc(authorId).stream()
                .map(AdMapper::toDto)
                .toList();
    }

    /**
     * Создаёт новое объявление от имени указанного пользователя.
     * <p>
     * Проверяет существование автора по UUID. Инициализирует сущность {@link Ad},
     * устанавливает автора, заголовок, описание, цену (или ноль, если не указана),
     * активирует объявление. Сохраняет в БД и возвращает DTO созданного объявления.
     *
     * @param authorId UUID пользователя — автора объявления
     * @param dto      DTO с данными для создания объявления
     * @return {@link AdDto} созданного объявления
     * @throws IllegalArgumentException если пользователь с указанным UUID не найден
     */
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

    /**
     * Обновляет существующее объявление.
     * <p>
     * Проверяет, что текущий пользователь является автором объявления. Если нет — выбрасывает
     * {@link AccessDeniedException}. Обновляет заголовок, описание и цену (если передана и неотрицательна).
     * Устанавливает текущую временную метку в поле updatedAt. Сохраняет изменения и возвращает DTO.
     *
     * @param id          UUID обновляемого объявления
     * @param currentUserId UUID текущего пользователя (для проверки прав)
     * @param dto         DTO с новыми данными для обновления
     * @return Optional с обновлённым {@link AdDto}
     * @throws AccessDeniedException если пользователь пытается обновить чужое объявление
     * @throws IllegalArgumentException если объявление с указанным UUID не найдено
     */
    @Transactional
    public Optional<AdDto> updateAd(UUID id, UUID currentUserId, AdUpdateDto dto) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found with id: " + id));

        if (!ad.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own ads");
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

    /**
     * Удаляет объявление.
     * <p>
     * Проверяет, что текущий пользователь является автором объявления. Если нет — выбрасывает
     * {@link AccessDeniedException}. Находит объявление по UUID, проверяет его существование
     * и удаляет через репозиторий.
     *
     * @param id          UUID удаляемого объявления
     * @param currentUserId UUID текущего пользователя (для проверки прав)
     * @throws AccessDeniedException если пользователь пытается удалить чужое объявление
     * @throws IllegalArgumentException если объявление с указанным UUID не найдено
     */
    @Transactional
    public void deleteAd(UUID id, UUID currentUserId) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found with id: " + id));

        if (!ad.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own ads");
        }

        adRepository.delete(ad);
    }
}
