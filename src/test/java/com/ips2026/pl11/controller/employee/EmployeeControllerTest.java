package com.ips2026.pl11.controller.employee;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.model.employee.Employee;

public class EmployeeControllerTest {

    private EmployeeDAO employeeDAO;
    private EmployeeController controller;

    @BeforeEach
    public void setUp() throws SQLException {
        employeeDAO = new EmployeeDAO();
        controller = new EmployeeController(employeeDAO);

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS interviews;");
            stmt.execute("DROP TABLE IF EXISTS interview_slots;");
            stmt.execute("DROP TABLE IF EXISTS gardening_session_employee;");
            stmt.execute("DROP TABLE IF EXISTS TEAM_MEMBER;");
            stmt.execute("DROP TABLE IF EXISTS TEAM;");
            stmt.execute("DROP TABLE IF EXISTS employees;");

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
                         "gender TEXT NOT NULL, " +
                         "username TEXT UNIQUE COLLATE NOCASE, " +
                         "password TEXT);");
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM employees;");
        }
    }

    @Test
    @DisplayName("registerEmployee: successfully inserts valid adult employee")
    void testRegisterEmployee_Success() {
        LocalDate adultBirthDate = LocalDate.now().minusYears(25);

        assertDoesNotThrow(() ->
            controller.registerEmployee("Pedri", "González", "11111111A", adultBirthDate,
                "600000001", "SPORTS", "Player", 5000000.0, "MALE", "pedri_gonzalez", "securePassword123")
        );

        assertDoesNotThrow(() -> {
            List<Employee> list = controller.getEmployees();
            assertEquals(1, list.size());
            assertEquals("Pedri", list.get(0).getFirstName());
        });
    }

    @Test
    @DisplayName("registerEmployee: throws IllegalArgumentException when required fields are blank or null")
    void testRegisterEmployee_RequiredFieldsValidation() {
        LocalDate birthDate = LocalDate.now().minusYears(25);

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
            controller.registerEmployee("  ", "González", "11111111A", birthDate,
                "600000001", "SPORTS", "Player", 5000000.0, "MALE", "pedri_gonzalez", "securePassword123")
        );
        assertEquals("All personal information fields are required.", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
            controller.registerEmployee("Pedri", "González", null, birthDate,
                "600000001", "SPORTS", "Player", 5000000.0, "MALE", "pedri_gonzalez", "securePassword123")
        );
        assertEquals("All personal information fields are required.", ex2.getMessage());
    }

    @Test
    @DisplayName("registerEmployee: throws IllegalArgumentException when salary is negative")
    void testRegisterEmployee_NegativeSalary_ThrowsException() {
        LocalDate birthDate = LocalDate.now().minusYears(25);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            controller.registerEmployee("Hansi", "Flick", "22222222B", birthDate,
                "611111111", "SPORTS", "COACH", -100.0, "MALE", "pedri_gonzalez", "securePassword123")
        );
        assertEquals("Gross annual salary must be greater than or equal to 0.", ex.getMessage());
    }

    @Test
    @DisplayName("registerEmployee: throws IllegalArgumentException when gender is invalid")
    void testRegisterEmployee_InvalidGender_ThrowsException() {
        LocalDate birthDate = LocalDate.now().minusYears(25);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            controller.registerEmployee("Pedri", "González", "11111111A", birthDate,
                "600000001", "SPORTS", "Player", 5000000.0, "OTHER", "pedri_gonzalez", "securePassword123")
        );
        assertEquals("Choose the gender of the employee.", ex.getMessage());
    }

    @Test
    @DisplayName("registerEmployee: permits underage employees only for 'Player' position")
    void testRegisterEmployee_UnderageRules() {
        LocalDate minorBirthDate = LocalDate.now().minusYears(16);

        assertDoesNotThrow(() ->
            controller.registerEmployee("Young", "Talent", "33333333C", minorBirthDate,
                "600000002", "SPORTS", "Player", 100000.0, "MALE", "pedri_gonzalez", "securePassword123")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            controller.registerEmployee("Young", "Coach", "44444444D", minorBirthDate,
                "600000003", "SPORTS", "COACH", 100000.0, "MALE", "pedri_gonzalez", "securePassword123")
        );
        assertEquals("Only players can be under 18 years of age.", ex.getMessage());
    }

    @Test
    @DisplayName("updateEmployee: successfully updates existing employee details")
    void testUpdateEmployee_Success() throws Exception {
        LocalDate birthDate = LocalDate.now().minusYears(25);
        controller.registerEmployee("Pablo", "Gavi", "55555555E", birthDate,
            "600000004", "SPORTS", "Player", 4000000.0, "MALE", "pedri_gonzalez", "securePassword123");

        List<Employee> list = controller.getEmployees();
        int id = list.get(0).getId();

        assertDoesNotThrow(() ->
            controller.updateEmployee(id, "Pablo Martin", "Paez Gavi", "55555555E", birthDate,
                "699999999", "SPORTS", "Player", 6000000.0, "MALE", "pedri_gonzalez", "securePassword123")
        );

        Optional<Employee> updated = employeeDAO.findById(id);
        assertTrue(updated.isPresent());
        assertEquals("Pablo Martin", updated.get().getFirstName());
        assertEquals("699999999", updated.get().getPhoneNumber());
        assertEquals(6000000.0, updated.get().getGrossAnnualSalary(), 0.001);
    }

    @Test
    @DisplayName("updateEmployee: throws IllegalArgumentException when updating underage employee to non-player position")
    void testUpdateEmployee_UnderagePositionConflict_ThrowsException() throws Exception {
        LocalDate minorBirthDate = LocalDate.now().minusYears(16);
        controller.registerEmployee("Young", "Player", "66666666F", minorBirthDate,
            "600000005", "SPORTS", "Player", 50000.0, "FEMALE", "pedri_gonzalez", "securePassword123");

        int id = controller.getEmployees().get(0).getId();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            controller.updateEmployee(id, "Young", "Player", "66666666F", minorBirthDate,
                "600000005", "SPORTS", "ASSISTANT", 50000.0, "FEMALE", "pedri_gonzalez", "securePassword123")
        );
        assertEquals("Only players can be under 18 years of age.", ex.getMessage());
    }

    @Test
    @DisplayName("deleteEmployee: removes employee record from database")
    void testDeleteEmployee_Success() throws Exception {
        LocalDate birthDate = LocalDate.now().minusYears(30);
        controller.registerEmployee("Marcus", "Sorg", "77777777G", birthDate,
            "600000006", "SPORTS", "COACH", 3000000.0, "MALE", "pedri_gonzalez", "securePassword123");

        int id = controller.getEmployees().get(0).getId();
        controller.deleteEmployee(id);

        assertEquals(0, controller.getEmployees().size());
    }

    @Test
    @DisplayName("getEmployeesByRole: retrieves list of employees matching specific position")
    void testGetEmployeesByRole_Success() throws Exception {
        LocalDate birthDate = LocalDate.now().minusYears(30);
        controller.registerEmployee("Pedri", "González", "11111111A", birthDate,
            "600000001", "SPORTS", "Player", 5000000.0, "MALE", "pedri_gonzalez1", "securePassword123");
        controller.registerEmployee("Hansi", "Flick", "22222222B", birthDate,
            "611111111", "SPORTS", "COACH", 8000000.0, "MALE", "pedri_gonzalez2", "securePassword123");

        List<Employee> coaches = controller.getEmployeesByRole("COACH");
        assertNotNull(coaches);
        assertEquals(1, coaches.size());
        assertEquals("Hansi", coaches.get(0).getFirstName());
    }
}