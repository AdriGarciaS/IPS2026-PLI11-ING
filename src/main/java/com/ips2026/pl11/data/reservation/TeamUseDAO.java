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
 * SQL queries to the FACILITY_TEAM_USE table (uses of the facilities by the
 * teams of the club).
 */
public class TeamUseDAO {

    /** Uses of a facility on a day, ordered by start time. */
    public List<TeamUse> findByFacilityAndDate(int facilityId, LocalDate date) throws SQLException {
        String sql = "SELECT facility_id, team_name, use_date, start_time, end_time FROM FACILITY_TEAM_USE "
                + "WHERE facility_id = ? AND use_date = ? ORDER BY start_time";
        List<TeamUse> uses = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, facilityId);
            statement.setString(2, date.toString()); // ISO format: YYYY-MM-DD

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
