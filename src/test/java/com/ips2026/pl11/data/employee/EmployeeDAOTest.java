package com.ips2026.pl11.data.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.employee.Employee;

/**
 * Integration Test Suite for EmployeeDAO.
 * Tests CRUD operations and role-based queries against SQLite database.
 */
public class EmployeeDAOTest {

    private EmployeeDAO employeeDAO;

    /**
     * Sets up in-memory/local SQLite table schema prior to each test.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        employeeDAO = new EmployeeDAO();

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "first_name TEXT NOT NULL, " +
                         "last_name TEXT NOT NULL, " +
                         "national_id TEXT NOT NULL UNIQUE, " +
                         "birth_date TEXT NOT NULL, " +
                         "phone_number TEXT NOT NULL, " +
                         "category TEXT NOT NULL, " +
                         "position TEXT NOT NULL, " +
                         "gross_annual_salary REAL NOT NULL, " +
                         "gender TEXT NOT NULL);");
        }
    }

    /**
     * Cleans up the employees table after each test execution.
     */
    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM employees;");
        }
    }

    @Test
    @DisplayName("insert and getAll: successfully inserts employee and retrieves full list")
    void testInsertAndGetAll_Success() throws SQLException {
        Employee emp = new Employee(null, "Pedri", "González", "11111111A",
                LocalDate.of(2002, 11, 25), "600000001", "SPORTS", "PLAYER", 5000000.0, "MALE");

        employeeDAO.insert(emp);

        List<Employee> list = employeeDAO.getAll();
        assertNotNull(list, "Retrieved list must not be null.");
        assertEquals(1, list.size(), "Database must contain exactly 1 employee.");
        assertEquals("Pedri", list.get(0).getFirstName());
        assertEquals("González", list.get(0).getLastName());
        assertEquals("11111111A", list.get(0).getNationalId());
    }

    @Test
    @DisplayName("findById: returns employee Optional when ID exists")
    void testFindById_Success() throws SQLException {
        Employee emp = new Employee(null, "Hansi", "Flick", "22222222B",
                LocalDate.of(1965, 2, 24), "611111111", "SPORTS", "COACH", 8000000.0, "MALE");

        employeeDAO.insert(emp);
        List<Employee> all = employeeDAO.getAll();
        int insertedId = all.get(0).getId();

        Optional<Employee> found = employeeDAO.findById(insertedId);
        assertTrue(found.isPresent(), "Employee must be found by valid ID.");
        assertEquals("Hansi", found.get().getFirstName());
        assertEquals("COACH", found.get().getPosition());
    }

    @Test
    @DisplayName("findById: returns empty Optional when ID does not exist")
    void testFindById_NotFound_ReturnsEmptyOptional() throws SQLException {
        Optional<Employee> found = employeeDAO.findById(999);
        assertFalse(found.isPresent(), "Optional must be empty for non-existent ID.");
    }

    @Test
    @DisplayName("update: updates employee fields in database excluding position")
    void testUpdate_Success() throws SQLException {
        Employee emp = new Employee(null, "Pablo", "Gavi", "33333333C",
                LocalDate.of(2004, 8, 5), "600000002", "SPORTS", "PLAYER", 4000000.0, "MALE");
        employeeDAO.insert(emp);

        List<Employee> all = employeeDAO.getAll();
        int insertedId = all.get(0).getId();

        Employee updatedEmp = new Employee(insertedId, "Pablo Martin", "Paez Gavi", "33333333C",
                LocalDate.of(2004, 8, 5), "699999999", "SPORTS", "MIDFIELDER", 6000000.0, "MALE");

        employeeDAO.update(updatedEmp);

        Optional<Employee> found = employeeDAO.findById(insertedId);
        assertTrue(found.isPresent());
        assertEquals("Pablo Martin", found.get().getFirstName());
        assertEquals("Paez Gavi", found.get().getLastName());
        assertEquals("699999999", found.get().getPhoneNumber());
        assertEquals(6000000.0, found.get().getGrossAnnualSalary(), 0.001);
        // Position remains unchanged in DB as per business rule
        assertEquals("PLAYER", found.get().getPosition());
    }

    @Test
    @DisplayName("delete: removes employee record from database")
    void testDelete_Success() throws SQLException {
        Employee emp = new Employee(null, "Marcus", "Sorg", "44444444D",
                LocalDate.of(1965, 12, 24), "600000003", "SPORTS", "COACH", 4000000.0, "MALE");
        employeeDAO.insert(emp);

        List<Employee> all = employeeDAO.getAll();
        int insertedId = all.get(0).getId();

        employeeDAO.delete(insertedId);

        Optional<Employee> found = employeeDAO.findById(insertedId);
        assertFalse(found.isPresent(), "Employee record should be deleted.");
        assertEquals(0, employeeDAO.getAll().size());
    }

    @Test
    @DisplayName("getEmployeesByRole: filters employees strictly by position role")
    void testGetEmployeesByRole_Success() throws SQLException {
        Employee player = new Employee(null, "Pedri", "González", "11111111A",
                LocalDate.of(2002, 11, 25), "600000001", "SPORTS", "PLAYER", 5000000.0, "MALE");
        Employee coach = new Employee(null, "Hansi", "Flick", "22222222B",
                LocalDate.of(1965, 2, 24), "611111111", "SPORTS", "COACH", 8000000.0, "MALE");

        employeeDAO.insert(player);
        employeeDAO.insert(coach);

        List<Employee> coaches = employeeDAO.getEmployeesByRole("COACH");
        assertNotNull(coaches);
        assertEquals(1, coaches.size());
        assertEquals("Hansi", coaches.get(0).getFirstName());

        List<Employee> players = employeeDAO.getEmployeesByRole("PLAYER");
        assertNotNull(players);
        assertEquals(1, players.size());
        assertEquals("Pedri", players.get(0).getFirstName());
    }
}