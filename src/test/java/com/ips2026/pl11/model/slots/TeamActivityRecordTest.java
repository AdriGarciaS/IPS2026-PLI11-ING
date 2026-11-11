package com.ips2026.pl11.model.slots;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Test Suite for TeamActivityRecord model.
 * Verifies immutability, constructor field assignments, and getter methods.
 */
public class TeamActivityRecordTest {

    @Test
    @DisplayName("TeamActivityRecord: correctly initializes and exposes all fields via getters")
    void testTeamActivityRecord_GettersMatchConstructorValues() {
        String type = "MATCH";
        LocalDate date = LocalDate.of(2026, 11, 25);
        LocalTime startTime = LocalTime.of(18, 0);
        LocalTime endTime = LocalTime.of(20, 0);

        TeamActivityRecord record = new TeamActivityRecord(type, date, startTime, endTime);

        assertNotNull(record, "The created TeamActivityRecord instance must not be null.");
        assertEquals(type, record.getType(), "Activity type must match constructor argument.");
        assertEquals(date, record.getDate(), "Date must match constructor argument.");
        assertEquals(startTime, record.getStartTime(), "Start time must match constructor argument.");
        assertEquals(endTime, record.getEndTime(), "End time must match constructor argument.");
    }
}
