package com.ips2026.pl11.controlador;

import com.ips2026.pl11.datos.EmployeeDAO;
import com.ips2026.pl11.modelo.Employee;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EmployeeController {

    private final EmployeeDAO employeeDAO;

    public EmployeeController(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public void registerEmployee(String firstName, String lastName, String nationalId,LocalDate birthDate, String phoneNumber,String category, String position, double salary) throws Exception {

        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank() || nationalId == null || nationalId.isBlank() || phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("All personal information fields are required.");
        }

        if (salary < 0) {
            throw new IllegalArgumentException("Gross annual salary must be greater than or equal to 0.");
        }

        Employee employee = new Employee(null,firstName.trim(),lastName.trim(), nationalId.trim(),birthDate,phoneNumber.trim(),category,position,salary);

        // Business rule: Underage employees are only permitted for the 'Player' position
        if (employee.isUnderage() && !position.equalsIgnoreCase("Player")) {
            throw new IllegalArgumentException("Only players can be under 18 years of age.");
        }

        employeeDAO.insert(employee);
    }
    
    public List<Employee> getEmployees() throws SQLException {
        return employeeDAO.getAll();
    }

    public void updateEmployee(int id, String firstName, String lastName, String nationalId,LocalDate birthDate, String phoneNumber, String category,String currentPosition, double salary) throws Exception {

        if (firstName == null || firstName.isBlank()|| lastName == null || lastName.isBlank()|| nationalId == null || nationalId.isBlank()|| phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("All personal information fields are required.");
        }

        if (salary < 0) {
            throw new IllegalArgumentException("Gross annual salary must be greater than or equal to 0.");
        }

        Employee employee = new Employee(id,firstName.trim(),lastName.trim(),nationalId.trim(),birthDate,phoneNumber.trim(),category,currentPosition,salary);

        if (employee.isUnderage() && !currentPosition.equalsIgnoreCase("Player")) {
            throw new IllegalArgumentException("Only players can be under 18 years of age.");
        }

        employeeDAO.update(employee);
    }

    public void deleteEmployee(int employeeId) throws SQLException {
        employeeDAO.delete(employeeId);
    }
}
