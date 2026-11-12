package com.ips2026.pl11.controller.news;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.data.news.NewsDAO;

public class NewsControllerTest {

    private NewsDAO newsDAO;
    private NewsController controller;

    @BeforeEach
    public void setUp() throws SQLException {
        newsDAO = new NewsDAO();
        controller = new NewsController(newsDAO);

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS news;");
            stmt.execute("CREATE TABLE news (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "headline TEXT NOT NULL, " +
                         "body_text TEXT NOT NULL, " +
                         "image_paths TEXT NOT NULL, " +
                         "publication_date TEXT NOT NULL, " +
                         "author_id INTEGER NOT NULL);");
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM news;");
        }
    }

    @Test
    @DisplayName("Publish article successfully with valid inputs and multiple images")
    void testPublishArticle_Success() {
        assertDoesNotThrow(() ->
            controller.publishArticle("Huge victory against rivals (3-0)", "First team won impressively...", List.of("images/victory1.png", "images/victory2.png"), 1)
        );
    }

    @Test
    @DisplayName("Throw exception when headline is blank or missing")
    void testPublishArticle_BlankHeadline_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            controller.publishArticle("   ", "Body text here...", List.of("images/victory.png"), 1)
        );
        assertEquals("Headline is required.", ex.getMessage());
    }

    @Test
    @DisplayName("Throw exception when images list is empty or missing")
    void testPublishArticle_MissingImages_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            controller.publishArticle("Great game", "Body text here...", List.of(), 1)
        );
        assertEquals("At least one image path is required.", ex.getMessage());
    }
}