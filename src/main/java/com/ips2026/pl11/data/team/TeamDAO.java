package com.ips2026.pl11.data.team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.team.NewTeam;
import com.ips2026.pl11.model.team.SportsEmployee;
import com.ips2026.pl11.model.team.StaffAssignment;

/**
 * SQL operations on the TEAM and TEAM_MEMBER tables.
 */
public class TeamDAO {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * True if there is already a team with that name (ignoring upper/lower case).
     */
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
     * Stores a new team with its players and extra technical staff, in a single
     * transaction: if anything fails (for example a player that joined another team
     * meanwhile), nothing is stored.
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

    /**
     * Retrieves all registered teams from the database for UI dropdowns.
     */
    public List<TeamItem> findAll() throws SQLException {
        List<TeamItem> teams = new ArrayList<>();
        String sql = "SELECT id, name FROM TEAM ORDER BY name ASC";

        try (Connection connection = ConexionBD.obtenerConexion();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                teams.add(new TeamItem(result.getInt("id"), result.getString("name")));
            }
        }
        return teams;
    }

    public List<TeamItem> findTeamsByCoach(int coachId) throws SQLException {
        List<TeamItem> teams = new ArrayList<>();
        String sql = "SELECT id, name, first_coach_id, second_coach_id FROM TEAM WHERE first_coach_id = ? OR second_coach_id = ? ORDER BY name ASC";

        try (Connection connection = ConexionBD.obtenerConexion();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, coachId);
            statement.setInt(2, coachId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    teams.add(new TeamItem(result.getInt("id"), result.getString("name"),
                        result.getInt("first_coach_id"), result.getInt("second_coach_id")));
                }
            }
        }
        return teams;
    }

    /**
     * Lightweight DTO representing a saved team for Swing dropdowns (JComboBox).
     */
    public static class TeamItem {
        private final int first_coach_id;
        private final int second_coach_id;
        private final int id;
        private final String name;

        public TeamItem(int id, String name, int first_coach_id, int second_coach_id) {
            this.id = id;
            this.name = name;
            this.first_coach_id = first_coach_id;
            this.second_coach_id = second_coach_id;
        }

        public TeamItem(int id, String name) {
            this(id, name, 0, 0);
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getFirstCoachId() {
            return first_coach_id;
        }

        public int getSecondCoachId() {
            return second_coach_id;
        }

        @Override
        public String toString() {
            return name; // Swing displays this in the JComboBox
        }
    }
}
