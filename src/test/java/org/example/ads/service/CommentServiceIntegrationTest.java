package org.example.ads.service;

import org.example.ads.dto.CommentDto;
import org.example.ads.entity.User;
import org.example.ads.repository.UserRepository;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    // Нужен для прямой вставки в БД, чтобы обойти проблемы с Hibernate
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID adId;
    private UUID authorId;
    private UUID otherUserId;

    @BeforeEach
    void setUp() {
        // 1. Создаём автора
        var author = new User();
        author.setEmail("author-" + UUID.randomUUID() + "@example.com");
        author.setPassword("pass_hash_test");
        author = userRepository.save(author);
        authorId = author.getId();

        // 2. Создаём другого пользователя
        var other = new User();
        other.setEmail("other-" + UUID.randomUUID() + "@example.com");
        other.setPassword("pass_hash_other");
        other = userRepository.save(other);
        otherUserId = other.getId();

        // 3. Создаём объявление ЧЕРЕЗ ПРЯМОЙ SQL
        // Мы НЕ используем new Ad() и adRepository.save().
        // Это гарантирует, что author_id будет вставлен правильно.
        adId = UUID.randomUUID();

        String sql = """
            INSERT INTO ads (
                id, author_id, title, description, price, active, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
        """;

        jdbcTemplate.update(sql,
                adId,          // id
                authorId,      // author_id (гарантированно не null)
                "Тестовый товар",
                "Описание товара",
                BigDecimal.valueOf(100),
                true
        );
    }

    @AfterEach
    void tearDown() {
        // Удаляем через SQL, так как мы вставляли через SQL
        if (adId != null) {
            jdbcTemplate.update("DELETE FROM ads WHERE id = ?", adId);
        }
        if (authorId != null) {
            jdbcTemplate.update("DELETE FROM users WHERE id = ?", authorId);
        }
        if (otherUserId != null) {
            jdbcTemplate.update("DELETE FROM users WHERE id = ?", otherUserId);
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
            commentService.deleteComment(commentId, authorId);
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
