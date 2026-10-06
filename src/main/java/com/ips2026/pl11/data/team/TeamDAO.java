package com.ips2026.pl11.data.team;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.team.NewTeam;
import com.ips2026.pl11.model.team.SportsEmployee;
import com.ips2026.pl11.model.team.StaffAssignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SQL operations on the TEAM and TEAM_MEMBER tables.
 */
public class TeamDAO {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** True if there is already a team with that name (ignoring upper/lower case). */
    public boolean existsName(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TEAM WHERE name = ? COLLATE NOCASE";
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name.strip());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    /**
     * Stores a new team with its players and extra technical staff, in a
     * single transaction: if anything fails (for example a player that
     * joined another team meanwhile), nothing is stored.
     *
     * @return the id of the new team
     */
    public int insert(NewTeam team, LocalDateTime createdAt) throws SQLException {
        String sqlTeam = "INSERT INTO TEAM (name, category, gender, first_coach_id, second_coach_id, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlMember = "INSERT INTO TEAM_MEMBER (team_id, employee_id, role, task) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConexionBD.obtenerConexion()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insertTeam = connection.prepareStatement(sqlTeam, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement insertMember = connection.prepareStatement(sqlMember)) {

                insertTeam.setString(1, team.getName());
                insertTeam.setString(2, team.getCategory().name());
                insertTeam.setString(3, team.getGender().name());
                insertTeam.setInt(4, team.getFirstCoach().getId());
                insertTeam.setInt(5, team.getSecondCoach().getId());
                insertTeam.setString(6, createdAt.format(DATE_TIME_FORMAT));
                insertTeam.executeUpdate();

                int teamId;
                try (ResultSet keys = insertTeam.getGeneratedKeys()) {
                    keys.next();
                    teamId = keys.getInt(1);
                }

                for (SportsEmployee player : team.getPlayers()) {
                    insertMember(insertMember, teamId, player.getId(), "PLAYER", null);
                }
                for (StaffAssignment assignment : team.getStaff()) {
                    insertMember(insertMember, teamId, assignment.getEmployee().getId(), "STAFF",
                            assignment.getTask().strip());
                }

                connection.commit();
                return teamId;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static void insertMember(PreparedStatement statement, int teamId, int employeeId, String role, String task)
            throws SQLException {
        statement.setInt(1, teamId);
        statement.setInt(2, employeeId);
        statement.setString(3, role);
        statement.setString(4, task);
        statement.executeUpdate();
    }
}
