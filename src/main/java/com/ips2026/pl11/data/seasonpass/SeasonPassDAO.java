package com.ips2026.pl11.data.seasonpass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;

/**
 * Data Access Object for handling Season Pass transactions and seat allocations matching the SEASON_PASS table.
 */
public class SeasonPassDAO {

	/**
     * Retrieves occupied seats joining with TEAM table using team name.
     */
    public List<int[]> getActivePassSeatsForMatch(String teamName, String grandstand, String section, String matchDate) throws SQLException {
        List<int[]> reservedSeats = new ArrayList<>();

        String sql = "SELECT sp.row_num, sp.seat_num FROM SEASON_PASS sp " +
                     "JOIN TEAM t ON sp.team_id = t.id " +
                     "WHERE t.name = ? AND sp.grandstand = ? AND sp.section = ? " +
                     "AND ? BETWEEN sp.start_date AND sp.end_date";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, teamName);
            stmt.setString(2, grandstand);
            stmt.setString(3, section);
            stmt.setString(4, matchDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int rowIndex = rs.getInt("row_num") - 1;
                    int colIndex = rs.getInt("seat_num") - 1;
                    reservedSeats.add(new int[]{rowIndex, colIndex});
                }
            }
        }
        return reservedSeats;
    }

    /**
     * Checks if a seat is occupied using team name lookup.
     */
    public boolean isSeatPassOccupiedByName(String teamName, String grandstand, String section, 
                                            int rowNum, int seatNum, String startDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM SEASON_PASS " +
                     "WHERE team_id = (SELECT id FROM TEAM WHERE name = ?) " +
                     "AND grandstand = ? AND section = ? " +
                     "AND row_num = ? AND seat_num = ? AND start_date = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, teamName);
            stmt.setString(2, grandstand);
            stmt.setString(3, section);
            stmt.setInt(4, rowNum);
            stmt.setInt(5, seatNum);
            stmt.setString(6, startDate);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Inserts a season pass mapping fan details (or DNI) to fan_id and teamName to team_id.
     */
    public boolean createSeasonPassWithHolder(String dni, String fullName, String email, String teamName, 
                                              String grandstand, String section, int rowNum, int seatNum, 
                                              String ageCategory, double price, String startDate, String endDate) throws SQLException {
        
        // 1. Get or create fan_id from FAN table using DNI/Holder details
        int fanId = getOrCreateFanId(dni, fullName, email);

        // 2. Insert into SEASON_PASS using fan_id and resolved team_id
        String sql = "INSERT INTO SEASON_PASS (fan_id, team_id, grandstand, section, row_num, seat_num, age_category, price, start_date, end_date) " +
                     "VALUES (?, (SELECT id FROM TEAM WHERE name = ?), ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fanId);
            stmt.setString(2, teamName);
            stmt.setString(3, grandstand);
            stmt.setString(4, section);
            stmt.setInt(5, rowNum);
            stmt.setInt(6, seatNum);
            stmt.setString(7, ageCategory);
            stmt.setDouble(8, price);
            stmt.setString(9, startDate);
            stmt.setString(10, endDate);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Helper method to retrieve existing fan ID or insert new fan record.
     */
    private int getOrCreateFanId(String dni, String fullName, String email) throws SQLException {
        String selectSql = "SELECT id FROM FAN WHERE dni = ?";
        
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            
            stmt.setString(1, dni);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Insert new fan if not exists
        String insertSql = "INSERT INTO FAN (dni, full_name, email) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, dni);
            stmt.setString(2, fullName);
            stmt.setString(3, email);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve or create Fan record.");
    }
}
