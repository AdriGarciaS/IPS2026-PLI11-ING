package com.ips2026.pl11.view.news;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MenuCardButton;

public class NewsMenuDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JPanel contentPanel = new JPanel();
    private MenuCardButton btnCreateNews;
    private MenuCardButton btnViewNews;
    private JFrame parentFrame;

    public NewsMenuDialog(JFrame parent) {
        super(parent, "News Management", true);
        this.parentFrame = parent;

        setBounds(100, 100, 520, 420);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new HeaderPanel("News", "Create and view news articles and club announcements"), BorderLayout.NORTH);

        contentPanel.setBorder(new EmptyBorder(20, 20, 16, 20));
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new GridLayout(2, 1, 0, 16));
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(getBtnCreateNews());
        contentPanel.add(getBtnViewNews());

        JPanel buttonPane = new JPanel();
        buttonPane.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, Branding.BORDER),
                new EmptyBorder(8, 0, 10, 16)));
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        btnClose.addActionListener(e -> dispose());
        buttonPane.add(btnClose);
        
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        Branding.applyTo(this);
    }

    private MenuCardButton getBtnCreateNews() {
        if (btnCreateNews == null) {
            btnCreateNews = new MenuCardButton("Create News Article", "Draft and publish news with headlines, text, and multiple images");
            btnCreateNews.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnCreateNews.setMnemonic('C');
            btnCreateNews.addActionListener(e -> {
                NewsCreationDialog dialog = new NewsCreationDialog(parentFrame);
                dialog.setVisible(true);
            });
        }
        return btnCreateNews;
    }

    private MenuCardButton getBtnViewNews() {
        if (btnViewNews == null) {
            btnViewNews = new MenuCardButton("View News Articles", "Browse published articles, view details, and attached images");
            btnViewNews.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnViewNews.setMnemonic('V');
            btnViewNews.addActionListener(e -> { openViewNewsDialog(); 
            });
        }
        return btnViewNews;
    }
    
    private void openViewNewsDialog() {
        try {
            NewsFeedWindow feedWindow = new NewsFeedWindow(parentFrame);
            feedWindow.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error opening News Feed: " + ex.getMessage(),
                    "News Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
