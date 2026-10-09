package com.ips2026.pl11.data.slots;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.slots.InterviewSlotRecord;

public class InterviewSlotDAO {

    /**
     * Checks if the player already has an assigned/booked interview on that specific date.
     */
    public boolean hasAssignedInterviewOnDate(int playerId, LocalDate date) throws SQLException {
        String query = "SELECT COUNT(*) FROM interviews i " +
                       "JOIN interview_slots s ON i.slot_id = s.id " +
                       "WHERE s.player_id = ? AND s.slot_date = ? AND i.status != 'CANCELLED'";
        
        try (Connection conn =  ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Checks if the slot conflicts with training sessions or matches for the player's team.
     */
    public boolean hasTeamActivityConflict(int playerId, LocalDate date, LocalTime start, LocalTime end) throws SQLException {
        // Checks match conflicts
        String matchQuery = "SELECT COUNT(*) FROM matches m " +
                            "JOIN team_players tp ON m.team_id = tp.team_id " +
                            "WHERE tp.player_id = ? AND m.match_date = ? " +
                            "AND (? < m.end_time AND ? > m.start_time)";

        // Checks training conflicts
        String trainingQuery = "SELECT COUNT(*) FROM trainings t " +
                               "JOIN team_players tp ON t.team_id = tp.team_id " +
                               "WHERE tp.player_id = ? AND t.training_date = ? " +
                               "AND (? < t.end_time AND ? > t.start_time)";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            try (PreparedStatement psMatch = conn.prepareStatement(matchQuery)) {
                psMatch.setInt(1, playerId);
                psMatch.setString(2, date.toString());
                psMatch.setString(3, start.toString());
                psMatch.setString(4, end.toString());
                try (ResultSet rs = psMatch.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }

            try (PreparedStatement psTraining = conn.prepareStatement(trainingQuery)) {
                psTraining.setInt(1, playerId);
                psTraining.setString(2, date.toString());
                psTraining.setString(3, start.toString());
                psTraining.setString(4, end.toString());
                try (ResultSet rs = psTraining.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if there is already an existing slot for the player that overlaps with the proposed time.
     */
    public boolean hasSlotOverlap(int playerId, LocalDate date, LocalTime start, LocalTime end) throws SQLException {
        String query = "SELECT COUNT(*) FROM interview_slots " +
                       "WHERE player_id = ? AND slot_date = ? AND status != 'CANCELLED' " +
                       "AND (? < end_time AND ? > start_time)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            ps.setString(2, date.toString());
            ps.setString(3, start.toString());
            ps.setString(4, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Inserts the new interview slot.
     */
    public void createSlot(int playerId, int coachId, LocalDate date, LocalTime start, LocalTime end) throws SQLException {
        String insertQuery = "INSERT INTO interview_slots (player_id, coach_id, slot_date, start_time, end_time, status) " +
                             "VALUES (?, ?, ?, ?, ?, 'AVAILABLE')";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(insertQuery)) {
            ps.setInt(1, playerId);
            ps.setInt(2, coachId);
            ps.setString(3, date.toString());
            ps.setString(4, start.toString());
            ps.setString(5, end.toString());
            ps.executeUpdate();
        }
    }
    
    public List<InterviewSlotRecord> getSlotsByPlayer(int playerId) throws SQLException {
        List<InterviewSlotRecord> list = new ArrayList<>();
        String sql = "SELECT s.id, e.first_name || ' ' || e.last_name AS player_name, " +
                     "s.slot_date, s.start_time, s.end_time, s.status " +
                     "FROM interview_slots s " +
                     "JOIN employees e ON s.player_id = e.id " +
                     (playerId > 0 ? "WHERE s.player_id = ? " : "") +
                     "ORDER BY s.slot_date DESC, s.start_time ASC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (playerId > 0) {
                ps.setInt(1, playerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new InterviewSlotRecord(
                            rs.getInt("id"),
                            rs.getString("player_name"),
                            rs.getString("slot_date"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }
}
