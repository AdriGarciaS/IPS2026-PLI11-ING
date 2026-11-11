package com.ips2026.pl11.controller.ticket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;

/**
 * Integration Test Suite for MatchTicketController (US 35082).
 * Verifies matrix occupation merging (tickets + season passes) and contiguous seat allocation logic.
 */
public class MatchTicketControllerTest {

    private MatchTicketController controller;

    /**
     * Recreates SQLite database tables prior to each test run, ensuring matches schema contains is_special_match.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        controller = new MatchTicketController();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS MATCH_TICKET;");
            stmt.execute("DROP TABLE IF EXISTS SEASON_PASS;");
            stmt.execute("DROP TABLE IF EXISTS matches;");
            stmt.execute("DROP TABLE IF EXISTS TEAM;");
            stmt.execute("DROP TABLE IF EXISTS FACILITY;");

            stmt.execute("CREATE TABLE FACILITY (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);");
            stmt.execute("CREATE TABLE TEAM (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, category TEXT, gender TEXT, first_coach_id INTEGER, second_coach_id INTEGER, created_at TEXT);");
            
            // Fixed schema definition matching column name 'is_special_match' expected by MatchDAO
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

            stmt.execute("CREATE TABLE SEASON_PASS (id INTEGER PRIMARY KEY AUTOINCREMENT, fan_id INTEGER, team_id INTEGER, grandstand TEXT, section TEXT, row_num INTEGER, seat_num INTEGER, age_category TEXT, price REAL, start_date TEXT, end_date TEXT);");

            stmt.execute("INSERT INTO TEAM (id, name) VALUES (1, 'First Team');");
            stmt.execute("INSERT INTO matches (id, team_id, rival, match_date, start_time, end_time, is_home, is_special_match, facility_id) VALUES (10, 1, 'Sporting', '2026-11-25', '18:00', '20:00', 1, 0, 1);");
            stmt.execute("INSERT INTO matches (id, team_id, rival, match_date, start_time, end_time, is_home, is_special_match, facility_id) VALUES (20, 1, 'Sporting', '2026-11-25', '18:00', '20:00', 1, 1, 1);");
        }
    }

    /**
     * Cleans up all database tables after test execution.
     */
    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM MATCH_TICKET;");
            stmt.execute("DELETE FROM SEASON_PASS;");
            stmt.execute("DELETE FROM matches;");
            stmt.execute("DELETE FROM TEAM;");
            stmt.execute("DELETE FROM FACILITY;");
        }
    }

    @Test
    @DisplayName("getOccupiedSeats: merges general tickets and regular season pass reservations into grid")
    void testGetOccupiedSeats_CombinesTicketsAndSeasonPasses() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO MATCH_TICKET (match_id, grandstand, section, row_num, seat_num, price, purchase_number) VALUES (10, 'A', 'A', 1, 1, 30.0, 'PN-TEST-001');");
            stmt.execute("INSERT INTO SEASON_PASS (fan_id, team_id, grandstand, section, row_num, seat_num, start_date, end_date) VALUES (1, 1, 'A', 'A', 1, 2, '2026-07-01', '2027-06-30');");
        }

        boolean[][] grid = controller.getOccupiedSeats(10, "A", "A");

        assertNotNull(grid);
        assertTrue(grid[0][0], "Seat (1,1) should be occupied by general ticket.");
        assertTrue(grid[0][1], "Seat (1,2) should be occupied by season pass.");
    }

    @Test
    @DisplayName("getOccupiedSeats: ignores season pass locks if match is flagged as Special Match")
    void testGetOccupiedSeats_SpecialMatchIgnoresSeasonPasses() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO SEASON_PASS (fan_id, team_id, grandstand, section, row_num, seat_num, start_date, end_date) VALUES (1, 1, 'A', 'A', 1, 2, '2026-07-01', '2027-06-30');");
        }

        boolean[][] grid = controller.getOccupiedSeats(20, "A", "A");

        assertNotNull(grid);
        assertTrue(!grid[0][1], "Seat (1,2) should NOT be locked by season pass during a Special Match.");
    }

    @Test
    @DisplayName("sellTickets: throws IllegalArgumentException when requested quantity is out of bounds (1-15)")
    void testSellTickets_InvalidQuantity_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> controller.sellTickets(10, "A", "A", 0, 30.0));
        assertThrows(IllegalArgumentException.class, () -> controller.sellTickets(10, "A", "A", 16, 30.0));
    }

    @Test
    @DisplayName("sellTickets: allocates contiguous seats in the same row successfully")
    void testSellTickets_ContiguousAllocation_Success() {
        assertDoesNotThrow(() -> {
            List<int[]> allocated = controller.sellTickets(10, "A", "A", 3, 30.0);
            assertNotNull(allocated);
            assertEquals(3, allocated.size());

            assertEquals(1, allocated.get(0)[0]);
            assertEquals(1, allocated.get(0)[1]);
            assertEquals(1, allocated.get(1)[0]);
            assertEquals(2, allocated.get(1)[1]);
            assertEquals(1, allocated.get(2)[0]);
            assertEquals(3, allocated.get(2)[1]);
        });
    }

    @Test
    @DisplayName("sellTickets: throws IllegalStateException when no row has enough contiguous seats")
    void testSellTickets_NoContiguousSeats_ThrowsException() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            for (int r = 1; r <= 10; r++) {
                for (int c = 1; c <= 15; c += 2) {
                    stmt.execute("INSERT INTO MATCH_TICKET (match_id, grandstand, section, row_num, seat_num, price, purchase_number) VALUES (10, 'A', 'A', " + r + ", " + c + ", 30.0, 'PN-TEST-ROW');");
                }
            }
        }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> controller.sellTickets(10, "A", "A", 2, 30.0));
        assertTrue(ex.getMessage().contains("There is no row in the chosen section"));
    }
}