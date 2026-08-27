package org.example.ads.controller;

import lombok.RequiredArgsConstructor;
import org.example.ads.dto.ResponseDto;
import org.example.ads.service.AdImageService;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Контроллер для работы с изображениями объявлений.
 * Реализует загрузку файла, привязку к объявлению и отдачу контента.
 * Вся бизнес-логика инкапсулирована в AdImageService для соблюдения принципов Clean Code.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final AdImageService adImageService;

    /**
     * Загрузка изображения и автоматическая привязка к объявлению.
     * Это атомарная операция: либо всё сохраняется (файл + запись в БД), либо ничего.
     *
     * @param adId  UUID объявления, к которому прикрепляется фото
     * @param file  Загружаемый файл (multipart/form-data)
     */
    @PostMapping("/ads/{adId}")
    public ResponseEntity<ResponseDto> uploadAndAttach(
            @PathVariable @NotBlank UUID adId,
            @RequestPart("file") MultipartFile file
    ) {
        log.info("Попытка загрузки изображения для объявления: {}", adId);

        // Проверка на пустой файл
        if (file.isEmpty()) {
            log.warn("Попытка загрузки пустого файла для объявления: {}", adId);
            return ResponseEntity.badRequest().body(
                    new ResponseDto(false, "Файл не может быть пустым", null)
            );
        }

        var result = adImageService.uploadImage(adId, file);

        return ResponseEntity.ok(new ResponseDto(
                true,
                "Изображение успешно загружено и привязано к объявлению",
                result.getImageUrl()
        ));
    }

    /**
     * Отдача изображения клиенту.
     * Определяет MIME-тип автоматически и устанавливает правильный заголовок Content-Type.
     *
     * @param fileName Имя файла (без пути), например, "a1b2c3d4.jpg"
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<?> getImage(@PathVariable String fileName) {
        Resource resource = adImageService.getResource(fileName);

        if (resource == null || !resource.exists()) {
            log.warn("Запрошен несуществующий файл: {}", fileName);
            return ResponseEntity.notFound().build();
        }

        String mimeType = getMimeType(fileName);
        log.debug("Отдача файла {} с типом {}", fileName, mimeType);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }

    /**
     * Утилитарный метод для определения MIME-типа по расширению файла.
     */
    private String getMimeType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "application/octet-stream";
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
