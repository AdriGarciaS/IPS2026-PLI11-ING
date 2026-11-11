package com.ips2026.pl11.view.ticket;

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.ips2026.pl11.model.ticket.SeatStatus;

/**
 * Visual seating grid panel representing a 10x15 stadium section (US 36757).
 * Displays seat status in real-time:
 * - Green (AVAILABLE): Open for general public sale.
 * - Yellow/Orange (SEASON_PASS_RESERVED): Member seat locked for Special Matches.
 * - Red (OCCUPIED): Ticket sold or regular season pass lock.
 */
public class StandBoardPanel extends JPanel {

    private final int ROWS = 10;
    private final int COLS = 15;
    private final SeatCell[][] board;

    public StandBoardPanel() {
        this.board = new SeatCell[ROWS][COLS];
        initBoard();
    }

    private void initBoard() {
        setLayout(new GridLayout(ROWS, COLS, 4, 4));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Seat Map (Green = Available, Yellow = Member Reserved, Red = Occupied)",
                TitledBorder.CENTER,
                TitledBorder.TOP
        ));

        // Initialize 10x15 non-interactive seat cells matching 1-based database seat numbers
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                SeatCell cell = new SeatCell(r + 1, c + 1);
                board[r][c] = cell;
                add(cell);
            }
        }
    }

    /**
     * Updates the board grid visually based on the 10x15 SeatStatus enum matrix.
     * 
     * @param statusMatrix 10x15 matrix containing SeatStatus values (AVAILABLE, SEASON_PASS_RESERVED, OCCUPIED).
     */
    public void updateBoard(SeatStatus[][] statusMatrix) {
        if (statusMatrix == null) return;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                SeatStatus status = statusMatrix[r][c];
                board[r][c].setStatus(status);
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Legacy support: Updates the board using a binary boolean matrix.
     * 
     * @param occupiedMatrix Matrix where true = occupied (Red) and false = available (Green).
     */
    public void updateBoard(boolean[][] occupiedMatrix) {
        if (occupiedMatrix == null) return;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                boolean isOccupied = occupiedMatrix[r][c];
                board[r][c].setStatus(isOccupied ? SeatStatus.OCCUPIED : SeatStatus.AVAILABLE);
            }
        }
        revalidate();
        repaint();
    }
}