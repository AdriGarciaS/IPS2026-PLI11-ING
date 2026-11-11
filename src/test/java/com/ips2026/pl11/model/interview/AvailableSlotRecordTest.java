package com.ips2026.pl11.model.interview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Test Suite for AvailableSlotRecord model.
 * Verifies field assignments, constructor initialization, getters, and setters.
 */
public class AvailableSlotRecordTest {

    @Test
    @DisplayName("AvailableSlotRecord: correctly initializes and exposes all fields via getters")
    void testAvailableSlotRecord_GettersMatchConstructorValues() {
        int id = 1;
        int playerId = 10;
        String playerName = "Pedri González";
        String date = "2026-11-20";
        String startTime = "10:00";
        String endTime = "10:30";

        AvailableSlotRecord slot = new AvailableSlotRecord(id, playerId, playerName, date, startTime, endTime);

        assertNotNull(slot, "The created AvailableSlotRecord instance must not be null.");
        assertEquals(id, slot.getId(), "Slot ID must match constructor argument.");
        assertEquals(playerId, slot.getPlayerId(), "Player ID must match constructor argument.");
        assertEquals(playerName, slot.getPlayerName(), "Player name must match constructor argument.");
        assertEquals(date, slot.getDate(), "Date string must match constructor argument.");
        assertEquals(startTime, slot.getStartTime(), "Start time string must match constructor argument.");
        assertEquals(endTime, slot.getEndTime(), "End time string must match constructor argument.");
    }

    @Test
    @DisplayName("AvailableSlotRecord: setPlayerName updates player name field correctly")
    void testAvailableSlotRecord_SetPlayerNameUpdatesField() {
        AvailableSlotRecord slot = new AvailableSlotRecord(2, 11, null, "2026-11-20", "11:00", "11:30");

        assertEquals(null, slot.getPlayerName(), "Initial player name should be null.");

        String updatedName = "Pablo Gavi";
        slot.setPlayerName(updatedName);

        assertEquals(updatedName, slot.getPlayerName(), "Player name must match updated value after setter invocation.");
    }
}