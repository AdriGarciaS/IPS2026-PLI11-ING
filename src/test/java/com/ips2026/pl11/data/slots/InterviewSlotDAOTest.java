package com.ips2026.pl11.data.slots;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
import com.ips2026.pl11.data.slots.InterviewSlotDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.slots.InterviewSlotRecord;
import com.ips2026.pl11.model.slots.TeamActivityRecord;

public class InterviewSlotDAOTest {

    private InterviewSlotDAO slotDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        slotDAO = new InterviewSlotDAO();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS FACILITY (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT NOT NULL, last_name TEXT NOT NULL, national_id TEXT NOT NULL UNIQUE, birth_date TEXT NOT NULL, phone_number TEXT NOT NULL, category TEXT NOT NULL, position TEXT NOT NULL, gross_annual_salary REAL NOT NULL, gender TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS TEAM (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, category TEXT NOT NULL, gender TEXT NOT NULL, first_coach_id INTEGER NOT NULL, second_coach_id INTEGER NOT NULL, created_at TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS TEAM_MEMBER (id INTEGER PRIMARY KEY AUTOINCREMENT, team_id INTEGER NOT NULL, employee_id INTEGER NOT NULL, role TEXT NOT NULL, task TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS interview_slots (id INTEGER PRIMARY KEY AUTOINCREMENT, player_id INTEGER NOT NULL, coach_id INTEGER NOT NULL, slot_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'AVAILABLE', media_name TEXT, media_contact TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP);");
            stmt.execute("CREATE TABLE IF NOT EXISTS matches (id INTEGER PRIMARY KEY AUTOINCREMENT, team_id INTEGER, match_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, is_home INTEGER NOT NULL CHECK (is_home IN (0, 1)), facility_id INTEGER, FOREIGN KEY (facility_id) REFERENCES FACILITY(id), CHECK ((is_home = 1 AND facility_id IS NOT NULL) OR (is_home = 0 AND facility_id IS NULL)));");
            stmt.execute("CREATE TABLE IF NOT EXISTS trainings (id INTEGER PRIMARY KEY AUTOINCREMENT, team_id INTEGER, training_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, facility_id INTEGER NOT NULL, coach_id INTEGER NOT NULL);");

            stmt.execute("INSERT OR IGNORE INTO FACILITY (id, name) VALUES (1, 'Main Pitch');");
            stmt.execute("INSERT OR IGNORE INTO employees (id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) VALUES (1, 'Pedri', 'González', '11111111A', '2002-11-25', '600000001', 'SPORTS', 'Midfielder', 5000000.0, 'MALE');");
            stmt.execute("INSERT OR IGNORE INTO employees (id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) VALUES (2, 'Hansi', 'Flick', '22222222B', '1965-02-24', '611111111', 'SPORTS', 'COACH', 8000000.0, 'MALE');");
            stmt.execute("INSERT OR IGNORE INTO employees (id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) VALUES (3, 'Marcus', 'Sorg', '33333333C', '1965-12-24', '600000003', 'SPORTS', 'COACH', 4000000.0, 'MALE');");

            stmt.execute("INSERT OR IGNORE INTO TEAM (id, name, category, gender, first_coach_id, second_coach_id, created_at) VALUES (1, 'First Team', 'FIRST_TEAM', 'MALE', 2, 3, '2026-01-01');");
            stmt.execute("INSERT OR IGNORE INTO TEAM_MEMBER (id, team_id, employee_id, role) VALUES (1, 1, 1, 'PLAYER');");
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
            stmt.execute("DELETE FROM FACILITY;");
        }
    }

    @Test
    @DisplayName("US 35733: createSlot inserts slot successfully")
    void testCreateSlot_Success() throws SQLException {
        slotDAO.createSlot(1, 2, LocalDate.parse("2026-11-25"), LocalTime.parse("10:00"), LocalTime.parse("10:30"));

        List<InterviewSlotRecord> slots = slotDAO.getAllSlots();
        assertNotNull(slots);
        assertEquals(1, slots.size());
        assertEquals("Pedri González", slots.get(0).getPlayerName());
    }

    @Test
    @DisplayName("US 35733: hasAssignedInterviewOnDate returns true when status is not AVAILABLE")
    void testHasAssignedInterviewOnDate_ReturnsTrueForAssigned() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO interview_slots (player_id, coach_id, slot_date, start_time, end_time, status) VALUES (1, 2, '2026-11-25', '10:00', '10:30', 'ASSIGNED')")) {
            ps.executeUpdate();
        }

