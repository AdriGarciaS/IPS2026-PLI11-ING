package com.ips2026.pl11.data.employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.employee.Employee;

public class EmployeeDAO {

	public void insert(Employee employee) throws SQLException {
        String sql = "INSERT INTO employees (first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());
            statement.setString(3, employee.getNationalId());
            statement.setString(4, employee.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            statement.setString(5, employee.getPhoneNumber());
            statement.setString(6, employee.getCategory());
            statement.setString(7, employee.getPosition());
            statement.setDouble(8, employee.getGrossAnnualSalary());

            statement.executeUpdate();
        }
    }

    public List<Employee> getAll() throws SQLException {
        String sql = "SELECT id, first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary FROM employees ORDER BY id";
        List<Employee> employees = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                employees.add(new Employee(
                        resultSet.getInt("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("national_id"),
                        LocalDate.parse(resultSet.getString("birth_date")),
                        resultSet.getString("phone_number"),
                        resultSet.getString("category"),
                        resultSet.getString("position"),
                        resultSet.getDouble("gross_annual_salary")
                ));
            }
        }
        return employees;
    }
    
    public void update(Employee employee) throws SQLException {
        // No se actualiza 'position' para respetar la regla de negocio
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, national_id = ?, "
                   + "birth_date = ?, phone_number = ?, category = ?, gross_annual_salary = ? "
                   + "WHERE id = ?";

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());
            statement.setString(3, employee.getNationalId());
            statement.setString(4, employee.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            statement.setString(5, employee.getPhoneNumber());
            statement.setString(6, employee.getCategory());
            statement.setDouble(7, employee.getGrossAnnualSalary());
            statement.setInt(8, employee.getId());

            statement.executeUpdate();
        }
    }
    
    public void delete(int employeeId) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);
            statement.executeUpdate();
        }
    }
}
