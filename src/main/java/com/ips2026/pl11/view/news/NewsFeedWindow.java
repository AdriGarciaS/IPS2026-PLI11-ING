package com.ips2026.pl11.view.news;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.ips2026.pl11.data.news.NewsDAO;
import com.ips2026.pl11.model.news.NewsArticle;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class NewsFeedWindow extends JDialog {

    private static final long serialVersionUID = 1L;

    private final NewsDAO newsDAO;
    private JPanel pnListContainer;
    private JPanel pnDetailContent;
    
    private JLabel lblDetailHeadline;
    private JLabel lblDetailMeta;
    private JLabel lblCarouselImage;
    private JTextArea txtDetailBody;
    private JButton btnPrevImage;
    private JButton btnNextImage;

    private List<NewsArticle> currentArticles;
    private NewsArticle selectedArticle;
    private int currentImageIndex = 0;
    private List<String> activeImagePaths;

    public NewsFeedWindow(JFrame parent) {
        super(parent, "Club News & Updates", true);
        this.newsDAO = new NewsDAO();

        Branding.applyTo(this);
        Branding.setInitialSize(this, 1200, 750, parent);
        getContentPane().setLayout(new BorderLayout(0, 0));

        getContentPane().add(new HeaderPanel("CLUB NEWS & UPDATES", "Browse recent announcements, match reports and club updates", true), BorderLayout.NORTH);

        JPanel pnMain = new JPanel(new GridLayout(1, 2, 15, 0));
        pnMain.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnMain.setOpaque(false);

        pnMain.add(createLeftFeedPanel());
        pnMain.add(createRightDetailPanel());

        getContentPane().add(pnMain, BorderLayout.CENTER);

        JPanel pnFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnFooter.setOpaque(false);
        JButton btnClose = new JButton("CLOSE");
        btnClose.addActionListener(e -> dispose());
        pnFooter.add(btnClose);
        getContentPane().add(pnFooter, BorderLayout.SOUTH);

        loadNewsFeed();
    }

    private JPanel createLeftFeedPanel() {
        JPanel pnWrapper = new JPanel(new BorderLayout(0, 5));
        pnWrapper.setOpaque(false);
        pnWrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                " LATEST NEWS FEED ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font(Branding.FONT_FAMILY, Font.BOLD, 12),
                Branding.NAVY
        ));

        pnListContainer = new JPanel();
        pnListContainer.setLayout(new BoxLayout(pnListContainer, BoxLayout.Y_AXIS));
        pnListContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(pnListContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        pnWrapper.add(scrollPane, BorderLayout.CENTER);
        return pnWrapper;
    }

    private JPanel createRightDetailPanel() {
        JPanel pnWrapper = new JPanel(new BorderLayout(0, 8));
        pnWrapper.setOpaque(false);
        pnWrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                " ARTICLE DETAIL VIEW ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font(Branding.FONT_FAMILY, Font.BOLD, 12),
                Branding.NAVY
        ));

        pnDetailContent = new JPanel(new BorderLayout(0, 10));
        pnDetailContent.setOpaque(false);
        pnDetailContent.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblPlaceholder = new JLabel("<html><div style='text-align: center; color: #6B7280; margin-top: 150px;'>"
                + "<b>Select a news article from the left feed</b><br>to view full content and image gallery."
                + "</div></html>", SwingConstants.CENTER);
        lblPlaceholder.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 14));
        pnDetailContent.add(lblPlaceholder, BorderLayout.CENTER);

        pnWrapper.add(pnDetailContent, BorderLayout.CENTER);
        return pnWrapper;
    }

    private void loadNewsFeed() {
        pnListContainer.removeAll();
        try {
            currentArticles = newsDAO.getAllArticles();

            if (currentArticles.isEmpty()) {
                JLabel lblEmpty = new JLabel("No news published yet", SwingConstants.CENTER);
                lblEmpty.setFont(new Font(Branding.FONT_FAMILY, Font.ITALIC, 14));
                lblEmpty.setForeground(Branding.TEXT_MUTED);
                pnListContainer.add(lblEmpty);
            } else {
                for (NewsArticle article : currentArticles) {
                    pnListContainer.add(createNewsCard(article));
                    pnListContainer.add(javax.swing.Box.createRigidArea(new Dimension(0, 8)));
                }
                displayArticleDetail(currentArticles.get(0));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading news feed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        pnListContainer.revalidate();
        pnListContainer.repaint();
    }

    private JPanel createNewsCard(NewsArticle article) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Branding.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblHeadline = new JLabel(article.getHeadline());
        lblHeadline.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
        lblHeadline.setForeground(Branding.NAVY);
        card.add(lblHeadline, BorderLayout.NORTH);

        String dateStr = article.getPublicationDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        JLabel lblMeta = new JLabel(dateStr + "  |  By Patricia Lago Martín"); // O autor real según ID
        lblMeta.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 11));
        lblMeta.setForeground(Branding.TEXT_MUTED);
        card.add(lblMeta, BorderLayout.CENTER);

        String bodySnippet = article.getBodyText().length() > 65 
                ? article.getBodyText().substring(0, 62) + "..." 
                : article.getBodyText();
        JLabel lblSnippet = new JLabel(bodySnippet);
        lblSnippet.setFont(new Font(Branding.FONT_FAMILY, Font.ITALIC, 11));
        card.add(lblSnippet, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                displayArticleDetail(article);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(241, 245, 249));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    private void displayArticleDetail(NewsArticle article) {
        this.selectedArticle = article;
        this.currentImageIndex = 0;
        
        String pathsRaw = article.getImagePaths(); 
        if (pathsRaw != null && !pathsRaw.isEmpty()) {
            activeImagePaths = List.of(pathsRaw.split(","));
        } else {
            activeImagePaths = List.of();
        }

        pnDetailContent.removeAll();
        pnDetailContent.setLayout(new BorderLayout(0, 10));

        JPanel pnHeaderDetail = new JPanel(new GridLayout(2, 1, 0, 2));
        pnHeaderDetail.setOpaque(false);

        lblDetailHeadline = new JLabel(article.getHeadline());
        lblDetailHeadline.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 18));
        lblDetailHeadline.setForeground(Branding.NAVY);
        pnHeaderDetail.add(lblDetailHeadline);

        String dateStr = article.getPublicationDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        lblDetailMeta = new JLabel(dateStr + "  |  Author ID: #" + article.getAuthorId());
        lblDetailMeta.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        lblDetailMeta.setForeground(Branding.TEXT_MUTED);
        pnHeaderDetail.add(lblDetailMeta);

        pnDetailContent.add(pnHeaderDetail, BorderLayout.NORTH);

        JPanel pnCenterContent = new JPanel(new BorderLayout(0, 10));
        pnCenterContent.setOpaque(false);

        JPanel pnCarousel = new JPanel(new BorderLayout(5, 0));
        pnCarousel.setBackground(new Color(248, 249, 250));
        pnCarousel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Branding.BORDER, 1, true),
                new EmptyBorder(6, 6, 6, 6)
        ));
        pnCarousel.setPreferredSize(new Dimension(0, 240));

        lblCarouselImage = new JLabel("", SwingConstants.CENTER);
        lblCarouselImage.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        updateCarouselImageDisplay();
        pnCarousel.add(lblCarouselImage, BorderLayout.CENTER);

        if (activeImagePaths.size() > 1) {
            btnPrevImage = new JButton("<");
            btnPrevImage.addActionListener(e -> {
                if (currentImageIndex > 0) {
                    currentImageIndex--;
                    updateCarouselImageDisplay();
                }
            });
            btnNextImage = new JButton(">");
            btnNextImage.addActionListener(e -> {
                if (currentImageIndex < activeImagePaths.size() - 1) {
                    currentImageIndex++;
                    updateCarouselImageDisplay();
                }
            });
            pnCarousel.add(btnPrevImage, BorderLayout.WEST);
            pnCarousel.add(btnNextImage, BorderLayout.EAST);
        }

        pnCenterContent.add(pnCarousel, BorderLayout.NORTH);

        JPanel pnBodyWrapper = new JPanel(new BorderLayout(0, 2));
        pnBodyWrapper.setOpaque(false);
        JLabel lblBodyTitle = new JLabel("ARTICLE CONTENT");
        lblBodyTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 11));
        lblBodyTitle.setForeground(Branding.NAVY_DARKER);
        pnBodyWrapper.add(lblBodyTitle, BorderLayout.NORTH);

        txtDetailBody = new JTextArea(article.getBodyText());
        txtDetailBody.setEditable(false);
        txtDetailBody.setLineWrap(true);
        txtDetailBody.setWrapStyleWord(true);
        txtDetailBody.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 13));
        JScrollPane scrollBody = new JScrollPane(txtDetailBody);
        pnBodyWrapper.add(scrollBody, BorderLayout.CENTER);

        pnCenterContent.add(pnBodyWrapper, BorderLayout.CENTER);
        pnDetailContent.add(pnCenterContent, BorderLayout.CENTER);

        pnDetailContent.revalidate();
        pnDetailContent.repaint();
    }

    private void updateCarouselImageDisplay() {
        if (activeImagePaths == null || activeImagePaths.isEmpty()) {
            lblCarouselImage.setIcon(null);
            lblCarouselImage.setText("No images attached to this article.");
            return;
        }

        String imgPath = activeImagePaths.get(currentImageIndex).trim();
        boolean loaded = false;

        try {
            String resourcePath = imgPath.startsWith("/") ? imgPath : "/" + imgPath;
            java.net.URL imgUrl = getClass().getResource(resourcePath);

            if (imgUrl != null) {
                javax.swing.ImageIcon originalIcon = new javax.swing.ImageIcon(imgUrl);
                java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(420, 210, java.awt.Image.SCALE_SMOOTH);
                lblCarouselImage.setIcon(new javax.swing.ImageIcon(scaledImage));
                lblCarouselImage.setText("");
                loaded = true;
            } else {
                File imgFile = new File(imgPath);
                if (imgFile.exists()) {
                    javax.swing.ImageIcon originalIcon = new javax.swing.ImageIcon(imgPath);
                    java.awt.Image scaledImage = originalIcon.getImage().getScaledInstance(420, 210, java.awt.Image.SCALE_SMOOTH);
                    lblCarouselImage.setIcon(new javax.swing.ImageIcon(scaledImage));
                    lblCarouselImage.setText("");
                    loaded = true;
                }
            }

            if (!loaded) {
                lblCarouselImage.setIcon(null);
                lblCarouselImage.setText("<html><center><b>Image Preview Not Available</b><br>(" + (currentImageIndex + 1) + " of " + activeImagePaths.size() + ")<br>" + imgPath + "</center></html>");
            }

        } catch (Exception ex) {
            lblCarouselImage.setIcon(null);
            lblCarouselImage.setText("Error loading image: " + imgPath);
        }
    }
}
