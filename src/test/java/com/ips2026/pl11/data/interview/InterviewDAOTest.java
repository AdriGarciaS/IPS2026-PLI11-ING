package com.ips2026.pl11.data.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
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
 * Integration Test Suite for InterviewDAO.
 * Tests full table retrieval of interview slots.
 */
public class InterviewDAOTest {

    private InterviewDAO interviewDAO;

    /**
     * Sets up test database tables prior to each test.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        interviewDAO = new InterviewDAO();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS interview_slots (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "player_id INTEGER NOT NULL, coach_id INTEGER, slot_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, " +
                         "status TEXT DEFAULT 'AVAILABLE', media_name TEXT, media_contact TEXT);");
        }
    }

    /**
     * Cleans up database state after test execution.
     */
    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM interview_slots;");
        }
    }

    @Test
    @DisplayName("findAll: retrieves all interview slots from table regardless of status")
    void testFindAll_Success() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (1, 10, 2, '2026-11-20', '10:00', '10:30', 'AVAILABLE');");
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (2, 11, 2, '2026-11-20', '11:00', '11:30', 'ASSIGNED');");
        }

        List<AvailableSlotRecord> slots = interviewDAO.findAll();

        assertNotNull(slots, "Returned list should not be null.");
        assertEquals(2, slots.size(), "Should retrieve all 2 slots from the database.");
        assertEquals(1, slots.get(0).getId());
        assertEquals(2, slots.get(1).getId());
    }

    @Test
    @DisplayName("findAll: returns empty list when no interview slots exist")
    void testFindAll_EmptyTable_ReturnsEmptyList() throws SQLException {
        List<AvailableSlotRecord> slots = interviewDAO.findAll();

        assertNotNull(slots, "Returned list should not be null.");
        assertEquals(0, slots.size(), "Returned list should be empty.");
    }
}
