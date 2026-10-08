package com.ips2026.pl11.data.team;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.team.SportsEmployee;
import com.ips2026.pl11.model.team.TeamRules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the sports employees (players and technical staff) from the
 * employees table, for the creation of teams.
 */
public class SportsEmployeeDAO {

    /** Players, with the team they already belong to (if any), ordered by surname. */
    public List<SportsEmployee> findPlayers() throws SQLException {
        String sql = "SELECT e.id, e.first_name, e.last_name, e.birth_date, e.position, e.gender, t.name AS team_name "
                + "FROM employees e "
                + "LEFT JOIN TEAM_MEMBER m ON m.employee_id = e.id AND m.role = 'PLAYER' "
                + "LEFT JOIN TEAM t ON t.id = m.team_id "
                + "WHERE e.category = 'SPORTS' AND e.position = ? "
                + "ORDER BY e.last_name, e.first_name";
        return query(sql);
    }

    /** Technical sports employees (sports employees that are not players), ordered by surname. */
    public List<SportsEmployee> findTechnicalStaff() throws SQLException {
        String sql = "SELECT e.id, e.first_name, e.last_name, e.birth_date, e.position, e.gender, NULL AS team_name "
                + "FROM employees e "
                + "WHERE e.category = 'SPORTS' AND e.position <> ? "
                + "ORDER BY e.last_name, e.first_name";
        return query(sql);
    }

    private static List<SportsEmployee> query(String sql) throws SQLException {
        List<SportsEmployee> employees = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, TeamRules.PLAYER_POSITION);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    employees.add(new SportsEmployee(
                            result.getInt("id"),
                            result.getString("first_name"),
                            result.getString("last_name"),
                            LocalDate.parse(result.getString("birth_date")),
                            result.getString("position"),
                            result.getString("gender"),
                            result.getString("team_name")
                    ));
                }
            }
        }
        return employees;
    }
}
