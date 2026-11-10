package com.ips2026.pl11.data.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;

public class MatchDAOTest {

    private MatchDAO matchDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        matchDAO = new MatchDAO();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS FACILITY (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS TEAM (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT, last_name TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS matches (id INTEGER PRIMARY KEY AUTOINCREMENT, team_id INTEGER, match_date TEXT, start_time TEXT, end_time TEXT, rival TEXT, is_home INTEGER, is_special_match INTEGER, facility_id INTEGER);");
            stmt.execute("CREATE TABLE IF NOT EXISTS trainings (id INTEGER PRIMARY KEY AUTOINCREMENT, facility_id INTEGER NOT NULL, training_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, coach_id INTEGER NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS FACILITY_RESERVATION (id INTEGER PRIMARY KEY AUTOINCREMENT, facility_id INTEGER, holder_name TEXT, reservation_date TEXT, start_time TEXT, end_time TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS gardening_session (id INTEGER PRIMARY KEY AUTOINCREMENT, facility_id INTEGER, date TEXT, start_time TEXT, end_time TEXT, type TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS interview_slots (id INTEGER PRIMARY KEY AUTOINCREMENT, player_id INTEGER, coach_id INTEGER, slot_date TEXT, start_time TEXT, end_time TEXT, status TEXT DEFAULT 'AVAILABLE', media_name TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS interviews (id INTEGER PRIMARY KEY AUTOINCREMENT, slot_id INTEGER UNIQUE, player_id INTEGER, interviewer_name TEXT, media_outlet TEXT);");

            stmt.execute("INSERT OR IGNORE INTO FACILITY (id, name) VALUES (1, 'Main Pitch');");
            stmt.execute("INSERT OR IGNORE INTO TEAM (id, name) VALUES (1, 'First Team');");
            stmt.execute("INSERT OR IGNORE INTO employees (id, first_name, last_name) VALUES (1, 'Pedri', 'González');");
            stmt.execute("INSERT OR IGNORE INTO employees (id, first_name, last_name) VALUES (2, 'Hansi', 'Flick');");
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM matches;");
            stmt.execute("DELETE FROM trainings;");
            stmt.execute("DELETE FROM FACILITY_RESERVATION;");
            stmt.execute("DELETE FROM gardening_session;");
            stmt.execute("DELETE FROM interviews;");
            stmt.execute("DELETE FROM interview_slots;");
        }
    }

    @Test
    @DisplayName("US 35775: Schedule home match creates row in matches table")
    void testCreateHomeMatch_Success() throws SQLException {
        int matchId = matchDAO.createHomeMatchWithConflictsCleared(1, "Sevilla FC", "2026-11-25", "18:00", "20:00", false, 1);
        assertTrue(matchId > 0);
    }

    @Test
    @DisplayName("US 35775: Cannot schedule home match if official team training overlaps")
    void testCreateHomeMatch_TrainingConflict_ThrowsException() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO trainings (facility_id, training_date, start_time, end_time, coach_id) VALUES (1, '2026-11-25', '17:30', '19:30', 2)")) {
            stmt.executeUpdate();
        }

        assertThrows(IllegalStateException.class, () -> {
            matchDAO.createHomeMatchWithConflictsCleared(1, "Sevilla FC", "2026-11-25", "18:00", "20:00", false, 1);
        });
    }

    @Test
    @DisplayName("US 35775: Cannot schedule home match if another match overlaps in same facility")
    void testCreateHomeMatch_MatchConflict_ThrowsException() throws SQLException {
        matchDAO.createHomeMatchWithConflictsCleared(1, "Sevilla FC", "2026-11-25", "18:00", "20:00", false, 1);

        assertThrows(IllegalStateException.class, () -> {
            matchDAO.createHomeMatchWithConflictsCleared(1, "Real Betis", "2026-11-25", "19:00", "21:00", false, 1);
        });
    }

    @Test
    @DisplayName("US 35775: getActivitiesForDay returns daily schedule including trainings and interviews")
    void testGetActivitiesForDay_LoadsInterviewsAndTrainings() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO trainings (facility_id, training_date, start_time, end_time, coach_id) VALUES (1, '2026-11-25', '10:00', '11:30', 2)")) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) VALUES (5, 1, 2, '2026-11-25', '12:00', '12:30', 'ASSIGNED')")) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO interviews (slot_id, player_id, interviewer_name, media_outlet) VALUES (5, 1, 'Reporter', 'Marca')")) {
                stmt.executeUpdate();
            }
        }

        List<Object[]> activities = matchDAO.getActivitiesForDay(1, "2026-11-25");

        assertNotNull(activities);
        assertFalse(activities.isEmpty(), "Activities list should contain the inserted training and interview");
        assertTrue(activities.size() >= 1);
    }
}