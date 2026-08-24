package org.example.ads.service;

import org.example.ads.entity.Ad;
import org.example.ads.entity.User;
import org.example.ads.repository.AdRepository;
import org.example.ads.repository.UserRepository;
import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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

    @BeforeEach
    void setUp() {
        var author = new User();
        author.setEmail("author@example.com");
        author.setPasswordHash("pass");
        authorId = userRepository.save(author).getId();

        var other = new User();
        other.setEmail("other@example.com");
        other.setPasswordHash("pass2");
        otherUserId = userRepository.save(other).getId();

        var ad = new Ad();
        ad.setAuthor(userRepository.findById(authorId).orElseThrow());
        ad.setTitle("Тестовый товар");
        ad.setPrice(100.0);
        adId = adRepository.save(ad).getId();
    }

    @Test
    void getCommentsForAd_empty_list_when_none() {
        List<?> comments = commentService.getCommentsForAd(adId);
        assertTrue(comments.isEmpty());
    }

    @Test
    void createComment_success() {
        String text = "Отличный товар!";
        var comment = commentService.createComment(adId, authorId, text);

        assertNotNull(comment);
        assertEquals(text, comment.getText());
        assertEquals(adId, comment.getAd().getId());
        assertEquals(authorId, comment.getAuthor().getId());
    }

    @Test
    void getCommentsForAd_returns_created_comment() {
        String text = "Комментарий 1";
        commentService.createComment(adId, authorId, text);

        var comments = commentService.getCommentsForAd(adId);

        assertEquals(1, comments.size());
        assertEquals(text, comments.get(0).getText());
    }

    @Test
    void deleteComment_own_success() {
        String text = "Мой комментарий";
        var comment = commentService.createComment(adId, authorId, text);
        UUID commentId = comment.getId();

        commentService.deleteComment(commentId, authorId);

        var remaining = commentService.getCommentsForAd(adId);
        assertTrue(remaining.isEmpty());
    }

    @Test
    void deleteComment_other_user_throws() {
        String text = "Чужой комментарий";
        var comment = commentService.createComment(adId, otherUserId, text);
        UUID commentId = comment.getId();

        assertThrows(AccessDeniedException.class, () -> {
            commentService.deleteComment(commentId, authorId); // автор пытается удалить чужой комментарий
        });
    }

    @Test
    void createComment_ad_not_found_throws() {
        UUID nonExistentAdId = UUID.randomUUID();
        String text = "Текст";

        assertThrows(NotFoundException.class, () -> {
            commentService.createComment(nonExistentAdId, authorId, text);
        });
    }

    @Test
    void createComment_user_not_found_throws() {
        UUID nonExistentUserId = UUID.randomUUID();
        String text = "Текст";

        assertThrows(NotFoundException.class, () -> {
            commentService.createComment(adId, nonExistentUserId, text);
        });
    }
}
