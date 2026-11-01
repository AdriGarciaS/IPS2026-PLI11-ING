package com.ips2026.pl11.data.gardening;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.gardening.GardeningSession;

public class GardeningSessionDAO {

    public void insert(GardeningSession gardeningSession) throws SQLException {
        String sql = "insert into gardening_session (facility_id, description, type, date, start_time, end_time) values (?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConexionBD.obtenerConexion();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, gardeningSession.getFacilityId());
            statement.setString(2, gardeningSession.getDescription());
            statement.setString(3, gardeningSession.getSessionType());
            statement.setString(4, gardeningSession.getDate());
            statement.setString(5, gardeningSession.getStartTime());
            statement.setString(6, gardeningSession.getEndTime());

            statement.executeUpdate();
        }
    }

    public List<GardeningSession> findAll() throws SQLException {
        String sql = "SELECT * FROM gardening_session";

        try (Connection connection = ConexionBD.obtenerConexion();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            var resultSet = statement.executeQuery();
            List<GardeningSession> sessions = new java.util.ArrayList<>();

            while (resultSet.next()) {
                GardeningSession session = new GardeningSession(resultSet.getLong("id"),
                    resultSet.getLong("facility_id"), resultSet.getString("description"), resultSet.getString("type"),
                    resultSet.getString("date"), resultSet.getString("start_time"), resultSet.getString("end_time"));

                sessions.add(session);
            }
            return sessions;
        }
    }

    public List<GardeningSession> findByGardenerId(int gardenerId) throws SQLException {
        String sql = "SELECT gs.* FROM gardening_session gs JOIN gardening_session_employee gse ON gs.id = gse.gardening_session_id WHERE gse.employee_id = ?";

        try (Connection connection = ConexionBD.obtenerConexion();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, gardenerId);
            var resultSet = statement.executeQuery();
            List<GardeningSession> sessions = new java.util.ArrayList<>();

            while (resultSet.next()) {
                GardeningSession session = new GardeningSession(resultSet.getLong("id"),
                    resultSet.getLong("facility_id"), resultSet.getString("description"), resultSet.getString("type"),
                    resultSet.getString("date"), resultSet.getString("start_time"), resultSet.getString("end_time"));

                sessions.add(session);
            }
            return sessions;
        }
    }

}
