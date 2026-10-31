package com.ips2026.pl11.data.ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.data.matches.MatchPriceDAO;
import com.ips2026.pl11.model.ticket.MatchTicket;

public class MatchTicketDAO {

    private MatchPriceDAO matchPriceDAO = new MatchPriceDAO();

    /**
     * Finds and reserves a contiguous block of requested seats in the same row.
     * Uses dynamic ticket pricing from MATCH_PRICE table and returns a list of MatchTicket models.
     */
    public List<MatchTicket> bookContiguousSeats(int matchId, String grandstand, String section, int quantity) throws SQLException {
        if (quantity < 1 || quantity > 15) {
            throw new IllegalArgumentException("You can only request between 1 and 15 seats.");
        }

        boolean[][] occupied = new boolean[11][16];
        
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
                    
                    if (rowIndex >= 1 && rowIndex <= 10 && colIndex >= 1 && colIndex <= 15) {
                        occupied[rowIndex][colIndex] = true;
                    }
                }
            }
        }

        List<Integer> validRows = new ArrayList<>();
        List<Integer> startingSeats = new ArrayList<>();

        for (int rowIndex = 1; rowIndex <= 10; rowIndex++) {
            int consecutiveFree = 0;
            for (int colIndex = 1; colIndex <= 15; colIndex++) {
                if (!occupied[rowIndex][colIndex]) {
                    consecutiveFree++;
                    
                    if (consecutiveFree == quantity) {
                        int startSeat = colIndex - quantity + 1;
                        validRows.add(rowIndex);
                        startingSeats.add(startSeat);
                        break;
                    }
                } else {
                    consecutiveFree = 0;
                }
            }
        }

        if (validRows.isEmpty()) {
            return Collections.emptyList();
        }

        int randomIndex = (int) (Math.random() * validRows.size());
        int chosenRow = validRows.get(randomIndex);
        int startSeat = startingSeats.get(randomIndex);

        // Fetch dynamic pricing for this match and section
        double dynamicPrice = matchPriceDAO.getPriceForMatch(matchId, grandstand, section);

        List<MatchTicket> purchasedTickets = new ArrayList<>();
        String nextNumberSql = "SELECT COALESCE(MAX(purchase_number), 0) + 1 FROM MATCH_TICKET";
        String insertSql = "INSERT INTO MATCH_TICKET (purchase_number, match_id, grandstand, section, row_num, seat_num, price) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement nextNumber = conn.prepareStatement(nextNumberSql);
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            int purchaseNumber;
            try (ResultSet rs = nextNumber.executeQuery()) {
                rs.next();
                purchaseNumber = rs.getInt(1);
            }

            for (int i = 0; i < quantity; i++) {
                int seatNum = startSeat + i;
                pstmt.setInt(1, purchaseNumber);
                pstmt.setInt(2, matchId);
                pstmt.setString(3, grandstand.trim());
                pstmt.setString(4, section.trim());
                pstmt.setInt(5, chosenRow);
                pstmt.setInt(6, seatNum);
                pstmt.setDouble(7, dynamicPrice);
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int ticketId = generatedKeys.getInt(1);
                        purchasedTickets.add(new MatchTicket(ticketId, matchId, grandstand, section, chosenRow, seatNum, dynamicPrice));
                    }
                }
            }
        }

        return purchasedTickets;
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