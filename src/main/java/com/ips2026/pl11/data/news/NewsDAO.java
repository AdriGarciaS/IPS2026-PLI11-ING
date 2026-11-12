package com.ips2026.pl11.data.news;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.news.NewsArticle;

public class NewsDAO {

	public void insertArticle(String headline, String bodyText, String imagePaths, int authorId) throws SQLException {
        String sql = "INSERT INTO NEWS (headline, body_text, image_paths, publication_date, author_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, headline);
            ps.setString(2, bodyText);
            ps.setString(3, imagePaths);
            ps.setString(4, LocalDate.now().toString());
            ps.setInt(5, authorId);
            ps.executeUpdate();
        }
    }

    public List<NewsArticle> getAllArticles() throws SQLException {
        List<NewsArticle> articles = new ArrayList<>();
        String sql = "SELECT id, headline, body_text, image_paths, publication_date, author_id FROM NEWS";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                articles.add(new NewsArticle(
                    rs.getInt("id"),
                    rs.getString("headline"),
                    rs.getString("body_text"),
                    rs.getString("image_paths"),
                    LocalDate.parse(rs.getString("publication_date")),
                    rs.getInt("author_id")
                ));
            }
        }
        return articles;
    }
}
