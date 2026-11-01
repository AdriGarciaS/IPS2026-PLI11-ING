package com.ips2026.pl11.view.ticket;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Visual-only seating grid panel representing a 10x15 stadium section.
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
                "Seat Map (Green = Available 'A', Red = Occupied 'O')",
                TitledBorder.CENTER,
                TitledBorder.TOP
        ));

        // Initialize 10x15 non-interactive seat cells
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                // 1-based indexing for row and seat numbers matching database
                SeatCell cell = new SeatCell(r + 1, c + 1);
                board[r][c] = cell;
                add(cell);
            }
        }
    }

    /**
     * Updates the board grid status according to the boolean matrix from the database.
     * 
     * @param occupiedMatrix Matrix where true = occupied (Red) and false = available (Green).
     */
    public void updateBoard(boolean[][] occupiedMatrix) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                boolean isOccupied = occupiedMatrix[r][c];
                board[r][c].setOccupied(isOccupied);
            }
        }
        revalidate();
        repaint();
    }
}