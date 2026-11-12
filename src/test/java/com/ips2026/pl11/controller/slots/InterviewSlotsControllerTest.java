package com.ips2026.pl11.controller.slots;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;

public class InterviewSlotsControllerTest {

private InterviewSlotsController controller;

@BeforeEach
public void setUp() throws SQLException {
    controller = new InterviewSlotsController();

    try (Connection conn = ConexionBD.obtenerConexion();
         Statement stmt = conn.createStatement()) {

        stmt.execute("DROP TABLE IF EXISTS interview_slots;");
        stmt.execute("DROP TABLE IF EXISTS matches;");
        stmt.execute("DROP TABLE IF EXISTS trainings;");
        stmt.execute("DROP TABLE IF EXISTS TEAM_MEMBER;");
        stmt.execute("DROP TABLE IF EXISTS TEAM;");
        stmt.execute("DROP TABLE IF EXISTS employees;");

        stmt.execute("CREATE TABLE IF NOT EXISTS employees (id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT NOT NULL, last_name TEXT NOT NULL, national_id TEXT NOT NULL UNIQUE, birth_date TEXT NOT NULL, phone_number TEXT NOT NULL, category TEXT NOT NULL, position TEXT NOT NULL, gross_annual_salary REAL NOT NULL, gender TEXT NOT NULL);");
        stmt.execute("CREATE TABLE IF NOT EXISTS TEAM (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, category TEXT NOT NULL, gender TEXT NOT NULL, first_coach_id INTEGER NOT NULL, second_coach_id INTEGER NOT NULL, created_at TEXT NOT NULL);");
        stmt.execute("CREATE TABLE IF NOT EXISTS TEAM_MEMBER (id INTEGER PRIMARY KEY AUTOINCREMENT, team_id INTEGER NOT NULL, employee_id INTEGER NOT NULL, role TEXT NOT NULL, task TEXT);");
        stmt.execute("CREATE TABLE IF NOT EXISTS interview_slots (id INTEGER PRIMARY KEY AUTOINCREMENT, player_id INTEGER NOT NULL, coach_id INTEGER NOT NULL, slot_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'AVAILABLE', media_name TEXT, media_contact TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP);");
        stmt.execute("CREATE TABLE IF NOT EXISTS matches (id INTEGER PRIMARY KEY AUTOINCREMENT, team_id INTEGER, match_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, is_home INTEGER NOT NULL);");
        stmt.execute("CREATE TABLE IF NOT EXISTS trainings (id INTEGER PRIMARY KEY AUTOINCREMENT, team_id INTEGER, training_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, facility_id INTEGER NOT NULL, coach_id INTEGER NOT NULL);");

        stmt.execute("INSERT OR IGNORE INTO employees (id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) VALUES (1, 'Pedri', 'González', '11111111A', '2002-11-25', '600000001', 'SPORTS', 'Midfielder', 5000000.0, 'MALE');");
        stmt.execute("INSERT OR IGNORE INTO employees (id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) VALUES (2, 'Hansi', 'Flick', '22222222B', '1965-02-24', '611111111', 'SPORTS', 'COACH', 8000000.0, 'MALE');");
    }
}

@AfterEach
public void tearDown() throws SQLException {
    try (Connection conn = ConexionBD.obtenerConexion();
         Statement stmt = conn.createStatement()) {
        stmt.execute("DELETE FROM interview_slots;");
        stmt.execute("DELETE FROM matches;");
        stmt.execute("DELETE FROM trainings;");
        stmt.execute("DELETE FROM TEAM_MEMBER;");
        stmt.execute("DELETE FROM TEAM;");
        stmt.execute("DELETE FROM employees;");
    }
}

@Test
@DisplayName("US 35733: Reject slot creation in the past")
void testCreateInterviewSlot_PastDate_ThrowsException() {
    LocalDate pastDate = LocalDate.now().minusDays(1);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
        controller.createInterviewSlot(1, 2, pastDate, LocalTime.parse("10:00"), LocalTime.parse("10:30"));
    });
    assertEquals("Cannot create interview slots in the past.", ex.getMessage());
}

@Test
@DisplayName("US 35733: Reject slot creation when end time is before start time")
void testCreateInterviewSlot_InvalidTimeInterval_ThrowsException() {
    LocalDate futureDate = LocalDate.now().plusDays(2);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
        controller.createInterviewSlot(1, 2, futureDate, LocalTime.parse("11:00"), LocalTime.parse("10:30"));
    });
    assertEquals("Slot end time must be strictly after start time.", ex.getMessage());
}

@Test
@DisplayName("US 35733: Reject slot creation if player has assigned interview on date")
void testCreateInterviewSlot_AssignedInterviewExists_ThrowsException() throws SQLException {
    LocalDate futureDate = LocalDate.now().plusDays(2);
    try (Connection conn = ConexionBD.obtenerConexion();
         PreparedStatement ps = conn.prepareStatement("INSERT INTO interview_slots (player_id, coach_id, slot_date, start_time, end_time, status) VALUES (1, 2, ?, '09:00', '09:30', 'ASSIGNED')")) {
        ps.setString(1, futureDate.toString());
        ps.executeUpdate();
    }

    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
        controller.createInterviewSlot(1, 2, futureDate, LocalTime.parse("14:00"), LocalTime.parse("14:30"));
    });
    assertEquals("The player already has an assigned interview on " + futureDate + ". No new slots can be created for this day.", ex.getMessage());
}

@Test
@DisplayName("US 35733: Create slot successfully under valid constraints")
void testCreateInterviewSlot_ValidParameters_Success() {
    LocalDate futureDate = LocalDate.now().plusDays(5);

    assertDoesNotThrow(() -> {
        controller.createInterviewSlot(1, 2, futureDate, LocalTime.parse("15:00"), LocalTime.parse("15:30"));
    });
}
}