package org.example.ads.service;

import org.example.ads.entity.Ad;
import org.example.ads.exception.NotFoundException;
import org.example.ads.repository.AdRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с изображениями объявлений.
 * Отвечает за сохранение файлов на диск и формирование публичных URL.
 * Для дипломной работы реализована локальная файловая система.
 * В промышленном проекте следует заменить на S3 / MinIO.
 */
@Service
@Slf4j
public class AdImageService {

    private static final String UPLOAD_DIR = "uploads";
    private final AdRepository adRepository;

    public AdImageService(AdRepository adRepository) {
        this.adRepository = adRepository;

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            log.warn("Не удалось создать директорию для загрузок: {}", e.getMessage());
        }
    }

    /**
     * Загружает изображение для объявления.
     * 1. Проверяет существование объявления.
     * 2. Валидирует файл (не пустой, допустимое расширение).
     * 3. Сохраняет файл с уникальным именем.
     * 4. Обновляет поле imageUrl в сущности Ad.
     *
     * @param adId   Идентификатор объявления
     * @param file   Загружаемый файл (MultipartFile)
     * @return Обновлённая сущность Ad с новым URL изображения
     */
    public Ad uploadImage(UUID adId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        Assert.isTrue(
                extension != null && List.of("jpg", "jpeg", "png", "gif", "webp").contains(extension.toLowerCase()),
                "Неподдерживаемый формат файла. Разрешены: jpg, jpeg, png, gif, webp"
        );

        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Объявление с ID " + adId + " не найдено"));

        String fileName = UUID.randomUUID() + "." + extension.toLowerCase();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        try {
            Path resolvedPath = filePath.toAbsolutePath().normalize();
            if (!resolvedPath.startsWith(Paths.get(UPLOAD_DIR).toAbsolutePath().normalize())) {
                throw new SecurityException("Недопустимый путь к файлу");
            }

            Files.write(filePath, file.getBytes());
            log.info("Изображение сохранено: {}", filePath.toAbsolutePath());
            String imageUrl = "/images/" + fileName;
            ad.setImageUrl(imageUrl);

            return adRepository.save(ad);
        } catch (IOException e) {
            log.error("Ошибка при сохранении файла: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось сохранить изображение", e);
        }
    }

    /**
     * Получает ресурс (файл) по имени для отдачи клиенту.
     * Используется контроллером для потоковой передачи файла.
     *
     * @param fileName Имя файла в папке uploads
     * @return Resource, содержащий файл
     */
    public Resource getResource(String fileName) {
        Path path = Paths.get(UPLOAD_DIR, fileName).toAbsolutePath().normalize();
        if (!path.startsWith(Paths.get(UPLOAD_DIR).toAbsolutePath().normalize())) {
            throw new SecurityException("Недопустимый путь");
        }

        if (!Files.exists(path)) {
            return null;
        }

        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка доступа к файлу", e);
        }
    }
}
