package com.ips2026.pl11.view.ticket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Represents a non-clickable visual seat cell in the stadium grid panel.
 * Displays green ('A') for available seats and red ('O') for occupied seats.
 */
public class SeatCell extends JButton {

    private final int rowNum;
    private final int seatNum;
    private boolean occupied;

    public SeatCell(int rowNum, int seatNum) {
        this.rowNum = rowNum;
        this.seatNum = seatNum;
        this.occupied = false;

        setFont(new Font("SansSerif", Font.BOLD, 12));
        setFocusable(false);
        setFocusPainted(false);
        setBorderPainted(true);

        // Consume mouse clicks to make the button non-interactive without graying it out
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                e.consume();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                e.consume();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }
        });

        updateVisualStatus();
    }

    public int getRowNum() {
        return rowNum;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
        updateVisualStatus();
    }

    /**
     * Updates background color and text based on seat occupancy.
     */
    private void updateVisualStatus() {
        if (occupied) {
            setBackground(new Color(220, 53, 69)); // Red for occupied
            setForeground(Color.WHITE);
            setText("O");
        } else {
            setBackground(new Color(40, 167, 69)); // Green for available
            setForeground(Color.WHITE);
            setText("A");
        }
        repaint();
    }
}