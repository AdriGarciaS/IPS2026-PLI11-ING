package com.ips2026.pl11.view.menu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import com.ips2026.pl11.view.common.Branding;

/**
 * Main menu button drawn as a flat "card": white rounded rectangle with a
 * title and a short description, highlighted in gold when the mouse is over it.
 */
public class MenuCardButton extends JButton {

    private static final long serialVersionUID = 1L;

    private static final int ARC = 16;
    private static final Color CARD = Color.WHITE;
    private static final Color CARD_HOVER = new Color(0xFFFAEE);
    private static final Color CARD_PRESSED = new Color(0xF8EBCB);
    private static final Color CARD_DISABLED = new Color(0xF8F9FB);
    private static final Color BORDER = new Color(0xD6DBE4);

    public MenuCardButton(String title, String description) {
        setText(title, description);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Branding.NAVY);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public final void setText(String title, String description) {
        setText("<html><div style='text-align:center'>"
                + "<span style='font-size:15px'><b>" + title + "</b></span><br>"
                + "<span style='font-size:10px; color:#6B7280'>" + description + "</span>"
                + "</div></html>");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean enabled = isEnabled();
        boolean hover = enabled && getModel().isRollover();
        boolean focused = enabled && isFocusOwner();

        Color fill = !enabled ? CARD_DISABLED
                : getModel().isPressed() ? CARD_PRESSED
                : hover ? CARD_HOVER : CARD;
        g2.setColor(fill);
        g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC);

        // Gold accent bar at the bottom of active cards, cut to the rounded corners.
        if (enabled) {
            Shape oldClip = g2.getClip();
            g2.clip(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC));
            g2.setColor(Branding.GOLD);
            g2.fillRect(1, getHeight() - 7, getWidth() - 3, 5);
            g2.setClip(oldClip);
        }

        g2.setStroke(new BasicStroke(hover || focused ? 2f : 1f));
        g2.setColor(hover || focused ? Branding.GOLD : BORDER);
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC);
        g2.dispose();

        super.paintComponent(g);
    }
}
