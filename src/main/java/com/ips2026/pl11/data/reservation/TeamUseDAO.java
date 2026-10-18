package com.ips2026.pl11.data.reservation;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.reservation.TeamUse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the uses of the facilities by the teams of the club: the trainings
 * and the home matches (matches played in the field of the rival do not
 * use our facilities).
 */
public class TeamUseDAO {

    /**
     * Uses of a facility on a day, ordered by start time. The team name says
     * what it is, for example "First Team (training)" or
     * "First Team (match vs Real Madrid)".
     */
    public List<TeamUse> findByFacilityAndDate(int facilityId, LocalDate date) throws SQLException {
        String sql = "SELECT tr.facility_id, COALESCE(t.name, 'A club team') || ' (training)' AS team_name, "
                + "tr.training_date AS use_date, tr.start_time, tr.end_time "
                + "FROM trainings tr LEFT JOIN TEAM t ON t.id = tr.team_id "
                + "WHERE tr.facility_id = ? AND tr.training_date = ? "
                + "UNION ALL "
                + "SELECT m.facility_id, COALESCE(t.name, 'A club team') "
                + "|| CASE WHEN m.rival IS NULL THEN ' (match)' ELSE ' (match vs ' || m.rival || ')' END AS team_name, "
                + "m.match_date AS use_date, m.start_time, m.end_time "
                + "FROM matches m LEFT JOIN TEAM t ON t.id = m.team_id "
                + "WHERE m.is_home = 1 AND m.facility_id = ? AND m.match_date = ? "
                + "ORDER BY start_time";
        List<TeamUse> uses = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, facilityId);
            statement.setString(2, date.toString()); // ISO format: YYYY-MM-DD
            statement.setInt(3, facilityId);
            statement.setString(4, date.toString());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    uses.add(new TeamUse(
                            result.getInt("facility_id"),
                            result.getString("team_name"),
                            LocalDate.parse(result.getString("use_date")),
                            LocalTime.parse(result.getString("start_time")),
                            LocalTime.parse(result.getString("end_time"))
                    ));
                }
            }
        }

        return uses;
    }
}
