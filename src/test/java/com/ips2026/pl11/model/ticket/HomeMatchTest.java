package com.ips2026.pl11.model.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Test Suite for HomeMatch model.
 * Verifies constructor field assignments, getter methods, display formatting, and toString implementation.
 */
public class HomeMatchTest {

    @Test
    @DisplayName("HomeMatch: correctly initializes and exposes all fields via getters")
    void testHomeMatch_GettersMatchConstructorValues() {
        int id = 50;
        String teamName = "First Team";
        String rival = "Real Sporting";
        String matchDate = "2026-11-20";
        String startTime = "18:00";
        boolean specialMatch = true;

        HomeMatch match = new HomeMatch(id, teamName, rival, matchDate, startTime, specialMatch);

        assertNotNull(match, "The created HomeMatch instance must not be null.");
        assertEquals(id, match.getId(), "Match ID must match constructor argument.");
        assertEquals(teamName, match.getTeamName(), "Team name must match constructor argument.");
        assertEquals(rival, match.getRival(), "Rival name must match constructor argument.");
        assertEquals(matchDate, match.getMatchDate(), "Match date must match constructor argument.");
        assertEquals(startTime, match.getStartTime(), "Start time must match constructor argument.");
        assertTrue(match.isSpecialMatch(), "Special match flag must match constructor argument.");
    }

    @Test
    @DisplayName("HomeMatch: represents regular non-special match correctly")
    void testHomeMatch_RegularMatchFlag() {
        HomeMatch match = new HomeMatch(51, "Reserve Team", "Llanera", "2026-11-21", "12:00", false);

        assertFalse(match.isSpecialMatch(), "isSpecialMatch must return false for regular matches.");
    }

    @Test
    @DisplayName("getDisplayName & toString: formats match title and date correctly")
    void testGetDisplayNameAndToString_FormattedCorrectly() {
        HomeMatch match = new HomeMatch(1, "First Team", "Oviedo Vetusta", "2026-11-25", "20:00", false);
        String expectedDisplayName = "First Team vs Oviedo Vetusta (2026-11-25 20:00)";

        assertEquals(expectedDisplayName, match.getDisplayName(), "getDisplayName format must match expected string.");
        assertEquals(expectedDisplayName, match.toString(), "toString() must return the same output as getDisplayName().");
    }
}
