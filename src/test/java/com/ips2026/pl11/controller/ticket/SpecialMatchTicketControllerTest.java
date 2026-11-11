package com.ips2026.pl11.controller.ticket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.seasonpass.SeasonDateUtils;

/**
 * Integration Test Suite for SpecialMatchTicketController.
 * Verifies member seat claim validations, cutoff deadlines, and special ticket confirmations.
 */
public class SpecialMatchTicketControllerTest {

    private SpecialMatchTicketController controller;

    /**
     * Recreates test database tables prior to each test run with matching schema constraints.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        controller = new SpecialMatchTicketController();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS MATCH_TICKET;");
            stmt.execute("DROP TABLE IF EXISTS SEASON_PASS;");
            stmt.execute("DROP TABLE IF EXISTS matches;");
            stmt.execute("DROP TABLE IF EXISTS FANS;");
            stmt.execute("DROP TABLE IF EXISTS TEAM;");
            stmt.execute("DROP TABLE IF EXISTS FACILITY;");

            stmt.execute("CREATE TABLE FACILITY (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);");
            stmt.execute("CREATE TABLE TEAM (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, category TEXT, gender TEXT, first_coach_id INTEGER, second_coach_id INTEGER, created_at TEXT);");
            
            stmt.execute("CREATE TABLE matches (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "team_id INTEGER, " +
                         "rival TEXT NOT NULL DEFAULT 'Opponent', " +
                         "match_date TEXT NOT NULL, " +
                         "start_time TEXT NOT NULL, " +
                         "end_time TEXT, " +
                         "is_home INTEGER NOT NULL CHECK (is_home IN (0, 1)), " +
                         "is_special_match INTEGER DEFAULT 0, " +
                         "facility_id INTEGER);");

            stmt.execute("CREATE TABLE MATCH_TICKET (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "match_id INTEGER NOT NULL, " +
                         "grandstand TEXT NOT NULL, " +
                         "section TEXT NOT NULL, " +
                         "row_num INTEGER NOT NULL, " +
                         "seat_num INTEGER NOT NULL, " +
                         "price REAL NOT NULL, " +
                         "purchase_number TEXT NOT NULL DEFAULT 'PN-000');");

            stmt.execute("CREATE TABLE FANS (id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT, last_name TEXT, national_id TEXT UNIQUE);");
            stmt.execute("CREATE TABLE SEASON_PASS (id INTEGER PRIMARY KEY AUTOINCREMENT, fan_id INTEGER, team_id INTEGER, grandstand TEXT, section TEXT, row_num INTEGER, seat_num INTEGER, age_category TEXT, price REAL, start_date TEXT, end_date TEXT);");

            // Seed required relational records: TEAM (id=1) and FANS (id=10, national_id='12345678X')
            stmt.execute("INSERT INTO TEAM (id, name) VALUES (1, 'First Team');");
            stmt.execute("INSERT INTO FANS (id, first_name, last_name, national_id) VALUES (10, 'Juan', 'Pérez', '12345678X');");
        }
    }

    /**
     * Cleans up database state after test execution.
     */
    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM MATCH_TICKET;");
            stmt.execute("DELETE FROM SEASON_PASS;");
            stmt.execute("DELETE FROM FANS;");
            stmt.execute("DELETE FROM matches;");
            stmt.execute("DELETE FROM TEAM;");
            stmt.execute("DELETE FROM FACILITY;");
        }
    }

    @Test
    @DisplayName("verifyAndClaimMemberSeat: throws IllegalArgumentException when match is not found")
    void testVerifyAndClaimMemberSeat_MatchNotFound_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            controller.verifyAndClaimMemberSeat(999, "12345678X")
        );
        assertEquals("Match not found.", ex.getMessage());
    }

    @Test
    @DisplayName("verifyAndClaimMemberSeat: throws IllegalStateException when match is not designated as Special")
    void testVerifyAndClaimMemberSeat_NotSpecialMatch_ThrowsException() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO matches (id, team_id, rival, match_date, start_time, end_time, is_home, is_special_match, facility_id) VALUES (10, 1, 'Sporting', '2026-11-25', '18:00', '20:00', 1, 0, 1);");
        }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            controller.verifyAndClaimMemberSeat(10, "12345678X")
        );
        assertEquals("This match is not designated as a Special Match.", ex.getMessage());
    }

    @Test
    @DisplayName("verifyAndClaimMemberSeat: throws IllegalStateException when payment cutoff deadline has passed")
    void testVerifyAndClaimMemberSeat_ExpiredDeadline_ThrowsException() throws SQLException {
        String pastMatchDate = LocalDate.now().minusDays(1).toString();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO matches (id, team_id, rival, match_date, start_time, end_time, is_home, is_special_match, facility_id) VALUES (20, 1, 'Sporting', '" + pastMatchDate + "', '18:00', '20:00', 1, 1, 1);");
        }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            controller.verifyAndClaimMemberSeat(20, "12345678X")
        );
        assertTrue(ex.getMessage().contains("payment deadline"));
    }

    @Test
    @DisplayName("confirmMemberSpecialTicket: throws IllegalStateException when seat is already booked")
    void testConfirmMemberSpecialTicket_SeatAlreadyBooked_ThrowsException() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO MATCH_TICKET (match_id, grandstand, section, row_num, seat_num, price, purchase_number) VALUES (30, 'B', 'C', 4, 8, 30.0, 'PN-TEST-100');");
        }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            controller.confirmMemberSpecialTicket(30, "B", "C", 4, 8, 30.0)
        );
        assertEquals("This seat has already been paid and secured for this match.", ex.getMessage());
    }

    @Test
    @DisplayName("confirmMemberSpecialTicket: successfully books ticket when seat is available")
    void testConfirmMemberSpecialTicket_Success() {
        assertDoesNotThrow(() -> {
            boolean success = controller.confirmMemberSpecialTicket(30, "B", "C", 5, 10, 30.0);
            assertTrue(success, "Ticket confirmation should return true for available seat.");
        });
    }
}