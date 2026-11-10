package com.ips2026.pl11.data.seasonpass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;

public class SeasonPassDAOTest {

    private SeasonPassDAO seasonPassDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        seasonPassDAO = new SeasonPassDAO();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS FAN (id INTEGER PRIMARY KEY AUTOINCREMENT, dni TEXT NOT NULL UNIQUE, full_name TEXT NOT NULL, email TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP);");
            stmt.execute("CREATE TABLE IF NOT EXISTS TEAM (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, category TEXT NOT NULL, gender TEXT NOT NULL, first_coach_id INTEGER NOT NULL, second_coach_id INTEGER NOT NULL, created_at TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT NOT NULL, last_name TEXT NOT NULL, national_id TEXT NOT NULL UNIQUE, birth_date TEXT NOT NULL, phone_number TEXT NOT NULL, category TEXT NOT NULL, position TEXT NOT NULL, gross_annual_salary REAL NOT NULL, gender TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS SEASON_PASS (id INTEGER PRIMARY KEY AUTOINCREMENT, fan_id INTEGER NOT NULL, team_id INTEGER NOT NULL, grandstand TEXT NOT NULL, section TEXT NOT NULL, row_num INTEGER NOT NULL, seat_num INTEGER NOT NULL, age_category TEXT NOT NULL, price REAL NOT NULL, start_date TEXT NOT NULL, end_date TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (team_id) REFERENCES TEAM(id), UNIQUE (team_id, grandstand, section, row_num, seat_num, start_date));");

            stmt.execute("INSERT OR IGNORE INTO employees (id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) VALUES (1, 'Coach', 'One', '00000000A', '1980-01-01', '600000000', 'SPORTS', 'COACH', 100000.0, 'MALE');");
            stmt.execute("INSERT OR IGNORE INTO employees (id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) VALUES (2, 'Coach', 'Two', '00000000B', '1980-01-01', '600000000', 'SPORTS', 'COACH', 100000.0, 'MALE');");
            stmt.execute("INSERT OR IGNORE INTO TEAM (id, name, category, gender, first_coach_id, second_coach_id, created_at) VALUES (1, 'First Team', 'FIRST_TEAM', 'MALE', 1, 2, '2026-01-01');");
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM SEASON_PASS;");
            stmt.execute("DELETE FROM FAN;");
            stmt.execute("DELETE FROM TEAM;");
            stmt.execute("DELETE FROM employees;");
        }
    }

    @Test
    @DisplayName("US 35738: createSeasonPassWithHolder creates fan and season pass")
    void testCreateSeasonPassWithHolder_Success() throws SQLException {
        boolean created = seasonPassDAO.createSeasonPassWithHolder("12345678A", "Juan Pérez", "juan@test.com", "First Team", "A", "A", 3, 5, "Adults", 200.00, "2026-09-01", "2027-06-30");
        assertTrue(created);

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM FAN WHERE dni = '12345678A'")) {
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    @DisplayName("US 35738: isSeatPassOccupiedByName returns true when occupied")
    void testIsSeatPassOccupiedByName_Occupied_ReturnsTrue() throws SQLException {
        seasonPassDAO.createSeasonPassWithHolder("12345678A", "Juan Pérez", "juan@test.com", "First Team", "A", "B", 4, 6, "Adults", 200.00, "2026-09-01", "2027-06-30");

        boolean occupied = seasonPassDAO.isSeatPassOccupiedByName("First Team", "A", "B", 4, 6, "2026-09-01");
        assertTrue(occupied);
    }

    @Test
    @DisplayName("US 35738: getActivePassSeatsForMatch returns zero-indexed positions")
    void testGetActivePassSeatsForMatch_ReturnsZeroBasedIndexes() throws SQLException {
        seasonPassDAO.createSeasonPassWithHolder("12345678A", "Juan Pérez", "juan@test.com", "First Team", "B", "C", 5, 10, "Adults", 200.00, "2026-09-01", "2027-06-30");

        List<int[]> reserved = seasonPassDAO.getActivePassSeatsForMatch("First Team", "B", "C", "2026-11-20");
        assertNotNull(reserved);
        assertEquals(1, reserved.size());
        assertEquals(4, reserved.get(0)[0]);
        assertEquals(9, reserved.get(0)[1]);
    }

    @Test
    @DisplayName("US 35777: getMemberRegularSeat returns member's seat details")
    void testGetMemberRegularSeat_ExistingMember_ReturnsSeatArray() throws SQLException {
        seasonPassDAO.createSeasonPassWithHolder("87654321Z", "Maria Gomez", "maria@test.com", "First Team", "C", "D", 2, 8, "Adults", 200.00, "2026-09-01", "2027-06-30");

        String[] seat = seasonPassDAO.getMemberRegularSeat("87654321Z", "First Team");
        assertNotNull(seat);
        assertEquals("C", seat[0]);
        assertEquals("D", seat[1]);
        assertEquals("2", seat[2]);
        assertEquals("8", seat[3]);
    }

    @Test
    @DisplayName("US 35777: getMemberRegularSeat returns null for non-existing member")
    void testGetMemberRegularSeat_NonExisting_ReturnsNull() throws SQLException {
        String[] seat = seasonPassDAO.getMemberRegularSeat("99999999X", "First Team");
        assertNull(seat);
    }
}
