package com.ips2026.pl11.model.news;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NewsArticleTest {

    @Test
    @DisplayName("NewsArticle constructor and getters work correctly")
    void testNewsArticleGetters() {
        LocalDate date = LocalDate.now();
        NewsArticle article = new NewsArticle(1, "Victory!", "Great match played today.", "images/win.png", date, 5);

        assertEquals(1, article.getId());
        assertEquals("Victory!", article.getHeadline());
        assertEquals("Great match played today.", article.getBodyText());
        assertEquals("images/win.png", article.getImagePaths());
        assertEquals(date, article.getPublicationDate());
        assertEquals(5, article.getAuthorId());
    }
}
