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

    public List<Object[]> getActivitiesForDay(int facilityId, String date) throws SQLException {
        List<Object[]> activities = new ArrayList<>();

        String sqlTrainings = "SELECT 'Training' AS type, start_time, end_time " +
                              "FROM trainings WHERE facility_id = ? AND training_date = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sqlTrainings)) {
            stmt.setInt(1, facilityId);
            stmt.setString(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(new Object[]{"Training", rs.getString("start_time"), rs.getString("end_time")});
                }
            }
        }

        String sqlMatches = "SELECT 'Match' AS type, start_time, end_time " +
                            "FROM matches WHERE is_home = 1 AND facility_id = ? AND match_date = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sqlMatches)) {
            stmt.setInt(1, facilityId);
            stmt.setString(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(new Object[]{"Match", rs.getString("start_time"), rs.getString("end_time")});
                }
            }
        }

        String sqlInterviews = "SELECT 'Interview (' || e.first_name || ' ' || e.last_name || ')' AS type, " +
                               "s.start_time, s.end_time " +
                               "FROM interview_slots s " +
                               "JOIN employees e ON s.player_id = e.id " +
                               "WHERE s.slot_date = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sqlInterviews)) {
            stmt.setString(1, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(new Object[]{rs.getString("type"), rs.getString("start_time"), rs.getString("end_time")});
                }
            }
        } catch (SQLException ex) {
        }
        String sqlReservations = "SELECT ('Reservation (' || holder_name || ')') AS type, start_time, end_time " +
                                 "FROM FACILITY_RESERVATION WHERE facility_id = ? AND reservation_date = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sqlReservations)) {
            stmt.setInt(1, facilityId);
            stmt.setString(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(new Object[]{rs.getString("type"), rs.getString("start_time"), rs.getString("end_time")});
                }
            }
        } catch (SQLException ex) {
        }

        String sqlGardening = "SELECT ('Gardening (' || type || ')') AS type, start_time, end_time " +
                              "FROM gardening_session WHERE facility_id = ? AND date = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sqlGardening)) {
            stmt.setInt(1, facilityId);
            stmt.setString(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(new Object[]{rs.getString("type"), rs.getString("start_time"), rs.getString("end_time")});
                }
            }
        } catch (SQLException ex) {
        }

        return activities;
    }

    /**
     * Schedules home match, validates training conflicts, and auto-cancels overlapping events:
     * facility reservations, gardening sessions, and interview slots/interviews.
     */
    public int createHomeMatchWithConflictsCleared(int teamId, String rival, String date, 
                                                  String startTime, String endTime, 
                                                  boolean isSpecial, int facilityId) throws SQLException {
        
        String sqlCheckTraining = "SELECT COUNT(*) FROM trainings " +
                                  "WHERE facility_id = ? AND training_date = ? " +
                                  "AND ((start_time <= ? AND end_time > ?) OR (start_time < ? AND end_time >= ?))";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sqlCheckTraining)) {
            stmt.setInt(1, facilityId);
            stmt.setString(2, date);
            stmt.setString(3, startTime);
            stmt.setString(4, startTime);
            stmt.setString(5, endTime);
            stmt.setString(6, endTime);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalStateException("Cannot create a match at the same time as an official team training session.");
                }
            }
        }

        String sqlMatch = "INSERT INTO matches (team_id, rival, match_date, start_time, end_time, is_home, is_special_match, facility_id) " +
                          "VALUES (?, ?, ?, ?, ?, 1, ?, ?)";
        
        String sqlCancelReservations = "DELETE FROM FACILITY_RESERVATION " +
                "WHERE facility_id = ? AND reservation_date = ? " +
                "AND ((start_time <= ? AND end_time > ?) OR (start_time < ? AND end_time >= ?))";

        String sqlCancelGardening = "DELETE FROM gardening_session " +
                "WHERE facility_id = ? AND date = ? " +
                "AND ((start_time <= ? AND end_time > ?) OR (start_time < ? AND end_time >= ?))";

        String sqlCancelInterviews = "DELETE FROM interviews WHERE slot_id IN (" +
                "SELECT id FROM interview_slots WHERE slot_date = ? " +
                "AND ((start_time <= ? AND end_time > ?) OR (start_time < ? AND end_time >= ?)))";

        String sqlCancelInterviewSlots = "DELETE FROM interview_slots " +
                "WHERE slot_date = ? " +
                "AND ((start_time <= ? AND end_time > ?) OR (start_time < ? AND end_time >= ?))";

        int matchId = -1;

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);

            try {
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

                try (PreparedStatement pstmt = conn.prepareStatement(sqlCancelReservations)) {
                    pstmt.setInt(1, facilityId);
                    pstmt.setString(2, date);
                    pstmt.setString(3, startTime);
                    pstmt.setString(4, startTime);
                    pstmt.setString(5, endTime);
                    pstmt.setString(6, endTime);
                    pstmt.executeUpdate();
                } catch (SQLException ex) { 
                	
                }

                try (PreparedStatement pstmt = conn.prepareStatement(sqlCancelGardening)) {
                    pstmt.setInt(1, facilityId);
                    pstmt.setString(2, date);
                    pstmt.setString(3, startTime);
                    pstmt.setString(4, startTime);
                    pstmt.setString(5, endTime);
                    pstmt.setString(6, endTime);
                    pstmt.executeUpdate();
                } catch (SQLException ex) { 
                	
                }

                try (PreparedStatement pstmt = conn.prepareStatement(sqlCancelInterviews)) {
                    pstmt.setString(1, date);
                    pstmt.setString(2, startTime);
                    pstmt.setString(3, startTime);
                    pstmt.setString(4, endTime);
                    pstmt.setString(5, endTime);
                    pstmt.executeUpdate();
                } catch (SQLException ex) { 
                	
                }

                try (PreparedStatement pstmt = conn.prepareStatement(sqlCancelInterviewSlots)) {
                    pstmt.setString(1, date);
                    pstmt.setString(2, startTime);
                    pstmt.setString(3, startTime);
                    pstmt.setString(4, endTime);
                    pstmt.setString(5, endTime);
                    pstmt.executeUpdate();
                } catch (SQLException ex) { 
                	
                }

                conn.commit();
                return matchId;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }
}