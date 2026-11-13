package com.ips2026.pl11.view.campaign;

import com.ips2026.pl11.model.campaign.CampaignStatus;
import com.ips2026.pl11.view.common.Branding;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * Line with the phases of a campaign (inactive, phase 1, phase 2, phase 3,
 * finished): the phases already passed have a tick, the current one is
 * highlighted and the next ones are grey.
 */
public class PhaseStepper extends JComponent {

    private static final long serialVersionUID = 1L;

    private static final int DIAMETER = 30;
    private static final int TOP = 8;
    private static final int SIDE_MARGIN = 50;

    private CampaignStatus current = CampaignStatus.INACTIVE;

    public PhaseStepper() {
        setPreferredSize(new Dimension(600, 76));
        setMinimumSize(new Dimension(400, 76));
    }

    public void setCurrent(CampaignStatus current) {
        this.current = current;
        setToolTipText("Current phase: " + current.getLabel());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        CampaignStatus[] phases = CampaignStatus.values();
        int width = getWidth() - 2 * SIDE_MARGIN;
        int step = width / (phases.length - 1);
        int centerY = TOP + DIAMETER / 2;

        // Lines between the circles: dark up to the current phase
        for (int i = 0; i < phases.length - 1; i++) {
            int x1 = SIDE_MARGIN + i * step;
            boolean passed = i < current.ordinal();
            g.setColor(passed ? Branding.NAVY : Branding.BORDER);
            g.setStroke(new BasicStroke(passed ? 4f : 3f));
            g.drawLine(x1, centerY, x1 + step, centerY);
        }

        Font labelFont = new Font(Branding.FONT_FAMILY, Font.PLAIN, 13);
        Font currentFont = new Font(Branding.FONT_FAMILY, Font.BOLD, 13);
        Font numberFont = new Font(Branding.FONT_FAMILY, Font.BOLD, 13);

        for (int i = 0; i < phases.length; i++) {
            int centerX = SIDE_MARGIN + i * step;
            int x = centerX - DIAMETER / 2;
            boolean passed = i < current.ordinal();
            boolean isCurrent = i == current.ordinal();

            if (passed || (isCurrent && current == CampaignStatus.FINISHED)) {
                // Phase done: navy circle with a tick (drawn, the font may not have the character)
                g.setColor(Branding.NAVY);
                g.fillOval(x, TOP, DIAMETER, DIAMETER);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawPolyline(new int[] { centerX - 7, centerX - 2, centerX + 7 },
                        new int[] { centerY, centerY + 5, centerY - 5 }, 3);
            } else if (isCurrent) {
                g.setColor(Color.WHITE);
                g.fillOval(x, TOP, DIAMETER, DIAMETER);
                g.setColor(Branding.NAVY);
                g.setStroke(new BasicStroke(3f));
                g.drawOval(x, TOP, DIAMETER, DIAMETER);
                if (i == 0) {
                    g.fillOval(centerX - 4, centerY - 4, 8, 8); // inactive has no number
                } else {
                    drawNumber(g, numberFont, i, centerX, centerY);
                }
            } else {
                g.setColor(Color.WHITE);
                g.fillOval(x, TOP, DIAMETER, DIAMETER);
                g.setColor(Branding.BORDER);
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(x, TOP, DIAMETER, DIAMETER);
                g.setColor(Branding.TEXT_MUTED);
                drawNumber(g, numberFont, i, centerX, centerY);
            }

            g.setFont(isCurrent ? currentFont : labelFont);
            g.setColor(isCurrent ? Branding.NAVY : passed ? Color.DARK_GRAY : Branding.TEXT_MUTED);
            FontMetrics labelMetrics = g.getFontMetrics();
            String label = phases[i].getLabel();
            g.drawString(label, centerX - labelMetrics.stringWidth(label) / 2, TOP + DIAMETER + 22);
        }
        g.dispose();
    }

    private static void drawNumber(Graphics2D g, Font font, int number, int centerX, int centerY) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        String text = String.valueOf(number);
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, centerY + metrics.getAscent() / 2 - 2);
    }
}
