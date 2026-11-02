package com.ips2026.pl11.data.training;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.training.TrainingSession;

public class TrainingSessionDAO {
    public void insert(TrainingSession trainingSession) throws SQLException {
        String sql = "insert into training_session (facility_id, team_id, coach_id, training_date, start_time, end_time) values (?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConexionBD.obtenerConexion();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, trainingSession.getFacility());
            statement.setLong(2, trainingSession.getTeam());
            statement.setLong(3, trainingSession.getCoach());
            statement.setString(4, trainingSession.getDate());
            statement.setString(5, trainingSession.getStartTime());
            statement.setString(6, trainingSession.getEndTime());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error inserting training session: " + e.getMessage(), e);
        }
    }
}
