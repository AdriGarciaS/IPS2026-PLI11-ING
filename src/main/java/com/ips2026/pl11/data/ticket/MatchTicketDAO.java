package com.ips2026.pl11.data.ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;

public class MatchTicketDAO {

    /**
     * Finds and reserves a contiguous block of requested seats in the same row.
     * Ensures that previously sold seats are subtracted from availability.
     */
    public List<int[]> bookContiguousSeats(int matchId, String grandstand, String section, int quantity) throws SQLException {
        if (quantity < 1 || quantity > 15) {
            throw new IllegalArgumentException("You can only request between 1 and 15 seats.");
        }

        // 1. Matrix to track occupied seats (10 rows x 15 seats, 1-indexed for readability)
        boolean[][] occupied = new boolean[11][16];
        
        // Fetch already sold tickets for this specific match, grandstand, and section.
        // Using UPPER and TRIM to avoid string formatting discrepancies in SQLite.
        String sqlOccupied = "SELECT row_num, seat_num FROM MATCH_TICKET " +
                             "WHERE match_id = ? AND UPPER(TRIM(grandstand)) = UPPER(TRIM(?)) " +
                             "AND UPPER(TRIM(section)) = UPPER(TRIM(?))";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sqlOccupied)) {
            
            pstmt.setInt(1, matchId);
            pstmt.setString(2, grandstand);
            pstmt.setString(3, section);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int rowIndex = rs.getInt("row_num");
                    int colIndex = rs.getInt("seat_num");
                    
                    // Mark seat as occupied if within valid bounds
                    if (rowIndex >= 1 && rowIndex <= 10 && colIndex >= 1 && colIndex <= 15) {
                        occupied[rowIndex][colIndex] = true;
                    }
                }
            }
        }

        // 2. Scan all rows to identify candidate rows with sufficient contiguous free seats
        List<Integer> validRows = new ArrayList<>();
        List<Integer> startingSeats = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= 10; rowIndex++) {
            int consecutiveFree = 0;
            for (int colIndex = 1; colIndex <= 15; colIndex++) {
                if (!occupied[rowIndex][colIndex]) {
                    consecutiveFree++;
                    
                    // A valid contiguous block of the requested size has been found
                    if (consecutiveFree == quantity) {
                        int startSeat = colIndex - quantity + 1;
                        validRows.add(rowIndex);
                        startingSeats.add(startSeat);
                        break; // Stop searching this row and proceed to the next one
                    }
                } else {
                    // Reset streak if an occupied seat is encountered
                    consecutiveFree = 0;
                }
            }
        }

        // Return empty list if no row satisfies the user's contiguous seat requirement
        if (validRows.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Randomly select one eligible row from the available candidates
        int randomIndex = (int) (Math.random() * validRows.size());
        int chosenRow = validRows.get(randomIndex);
        int startSeat = startingSeats.get(randomIndex);

        // 4. Batch-insert the newly reserved seats into the MATCH_TICKET table
        List<int[]> assignedSeats = new ArrayList<>();
        String insertSql = "INSERT INTO MATCH_TICKET (match_id, grandstand, section, row_num, seat_num, price) " +
                           "VALUES (?, ?, ?, ?, ?, 30.0)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            
            for (int i = 0; i < quantity; i++) {
                int seatNum = startSeat + i;
                pstmt.setInt(1, matchId);
                pstmt.setString(2, grandstand.trim());
                pstmt.setString(3, section.trim());
                pstmt.setInt(4, chosenRow);
                pstmt.setInt(5, seatNum);
                pstmt.addBatch();

                assignedSeats.add(new int[]{chosenRow, seatNum});
            }
            pstmt.executeBatch();
        }

        return assignedSeats;
    }
    
    /**
     * Retrieves a 10x15 matrix representing occupied seats sold specifically for this match.
     */
    public boolean[][] getOccupiedSeats(int matchId, String grandstand, String section) throws SQLException {
    	boolean[][] grid = new boolean[10][15];

        String sql = "SELECT row_num, seat_num FROM MATCH_TICKET " +
                     "WHERE match_id = ? AND grandstand = ? AND section = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, matchId);
            stmt.setString(2, grandstand);
            stmt.setString(3, section);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Convert 1-based database index to 0-based Java array index
                    int rowIndex = rs.getInt("row_num") - 1;
                    int colIndex = rs.getInt("seat_num") - 1;

                    if (rowIndex >= 0 && rowIndex < 10 && colIndex >= 0 && colIndex < 15) {
                        grid[rowIndex][colIndex] = true;
                    }
                }
            }
        }
        return grid;
    }
}