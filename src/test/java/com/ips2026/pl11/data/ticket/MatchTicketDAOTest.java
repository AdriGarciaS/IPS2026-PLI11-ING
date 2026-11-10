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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;

public class MatchTicketDAOTest {

    private MatchTicketDAO matchTicketDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        matchTicketDAO = new MatchTicketDAO();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS matches (id INTEGER PRIMARY KEY AUTOINCREMENT, match_date TEXT, start_time TEXT, is_home INTEGER);");
            stmt.execute("CREATE TABLE IF NOT EXISTS MATCH_TICKET (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "purchase_number INTEGER NOT NULL, " +
                         "match_id INTEGER NOT NULL, " +
                         "grandstand TEXT NOT NULL, " +
                         "section TEXT NOT NULL, " +
                         "row_num INTEGER NOT NULL, " +
                         "seat_num INTEGER NOT NULL, " +
                         "price REAL NOT NULL, " +
                         "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                         "UNIQUE(match_id, grandstand, section, row_num, seat_num));");

            stmt.execute("INSERT OR IGNORE INTO matches (id, match_date, start_time, is_home) VALUES (1, '2026-11-20', '18:00', 1);");
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM MATCH_TICKET;");
            stmt.execute("DELETE FROM matches WHERE id = 1;");
        }
    }

    @Test
    @DisplayName("US 35083: getOccupiedSeats returns empty 10x15 matrix when no tickets are sold")
    void testGetOccupiedSeats_NoTicketsSold_ReturnsAllFalse() throws SQLException {
        boolean[][] occupied = matchTicketDAO.getOccupiedSeats(1, "A", "A");

        assertNotNull(occupied);
        assertEquals(10, occupied.length);
        assertEquals(15, occupied[0].length);

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 15; c++) {
                assertFalse(occupied[r][c]);
            }
        }
    }

    @Test
    @DisplayName("US 35083: getOccupiedSeats correctly maps sold seats to true")
    void testGetOccupiedSeats_SoldSeats_ReturnsTrueAtExactCoordinates() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO MATCH_TICKET (purchase_number, match_id, grandstand, section, row_num, seat_num, price) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, 9991);
            stmt.setInt(2, 1);
            stmt.setString(3, "A");
            stmt.setString(4, "A");
            stmt.setInt(5, 5); // Fila 5 -> Índice [4]
            stmt.setInt(6, 10); // Asiento 10 -> Índice [9]
            stmt.setDouble(7, 30.00);
            stmt.executeUpdate();
        }

        boolean[][] occupied = matchTicketDAO.getOccupiedSeats(1, "A", "A");

        assertTrue(occupied[4][9], "Row 5 Seat 10 should be occupied (index 4, 9)");
        assertFalse(occupied[0][0], "Row 1 Seat 1 should remain available");
    }

    @Test
    @DisplayName("US 35082: Inserting duplicate seat ticket for same match throws SQLException")
    void testInsertDuplicateTicket_ThrowsSQLException() throws SQLException {
        String insertSql = "INSERT INTO MATCH_TICKET (purchase_number, match_id, grandstand, section, row_num, seat_num, price) VALUES (100, 1, 'B', 'C', 1, 1, 30.00)";

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute(insertSql);
        }

        assertThrows(SQLException.class, () -> {
            try (Connection conn = ConexionBD.obtenerConexion();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(insertSql);
            }
        });
    }

    @Test
    @DisplayName("US 35082: Single purchase number groups multiple seats correctly")
    void testMultipleTicketsSamePurchaseNumber() throws SQLException {
        int purchaseNumber = 8882;
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO MATCH_TICKET (purchase_number, match_id, grandstand, section, row_num, seat_num, price) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            
            // Ticket 1: Fila 1, Asiento 1 -> Índice [0][0]
            stmt.setInt(1, purchaseNumber);
            stmt.setInt(2, 1);
            stmt.setString(3, "A");
            stmt.setString(4, "B");
            stmt.setInt(5, 1);
            stmt.setInt(6, 1);
            stmt.setDouble(7, 35.00);
            stmt.executeUpdate();

            // Ticket 2: Fila 1, Asiento 2 -> Índice [0][1]
            stmt.setInt(1, purchaseNumber);
            stmt.setInt(2, 1);
            stmt.setString(3, "A");
            stmt.setString(4, "B");
            stmt.setInt(5, 1);
            stmt.setInt(6, 2);
            stmt.setDouble(7, 35.00);
            stmt.executeUpdate();
        }

        boolean[][] occupied = matchTicketDAO.getOccupiedSeats(1, "A", "B");
        assertTrue(occupied[0][0], "Row 1 Seat 1 must be occupied");
        assertTrue(occupied[0][1], "Row 1 Seat 2 must be occupied");
    }
}
