package com.ips2026.pl11.data.ticket;

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
 * Integration Test Suite for MatchTicketDAO.
 * Verifies table creation, seat matrix queries, seat booking, and unique seat constraints.
 */
public class MatchTicketDAOTest {

    private MatchTicketDAO ticketDAO;

    /**
     * Recreates database tables prior to each test, setting up a UNIQUE constraint on seat coordinates.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        ticketDAO = new MatchTicketDAO();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS MATCH_TICKET;");

            // UNIQUE constraint on (match_id, grandstand, section, row_num, seat_num) forces SQLite to throw SQLException on duplicates
            stmt.execute("CREATE TABLE MATCH_TICKET (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "match_id INTEGER NOT NULL, " +
                         "grandstand TEXT NOT NULL, " +
                         "section TEXT NOT NULL, " +
                         "row_num INTEGER NOT NULL, " +
                         "seat_num INTEGER NOT NULL, " +
                         "price REAL NOT NULL, " +
                         "purchase_number TEXT NOT NULL DEFAULT 'PN-000', " +
                         "UNIQUE(match_id, grandstand, section, row_num, seat_num));");
        }
    }

    /**
     * Cleans up database table after test execution.
     */
    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM MATCH_TICKET;");
        }
    }

    @Test
    @DisplayName("getOccupiedSeats: returns 10x15 boolean matrix with true at occupied seat coordinates")
    void testGetOccupiedSeats_ReturnsMatrixWithOccupiedSeats() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO MATCH_TICKET (match_id, grandstand, section, row_num, seat_num, price, purchase_number) " +
                         "VALUES (10, 'A', 'A', 1, 1, 30.0, 'PN-101');");
            stmt.execute("INSERT INTO MATCH_TICKET (match_id, grandstand, section, row_num, seat_num, price, purchase_number) " +
                         "VALUES (10, 'A', 'A', 5, 12, 30.0, 'PN-102');");
        }

        boolean[][] occupied = ticketDAO.getOccupiedSeats(10, "A", "A");

        assertNotNull(occupied, "Occupied seats matrix must not be null.");
        assertEquals(10, occupied.length, "Matrix must have 10 rows.");
        assertEquals(15, occupied[0].length, "Matrix must have 15 columns.");
        assertTrue(occupied[0][0], "Seat (Row 1, Seat 1) should be marked as occupied.");
        assertTrue(occupied[4][11], "Seat (Row 5, Seat 12) should be marked as occupied.");
    }

    @Test
    @DisplayName("bookSpecificSeat: inserts ticket and returns true when seat is available")
    void testBookSpecificSeat_Success() {
        assertDoesNotThrow(() -> {
            boolean success = ticketDAO.bookSpecificSeat(10, "A", "A", 3, 7, 30.0);
            assertTrue(success, "bookSpecificSeat should return true on successful insertion.");
        });
    }

    @Test
    @DisplayName("bookContiguousSeats: allocates contiguous seats in specified row and returns ticket coordinates")
    void testBookContiguousSeats_Success() {
        assertDoesNotThrow(() -> {
            List<int[]> booked = ticketDAO.bookContiguousSeats(10, "A", "A", 2, 5, 3, 30.0);
            assertNotNull(booked, "Booked seats list must not be null.");
            assertEquals(3, booked.size(), "Should book exactly 3 seats.");
            assertEquals(2, booked.get(0)[0], "Row number must match target row.");
            assertEquals(5, booked.get(0)[1], "First seat number must match start col.");
            assertEquals(6, booked.get(1)[1], "Second seat number must be contiguous.");
            assertEquals(7, booked.get(2)[1], "Third seat number must be contiguous.");
        });
    }

    @Test
    @DisplayName("insert duplicate ticket: throws SQLException when double booking same seat")
    void testInsertDuplicateTicket_ThrowsSQLException() throws SQLException {
        ticketDAO.bookSpecificSeat(10, "A", "A", 4, 4, 30.0);

        assertThrows(SQLException.class, () ->
            ticketDAO.bookSpecificSeat(10, "A", "A", 4, 4, 30.0),
            "Inserting a duplicate ticket for the same seat must throw a SQLException."
        );
    }
}