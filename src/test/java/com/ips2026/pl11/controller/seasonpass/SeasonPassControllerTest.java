package com.ips2026.pl11.controller.seasonpass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;

public class SeasonPassControllerTest {

    private SeasonPassController controller;

    @BeforeEach
    public void setUp() throws SQLException {
        controller = new SeasonPassController();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            // Drop existing tables to avoid schema mismatches with preexisting database instances
            stmt.execute("DROP TABLE IF EXISTS SEASON_PASS;");
            stmt.execute("DROP TABLE IF EXISTS FAN;");
            stmt.execute("DROP TABLE IF EXISTS TEAM;");
            stmt.execute("DROP TABLE IF EXISTS employees;");

            stmt.execute("CREATE TABLE IF NOT EXISTS FAN (id INTEGER PRIMARY KEY AUTOINCREMENT, dni TEXT NOT NULL UNIQUE, full_name TEXT NOT NULL, email TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP);");
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT NOT NULL, last_name TEXT NOT NULL, national_id TEXT NOT NULL UNIQUE, birth_date TEXT NOT NULL, phone_number TEXT NOT NULL, category TEXT NOT NULL, position TEXT NOT NULL, gross_annual_salary REAL NOT NULL, gender TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS TEAM (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, category TEXT NOT NULL, gender TEXT NOT NULL, first_coach_id INTEGER NOT NULL, second_coach_id INTEGER NOT NULL, created_at TEXT NOT NULL);");
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
    @DisplayName("US 35738: Calculate price discounts per age category")
    void testCalculatePrice_DiscountsApplied() {
        double base = 100.0;

        assertEquals(50.0, controller.calculatePrice(base, "Children"), 0.001);
        assertEquals(80.0, controller.calculatePrice(base, "Under 26"), 0.001);
        assertEquals(70.0, controller.calculatePrice(base, "Seniors"), 0.001);
        assertEquals(100.0, controller.calculatePrice(base, "Adults"), 0.001);
    }

    @Test
    @DisplayName("US 35738: Blank holder details throw IllegalArgumentException")
    void testPurchaseSeasonPass_BlankDetails_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            controller.purchaseSeasonPass("", "Juan", "juan@test.com", "First Team", "A", "A", 1, 1, "Adults", 100.0);
        });
        assertEquals("All pass holder details (DNI, Name, Email) are required.", ex.getMessage());
    }

    @Test
    @DisplayName("US 35738: Already occupied seat throws IllegalStateException")
    void testPurchaseSeasonPass_OccupiedSeat_ThrowsException() throws Exception {
        controller.purchaseSeasonPass("12345678A", "Juan", "juan@test.com", "First Team", "A", "A", 1, 1, "Adults", 100.0);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            controller.purchaseSeasonPass("87654321B", "Pedro", "pedro@test.com", "First Team", "A", "A", 1, 1, "Adults", 100.0);
        });
        assertEquals("Seat Row 1, Seat 1 is already occupied by another season pass holder.", ex.getMessage());
    }

    @Test
    @DisplayName("US 35738: getOccupiedSeasonPassSeats returns 10x15 grid")
    void testGetOccupiedSeasonPassSeats_ReturnsGrid() throws Exception {
        controller.purchaseSeasonPass("12345678A", "Juan", "juan@test.com", "First Team", "B", "B", 3, 4, "Adults", 100.0);

        boolean[][] grid = controller.getOccupiedSeasonPassSeats("First Team", "B", "B");
        assertTrue(grid[2][3]);
        assertFalse(grid[0][0]);
    }
}