package org.example.ads.service;

import org.example.ads.dto.CommentDto;
import org.example.ads.entity.Ad;
import org.example.ads.entity.User;
import org.example.ads.repository.AdRepository;
import org.example.ads.repository.UserRepository;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты сервиса комментариев.
 * Проверяют CRUD, авторизацию, обработку ошибок.
 * Используют полную загрузку контекста Spring Boot и явную очистку данных.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID adId;
    private UUID authorId;
    private UUID otherUserId;

    /**
     * Подготовка тестовых данных перед каждым тестом.
     * Используем UUID в email, чтобы избежать дублирования при параллельных запусках.
     */
    @BeforeEach
    void setUp() {
        // Создаём автора объявления
        var author = new User();
        author.setEmail("author-" + UUID.randomUUID() + "@example.com");
        author.setPassword("pass_hash_test");
        authorId = userRepository.save(author).getId();

        // Создаём другого пользователя (для проверки прав доступа)
        var other = new User();
        other.setEmail("other-" + UUID.randomUUID() + "@example.com");
        other.setPassword("pass_hash_other");
        otherUserId = userRepository.save(other).getId();

        // Создаём тестовое объявление
        var ad = new Ad();
        ad.setAuthor(userRepository.findById(authorId).orElseThrow());
        ad.setTitle("Тестовый товар");
        ad.setDescription("Описание товара");
        ad.setPrice(BigDecimal.valueOf(100));
        adId = adRepository.save(ad).getId();
    }

    /**
     * Явная очистка данных после каждого теста.
     * Это делает тесты полностью изолированными и предотвращает ошибки FK.
     */
    @AfterEach
    void tearDown() {
        // Удаляем комментарии (если они есть) — порядок важен из-за FK
        // Если в Comment есть свой репозиторий, лучше использовать его.
        // Здесь полагаемся на каскадное удаление либо удаляем вручную, если возможно.
        // Для простоты и надёжности — удаляем объявление (удалит комментарии при cascade=REMOVE)
        if (adRepository.existsById(adId)) {
            adRepository.deleteById(adId);
        }
        if (userRepository.existsById(authorId)) {
            userRepository.deleteById(authorId);
        }
        if (userRepository.existsById(otherUserId)) {
            userRepository.deleteById(otherUserId);
        }
    }

    @Test
    void getCommentsForAd_empty_list_when_none() {
        List<CommentDto> comments = commentService.getCommentsForAd(adId);
        assertTrue(comments.isEmpty(), "Список комментариев должен быть пустым, если их нет");
    }

    @Test
    void createComment_success() {
        String contentText = "Отличный товар!";
        var comment = commentService.createComment(adId, authorId, contentText);

        assertNotNull(comment, "Созданный комментарий не должен быть null");
        assertEquals(contentText, comment.getContent(), "Текст комментария должен совпадать");
        assertEquals(adId, comment.getAdId(), "ID объявления должен совпадать");
        assertEquals(authorId, comment.getUserId(), "ID пользователя должен совпадать");
    }

    @Test
    void getCommentsForAd_returns_created_comment() {
        String text = "Комментарий 1";
        var comment = commentService.createComment(adId, authorId, text);
        UUID commentId = comment.getId();

        List<CommentDto> comments = commentService.getCommentsForAd(adId);

        assertEquals(1, comments.size(), "Должен быть ровно один комментарий");
        assertEquals(text, comments.get(0).getContent(), "Текст комментария должен совпадать");
        assertEquals(commentId, comments.get(0).getId(), "ID комментария должен совпадать");
    }

    @Test
    void deleteComment_own_success() {
        String text = "Мой комментарий";
        var comment = commentService.createComment(adId, authorId, text);
        UUID commentId = comment.getId();

        commentService.deleteComment(commentId, authorId);

        List<CommentDto> remaining = commentService.getCommentsForAd(adId);
        assertTrue(remaining.isEmpty(), "После удаления собственного комментария список должен быть пустым");
    }

    @Test
    void deleteComment_other_user_throws() {
        String text = "Чужой комментарий";
        var comment = commentService.createComment(adId, otherUserId, text);
        UUID commentId = comment.getId();

        assertThrows(AccessDeniedException.class, () -> {
            commentService.deleteComment(commentId, authorId); // автор пытается удалить чужой комментарий
        }, "Должно выбрасываться AccessDeniedException при попытке удалить чужой комментарий");
    }

    @Test
    void createComment_ad_not_found_throws() {
        UUID nonExistentAdId = UUID.randomUUID();
        String text = "Текст";

        assertThrows(NotFoundException.class, () -> {
            commentService.createComment(nonExistentAdId, authorId, text);
        }, "Должно выбрасываться NotFoundException при создании комментария к несуществующему объявлению");
    }

    @Test
    void createComment_user_not_found_throws() {
        UUID nonExistentUserId = UUID.randomUUID();
        String text = "Текст";

        assertThrows(NotFoundException.class, () -> {
            commentService.createComment(adId, nonExistentUserId, text);
        }, "Должно выбрасываться NotFoundException при создании комментария от имени несуществующего пользователя");
    }
}
