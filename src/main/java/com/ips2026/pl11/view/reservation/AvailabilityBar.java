package com.ips2026.pl11.view.reservation;

import com.ips2026.pl11.model.reservation.ReservationRules;
import com.ips2026.pl11.model.reservation.TimeSlot;
import com.ips2026.pl11.view.common.Branding;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;

/**
 * Time bar of a day from 08:00 to 22:00: shows in colors the team uses, the
 * blocked time after them, the reservations and the free time, and marks the
 * period that is being booked. Clicking on the bar chooses that start time.
 */
public class AvailabilityBar extends JComponent {

    private static final long serialVersionUID = 1L;

    private static final Color FREE = Color.WHITE;
    private static final Color TEAM_USE = Branding.NAVY;
    private static final Color BLOCKED = new Color(0xF7E3B0);
    private static final Color RESERVED = new Color(0x8DA2C0);
    private static final Color PAST = new Color(0xE2E5EA);

    private static final int LABELS_HEIGHT = 18;
    private static final int BAR_HEIGHT = 34;
    private static final int SIDE_MARGIN = 12;

    private static final int FIRST_MINUTE = ReservationRules.OPENING_TIME.toSecondOfDay() / 60;
    private static final int LAST_MINUTE = ReservationRules.CLOSING_TIME.toSecondOfDay() / 60;

    private transient List<TimeSlot> timeline = new ArrayList<>();
    private LocalTime selectionStart;
    private int selectionHours;
    private boolean selectionAvailable;
    private transient Consumer<LocalTime> timeClickedListener;

    public AvailabilityBar() {
        setPreferredSize(new Dimension(600, LABELS_HEIGHT + BAR_HEIGHT + 4));
        setMinimumSize(new Dimension(300, LABELS_HEIGHT + BAR_HEIGHT + 4));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ToolTipManager.sharedInstance().registerComponent(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                LocalTime time = timeAt(event.getX());
                if (time != null && timeClickedListener != null) {
                    timeClickedListener.accept(time);
                }
            }
        });
    }

    public void setTimeline(List<TimeSlot> timeline) {
        this.timeline = new ArrayList<>(timeline);
        repaint();
    }

    /** Marks the period being booked: green outline if it is available, red if not. */
    public void setSelection(LocalTime start, int hours, boolean available) {
        this.selectionStart = start;
        this.selectionHours = hours;
        this.selectionAvailable = available;
        repaint();
    }

    /** Called with the time of the place where the user clicks. */
    public void setTimeClickedListener(Consumer<LocalTime> listener) {
        this.timeClickedListener = listener;
    }

    /** Color used for each type of period (also used by the legend). */
    public static Color colorOf(TimeSlot.Type type) {
        return switch (type) {
            case FREE -> FREE;
            case TEAM_USE -> TEAM_USE;
            case BLOCKED_AFTER_TEAM -> BLOCKED;
            case RESERVED -> RESERVED;
            case PAST -> PAST;
        };
    }

    /** Small square with the color of a type of period, for the legend. */
    public static Icon legendIcon(TimeSlot.Type type) {
        return new Icon() {
            @Override
            public void paintIcon(Component component, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                Rectangle square = new Rectangle(x, y, 13, 13);
                paintSlot(g2, square, type);
                g2.setColor(Branding.BORDER.darker());
                g2.draw(square);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 14;
            }

            @Override
            public int getIconHeight() {
                return 14;
            }
        };
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        LocalTime time = timeAt(event.getX());
        if (time == null) {
            return null;
        }
        for (TimeSlot slot : timeline) {
            if (slot.contains(time)) {
                return slot.getStart() + " - " + slot.getEnd() + "  " + describe(slot);
            }
        }
        return null;
    }

    private static String describe(TimeSlot slot) {
        return switch (slot.getType()) {
            case FREE -> "Free (click to start the reservation here)";
            case TEAM_USE -> "Used by " + slot.getDescription();
            case BLOCKED_AFTER_TEAM -> "Blocked after the use of " + slot.getDescription();
            case RESERVED -> "Reserved";
            case PAST -> "Already past";
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int barTop = LABELS_HEIGHT;
        Rectangle bar = new Rectangle(SIDE_MARGIN, barTop, barWidth(), BAR_HEIGHT);

        for (TimeSlot slot : timeline) {
            int x1 = xOf(slot.getStart());
            int x2 = xOf(slot.getEnd());
            paintSlot(g2, new Rectangle(x1, barTop, x2 - x1, BAR_HEIGHT), slot.getType());
        }

        // Hour marks and labels.
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        FontMetrics metrics = g2.getFontMetrics();
        for (int hour = FIRST_MINUTE / 60; hour <= LAST_MINUTE / 60; hour++) {
            int x = xOf(LocalTime.of(hour == 24 ? 0 : hour, 0));
            if (hour == LAST_MINUTE / 60) {
                x = bar.x + bar.width;
            }
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawLine(x, barTop, x, barTop + BAR_HEIGHT);
            g2.setColor(Branding.TEXT_MUTED);
            String label = String.valueOf(hour);
            g2.drawString(label, x - metrics.stringWidth(label) / 2, LABELS_HEIGHT - 5);
        }

        g2.setColor(Branding.BORDER.darker());
        g2.draw(bar);

        // Period being booked.
        if (selectionStart != null && selectionHours > 0) {
            int x1 = xOf(selectionStart);
            int x2 = xOf(selectionStart.plusHours(selectionHours), selectionStart);
            Color color = selectionAvailable ? Branding.SUCCESS : Branding.ERROR;
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
            g2.fillRect(x1, barTop - 3, x2 - x1, BAR_HEIGHT + 6);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRect(x1, barTop - 3, x2 - x1, BAR_HEIGHT + 6);
        }
        g2.dispose();
    }

    /** Fills a rectangle with the color of the type; the blocked time is also striped. */
    private static void paintSlot(Graphics2D g2, Rectangle area, TimeSlot.Type type) {
        g2.setColor(colorOf(type));
        g2.fill(area);
        if (type == TimeSlot.Type.BLOCKED_AFTER_TEAM) {
            Shape oldClip = g2.getClip();
            g2.clip(area);
            g2.setColor(Branding.GOLD);
            g2.setStroke(new BasicStroke(1.5f));
            for (int x = area.x - area.height; x < area.x + area.width; x += 7) {
                g2.drawLine(x, area.y + area.height, x + area.height, area.y);
            }
            g2.setClip(oldClip);
        }
    }

    private int barWidth() {
        return Math.max(1, getWidth() - 2 * SIDE_MARGIN);
    }

    private int xOf(LocalTime time) {
        int minute = time.getHour() * 60 + time.getMinute();
        return SIDE_MARGIN + (int) Math.round((minute - FIRST_MINUTE) * (double) barWidth() / (LAST_MINUTE - FIRST_MINUTE));
    }

    /** Like {@link #xOf(LocalTime)} but for an end time that may be past midnight. */
    private int xOf(LocalTime end, LocalTime start) {
        return end.isBefore(start) ? SIDE_MARGIN + barWidth() : xOf(end);
    }

    /** Time of the minute at the horizontal position {@code x}, or null if it is outside the bar. */
    private LocalTime timeAt(int x) {
        if (x < SIDE_MARGIN || x >= SIDE_MARGIN + barWidth()) {
            return null;
        }
        int minute = FIRST_MINUTE + (int) ((x - SIDE_MARGIN) * (double) (LAST_MINUTE - FIRST_MINUTE) / barWidth());
        return LocalTime.of(minute / 60, minute % 60);
    }
}
