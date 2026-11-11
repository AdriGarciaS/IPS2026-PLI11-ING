package com.ips2026.pl11.controller.interview;

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
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.interview.InterviewDAO;
import com.ips2026.pl11.model.interview.AvailableSlotRecord;

public class InterviewControllerTest {

    private InterviewController controller;

    @BeforeEach
    public void setUp() throws SQLException {
        InterviewDAO interviewDAO = new InterviewDAO();
        EmployeeDAO employeeDAO = new EmployeeDAO();
        controller = new InterviewController(interviewDAO, employeeDAO);

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "first_name TEXT NOT NULL, last_name TEXT NOT NULL, national_id TEXT, birth_date TEXT, " +
                         "phone_number TEXT, category TEXT, position TEXT, gross_annual_salary REAL, gender TEXT);");

            stmt.execute("CREATE TABLE IF NOT EXISTS interview_slots (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "player_id INTEGER NOT NULL, coach_id INTEGER, slot_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, " +
                         "status TEXT DEFAULT 'AVAILABLE', media_name TEXT, media_contact TEXT);");

            stmt.execute("INSERT OR IGNORE INTO employees (id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) " +
                         "VALUES (10, 'Pedri', 'González', '11111111A', '2002-11-25', '600000001', 'SPORTS', 'PLAYER', 5000000.0, 'MALE');");
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM interview_slots;");
            stmt.execute("DELETE FROM employees;");
        }
    }

    @Test
    @DisplayName("getInterviewSlots: concatena el nombre y apellido del jugador en los slots recuperados")
    void testGetInterviewSlots_PopulatesPlayerName() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (1, 10, 2, '2026-11-20', '10:00', '10:30', 'AVAILABLE');");
        }

        List<AvailableSlotRecord> result = controller.getInterviewSlots();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pedri González", result.get(0).getPlayerName());
    }

    @Test
    @DisplayName("getInterviewSlots: mantiene el campo de nombre nulo si el id de empleado no existe")
    void testGetInterviewSlots_EmployeeNotFound() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO interview_slots (id, player_id, coach_id, slot_date, start_time, end_time, status) " +
                         "VALUES (2, 99, 2, '2026-11-20', '11:00', '11:30', 'AVAILABLE');");
        }

        List<AvailableSlotRecord> result = controller.getInterviewSlots();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(null, result.get(0).getPlayerName());
    }
}
