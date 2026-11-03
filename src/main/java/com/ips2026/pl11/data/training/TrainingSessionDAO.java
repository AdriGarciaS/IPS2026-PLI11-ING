package com.ips2026.pl11.data.training;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.training.TrainingSession;

public class TrainingSessionDAO {
    public void insert(TrainingSession trainingSession) throws SQLException {
        String sql = "insert into trainings (facility_id, team_id, coach_id, training_date, start_time, end_time) values (?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConexionBD.obtenerConexion();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, trainingSession.getFacility());
            statement.setLong(2, trainingSession.getTeam());
            statement.setLong(3, trainingSession.getCoach());
            statement.setString(4, String.valueOf(trainingSession.getDate()));
            statement.setString(5, String.valueOf(trainingSession.getStartTime()));
            statement.setString(6, String.valueOf(trainingSession.getEndTime()));

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error inserting training session: " + e.getMessage(), e);
        }
    }

    public List<TrainingSession> findAll() {
        String sql = "select * from trainings";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            var resultSet = statement.executeQuery();
            List<TrainingSession> trainingSessions = new java.util.ArrayList<>();

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                long facilityId = resultSet.getLong("facility_id");
                long teamId = resultSet.getLong("team_id");
                long coachId = resultSet.getLong("coach_id");
                LocalDate date = LocalDate.parse(resultSet.getString("training_date"));
                LocalTime startTime = LocalTime.parse(resultSet.getString("start_time"));
                LocalTime endTime = LocalTime.parse(resultSet.getString("end_time"));

                TrainingSession trainingSession = new TrainingSession(id, date, startTime, endTime, facilityId, coachId, teamId);
                trainingSessions.add(trainingSession);
            }

            return trainingSessions;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving training sessions: " + e.getMessage(), e);
        }
    }
}
