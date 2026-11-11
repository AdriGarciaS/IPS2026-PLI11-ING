package com.ips2026.pl11.controller.interview;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.interview.AvailableSlotRecord;

/**
 * Integration Test Suite for BookInterviewController.
 * Validates slot retrieval, input validations, and booking transactions against SQLite.
 */
public class BookInterviewControllerTest {

    private BookInterviewController controller;

    /**
     * Sets up in-memory/local SQLite database schema and seeds initial employee data prior to each test.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        controller = new BookInterviewController();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            // Create schema for EMPLOYEES and INTERVIEW_SLOTS tables
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "first_name TEXT, last_name TEXT, national_id TEXT, birth_date TEXT, " +
                         "phone_number TEXT, category TEXT, position TEXT, gross_annual_salary REAL, gender TEXT);");

            stmt.execute("CREATE TABLE IF NOT EXISTS interview_slots (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "player_id INTEGER, coach_id INTEGER, slot_date TEXT, start_time TEXT, end_time TEXT, " +
                         "status TEXT DEFAULT 'AVAILABLE', media_name TEXT, media_contact TEXT);");

            // Insert sample player record required for relational JOIN queries
            stmt.execute("INSERT OR REPLACE INTO employees (id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) " +
                         "VALUES (1, 'Pedri', 'González', '11111111A', '2002-11-25', '600000001', 'SPORTS', 'PLAYER', 5000000.0, 'MALE');");
        }
    }

    /**
     * Cleans up test tables after each test execution to maintain database isolation.
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
    @DisplayName("loadAvailableSlots: successfully retrieves AVAILABLE slots from database")
    void testLoadAvailableSlots_Success() throws SQLException {
        // Insert available slot for player ID 1
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (1, 1, 2, '2026-11-20', '10:00', '10:30', 'AVAILABLE');");
        }

        List<AvailableSlotRecord> slots = controller.loadAvailableSlots();

        assertNotNull(slots, "The returned slot list should not be null.");
        assertEquals(1, slots.size(), "Should retrieve exactly 1 available slot from database.");
        assertEquals(1, slots.get(0).getId(), "Retrieved slot ID should match inserted record.");
    }

    @Test
    @DisplayName("bookInterview: throws IllegalArgumentException when slot parameter is null")
    void testBookInterview_NullSlot_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            controller.bookInterview(null, "Radio Marca", "contact@radiomarca.com")
        );

        assertEquals("Please select an available slot from the table.", ex.getMessage(),
                "Exception message must match requirement when slot is null.");
    }

    @Test
    @DisplayName("bookInterview: throws IllegalArgumentException when media name is null or blank")
    void testBookInterview_EmptyMediaName_ThrowsException() {
        AvailableSlotRecord slot = new AvailableSlotRecord(1, 1, "Pedri González", "2026-11-20", "10:00", "10:30");

        // Case 1: Null media name
        assertThrows(IllegalArgumentException.class, () ->
            controller.bookInterview(slot, null, "contact@radiomarca.com"),
            "Should throw exception when media name is null."
        );

        // Case 2: Blank media name
        assertThrows(IllegalArgumentException.class, () ->
            controller.bookInterview(slot, "   ", "contact@radiomarca.com"),
            "Should throw exception when media name is blank."
        );
    }

    @Test
    @DisplayName("bookInterview: successfully books slot in DB and trims whitespace from inputs")
    void testBookInterview_Success() throws SQLException {
        // Seed available slot
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (1, 1, 2, '2026-11-20', '10:00', '10:30', 'AVAILABLE');");
        }

        AvailableSlotRecord slot = new AvailableSlotRecord(1, 1, "Pedri González", "2026-11-20", "10:00", "10:30");

        // Execute booking controller method with extra padding whitespace
        assertDoesNotThrow(() ->
            controller.bookInterview(slot, "  Radio Marca  ", "  contact@radiomarca.com  ")
        );

        // Verify database updates
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT status, media_name, media_contact FROM interview_slots WHERE id = 1")) {
            
            assertTrue(rs.next(), "Booked slot record should exist in database.");
            assertEquals("Radio Marca", rs.getString("media_name"), "Media name should be stored trimmed.");
            assertEquals("contact@radiomarca.com", rs.getString("media_contact"), "Media contact should be stored trimmed.");
        }
    }
}

    