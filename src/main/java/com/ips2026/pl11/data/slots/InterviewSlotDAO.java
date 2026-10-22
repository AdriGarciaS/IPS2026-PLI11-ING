package com.ips2026.pl11.data.slots;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.slots.InterviewSlotRecord;
import com.ips2026.pl11.model.slots.TeamActivityRecord;

public class InterviewSlotDAO {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Checks if the player already has an assigned/booked interview on that date.
     * Evaluates against both booked interview_slots and the interviews table.
     */
    public boolean hasAssignedInterviewOnDate(int playerId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM interview_slots " +
                     "WHERE player_id = ? AND slot_date = ? AND status <> 'AVAILABLE'";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ps.setString(2, date.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the proposed slot overlaps with matches or scheduled training sessions.
     */
    public boolean hasTeamActivityConflict(int playerId, LocalDate date, LocalTime start, LocalTime end) throws SQLException {
        String dateStr = date.toString();
        String startStr = start.format(TIME_FMT);
        String endStr = end.format(TIME_FMT);

        // 1. Conflict with matches
        String sqlMatches = "SELECT COUNT(*) FROM matches " +
                            "WHERE (team_id IN (SELECT team_id FROM TEAM_MEMBER WHERE employee_id = ? AND role = 'PLAYER') OR team_id IS NULL) AND match_date = ? " +
                            "AND NOT (? <= start_time OR ? >= end_time)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sqlMatches)) {

            ps.setInt(1, playerId);
            ps.setString(2, dateStr);
            ps.setString(3, endStr);
            ps.setString(4, startStr);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }

        // 2. Conflict with trainings
        String sqlTrainings = "SELECT COUNT(*) FROM trainings " +
                              "WHERE (team_id IN (SELECT team_id FROM TEAM_MEMBER WHERE employee_id = ? AND role = 'PLAYER') OR team_id IS NULL) AND training_date = ? " +
                              "AND NOT (? <= start_time OR ? >= end_time)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sqlTrainings)) {

            ps.setInt(1, playerId);
            ps.setString(2, dateStr);
            ps.setString(3, endStr);
            ps.setString(4, startStr);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the player already has another slot overlapping this interval.
     */
    public boolean hasSlotOverlap(int playerId, LocalDate date, LocalTime start, LocalTime end) throws SQLException {
        String sql = "SELECT COUNT(*) FROM interview_slots " +
                     "WHERE player_id = ? AND slot_date = ? " +
                     "AND NOT (? <= start_time OR ? >= end_time)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ps.setString(2, date.toString());
            ps.setString(3, end.format(TIME_FMT));
            ps.setString(4, start.format(TIME_FMT));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Inserts a newly created interview slot into interview_slots.
     */
    public void createSlot(int playerId, int coachId, LocalDate date, LocalTime start, LocalTime end) throws SQLException {
        String sql = "INSERT INTO interview_slots (player_id, coach_id, slot_date, start_time, end_time, status) " +
                     "VALUES (?, ?, ?, ?, ?, 'AVAILABLE')";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ps.setInt(2, coachId);
            ps.setString(3, date.toString());
            ps.setString(4, start.format(TIME_FMT));
            ps.setString(5, end.format(TIME_FMT));

            ps.executeUpdate();
        }
    }

    /**
     * Retrieves all interview slots.
     */
    public List<InterviewSlotRecord> getAllSlots() throws SQLException {
        String sql = "SELECT s.id, (e.first_name || ' ' || e.last_name) AS player_name, " +
                     "s.slot_date, s.start_time, s.end_time, s.status " +
                     "FROM interview_slots s " +
                     "JOIN employees e ON s.player_id = e.id " +
                     "ORDER BY s.slot_date DESC, s.start_time ASC";

        List<InterviewSlotRecord> slots = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String playerName = rs.getString("player_name");
                LocalDate date = LocalDate.parse(rs.getString("slot_date"));
                LocalTime start = LocalTime.parse(rs.getString("start_time"));
                LocalTime end = LocalTime.parse(rs.getString("end_time"));
                String status = rs.getString("status");

                slots.add(new InterviewSlotRecord(id, playerName, date, start, end, status));
            }
        }
        return slots;
    }

