package com.ips2026.pl11.view.ticket;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

import com.ips2026.pl11.model.ticket.SeatStatus;

/**
 * Represents a visual seat cell in the stadium grid panel (US 36757).
 * Displays green ('A') for available seats, yellow/orange ('R') for member reserved seats in special matches,
 * and red ('O') for occupied seats.
 */
public class SeatCell extends JButton {

    private final int rowNum;
    private final int seatNum;
    private SeatStatus status;

    public SeatCell(int rowNum, int seatNum) {
        this.rowNum = rowNum;
        this.seatNum = seatNum;
        this.status = SeatStatus.AVAILABLE;

        setFont(new Font("SansSerif", Font.BOLD, 12));
        setFocusable(false);
        setFocusPainted(false);
        setBorderPainted(true);
        setOpaque(true);

        // Consume mouse clicks to make the button non-interactive without graying out the colors
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

    public SeatStatus getStatus() {
        return status;
    }

    /**
     * Updates the seat cell visual appearance, text code, and tooltips based on SeatStatus enum.
     * 
     * @param status Target status (AVAILABLE, SEASON_PASS_RESERVED, OCCUPIED)
     */
    public void setStatus(SeatStatus status) {
        this.status = (status != null) ? status : SeatStatus.AVAILABLE;
        updateVisualStatus();
    }

    /**
     * Legacy support method for binary occupancy flags.
     * 
     * @param occupied true = OCCUPIED ('O', Red), false = AVAILABLE ('A', Green)
     */
    public void setOccupied(boolean occupied) {
        this.status = occupied ? SeatStatus.OCCUPIED : SeatStatus.AVAILABLE;
        updateVisualStatus();
    }

    public boolean isOccupied() {
        return status == SeatStatus.OCCUPIED || status == SeatStatus.SEASON_PASS_RESERVED;
    }

    /**
     * Updates background color, letter indicator, and tooltip depending on SeatStatus.
     */
    private void updateVisualStatus() {
        if (status == null) {
            status = SeatStatus.AVAILABLE;
        }

        switch (status) {
            case AVAILABLE -> {
                setBackground(status.getColor()); // Green
                setForeground(Color.WHITE);
                setText("A");
                setToolTipText(String.format("Row %d, Seat %d: Available for general public", rowNum, seatNum));
            }
            case SEASON_PASS_RESERVED -> {
                setBackground(status.getColor()); // Yellow/Orange
                setForeground(Color.BLACK);
                setText("R");
                setToolTipText(String.format("Row %d, Seat %d: Reserved for Season Pass Member (Special Match)", rowNum, seatNum));
            }
            case OCCUPIED -> {
                setBackground(status.getColor()); // Red
                setForeground(Color.WHITE);
                setText("O");
                setToolTipText(String.format("Row %d, Seat %d: Occupied / Sold", rowNum, seatNum));
            }
        }
        repaint();
    }
}