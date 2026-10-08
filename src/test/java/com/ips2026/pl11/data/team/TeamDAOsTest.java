package com.ips2026.pl11.data.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.team.NewTeam;
import com.ips2026.pl11.model.team.SportsEmployee;
import com.ips2026.pl11.model.team.StaffAssignment;
import com.ips2026.pl11.model.team.TeamCategory;
import com.ips2026.pl11.model.team.TeamGender;

/**
 * Tests of SportsEmployeeDAO and TeamDAO on a temporary SQLite database
 * created with the real schema.sql.
 */
@DisplayName("Sports team DAOs")
class TeamDAOsTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 6, 18, 30, 15);

    @TempDir
    Path folder;

    private final SportsEmployeeDAO employeeDAO = new SportsEmployeeDAO();
    private final TeamDAO teamDAO = new TeamDAO();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
        StringBuilder sql = new StringBuilder("INSERT INTO employees (id, first_name, last_name, national_id, birth_date, "
                + "phone_number, category, position, gross_annual_salary, gender) VALUES ");
        // Players 1..9 (cadetes), coaches 101..103, a non-sports employee 200.
        for (int i = 1; i <= 9; i++) {
            sql.append("(").append(i).append(", 'Player', 'Number").append(i).append("', 'P").append(i)
                    .append("', '2012-05-01', '600', 'SPORTS', 'Player', 0, 'MALE'), ");
        }
        sql.append("(101, 'Luis', 'Moreno', 'C1', '1978-04-11', '600', 'SPORTS', 'Coach', 1, NULL), ")
                .append("(102, 'Carlos', 'Vega', 'C2', '1981-04-12', '600', 'SPORTS', 'Coach', 1, NULL), ")
                .append("(103, 'Ana', 'Torres', 'C3', '1984-04-13', '600', 'SPORTS', 'Coach', 1, NULL), ")
                .append("(200, 'Sarah', 'Jenkins', 'N1', '1985-04-12', '600', 'NON_SPORTS', 'General Manager', 1, 'FEMALE')");
        TestDatabase.execute(sql.toString());
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    private NewTeam team(String name, int firstPlayer, int lastPlayer) throws SQLException {
        List<SportsEmployee> players = employeeDAO.findPlayers().stream()
                .filter(p -> p.getId() >= firstPlayer && p.getId() <= lastPlayer).toList();
        List<SportsEmployee> staff = employeeDAO.findTechnicalStaff();
        SportsEmployee luis = staff.stream().filter(e -> e.getId() == 101).findFirst().orElseThrow();
        SportsEmployee carlos = staff.stream().filter(e -> e.getId() == 102).findFirst().orElseThrow();
        SportsEmployee ana = staff.stream().filter(e -> e.getId() == 103).findFirst().orElseThrow();
        return new NewTeam(name, TeamCategory.CADETE, TeamGender.MALE, luis, carlos, players,
                List.of(new StaffAssignment(ana, "Goalkeepers")));
    }

    @Test
    @DisplayName("findPlayers returns only the sports employees that are players, with their data")
    void findPlayers() throws SQLException {
        List<SportsEmployee> players = employeeDAO.findPlayers();

        assertEquals(9, players.size());
        SportsEmployee first = players.get(0);
        assertEquals(1, first.getId());
        assertEquals("Player Number1", first.getFullName());
        assertEquals(2012, first.getBirthDate().getYear());
        assertEquals("MALE", first.getGender());
        assertNull(first.getCurrentTeam());
    }

    @Test
    @DisplayName("findTechnicalStaff returns the sports employees that are not players")
    void findTechnicalStaff() throws SQLException {
        List<SportsEmployee> staff = employeeDAO.findTechnicalStaff();

        assertEquals(List.of(101, 102, 103).stream().sorted().toList(),
                staff.stream().map(SportsEmployee::getId).sorted().toList());
        assertTrue(staff.stream().noneMatch(SportsEmployee::isPlayer));
    }

    @Test
    @DisplayName("insert stores the team, its players and its staff in one go")
    void insertStoresEverything() throws SQLException {
        int id = teamDAO.insert(team("Cadete A", 1, 7), CREATED_AT);

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM TEAM"));
        assertEquals(101, TestDatabase.queryInt("SELECT first_coach_id FROM TEAM WHERE id = " + id));
        assertEquals(102, TestDatabase.queryInt("SELECT second_coach_id FROM TEAM WHERE id = " + id));
        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM TEAM WHERE category = 'CADETE' AND gender = 'MALE' "
                + "AND name = 'Cadete A' AND created_at = '2026-10-06 18:30:15'"));
        assertEquals(7, TestDatabase.queryInt("SELECT COUNT(*) FROM TEAM_MEMBER WHERE team_id = " + id + " AND role = 'PLAYER'"));
        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM TEAM_MEMBER WHERE team_id = " + id
                + " AND role = 'STAFF' AND employee_id = 103 AND task = 'Goalkeepers'"));
    }

    @Test
    @DisplayName("after creating a team, its players show the team they belong to")
    void playersShowTheirTeam() throws SQLException {
        teamDAO.insert(team("Cadete A", 1, 7), CREATED_AT);

        List<SportsEmployee> players = employeeDAO.findPlayers();
        assertEquals("Cadete A", players.get(0).getCurrentTeam());
        assertNull(players.get(8).getCurrentTeam()); // player 9 is free
    }

    @Test
    @DisplayName("existsName ignores upper and lower case")
    void existsNameIgnoresCase() throws SQLException {
        assertFalse(teamDAO.existsName("Cadete A"));

        teamDAO.insert(team("Cadete A", 1, 7), CREATED_AT);

        assertTrue(teamDAO.existsName("cadete a"));
        assertTrue(teamDAO.existsName("  CADETE A "));
    }

    @Test
    @DisplayName("the database does not allow a player in two teams: nothing of the second team is stored")
    void playerCanNotBeInTwoTeams() throws SQLException {
        teamDAO.insert(team("Cadete A", 1, 7), CREATED_AT);

        // Players 3..9: 3 to 7 are already in Cadete A.
        assertThrows(SQLException.class, () -> teamDAO.insert(team("Cadete B", 3, 9), CREATED_AT));

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM TEAM"));
        assertEquals(8, TestDatabase.queryInt("SELECT COUNT(*) FROM TEAM_MEMBER"));
    }

    @Test
    @DisplayName("the database does not allow two teams with the same name")
    void duplicateNameIsRejectedByDatabase() throws SQLException {
        teamDAO.insert(team("Cadete A", 1, 7), CREATED_AT);

        assertThrows(SQLException.class, () -> teamDAO.insert(team("CADETE A", 8, 9), CREATED_AT));
        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM TEAM"));
    }
}