    /**
     * Retrieves all coaches from the employees table.
     */
    public List<Employee> getAllCoaches() throws SQLException {
        String sql = "SELECT id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender " +
                     "FROM employees WHERE UPPER(position) = 'COACH' ORDER BY last_name, first_name";

        List<Employee> coaches = new ArrayList<>();

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                coaches.add(mapResultSetToEmployee(rs));
            }
        }
        return coaches;
    }

    /**
     * Retrieves all players from the employees table.
     */
    public List<Employee> getPlayersByCoach(int coachId) throws SQLException {
    	List<Employee> players = new ArrayList<>();
        String sql = "SELECT e.* FROM employees e " +
                     "JOIN TEAM_MEMBER tm ON e.id = tm.employee_id " +
                     "JOIN TEAM t ON tm.team_id = t.id " +
                     "WHERE tm.role = 'PLAYER' " +
                     "AND (t.first_coach_id = ? OR t.second_coach_id = ?)";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, coachId);
            pstmt.setInt(2, coachId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Adds Employees
                players.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    /**
     * Maps an employees table row to the immutable Employee model.
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String nationalId = rs.getString("national_id");

        String birthDateStr = rs.getString("birth_date");
        LocalDate birthDate = (birthDateStr != null && !birthDateStr.trim().isEmpty())
                ? LocalDate.parse(birthDateStr)
                : null;

        String phoneNumber = rs.getString("phone_number");
        String category = rs.getString("category");
        String position = rs.getString("position");
        double grossAnnualSalary = rs.getDouble("gross_annual_salary");
        String gender = rs.getString("gender");

        return new Employee(
                id,
                firstName,
                lastName,
                nationalId,
                birthDate,
                phoneNumber,
                category,
                position,
                grossAnnualSalary,
                gender
        );
    }
    
    public List<InterviewSlotRecord> getSlotsByCoach(int coachId) throws SQLException {
    	List<InterviewSlotRecord> slots = new ArrayList<>();
        
        String sql = "SELECT s.id, e.first_name || ' ' || e.last_name AS player_name, " +
                     "s.slot_date AS date, s.start_time, s.end_time, s.status " +
                     "FROM INTERVIEW_SLOTS s " +
                     "JOIN employees e ON s.player_id = e.id " +
                     "JOIN TEAM_MEMBER tm ON e.id = tm.employee_id " +
                     "JOIN TEAM t ON tm.team_id = t.id " +
                     "WHERE (t.first_coach_id = ? OR t.second_coach_id = ?) " +
                     "ORDER BY s.slot_date DESC, s.start_time ASC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, coachId);
            pstmt.setInt(2, coachId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    slots.add(new InterviewSlotRecord(
                        rs.getInt("id"),
                        rs.getString("player_name"),
                        LocalDate.parse(rs.getString("date")),
                        LocalTime.parse(rs.getString("start_time")),
                        LocalTime.parse(rs.getString("end_time")),
                        rs.getString("status")
                    ));
                }
            }
        }
        return slots;
    }
    
    public List<TeamActivityRecord> getTeamActivitiesByCoach(int coachId) throws SQLException {
        List<TeamActivityRecord> activities = new ArrayList<>();
        
        String sql = "SELECT 'MATCH' AS activity_type, m.match_date AS activity_date, m.start_time, m.end_time " +
                     "FROM matches m " +
                     "JOIN TEAM t ON m.team_id = t.id " +
                     "WHERE t.first_coach_id = ? OR t.second_coach_id = ? " +
                     "UNION ALL " +
                     "SELECT 'TRAINING' AS activity_type, tr.training_date AS activity_date, tr.start_time, tr.end_time " +
                     "FROM trainings tr " +
                     "JOIN TEAM t ON tr.team_id = t.id " +
                     "WHERE t.first_coach_id = ? OR t.second_coach_id = ? " +
                     "ORDER BY activity_date DESC, start_time ASC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, coachId);
            pstmt.setInt(2, coachId);
            pstmt.setInt(3, coachId);
            pstmt.setInt(4, coachId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(new TeamActivityRecord(
                        rs.getString("activity_type"),
                        LocalDate.parse(rs.getString("activity_date")),
                        LocalTime.parse(rs.getString("start_time")),
                        LocalTime.parse(rs.getString("end_time"))
                    ));
                }
            }
        }
        return activities;
    }
}