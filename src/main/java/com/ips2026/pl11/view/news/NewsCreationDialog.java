package com.ips2026.pl11.view.news;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.ips2026.pl11.controller.news.NewsController;
import com.ips2026.pl11.data.news.NewsDAO;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class NewsCreationDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JPanel contentPanel = new JPanel();
    private JTextField txtHeadline;
    private JTextArea txtBodyText;
    private JTextField txtAuthor;
    private JTextField txtDate;
    private DefaultListModel<String> imageListModel;
    private JList<String> listImages;

    private NewsController newsController;
    private int loggedInEmployeeId = 1; 

    public NewsCreationDialog(JFrame parent) {
        super(parent, "Create New Article", true);
        this.newsController = new NewsController(new NewsDAO());

        setBounds(100, 100, 680, 580);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(new HeaderPanel("Create New Article", "Publish news and announcements for fans and users"), BorderLayout.NORTH);

        contentPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        contentPanel.setLayout(new GridBagLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // --- Headline ---
        JLabel lblHeadline = new JLabel("Article Headline (max 150 chars):");
        lblHeadline.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        contentPanel.add(lblHeadline, gbc);

        gbc.gridy++;
        txtHeadline = new JTextField();
        contentPanel.add(txtHeadline, gbc);

        // --- Featured Images (Multiple Selection) ---
        gbc.gridy++;
        JLabel lblImages = new JLabel("Featured Images Paths (Select one or more):");
        lblImages.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        contentPanel.add(lblImages, gbc);

        gbc.gridy++;
        JPanel pnImages = new JPanel(new BorderLayout(8, 0));
        pnImages.setOpaque(false);

        imageListModel = new DefaultListModel<>();
        listImages = new JList<>(imageListModel);
        JScrollPane scrollImages = new JScrollPane(listImages);
        scrollImages.setPreferredSize(new Dimension(200, 70));
        pnImages.add(scrollImages, BorderLayout.CENTER);

        JButton btnAddImages = new JButton("Add Images...");
        btnAddImages.addActionListener(e -> chooseMultipleImages());
        pnImages.add(btnAddImages, BorderLayout.EAST);

        contentPanel.add(pnImages, gbc);

        // --- Body Text ---
        gbc.gridy++;
        JLabel lblBody = new JLabel("Body Text:");
        lblBody.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        contentPanel.add(lblBody, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        txtBodyText = new JTextArea(5, 20);
        txtBodyText.setLineWrap(true);
        txtBodyText.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtBodyText);
        contentPanel.add(scrollPane, gbc);

        // --- Author & Date Info Panel ---
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        JPanel pnMeta = new JPanel(new GridBagLayout());
        pnMeta.setOpaque(false);
        GridBagConstraints gbcMeta = new GridBagConstraints();
        gbcMeta.insets = new Insets(2, 2, 2, 2);
        gbcMeta.fill = GridBagConstraints.HORIZONTAL;
        gbcMeta.weightx = 0.5;

        gbcMeta.gridx = 0;
        gbcMeta.gridy = 0;
        pnMeta.add(new JLabel("Author:"), gbcMeta);
        gbcMeta.gridy = 1;
        txtAuthor = new JTextField("Employee #" + loggedInEmployeeId);
        txtAuthor.setEditable(false);
        pnMeta.add(txtAuthor, gbcMeta);

        gbcMeta.gridx = 1;
        gbcMeta.gridy = 0;
        pnMeta.add(new JLabel("Publication Date:"), gbcMeta);
        gbcMeta.gridy = 1;
        txtDate = new JTextField(java.time.LocalDate.now().toString());
        txtDate.setEditable(false);
        pnMeta.add(txtDate, gbcMeta);

        contentPanel.add(pnMeta, gbc);

        // --- Buttons Footer ---
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        JButton btnPublish = new JButton("Publish Article");
        btnPublish.addActionListener(e -> publishArticleAction());
        buttonPane.add(btnPublish);
        getRootPane().setDefaultButton(btnPublish);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        buttonPane.add(btnCancel);
    }

    private void chooseMultipleImages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                String path = file.getAbsolutePath();
                if (!imageListModel.contains(path)) {
                    imageListModel.addElement(path);
                }
            }
        }
    }

    private void publishArticleAction() {
        String headline = txtHeadline.getText();
        String bodyText = txtBodyText.getText();
        List<String> imagePaths = Collections.list(imageListModel.elements());

        try {
            newsController.publishArticle(headline, bodyText, imagePaths, loggedInEmployeeId);
            JOptionPane.showMessageDialog(this, "Article published successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}