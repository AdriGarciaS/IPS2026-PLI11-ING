package com.ips2026.pl11.controller.news;

import java.sql.SQLException;
import java.util.List;

import com.ips2026.pl11.data.news.NewsDAO;

public class NewsController {

    private NewsDAO newsDAO;

    public NewsController(NewsDAO newsDAO) {
        this.newsDAO = newsDAO;
    }

    public void publishArticle(String headline, String bodyText, List<String> imagePaths, int authorId) throws SQLException {
        if (headline == null || headline.trim().isEmpty()) {
            throw new IllegalArgumentException("Headline is required.");
        }
        if (headline.length() > 150) {
            throw new IllegalArgumentException("Headline cannot exceed 150 characters.");
        }
        if (bodyText == null || bodyText.trim().isEmpty()) {
            throw new IllegalArgumentException("Main text body is required.");
        }
        if (imagePaths == null || imagePaths.isEmpty()) {
            throw new IllegalArgumentException("At least one image path is required.");
        }

        String joinedImages = String.join(",", imagePaths);

        newsDAO.insertArticle(headline.trim(), bodyText.trim(), joinedImages, authorId);
    }
}
