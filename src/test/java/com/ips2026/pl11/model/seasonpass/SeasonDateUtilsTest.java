package com.ips2026.pl11.model.seasonpass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Test Suite for SeasonDateUtils.
 * Verifies seasonal boundary calculations (July 1st to June 30th) based on current calendar date.
 */
public class SeasonDateUtilsTest {

    @Test
    @DisplayName("getCurrentSeasonBounds: returns non-null array with exactly two date strings")
    void testGetCurrentSeasonBounds_Structure() {
        String[] bounds = SeasonDateUtils.getCurrentSeasonBounds();

        assertNotNull(bounds, "Returned season bounds array must not be null.");
        assertEquals(2, bounds.length, "Season bounds array must contain exactly 2 elements [startDate, endDate].");
        assertNotNull(bounds[0], "Start date string must not be null.");
        assertNotNull(bounds[1], "End date string must not be null.");
    }

    @Test
    @DisplayName("getCurrentSeasonBounds: format and year logic match July 1st to June 30th cycle")
    void testGetCurrentSeasonBounds_DateFormatAndLogic() {
        String[] bounds = SeasonDateUtils.getCurrentSeasonBounds();
        String startDate = bounds[0];
        String endDate = bounds[1];

        // Format checks (YYYY-07-01 and YYYY-06-30)
        assertTrue(startDate.endsWith("-07-01"), "Start date must end with '-07-01'.");
        assertTrue(endDate.endsWith("-06-30"), "End date must end with '-06-30'.");

        int startYear = Integer.parseInt(startDate.substring(0, 4));
        int endYear = Integer.parseInt(endDate.substring(0, 4));

        // The end year must be exactly one year after the start year
        assertEquals(startYear + 1, endYear, "End year must be exactly one calendar year after start year.");

        // Year logic validation relative to LocalDate.now()
        LocalDate now = LocalDate.now();
        if (now.getMonthValue() < 7) {
            assertEquals(now.getYear() - 1, startYear, "If before July, season start year must be previous calendar year.");
            assertEquals(now.getYear(), endYear, "If before July, season end year must be current calendar year.");
        } else {
            assertEquals(now.getYear(), startYear, "If July or later, season start year must be current calendar year.");
            assertEquals(now.getYear() + 1, endYear, "If July or later, season end year must be next calendar year.");
        }
    }
}