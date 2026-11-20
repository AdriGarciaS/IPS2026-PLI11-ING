package com.ips2026.pl11.model.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Test Suite for Employee model.
 * Verifies constructor initialization, getter methods, and age calculation logic (isUnderage).
 */
public class EmployeeTest {

    @Test
    @DisplayName("Employee: correctly initializes and exposes all fields via getters")
    void testEmployee_GettersMatchConstructorValues() {
        Integer id = 10;
        String firstName = "Pedri";
        String lastName = "González";
        String nationalId = "11111111A";
        LocalDate birthDate = LocalDate.of(2002, 11, 25);
        String phoneNumber = "600000001";
        String category = "SPORTS";
        String position = "PLAYER";
        double grossAnnualSalary = 5000000.0;
        String gender = "MALE";

        String username = "pedri_gonzalez";
        String password = "securePassword123";
        
        Employee employee = new Employee(id, firstName, lastName, nationalId, birthDate,
                                         phoneNumber, category, position, grossAnnualSalary, gender, username, password);

        assertNotNull(employee, "The created Employee instance must not be null.");
        assertEquals(id, employee.getId(), "Employee ID must match constructor argument.");
        assertEquals(firstName, employee.getFirstName(), "First name must match constructor argument.");
        assertEquals(lastName, employee.getLastName(), "Last name must match constructor argument.");
        assertEquals(nationalId, employee.getNationalId(), "National ID must match constructor argument.");
        assertEquals(birthDate, employee.getBirthDate(), "Birth date must match constructor argument.");
        assertEquals(phoneNumber, employee.getPhoneNumber(), "Phone number must match constructor argument.");
        assertEquals(category, employee.getCategory(), "Category must match constructor argument.");
        assertEquals(position, employee.getPosition(), "Position must match constructor argument.");
        assertEquals(grossAnnualSalary, employee.getGrossAnnualSalary(), 0.001, "Salary must match constructor argument.");
        assertEquals(gender, employee.getGender(), "Gender must match constructor argument.");
    }

    @Test
    @DisplayName("isUnderage: returns true when employee age is less than 18")
    void testIsUnderage_Under18_ReturnsTrue() {
        // Birth date set 16 years ago from current date
        LocalDate minorBirthDate = LocalDate.now().minusYears(16);
        Employee employee = new Employee(1, "Young", "Talent", "22222222B", minorBirthDate,
                                         "600000002", "SPORTS", "PLAYER", 100000.0, "MALE", "young_talent", "securePassword123");

        assertTrue(employee.isUnderage(), "isUnderage must return true for employees under 18 years old.");
    }

    @Test
    @DisplayName("isUnderage: returns false when employee age is 18 or older")
    void testIsUnderage_18OrOlder_ReturnsFalse() {
        // Birth date set exactly 18 years ago from current date
        LocalDate adultBirthDate = LocalDate.now().minusYears(18);
        Employee adultEmployee = new Employee(2, "Adult", "Player", "33333333C", adultBirthDate,
                                              "600000003", "SPORTS", "PLAYER", 500000.0, "MALE", "adult_player", "securePassword123");

        assertFalse(adultEmployee.isUnderage(), "isUnderage must return false for employees who are 18 or older.");
    }

    @Test
    @DisplayName("isUnderage: returns false when birth date is null")
    void testIsUnderage_NullBirthDate_ReturnsFalse() {
        Employee employee = new Employee(3, "Unknown", "Age", "44444444D", null,
                                         "600000004", "NON_SPORTS", "STAFF", 30000.0, "FEMALE", "employee", "securePassword123");

        assertFalse(employee.isUnderage(), "isUnderage must return false when birth date is null.");
    }
}
