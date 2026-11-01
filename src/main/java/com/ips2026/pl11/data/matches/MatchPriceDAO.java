package com.ips2026.pl11.data.matches;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ips2026.pl11.data.ConexionBD;

public class MatchPriceDAO {

    /**
     * Saves category-based pricing per grandstand and section for a scheduled match.
     */
    public void saveMatchPrice(int matchId, String grandstand, String section, double price) throws SQLException {
        String sql = "INSERT INTO MATCH_PRICE (match_id, grandstand, section, price) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, matchId);
            pstmt.setString(2, grandstand.trim());
            pstmt.setString(3, section.trim());
            pstmt.setDouble(4, price);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves the specific ticket price for a given match, grandstand, and section.
     * Falls back to a default rate of 30.0 if no price was defined.
     */
    public double getPriceForMatch(int matchId, String grandstand, String section) throws SQLException {
        String sql = "SELECT price FROM MATCH_PRICE " +
                     "WHERE match_id = ? AND UPPER(TRIM(grandstand)) = UPPER(TRIM(?)) " +
                     "AND UPPER(TRIM(section)) = UPPER(TRIM(?))";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, matchId);
            pstmt.setString(2, grandstand);
            pstmt.setString(3, section);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("price");
                }
            }
        }
        return 30.0; // Fallback default price
    }
}