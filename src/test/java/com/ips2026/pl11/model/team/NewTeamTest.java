package com.ips2026.pl11.model.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NewTeam")
class NewTeamTest {

    private static final int YEAR = 2026;

    private static final SportsEmployee COACH_1 = coach(101, "Luis", "Moreno");
    private static final SportsEmployee COACH_2 = coach(102, "Carlos", "Vega");
    private static final SportsEmployee COACH_3 = coach(103, "Ana", "Torres");

    private static SportsEmployee coach(int id, String first, String last) {
        return new SportsEmployee(id, first, last, LocalDate.of(1980, 1, 1), "Coach", null, null);
    }

    /** {@code count} male cadete players (14 years old in 2026). */
    private static List<SportsEmployee> cadetePlayers(int count) {
        List<SportsEmployee> players = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            players.add(new SportsEmployee(i, "Player", "Number" + i, LocalDate.of(2012, 5, 1), "Player", "MALE", null));
        }
        return players;
    }

    private static NewTeam team(String name, List<SportsEmployee> players, List<StaffAssignment> staff) {
        return new NewTeam(name, TeamCategory.CADETE, TeamGender.MALE, COACH_1, COACH_2, players, staff);
    }

    private static void assertHasProblem(List<String> problems, String expectedText) {
        assertTrue(problems.stream().anyMatch(problem -> problem.contains(expectedText)),
                () -> "expected a problem with '" + expectedText + "' but they were: " + problems);
    }

    @Test
    @DisplayName("a complete team with exactly 7 players (the minimum) has no problems")
    void validTeamWithSevenPlayers() {
        assertEquals(List.of(), team("Cadete A", cadetePlayers(7), List.of()).findProblems(YEAR));
    }

    @Test
    @DisplayName("there is no maximum of players or technical staff")
    void noMaximum() {
        List<StaffAssignment> staff = List.of(new StaffAssignment(COACH_3, "Goalkeepers"));

        assertEquals(List.of(), team("Cadete A", cadetePlayers(40), staff).findProblems(YEAR));
    }

    @Test
    @DisplayName("a team with 6 players can not be created")
    void sixPlayersAreNotEnough() {
        assertHasProblem(team("Cadete A", cadetePlayers(6), List.of()).findProblems(YEAR), "at least 7 players (it has 6)");
    }

    @Test
    @DisplayName("the name is required and has a maximum length")
    void nameRules() {
        assertHasProblem(team("   ", cadetePlayers(7), List.of()).findProblems(YEAR), "Enter the name");
        assertHasProblem(team("x".repeat(61), cadetePlayers(7), List.of()).findProblems(YEAR), "longer than 60");
        assertEquals(List.of(), team("x".repeat(60), cadetePlayers(7), List.of()).findProblems(YEAR));
    }

    @Test
    @DisplayName("the category and the gender must be chosen")
    void categoryAndGenderRequired() {
        List<String> problems = new NewTeam("Cadete A", null, null, COACH_1, COACH_2, cadetePlayers(7), List.of())
                .findProblems(YEAR);

        assertHasProblem(problems, "first or subsidiary team, or the youth category");
        assertHasProblem(problems, "gender of the team");
    }

    @Test
    @DisplayName("both coaches are mandatory")
    void coachesAreMandatory() {
        List<String> problems = new NewTeam("Cadete A", TeamCategory.CADETE, TeamGender.MALE, null, null,
                cadetePlayers(7), List.of()).findProblems(YEAR);

        assertHasProblem(problems, "Choose the first coach");
        assertHasProblem(problems, "Choose the second coach");
    }

    @Test
    @DisplayName("the first and second coach must be different people")
    void coachesMustBeDifferent() {
        List<String> problems = new NewTeam("Cadete A", TeamCategory.CADETE, TeamGender.MALE, COACH_1, COACH_1,
                cadetePlayers(7), List.of()).findProblems(YEAR);

        assertHasProblem(problems, "must be different people");
    }

    @Test
    @DisplayName("a player can not be a coach")
    void coachMustBeTechnical() {
        SportsEmployee player = cadetePlayers(8).get(7);
        List<String> problems = new NewTeam("Cadete A", TeamCategory.CADETE, TeamGender.MALE, player, COACH_2,
                cadetePlayers(7), List.of()).findProblems(YEAR);

        assertHasProblem(problems, "first coach must be a technical sports employee");
    }

    @Test
    @DisplayName("every player that breaks the age or gender rules is listed by name")
    void ineligiblePlayersAreListed() {
        List<SportsEmployee> players = cadetePlayers(7);
        players.add(new SportsEmployee(50, "Nico", "Vidal", LocalDate.of(2013, 1, 1), "Player", "MALE", null));   // 13
        players.add(new SportsEmployee(51, "Lucia", "Vidal", LocalDate.of(2012, 1, 1), "Player", "FEMALE", null)); // female

        List<String> problems = team("Cadete A", players, List.of()).findProblems(YEAR);

        assertEquals(2, problems.size());
        assertHasProblem(problems, "Nico Vidal: Age 13 in 2026");
        assertHasProblem(problems, "Lucia Vidal: Female player");
    }

    @Test
    @DisplayName("extra technical staff need a task and can not be one of the coaches")
    void staffRules() {
        List<StaffAssignment> staff = List.of(new StaffAssignment(COACH_3, "  "), new StaffAssignment(COACH_1, "Physio"));

        List<String> problems = team("Cadete A", cadetePlayers(7), staff).findProblems(YEAR);

        assertHasProblem(problems, "Enter the task of Ana Torres");
        assertHasProblem(problems, "Luis Moreno is already a coach of the team");
    }
}
