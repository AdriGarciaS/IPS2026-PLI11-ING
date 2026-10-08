package com.ips2026.pl11.controller.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.team.SportsEmployeeDAO;
import com.ips2026.pl11.data.team.TeamDAO;
import com.ips2026.pl11.model.team.NewTeam;
import com.ips2026.pl11.model.team.SportsEmployee;
import com.ips2026.pl11.model.team.TeamCategory;
import com.ips2026.pl11.model.team.TeamGender;

/**
 * Tests of the team creation controller without a database: the DAOs are
 * small fakes that keep the data in lists, and the year is fixed with a
 * {@link Clock}.
 */
@DisplayName("TeamCreationController")
class TeamCreationControllerTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Madrid");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 6, 18, 30, 15);

    private static final SportsEmployee COACH_1 = coach(101, "Luis", "Moreno");
    private static final SportsEmployee COACH_2 = coach(102, "Carlos", "Vega");
    private static final SportsEmployee COACH_3 = coach(103, "Ana", "Torres");

    private static SportsEmployee coach(int id, String first, String last) {
        return new SportsEmployee(id, first, last, LocalDate.of(1980, 1, 1), "Coach", null, null);
    }

    private static SportsEmployee player(int id, String first, String last, int birthYear, String gender, String team) {
        return new SportsEmployee(id, first, last, LocalDate.of(birthYear, 5, 1), "Player", gender, team);
    }

    /** Fake employees: 8 male cadetes, 1 female cadete, 1 infantil and 1 cadete already in a team. */
    private static class FakeEmployeeDAO extends SportsEmployeeDAO {
        final List<SportsEmployee> players = new ArrayList<>(List.of(
                player(1, "Hugo", "Garcia", 2012, "MALE", null),
                player(2, "Leo", "Garrido", 2011, "MALE", null),
                player(3, "Pablo", "Ruiz", 2012, "MALE", null),
                player(4, "Mario", "Sanz", 2011, "MALE", null),
                player(5, "Alvaro", "Gil", 2012, "MALE", null),
                player(6, "Adrian", "Ortega", 2011, "MALE", null),
                player(7, "Diego", "Molina", 2012, "MALE", null),
                player(8, "Ivan", "Romero", 2011, "MALE", null),
                player(9, "Lucia", "Vidal", 2012, "FEMALE", null),
                player(10, "Martin", "Cano", 2014, "MALE", null),
                player(11, "Sergio", "Nieto", 2012, "MALE", "Cadete B")));

        @Override
        public List<SportsEmployee> findPlayers() {
            return new ArrayList<>(players);
        }

        @Override
        public List<SportsEmployee> findTechnicalStaff() {
            return List.of(COACH_1, COACH_2, COACH_3);
        }
    }

    /** Fake teams table: remembers the inserted teams and their names. */
    private static class FakeTeamDAO extends TeamDAO {
        final List<NewTeam> inserted = new ArrayList<>();
        final List<String> names = new ArrayList<>(List.of("Juvenil A"));
        LocalDateTime createdAt;

        @Override
        public boolean existsName(String name) {
            return names.stream().anyMatch(existing -> existing.equalsIgnoreCase(name.strip()));
        }

        @Override
        public int insert(NewTeam team, LocalDateTime when) {
            inserted.add(team);
            names.add(team.getName());
            createdAt = when;
            return inserted.size();
        }
    }

    private FakeEmployeeDAO employeeDAO;
    private FakeTeamDAO teamDAO;
    private TeamCreationController controller;

    @BeforeEach
    void setUp() throws SQLException {
        employeeDAO = new FakeEmployeeDAO();
        teamDAO = new FakeTeamDAO();
        controller = new TeamCreationController(employeeDAO, teamDAO, Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE));
        controller.load();
    }

    private void addCadetes(int count) {
        List<SportsEmployee> eligible = controller.searchAvailablePlayers("", TeamCategory.CADETE, TeamGender.MALE, true);
        for (SportsEmployee player : eligible.subList(0, count)) {
            controller.addPlayer(player, TeamCategory.CADETE, TeamGender.MALE);
        }
    }

    private NewTeam cadeteTeam(String name) {
        return controller.buildTeam(name, TeamCategory.CADETE, TeamGender.MALE, COACH_1, COACH_2);
    }

    @Test
    @DisplayName("the ages are counted with the current year")
    void referenceYearIsCurrentYear() {
        assertEquals(2026, controller.getReferenceYear());
    }

    @Test
    @DisplayName("the coaches and staff are chosen from the technical sports employees")
    void technicalStaffIsLoaded() {
        assertEquals(List.of(COACH_1, COACH_2, COACH_3), controller.getTechnicalStaff());
    }

    @Test
    @DisplayName("only players that can join are offered: right age, right gender and in no other team")
    void onlyEligiblePlayersAreOffered() {
        List<SportsEmployee> offered = controller.searchAvailablePlayers("", TeamCategory.CADETE, TeamGender.MALE, true);

        assertEquals(8, offered.size()); // not Lucia (female), Martin (infantil) or Sergio (in Cadete B)
        assertEquals(11, controller.searchAvailablePlayers("", TeamCategory.CADETE, TeamGender.MALE, false).size());
    }

    @Test
    @DisplayName("the search by name is not case sensitive and players already added are not offered again")
    void searchAndAddedPlayers() {
        assertEquals(2, controller.searchAvailablePlayers("GAR", TeamCategory.CADETE, TeamGender.MALE, true).size());

        controller.addPlayer(employeeDAO.players.get(0), TeamCategory.CADETE, TeamGender.MALE); // Hugo Garcia

        assertEquals(1, controller.searchAvailablePlayers("gar", TeamCategory.CADETE, TeamGender.MALE, true).size());
    }

    @Test
    @DisplayName("a player that can not join is rejected with the reason")
    void ineligiblePlayerIsRejected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> controller.addPlayer(employeeDAO.players.get(9), TeamCategory.CADETE, TeamGender.MALE));

        assertTrue(error.getMessage().contains("Age 12 in 2026"));
        assertTrue(controller.getTeamPlayers().isEmpty());
    }

    @Test
    @DisplayName("players can be removed from the new team")
    void removePlayer() {
        addCadetes(3);
        controller.removePlayer(controller.getTeamPlayers().get(0));

        assertEquals(2, controller.getTeamPlayers().size());
    }

    @Test
    @DisplayName("extra staff need a task and can not be a coach or be added twice")
    void staffRules() {
        assertThrows(IllegalArgumentException.class, () -> controller.addStaff(COACH_3, " ", COACH_1, COACH_2));
        assertThrows(IllegalArgumentException.class, () -> controller.addStaff(COACH_1, "Physio", COACH_1, COACH_2));

        controller.addStaff(COACH_3, "  Goalkeepers ", COACH_1, COACH_2);
        assertThrows(IllegalArgumentException.class, () -> controller.addStaff(COACH_3, "Physio", COACH_1, COACH_2));

        assertEquals(1, controller.getTeamStaff().size());
        assertEquals("Goalkeepers", controller.getTeamStaff().get(0).getTask());

        controller.removeStaff(COACH_3);
        assertTrue(controller.getTeamStaff().isEmpty());
    }

    @Test
    @DisplayName("a name that is already used by another team is a problem (ignoring case)")
    void duplicateNameIsAProblem() throws SQLException {
        addCadetes(7);

        List<String> problems = controller.findProblems(cadeteTeam("JUVENIL a"));

        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("already a team called"));
    }

    @Test
    @DisplayName("creating a valid team stores it with its players, staff and creation time, and empties the form")
    void createStoresTeam() throws SQLException {
        addCadetes(7);
        controller.addStaff(COACH_3, "Goalkeepers", COACH_1, COACH_2);

        int id = controller.create(cadeteTeam("Cadete A"));

        assertEquals(1, id);
        NewTeam stored = teamDAO.inserted.get(0);
        assertEquals("Cadete A", stored.getName());
        assertEquals(TeamCategory.CADETE, stored.getCategory());
        assertEquals(TeamGender.MALE, stored.getGender());
        assertEquals(COACH_1, stored.getFirstCoach());
        assertEquals(COACH_2, stored.getSecondCoach());
        assertEquals(7, stored.getPlayers().size());
        assertEquals(1, stored.getStaff().size());
        assertEquals(NOW.withNano(0), teamDAO.createdAt);
        assertTrue(controller.getTeamPlayers().isEmpty());
        assertTrue(controller.getTeamStaff().isEmpty());
    }

    @Test
    @DisplayName("a team with less than 7 players is not stored")
    void createWithSixPlayersFails() {
        addCadetes(6);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> controller.create(cadeteTeam("Cadete A")));

        assertTrue(error.getMessage().contains("at least 7 players"));
        assertTrue(teamDAO.inserted.isEmpty());
        assertEquals(6, controller.getTeamPlayers().size(), "the form keeps the players so it can be fixed");
    }

    @Test
    @DisplayName("before storing, the players are read again: one that joined another team meanwhile is rejected")
    void playerThatJoinedAnotherTeamMeanwhile() {
        addCadetes(7);
        SportsEmployee taken = controller.getTeamPlayers().get(0);
        employeeDAO.players.replaceAll(p -> p.equals(taken)
                ? player(p.getId(), p.getFirstName(), p.getLastName(), p.getBirthDate().getYear(), p.getGender(), "Cadete B")
                : p);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> controller.create(cadeteTeam("Cadete A")));

        assertTrue(error.getMessage().contains("Already in Cadete B"));
        assertTrue(teamDAO.inserted.isEmpty());
    }

    @Test
    @DisplayName("nothing is chosen at first: no category, no gender")
    void emptyForm() {
        NewTeam team = controller.buildTeam("", null, null, null, null);

        assertNull(team.getCategory());
        assertTrue(team.getPlayers().isEmpty());
    }
}
