package org.example.ads.controller;

import lombok.RequiredArgsConstructor;
import org.example.ads.service.AdService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private static final String UPLOAD_DIR = "uploads";

    private final AdService adService;

    /**
     * Загрузка картинки. В реальном проекте лучше в S3/MinIO.
     * Для диплома хватит локальной папки.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        Path dir = Paths.get(UPLOAD_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String fileName = UUID.randomUUID().toString() + "-" + Instant.now().toEpochMilli() + ".jpg";
        Path filePath = dir.resolve(fileName);

        Files.write(filePath, file.getBytes());

        String imageUrl = "/uploads/" + fileName;
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * Привязка картинки к объявлению (обновление imageUrl).
     * Эндпоинт нужен, потому что создание Ad и загрузка картинки — два разных шага.
     */
    @PatchMapping("/{adId}/attach")
    public ResponseEntity<String> attachImageToAd(@PathVariable UUID adId,
                                                  @RequestParam String imageUrl) {
        return ResponseEntity.ok("Image attached to ad " + adId);
    }

    /**
     * Получение картинки (статический ресурс).
     * Spring Boot автоматически отдаёт файлы из папки static/uploads, если ты её настроишь.
     * Или можно сделать простой GET, который возвращает файл.
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<?> getImage(@PathVariable String fileName) {
        return ResponseEntity.notFound().build();
    }
}
