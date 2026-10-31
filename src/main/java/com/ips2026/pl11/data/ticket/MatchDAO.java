package com.ips2026.pl11.data.ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.ticket.HomeMatch;

public class MatchDAO {

    /**
     * Retrieves only matches played as home team (is_home = 1).
     */
    public List<HomeMatch> getHomeMatches() throws SQLException {
        List<HomeMatch> matches = new ArrayList<>();
        String sql = "SELECT m.id, t.name AS team_name, m.rival, m.match_date, m.start_time, m.is_special_match " +
                     "FROM matches m " +
                     "JOIN TEAM t ON m.team_id = t.id " +
                     "WHERE m.is_home = 1 " +
                     "ORDER BY m.match_date ASC, m.start_time ASC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                matches.add(new HomeMatch(
                        rs.getInt("id"),
                        rs.getString("team_name"),
                        rs.getString("rival"),
                        rs.getString("match_date"),
                        rs.getString("start_time"),
                        rs.getBoolean("is_special_match")
                ));
            }
        }
        return matches;
    }

    /**
     * Retrieves a single home match by its unique identifier.
     */
    public HomeMatch getMatchById(int matchId) throws SQLException {
        String sql = "SELECT m.id, t.name AS team_name, m.rival, m.match_date, m.start_time, m.is_special_match " +
                     "FROM matches m " +
                     "JOIN TEAM t ON m.team_id = t.id " +
                     "WHERE m.id = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, matchId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new HomeMatch(
                            rs.getInt("id"),
                            rs.getString("team_name"),
                            rs.getString("rival"),
                            rs.getString("match_date"),
                            rs.getString("start_time"),
                            rs.getBoolean("is_special_match")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Inserts a new home match and automatically clears facility conflicts (external rentals and team practices).
     * US 35775 Requirement.
     */
    public int createHomeMatchWithConflictsCleared(int teamId, String rival, String date, 
                                                  String startTime, String endTime, 
                                                  boolean isSpecial, int facilityId) throws SQLException {
        
        String sqlMatch = "INSERT INTO matches (team_id, rival, match_date, start_time, end_time, is_home, is_special_match, facility_id) " +
                          "VALUES (?, ?, ?, ?, ?, 1, ?, ?)";
        
        String sqlCancelReservations = "DELETE FROM FACILITY_RESERVATION " +
                "WHERE facility_id = ? AND reservation_date = ? " +
                "AND ((start_time <= ? AND end_time > ?) OR (start_time < ? AND end_time >= ?))";

        String sqlCancelTrainings = "DELETE FROM TRAININGS WHERE facility_id = ? AND training_date = ?";

        int matchId = -1;

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false); // Begin Transaction

            try {
                // 1. Insert Match and retrieve generated ID
                try (PreparedStatement pstmt = conn.prepareStatement(sqlMatch, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, teamId);
                    pstmt.setString(2, rival);
                    pstmt.setString(3, date);
                    pstmt.setString(4, startTime);
                    pstmt.setString(5, endTime);
                    pstmt.setInt(6, isSpecial ? 1 : 0);
                    pstmt.setInt(7, facilityId);
                    pstmt.executeUpdate();

                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            matchId = rs.getInt(1);
                        }
                    }
                }

                // 2. Clear external facility reservations in conflict
                try (PreparedStatement pstmt = conn.prepareStatement(sqlCancelReservations)) {
                    pstmt.setInt(1, facilityId);
                    pstmt.setString(2, date);
                    pstmt.setString(3, startTime);
                    pstmt.setString(4, startTime);
                    pstmt.setString(5, endTime);
                    pstmt.setString(6, endTime);
                    pstmt.executeUpdate();
                }

                // 3. Clear team practice sessions in conflict
                try (PreparedStatement pstmt = conn.prepareStatement(sqlCancelTrainings)) {
                    pstmt.setInt(1, facilityId);
                    pstmt.setString(2, date);
                    pstmt.executeUpdate();
                }

                conn.commit(); // Commit Transaction
                return matchId;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }
}