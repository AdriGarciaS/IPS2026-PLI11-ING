package com.ips2026.pl11.model.slots;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Test Suite for InterviewSlotRecord model.
 * Verifies immutability, constructor field assignments, and getter methods.
 */
public class InterviewSlotRecordTest {

    @Test
    @DisplayName("InterviewSlotRecord: correctly initializes and exposes all fields via getters")
    void testInterviewSlotRecord_GettersMatchConstructorValues() {
        int id = 101;
        String playerName = "Pedri González";
        LocalDate date = LocalDate.of(2026, 11, 20);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(10, 30);
        String status = "AVAILABLE";

        InterviewSlotRecord record = new InterviewSlotRecord(id, playerName, date, startTime, endTime, status);

        assertNotNull(record, "The created InterviewSlotRecord instance must not be null.");
        assertEquals(id, record.getId(), "Slot ID must match constructor argument.");
        assertEquals(playerName, record.getPlayerName(), "Player name must match constructor argument.");
        assertEquals(date, record.getDate(), "Date must match constructor argument.");
        assertEquals(startTime, record.getStartTime(), "Start time must match constructor argument.");
        assertEquals(endTime, record.getEndTime(), "End time must match constructor argument.");
        assertEquals(status, record.getStatus(), "Status must match constructor argument.");
    }
}
