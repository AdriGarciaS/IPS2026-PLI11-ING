package com.ips2026.pl11.data.injury;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.injury.Injury;

public class InjuryDAO {

    public void registerInjury(Injury injury) throws SQLException {
        String sql = "INSERT INTO injury (player_id, match_id, training_id, description, injury_date, is_healed, doctor_id, injury_time, evolution_notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (var connection = ConexionBD.obtenerConexion(); var pst = connection.prepareStatement(sql)) {
            pst.setInt(1, injury.getPlayerId());
            pst.setInt(2, injury.getMatchId());
            pst.setInt(3, injury.getTrainingId());
            pst.setString(4, injury.getDescription());
            pst.setString(5, String.valueOf(injury.getInjuryDate()));
            pst.setInt(6, injury.isHealed() ? 1 : 0);
            pst.setInt(7, injury.getDoctorId());
            pst.setString(8, String.valueOf(injury.getInjuryTime()));
            pst.setString(9, injury.getParsedEvolutionNotes()); 

            pst.executeUpdate();
        }
    }

    public List<Injury> getAllInjuries() throws SQLException {
        List<Injury> injuries = new ArrayList<>();
        String sql = "SELECT * FROM injury";
        try (var connection = ConexionBD.obtenerConexion();
            var pst = connection.prepareStatement(sql);
            var rs = pst.executeQuery()) {
            while (rs.next()) {
                Injury injury = new Injury(rs.getInt("id"), rs.getInt("player_id"), rs.getInt("match_id"),
                    rs.getInt("training_id"), rs.getString("description"), LocalDate.parse(rs.getString("injury_date")),
                    rs.getInt("is_healed") == 1, rs.getInt("doctor_id"), LocalTime.parse(rs.getString("injury_time")),
                    rs.getString("evolution_notes"));
                injuries.add(injury);
            }
        }
        return injuries;
    }

}
