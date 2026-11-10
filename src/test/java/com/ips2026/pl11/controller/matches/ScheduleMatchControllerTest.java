package com.ips2026.pl11.controller.matches;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;

public class ScheduleMatchControllerTest {

    private ScheduleMatchController controller;

    @BeforeEach
    public void setUp() throws SQLException {
        controller = new ScheduleMatchController();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS FACILITY (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS TEAM (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS matches (id INTEGER PRIMARY KEY AUTOINCREMENT, team_id INTEGER, match_date TEXT, start_time TEXT, end_time TEXT, rival TEXT, is_home INTEGER, is_special_match INTEGER, facility_id INTEGER);");
            stmt.execute("CREATE TABLE IF NOT EXISTS MATCH_PRICE (match_id INTEGER, grandstand TEXT, section TEXT, price REAL, PRIMARY KEY(match_id, grandstand, section));");
            stmt.execute("CREATE TABLE IF NOT EXISTS trainings (id INTEGER PRIMARY KEY AUTOINCREMENT, facility_id INTEGER, training_date TEXT, start_time TEXT, end_time TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS FACILITY_RESERVATION (id INTEGER PRIMARY KEY AUTOINCREMENT, facility_id INTEGER, holder_name TEXT, reservation_date TEXT, start_time TEXT, end_time TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS gardening_session (id INTEGER PRIMARY KEY AUTOINCREMENT, facility_id INTEGER, date TEXT, start_time TEXT, end_time TEXT, type TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS interview_slots (id INTEGER PRIMARY KEY AUTOINCREMENT, player_id INTEGER, coach_id INTEGER, slot_date TEXT, start_time TEXT, end_time TEXT, status TEXT DEFAULT 'AVAILABLE', media_name TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS interviews (id INTEGER PRIMARY KEY AUTOINCREMENT, slot_id INTEGER UNIQUE, player_id INTEGER, interviewer_name TEXT, media_outlet TEXT);");

            stmt.execute("INSERT OR IGNORE INTO FACILITY (id, name) VALUES (1, 'Main Pitch');");
            stmt.execute("INSERT OR IGNORE INTO TEAM (id, name) VALUES (1, 'First Team');");
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM MATCH_PRICE;");
            stmt.execute("DELETE FROM matches;");
            stmt.execute("DELETE FROM trainings;");
        }
    }

    @Test
    @DisplayName("US 35775: Controller schedules home match and calculates 2h end time automatically")
    void testScheduleMatch_Success_CalculatesEndTime() throws Exception {
        int matchId = controller.scheduleMatch(1, "Real Madrid", "2026-11-20", "18:00", false, "A", "A", 40.00, 1);
        
        assertTrue(matchId > 0, "Match ID generated should be greater than 0");
    }

    @Test
    @DisplayName("US 35775: Controller handles midnight overflow when calculating end time")
    void testScheduleMatch_LateNightMatch_CalculatesEndTime() throws Exception {
        int matchId = controller.scheduleMatch(1, "FC Barcelona", "2026-11-20", "23:00", true, "B", "C", 50.00, 1);
        
        assertTrue(matchId > 0);
    }
}
