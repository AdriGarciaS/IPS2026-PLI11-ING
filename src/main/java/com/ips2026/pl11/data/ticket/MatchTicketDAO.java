package com.ips2026.pl11.data.ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;

/**
 * Data Access Object for handling MATCH_TICKET records and seat allocations.
 */
public class MatchTicketDAO {

    /**
     * Retrieves a 10x15 matrix grid representing occupied seats sold for a specific match, grandstand, and section.
     */
    public boolean[][] getOccupiedSeats(int matchId, String grandstand, String section) throws SQLException {
        boolean[][] grid = new boolean[10][15];
        String sql = "SELECT row_num, seat_num FROM MATCH_TICKET " +
                     "WHERE match_id = ? AND UPPER(TRIM(grandstand)) = UPPER(TRIM(?)) " +
                     "AND UPPER(TRIM(section)) = UPPER(TRIM(?))";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, matchId);
            stmt.setString(2, grandstand);
            stmt.setString(3, section);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int r = rs.getInt("row_num") - 1;
                    int c = rs.getInt("seat_num") - 1;
                    if (r >= 0 && r < 10 && c >= 0 && c < 15) {
                        grid[r][c] = true;
                    }
                }
            }
        }
        return grid;
    }

    /**
     * Books a contiguous block of seats in a specific row and section (US 35082).
     * Shares a single generated purchase_number across all tickets in the transaction.
     */
    public List<int[]> bookContiguousSeats(int matchId, String grandstand, String section, 
                                          int rowNum, int startSeatNum, int quantity, double price) throws SQLException {
        List<int[]> bookedSeats = new ArrayList<>();

        String nextNumberSql = "SELECT COALESCE(MAX(purchase_number), 0) + 1 FROM MATCH_TICKET";
        String insertSql = "INSERT INTO MATCH_TICKET (purchase_number, match_id, grandstand, section, row_num, seat_num, price) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);

            try {
                int purchaseNumber;
                try (PreparedStatement nextStmt = conn.prepareStatement(nextNumberSql);
                     ResultSet rs = nextStmt.executeQuery()) {
                    rs.next();
                    purchaseNumber = rs.getInt(1);
                }

                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (int i = 0; i < quantity; i++) {
                        int currentSeat = startSeatNum + i;

                        pstmt.setInt(1, purchaseNumber);
                        pstmt.setInt(2, matchId);
                        pstmt.setString(3, grandstand.trim());
                        pstmt.setString(4, section.trim());
                        pstmt.setInt(5, rowNum);
                        pstmt.setInt(6, currentSeat);
                        pstmt.setDouble(7, price);
                        pstmt.addBatch();

                        bookedSeats.add(new int[]{rowNum, currentSeat});
                    }
                    pstmt.executeBatch();
                }

                conn.commit();
                return bookedSeats;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }
    
    public boolean bookSpecificSeat(int matchId, String grandstand, String section, int rowNum, int seatNum, double price) throws SQLException {
        String nextNumberSql = "SELECT COALESCE(MAX(purchase_number), 0) + 1 FROM MATCH_TICKET";
        String insertSql = "INSERT INTO MATCH_TICKET (purchase_number, match_id, grandstand, section, row_num, seat_num, price) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement nextNumber = conn.prepareStatement(nextNumberSql);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            int purchaseNumber;
            try (ResultSet rs = nextNumber.executeQuery()) {
                rs.next();
                purchaseNumber = rs.getInt(1);
            }

            pstmt.setInt(1, purchaseNumber);
            pstmt.setInt(2, matchId);
            pstmt.setString(3, grandstand.trim());
            pstmt.setString(4, section.trim());
            pstmt.setInt(5, rowNum);
            pstmt.setInt(6, seatNum);
            pstmt.setDouble(7, price);

            return pstmt.executeUpdate() > 0;
        }
    }
    
    public void initializeMatchTicketsWithPrice(int matchId, String grandstand, String section, double price) throws SQLException {
        String sql = "INSERT INTO MATCH_TICKET (purchase_number, match_id, grandstand, section, row_num, seat_num, price) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int purchaseNumber = 1;

            // Iterate over standard stadium layout (10 rows x 15 seats)
            for (int r = 1; r <= 10; r++) {
                for (int c = 1; c <= 15; c++) {
                    pstmt.setInt(1, purchaseNumber++);
                    pstmt.setInt(2, matchId);
                    pstmt.setString(3, grandstand);
                    pstmt.setString(4, section);
                    pstmt.setInt(5, r);
                    pstmt.setInt(6, c);
                    pstmt.setDouble(7, price);
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        }
    }
}