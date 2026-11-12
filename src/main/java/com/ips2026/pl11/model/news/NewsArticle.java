package com.ips2026.pl11.model.news;

import java.time.LocalDate;

public class NewsArticle {
    private int id;
    private String headline;
    private String bodyText;
    private String imagePaths;
    private LocalDate publicationDate;
    private int authorId;

    public NewsArticle(int id, String headline, String bodyText, String imagePaths, LocalDate publicationDate, int authorId) {
        this.id = id;
        this.headline = headline;
        this.bodyText = bodyText;
        this.imagePaths = imagePaths;
        this.publicationDate = publicationDate;
        this.authorId = authorId;
    }

    public int getId() { 
    	return id; 
    }
    
    public String getHeadline() { 
    	return headline; 
    }
    
    public String getBodyText() { 
    	return bodyText; 
    }
    
    public String getImagePaths() { 
    	return imagePaths; 
    }
    
    public LocalDate getPublicationDate() { 
    	return publicationDate; 
    }
    
    public int getAuthorId() { 
    	return authorId; 
    }
}
