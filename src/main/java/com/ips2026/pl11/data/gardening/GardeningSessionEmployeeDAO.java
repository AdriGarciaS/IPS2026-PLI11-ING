package com.ips2026.pl11.data.gardening;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;

public class GardeningSessionEmployeeDAO {

    public void insert(long sessionId, long employeeId) throws SQLException {
        String sql = """
            INSERT INTO gardening_session_employee
                (gardening_session_id, employee_id)
            VALUES (?, ?)
            """;

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, sessionId);
            statement.setLong(2, employeeId);

            statement.executeUpdate();
        }
    }

    public void delete(long sessionId, long employeeId) throws SQLException {
        String sql = """
            DELETE FROM gardening_session_employee
            WHERE gardening_session_id = ?
            AND employee_id = ?
            """;

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, sessionId);
            statement.setLong(2, employeeId);

            statement.executeUpdate();
        }
    }

    public List<Long> findEmployeeIdsBySessionId(long sessionId) throws SQLException {
        String sql = """
            SELECT employee_id
            FROM gardening_session_employee
            WHERE gardening_session_id = ?
            """;

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, sessionId);

            var resultSet = statement.executeQuery();
            List<Long> employeeIds = new ArrayList<>();

            while (resultSet.next()) {
                employeeIds.add(resultSet.getLong("employee_id"));
            }

            return employeeIds;
        }
    }

}
