package com.ips2026.pl11.model.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("TeamRules and TeamCategory")
class TeamRulesTest {

    private static final int YEAR = 2026;

    /** A player of the given age in 2026 (age counted by year). */
    private static SportsEmployee player(int age, String gender) {
        return new SportsEmployee(1, "Hugo", "Garcia", LocalDate.of(YEAR - age, 6, 15), "Player", gender, null);
    }

    @Test
    @DisplayName("the age is counted by year: born on 31/12/2012 or 01/01/2012, both are 14 in 2026")
    void ageIsCountedByYear() {
        assertEquals(14, TeamRules.ageInYear(LocalDate.of(2012, 12, 31), YEAR));
        assertEquals(14, TeamRules.ageInYear(LocalDate.of(2012, 1, 1), YEAR));
    }

    @ParameterizedTest(name = "{0}: {1} years -> {2}")
    @DisplayName("each youth category accepts exactly its ages (limits included)")
    @CsvSource({
            "PREBENJAMIN, 5, false", "PREBENJAMIN, 6, true", "PREBENJAMIN, 7, true", "PREBENJAMIN, 8, false",
            "BENJAMIN, 7, false", "BENJAMIN, 8, true", "BENJAMIN, 9, true", "BENJAMIN, 10, false",
            "ALEVIN, 9, false", "ALEVIN, 10, true", "ALEVIN, 11, true", "ALEVIN, 12, false",
            "INFANTIL, 11, false", "INFANTIL, 12, true", "INFANTIL, 13, true", "INFANTIL, 14, false",
            "CADETE, 13, false", "CADETE, 14, true", "CADETE, 15, true", "CADETE, 16, false",
            "JUVENIL, 15, false", "JUVENIL, 16, true", "JUVENIL, 18, true", "JUVENIL, 19, false"
    })
    void youthCategoryAgeLimits(TeamCategory category, int age, boolean accepted) {
        assertEquals(accepted, category.acceptsAge(age));
    }

    @Test
    @DisplayName("professional teams (first and subsidiary) have no age limit")
    void professionalTeamsHaveNoAgeLimit() {
        assertTrue(TeamCategory.FIRST_TEAM.isProfessional());
        assertTrue(TeamCategory.SUBSIDIARY.isProfessional());
        assertTrue(TeamCategory.FIRST_TEAM.acceptsAge(17));
        assertTrue(TeamCategory.SUBSIDIARY.acceptsAge(40));
        assertFalse(TeamCategory.CADETE.isProfessional());
    }

    @Test
    @DisplayName("the age rule is explained with the years of birth")
    void ageRuleText() {
        assertEquals("Players must be 14-15 years old in 2026 (born 2011-2012).", TeamCategory.CADETE.describeAgeRule(YEAR));
        assertEquals("No age limit for the players.", TeamCategory.FIRST_TEAM.describeAgeRule(YEAR));
        assertEquals("Cadete (14-15)", TeamCategory.CADETE.toString());
    }

    @Test
    @DisplayName("a player of the right age and gender can join")
    void eligiblePlayer() {
        assertTrue(TeamRules.findPlayerProblem(player(14, "MALE"), TeamCategory.CADETE, TeamGender.MALE, YEAR).isEmpty());
    }

    @Test
    @DisplayName("a player of the wrong age can not join and the reason says why")
    void wrongAge() {
        Optional<String> problem = TeamRules.findPlayerProblem(player(13, "MALE"), TeamCategory.CADETE, TeamGender.MALE, YEAR);

        assertEquals("Age 13 in 2026, Cadete is 14-15", problem.orElse(""));
    }

    @Test
    @DisplayName("male and female teams only accept players of that gender; mixed teams accept anyone")
    void genderRules() {
        assertTrue(TeamRules.findPlayerProblem(player(14, "FEMALE"), TeamCategory.CADETE, TeamGender.MALE, YEAR).isPresent());
        assertTrue(TeamRules.findPlayerProblem(player(14, "MALE"), TeamCategory.CADETE, TeamGender.FEMALE, YEAR).isPresent());
        assertTrue(TeamRules.findPlayerProblem(player(14, "FEMALE"), TeamCategory.CADETE, TeamGender.FEMALE, YEAR).isEmpty());
        assertTrue(TeamRules.findPlayerProblem(player(14, "FEMALE"), TeamCategory.CADETE, TeamGender.MIXED, YEAR).isEmpty());
        assertTrue(TeamRules.findPlayerProblem(player(14, "MALE"), TeamCategory.CADETE, TeamGender.MIXED, YEAR).isEmpty());
    }

    @Test
    @DisplayName("a player without registered gender can only join mixed teams")
    void unknownGenderOnlyMixed() {
        assertEquals("Gender not registered (only mixed teams)",
                TeamRules.findPlayerProblem(player(14, null), TeamCategory.CADETE, TeamGender.MALE, YEAR).orElse(""));
        assertTrue(TeamRules.findPlayerProblem(player(14, null), TeamCategory.CADETE, TeamGender.MIXED, YEAR).isEmpty());
    }

    @Test
    @DisplayName("a player that is already in a team can not join another one")
    void alreadyInATeam() {
        SportsEmployee inTeam = new SportsEmployee(1, "Marc", "Bernal", LocalDate.of(2010, 3, 2), "Player", "MALE", "Juvenil A");

        assertEquals("Already in Juvenil A",
                TeamRules.findPlayerProblem(inTeam, TeamCategory.JUVENIL, TeamGender.MALE, YEAR).orElse(""));
    }

    @Test
    @DisplayName("a technical employee can not be added as a player")
    void coachIsNotAPlayer() {
        SportsEmployee coach = new SportsEmployee(9, "Ana", "Torres", LocalDate.of(1981, 4, 12), "Coach", null, null);

        assertEquals("Not a player", TeamRules.findPlayerProblem(coach, TeamCategory.FIRST_TEAM, TeamGender.MIXED, YEAR).orElse(""));
    }

    @Test
    @DisplayName("while the category or gender is not chosen, they are not checked")
    void nothingChosenYet() {
        assertTrue(TeamRules.findPlayerProblem(player(30, null), null, null, YEAR).isEmpty());
    }
}