        boolean hasAssigned = slotDAO.hasAssignedInterviewOnDate(1, LocalDate.parse("2026-11-25"));
        assertTrue(hasAssigned);
    }

    @Test
    @DisplayName("US 35733: hasTeamActivityConflict detects match overlap")
    void testHasTeamActivityConflict_MatchOverlap_ReturnsTrue() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO matches (team_id, match_date, start_time, end_time, is_home, facility_id) VALUES (1, '2026-11-25', '18:00', '20:00', 1, 1)")) {
            ps.executeUpdate();
        }

        boolean conflict = slotDAO.hasTeamActivityConflict(1, LocalDate.parse("2026-11-25"), LocalTime.parse("18:30"), LocalTime.parse("19:00"));
        assertTrue(conflict);
    }

    @Test
    @DisplayName("US 35733: hasTeamActivityConflict detects training overlap")
    void testHasTeamActivityConflict_TrainingOverlap_ReturnsTrue() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO trainings (team_id, training_date, start_time, end_time, facility_id, coach_id) VALUES (1, '2026-11-25', '11:00', '13:00', 1, 2)")) {
            ps.executeUpdate();
        }

        boolean conflict = slotDAO.hasTeamActivityConflict(1, LocalDate.parse("2026-11-25"), LocalTime.parse("12:00"), LocalTime.parse("12:30"));
        assertTrue(conflict);
    }

    @Test
    @DisplayName("US 35733: getAllCoaches returns employees with position COACH")
    void testGetAllCoaches_ReturnsCoachList() throws SQLException {
        List<Employee> coaches = slotDAO.getAllCoaches();
        assertNotNull(coaches);
        assertEquals(2, coaches.size());
    }

    @Test
    @DisplayName("US 35733: getPlayersByCoach retrieves players of coach's team")
    void testGetPlayersByCoach_ReturnsPlayers() throws SQLException {
        List<Employee> players = slotDAO.getPlayersByCoach(2);
        assertNotNull(players);
        assertEquals(1, players.size());
        assertEquals("Pedri", players.get(0).getFirstName());
    }

    @Test
    @DisplayName("US 35733: getTeamActivitiesByCoach retrieves matches and trainings")
    void testGetTeamActivitiesByCoach_ReturnsCombinedList() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO matches (team_id, match_date, start_time, end_time, is_home, facility_id) VALUES (1, '2026-11-25', '18:00', '20:00', 1, 1)")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO trainings (team_id, training_date, start_time, end_time, facility_id, coach_id) VALUES (1, '2026-11-25', '10:00', '12:00', 1, 2)")) {
                ps.executeUpdate();
            }
        }

        List<TeamActivityRecord> activities = slotDAO.getTeamActivitiesByCoach(2);
        assertNotNull(activities);
        assertEquals(2, activities.size());
    }
    
    @Test
    @DisplayName("US 36755: getSlotsByTeam returns ALL slots (AVAILABLE and ASSIGNED) for a specific team")
    void testGetSlotsByTeam_ReturnsAllStatusesForSelectedTeam() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            // Team 1 (Pedri) -> 1 AVAILABLE slot, 1 ASSIGNED slot
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO interview_slots (player_id, coach_id, slot_date, start_time, end_time, status) VALUES (1, 2, '2026-11-25', '10:00', '10:30', 'AVAILABLE')")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO interview_slots (player_id, coach_id, slot_date, start_time, end_time, status) VALUES (1, 2, '2026-11-25', '11:00', '11:30', 'ASSIGNED')")) {
                ps.executeUpdate();
            }

            // Team 2 (Pablo Gavi) -> 1 AVAILABLE slot (Must NOT be retrieved for Team 1)
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO interview_slots (player_id, coach_id, slot_date, start_time, end_time, status) VALUES (4, 3, '2026-11-25', '12:00', '12:30', 'AVAILABLE')")) {
                ps.executeUpdate();
            }
        }

        List<InterviewSlotRecord> slotsTeam1 = slotDAO.getSlotsByTeam(1);
        assertNotNull(slotsTeam1);
        assertEquals(2, slotsTeam1.size());
        assertTrue(slotsTeam1.stream().anyMatch(s -> "AVAILABLE".equals(s.getStatus())));
        assertTrue(slotsTeam1.stream().anyMatch(s -> "ASSIGNED".equals(s.getStatus())));
        assertTrue(slotsTeam1.stream().allMatch(s -> "Pedri González".equals(s.getPlayerName())));
    }

    @Test
    @DisplayName("US 36755: getTeamScheduleByTeamId retrieves matches, trainings, and all interview slots for selected team")
    void testGetTeamScheduleByTeamId_ReturnsFullTeamSchedule() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            // Insert Match for Team 1
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO matches (team_id, match_date, start_time, end_time, is_home, facility_id) VALUES (1, '2026-11-25', '18:00', '20:00', 1, 1)")) {
                ps.executeUpdate();
            }
            // Insert Training for Team 1
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO trainings (team_id, training_date, start_time, end_time, facility_id, coach_id) VALUES (1, '2026-11-25', '10:00', '12:00', 1, 2)")) {
                ps.executeUpdate();
            }
            // Insert Interview Slot for Team 1 (Pedri)
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO interview_slots (player_id, coach_id, slot_date, start_time, end_time, status) VALUES (1, 2, '2026-11-25', '14:00', '14:30', 'AVAILABLE')")) {
                ps.executeUpdate();
            }
        }

        List<TeamActivityRecord> schedule = slotDAO.getTeamScheduleByTeamId(1);
        assertNotNull(schedule);
        assertEquals(3, schedule.size());
        assertTrue(schedule.stream().anyMatch(a -> "MATCH".equals(a.getType())));
        assertTrue(schedule.stream().anyMatch(a -> "TRAINING".equals(a.getType())));
        assertTrue(schedule.stream().anyMatch(a -> "INTERVIEW (AVAILABLE)".equals(a.getType())));
    }
}
