package com.ips2026.pl11.view.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Club header that goes at the top ({@code BorderLayout.NORTH}) of every
 * window: navy band with the logo, a white title, a gold subtitle and a gold
 * line below.
 *
 * <p>The main menu uses the large version; every other window uses the
 * compact one, so all the windows look the same.</p>
 */
public class HeaderPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel lblLogo;
    private final JLabel lblTitle;
    private final JLabel lblSubtitle;

    /** Compact header, for every window except the main menu. */
    public HeaderPanel(String title, String subtitle) {
        this(title, subtitle, false);
    }

    /** No-argument constructor so it can be used from the WindowBuilder palette. */
    public HeaderPanel() {
        this("Title", "Subtitle");
    }

    /**
     * @param large true for the main menu (bigger logo and title)
     */
    public HeaderPanel(String title, String subtitle, boolean large) {
        int logoSize = large ? 84 : 52;
        int padding = large ? 16 : 10;

        setBackground(Branding.NAVY);
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 4, 0, Branding.GOLD),
                new EmptyBorder(padding, 20, padding, 20)));
        setLayout(new BorderLayout(large ? 18 : 14, 0));

        lblLogo = new JLabel(Branding.getLogo(logoSize));
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblLogo, BorderLayout.WEST);

        lblTitle = new JLabel(title);
        lblTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, large ? 26 : 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setVerticalAlignment(SwingConstants.BOTTOM);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, large ? 14 : 13));
        lblSubtitle.setForeground(Branding.GOLD);
        lblSubtitle.setVerticalAlignment(SwingConstants.TOP);

        JPanel pnTitles = new JPanel(new GridLayout(2, 1, 0, 2));
        pnTitles.setOpaque(false);
        pnTitles.add(lblTitle);
        pnTitles.add(lblSubtitle);
        add(pnTitles, BorderLayout.CENTER);
    }

    public void setTitle(String title) {
        lblTitle.setText(title);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }
}
