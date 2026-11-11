package com.ips2026.pl11.data.interview;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.interview.AvailableSlotRecord;

/**
 * Integration Test Suite for InterviewBookingDAO.
 * Tests slot retrieval and transactional booking operations against SQLite database.
 */
public class InterviewBookingDAOTest {

    private InterviewBookingDAO bookingDAO;

    /**
     * Sets up database tables and seeds initial test data before each test execution.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        bookingDAO = new InterviewBookingDAO();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            // Create EMPLOYEES table
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "first_name TEXT NOT NULL, last_name TEXT NOT NULL, national_id TEXT, birth_date TEXT, " +
                         "phone_number TEXT, category TEXT, position TEXT, gross_annual_salary REAL, gender TEXT);");

            // Create INTERVIEW_SLOTS table
            stmt.execute("CREATE TABLE IF NOT EXISTS interview_slots (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "player_id INTEGER NOT NULL, coach_id INTEGER, slot_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, " +
                         "status TEXT DEFAULT 'AVAILABLE', media_name TEXT, media_contact TEXT);");

            // Seed player employee record required for INNER JOIN queries
            stmt.execute("INSERT OR REPLACE INTO employees (id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) " +
                         "VALUES (1, 'Pedri', 'González', '11111111A', '2002-11-25', '600000001', 'SPORTS', 'PLAYER', 5000000.0, 'MALE');");
        }
    }

    /**
     * Cleans up database state after each test execution.
     */
    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM interview_slots;");
            stmt.execute("DELETE FROM employees;");
        }
    }

    @Test
    @DisplayName("getAvailableSlots: retrieves available slots with concatenated player full name")
    void testGetAvailableSlots_Success() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (10, 1, 2, '2026-11-20', '10:00', '10:30', 'AVAILABLE');");
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (11, 1, 2, '2026-11-20', '11:00', '11:30', 'ASSIGNED');");
        }

        List<AvailableSlotRecord> slots = bookingDAO.getAvailableSlots();

        assertNotNull(slots, "Returned slots list must not be null.");
        assertEquals(1, slots.size(), "Should only retrieve slots with AVAILABLE status.");
        assertEquals(10, slots.get(0).getId(), "Slot ID must match the available record.");
        assertEquals("Pedri González", slots.get(0).getPlayerName(), "Player name must be joined correctly.");
    }

    @Test
    @DisplayName("bookSlotAndRemoveRemaining: assigns chosen slot and removes remaining slots for same player on date")
    void testBookSlotAndRemoveRemaining_Success() throws SQLException {
        // Insert multiple available slots for player 1 on the same date
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (10, 1, 2, '2026-11-20', '10:00', '10:30', 'AVAILABLE');");
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (11, 1, 2, '2026-11-20', '11:00', '11:30', 'AVAILABLE');");
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (12, 1, 2, '2026-11-20', '12:00', '12:30', 'AVAILABLE');");
        }

        // Book slot ID 10
        assertDoesNotThrow(() ->
            bookingDAO.bookSlotAndRemoveRemaining(10, 1, "2026-11-20", "Radio Marca", "contact@radiomarca.com")
        );

        // Verify slot 10 is ASSIGNED and remaining slots (11, 12) are deleted
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs10 = stmt.executeQuery("SELECT status, media_name, media_contact FROM interview_slots WHERE id = 10")) {
                assertTrue(rs10.next(), "Booked slot 10 must still exist.");
                assertEquals("ASSIGNED", rs10.getString("status"), "Status must be updated to ASSIGNED.");
                assertEquals("Radio Marca", rs10.getString("media_name"));
                assertEquals("contact@radiomarca.com", rs10.getString("media_contact"));
            }

            try (ResultSet rsOthers = stmt.executeQuery("SELECT COUNT(*) FROM interview_slots WHERE player_id = 1 AND slot_date = '2026-11-20'")) {
                assertTrue(rsOthers.next());
                assertEquals(1, rsOthers.getInt(1), "Only 1 booked slot must remain in database for this player and date.");
            }
        }
    }

    @Test
    @DisplayName("bookSlotAndRemoveRemaining: throws SQLException when selecting slot that is no longer AVAILABLE")
    void testBookSlotAndRemoveRemaining_SlotAlreadyBooked_ThrowsException() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (10, 1, 2, '2026-11-20', '10:00', '10:30', 'ASSIGNED');");
        }

        SQLException ex = assertThrows(SQLException.class, () ->
            bookingDAO.bookSlotAndRemoveRemaining(10, 1, "2026-11-20", "Radio Marca", "contact@radiomarca.com")
        );

        assertEquals("The selected slot is no longer available.", ex.getMessage());
    }
}
