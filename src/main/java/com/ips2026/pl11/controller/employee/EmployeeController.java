package com.ips2026.pl11.controller.employee;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.model.employee.Employee;

public class EmployeeController {

    private final EmployeeDAO employeeDAO;

    public EmployeeController(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public void registerEmployee(String firstName, String lastName, String nationalId, LocalDate birthDate,
        String phoneNumber, String category, String position, double salary, String gender) throws Exception {

        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank() || nationalId == null
            || nationalId.isBlank() || phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("All personal information fields are required.");
        }

        if (salary < 0) {
            throw new IllegalArgumentException("Gross annual salary must be greater than or equal to 0.");
        }

        checkGender(gender);

        Employee employee = new Employee(null, firstName.trim(), lastName.trim(), nationalId.trim(), birthDate,
            phoneNumber.trim(), category, position, salary, gender);

        // Business rule: Underage employees are only permitted for the 'Player'
        // position
        if (employee.isUnderage() && !position.equalsIgnoreCase("Player")) {
            throw new IllegalArgumentException("Only players can be under 18 years of age.");
        }

        employeeDAO.insert(employee);
    }

    public List<Employee> getEmployees() throws SQLException {
        return employeeDAO.getAll();
    }

    public void updateEmployee(int id, String firstName, String lastName, String nationalId, LocalDate birthDate,
        String phoneNumber, String category, String currentPosition, double salary, String gender) throws Exception {

        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank() || nationalId == null
            || nationalId.isBlank() || phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("All personal information fields are required.");
        }

        if (salary < 0) {
            throw new IllegalArgumentException("Gross annual salary must be greater than or equal to 0.");
        }

        checkGender(gender);

        Employee employee = new Employee(id, firstName.trim(), lastName.trim(), nationalId.trim(), birthDate,
            phoneNumber.trim(), category, currentPosition, salary, gender);

        if (employee.isUnderage() && !currentPosition.equalsIgnoreCase("Player")) {
            throw new IllegalArgumentException("Only players can be under 18 years of age.");
        }

        employeeDAO.update(employee);
    }

    // Gender is required: players can only join teams of their gender
    private void checkGender(String gender) {
        if (!"MALE".equals(gender) && !"FEMALE".equals(gender)) {
            throw new IllegalArgumentException("Choose the gender of the employee.");
        }
    }

    public void deleteEmployee(int employeeId) throws SQLException {
        employeeDAO.delete(employeeId);
    }

    public List<Employee> getEmployeesByRole(String string) throws SQLException {
        return employeeDAO.getEmployeesByRole(string);

    }
}
