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
     * Checks if a specific seat is already registered under an active Season Pass for a team in the given season.
     */
    public boolean isSeatPassOccupied(int teamId, String grandstand, String section, 
                                     int rowNum, int seatNum, String seasonStartDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM SEASON_PASS " +
                     "WHERE team_id = ? AND grandstand = ? AND section = ? " +
                     "AND row_num = ? AND seat_num = ? AND start_date = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teamId);
            stmt.setString(2, grandstand);
            stmt.setString(3, section);
            stmt.setInt(4, rowNum);
            stmt.setInt(5, seatNum);
            stmt.setString(6, seasonStartDate);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Inserts a new Season Pass record into the database.
     */
    public boolean createSeasonPass(int fanId, int teamId, String grandstand, String section, 
                                    int rowNum, int seatNum, String ageCategory, double price, 
                                    String startDate, String endDate) throws SQLException {
        String sql = "INSERT INTO SEASON_PASS (fan_id, team_id, grandstand, section, row_num, seat_num, age_category, price, start_date, end_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fanId);
            stmt.setInt(2, teamId);
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
     * Retrieves occupied seat coordinates for active season passes associated with a team name.
     */
    public List<int[]> getActivePassSeatsForMatch(String teamName, String grandstand, String section, String matchDate) throws SQLException {
        List<int[]> reservedSeats = new ArrayList<>();

        String sql = "SELECT row_num, seat_num FROM SEASON_PASS " +
                     "WHERE team_name = ? AND grandstand = ? AND section = ? " +
                     "AND ? BETWEEN start_date AND end_date";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, teamName);
            stmt.setString(2, grandstand);
            stmt.setString(3, section);
            stmt.setString(4, matchDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Convert 1-based DB index to 0-based array index
                    int rowIndex = rs.getInt("row_num") - 1;
                    int colIndex = rs.getInt("seat_num") - 1;
                    reservedSeats.add(new int[]{rowIndex, colIndex});
                }
            }
        }
        return reservedSeats;
    }
}
