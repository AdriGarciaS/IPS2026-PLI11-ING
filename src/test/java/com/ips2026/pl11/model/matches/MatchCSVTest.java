package com.ips2026.pl11.model.matches;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;

public class MatchCSVTest {

    @Test
    public void testMatchCSVHomeCreation() {
        // Arrange
        int teamId = 1;
        String matchDate = "2026-11-28";
        LocalTime startTime = LocalTime.of(18, 0);
        LocalTime endTime = LocalTime.of(20, 0);
        String rival = "Rayo Vallecano";
        boolean isHome = true;
        boolean isSpecialMatch = false;
        Integer facilityId = 1;

        // Act
        MatchCSV match = new MatchCSV(teamId, matchDate, startTime, endTime, rival, isHome, isSpecialMatch, facilityId);

        // Assert
        assertEquals(teamId, match.getTeamId());
        assertEquals(matchDate, match.getMatchDate());
        assertEquals(startTime, match.getStartTime());
        assertEquals(endTime, match.getEndTime());
        assertEquals(rival, match.getRival());
        assertTrue(match.isHome());
        assertFalse(match.isSpecialMatch());
        assertEquals(facilityId, match.getFacilityId());
    }

    @Test
    public void testMatchCSVAwayNullifiesFacilityId() {
        int teamId = 2;
        String matchDate = "2026-12-05";
        LocalTime startTime = LocalTime.of(21, 0);
        LocalTime endTime = LocalTime.of(23, 0);
        String rival = "Real Madrid";
        boolean isHome = false;
        boolean isSpecialMatch = true;
        Integer facilityId = 1; // Aunque se le pase una instalación, al ser away debe ignorarse/anularse

        // Act
        MatchCSV match = new MatchCSV(teamId, matchDate, startTime, endTime, rival, isHome, isSpecialMatch, facilityId);

        // Assert
        assertEquals(teamId, match.getTeamId());
        assertEquals(matchDate, match.getMatchDate());
        assertEquals(startTime, match.getStartTime());
        assertEquals(endTime, match.getEndTime());
        assertEquals(rival, match.getRival());
        assertFalse(match.isHome());
        assertTrue(match.isSpecialMatch());
        assertNull(match.getFacilityId(), "Away matches should have a null facilityId regardless of input");
    }
}

