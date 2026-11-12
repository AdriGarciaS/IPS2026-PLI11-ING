package com.ips2026.pl11.data.news;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.news.NewsArticle;

public class NewsDAOTest {

    private NewsDAO newsDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        newsDAO = new NewsDAO();

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
    @DisplayName("NewsDAO successfully inserts and retrieves articles with multiple images")
    void testInsertAndRetrieveArticle() throws SQLException {
        newsDAO.insertArticle("Derby Match Preview", "The team is ready for the clash...", "images/derby1.png,images/derby2.png", 2);

        List<NewsArticle> articles = newsDAO.getAllArticles();
        assertNotNull(articles);
        assertEquals(1, articles.size());
        assertEquals("Derby Match Preview", articles.get(0).getHeadline());
        assertEquals("images/derby1.png,images/derby2.png", articles.get(0).getImagePaths()); // o getImagePath() según tu modelo
        assertEquals(2, articles.get(0).getAuthorId());
    }
}