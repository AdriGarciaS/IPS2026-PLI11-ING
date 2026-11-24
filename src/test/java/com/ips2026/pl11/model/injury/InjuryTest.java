package com.ips2026.pl11.model.injury;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InjuryTest {

    @Test
    @DisplayName("Injury: correctly initializes and exposes all fields via getters")
    void testInjury_GettersMatchConstructorValues() {
        int id = 1;
        int playerId = 10;
        int matchId = 20;
        int trainingId = 30;
        String description = "Sprained ankle";
        LocalDate injuryDate = LocalDate.of(2026, 11, 20);
        boolean isHealed = false;
        int doctorId = 40;
        LocalTime injuryTime = LocalTime.of(15, 30);

        Injury injury = new Injury(id, playerId, matchId, trainingId, description, injuryDate, isHealed, doctorId,
            injuryTime);

        assertNotNull(injury);
        assertEquals(id, injury.getId());
        assertEquals(playerId, injury.getPlayerId());
        assertEquals(matchId, injury.getMatchId());
        assertEquals(trainingId, injury.getTrainingId());
        assertEquals(description, injury.getDescription());
        assertEquals(injuryDate, injury.getInjuryDate());
        assertEquals(isHealed, injury.isHealed());
        assertEquals(doctorId, injury.getDoctorId());
        assertEquals(injuryTime, injury.getInjuryTime());
    }

    @Test
    @DisplayName("Injury: correctly initializes when injury is healed")
    void testInjury_InitiallyHealed() {
        Injury injury = new Injury(1, 10, 20, 30, "Recovered injury", LocalDate.of(2026, 11, 20), true, 40,
            LocalTime.of(10, 0));

        assertTrue(injury.isHealed());
    }

    @Test
    @DisplayName("Injury: correctly initializes when injury is not healed")
    void testInjury_InitiallyNotHealed() {
        Injury injury = new Injury(1, 10, 20, 30, "Unhealed injury", LocalDate.of(2026, 11, 20), false, 40,
            LocalTime.of(10, 0));

        assertFalse(injury.isHealed());
    }

    @Test
    @DisplayName("Injury: setHealed changes healing status to true")
    void testInjury_SetHealedToTrue() {
        Injury injury = new Injury(1, 10, 20, 30, "Broken arm", LocalDate.of(2026, 11, 20), false, 40,
            LocalTime.of(10, 0));

        injury.setHealed(true);

        assertTrue(injury.isHealed());
    }

    @Test
    @DisplayName("Injury: setHealed changes healing status to false")
    void testInjury_SetHealedToFalse() {
        Injury injury = new Injury(1, 10, 20, 30, "Broken arm", LocalDate.of(2026, 11, 20), true, 40,
            LocalTime.of(10, 0));

        injury.setHealed(false);

        assertFalse(injury.isHealed());
    }

    @Test
    @DisplayName("Injury: setHealed can be called repeatedly")
    void testInjury_SetHealedRepeatedly() {
        Injury injury = new Injury(1, 10, 20, 30, "Broken arm", LocalDate.of(2026, 11, 20), false, 40,
            LocalTime.of(10, 0));

        injury.setHealed(true);
        assertTrue(injury.isHealed());

        injury.setHealed(false);
        assertFalse(injury.isHealed());

        injury.setHealed(true);
        assertTrue(injury.isHealed());
    }

    @Test
    @DisplayName("Injury: setDescription updates the description")
    void testInjury_SetDescription() {
        Injury injury = new Injury(1, 10, 20, 30, "Sprained ankle", LocalDate.of(2026, 11, 20), false, 40,
            LocalTime.of(10, 0));

        String newDescription = "Severe sprained ankle";

        injury.setDescription(newDescription);

        assertEquals(newDescription, injury.getDescription());
    }

    @Test
    @DisplayName("Injury: setDescription can set an empty description")
    void testInjury_SetDescriptionEmpty() {
        Injury injury = new Injury(1, 10, 20, 30, "Sprained ankle", LocalDate.of(2026, 11, 20), false, 40,
            LocalTime.of(10, 0));

        injury.setDescription("");

        assertEquals("", injury.getDescription());
    }

    @Test
    @DisplayName("Injury: setDescription can set description to null")
    void testInjury_SetDescriptionNull() {
        Injury injury = new Injury(1, 10, 20, 30, "Sprained ankle", LocalDate.of(2026, 11, 20), false, 40,
            LocalTime.of(10, 0));

        injury.setDescription(null);

        assertNull(injury.getDescription());
    }

    @Test
    @DisplayName("Injury: correctly initializes with null optional values")
    void testInjury_NullValues() {
        Injury injury = new Injury(1, 10, 20, 30, null, null, false, 40, null);

        assertNotNull(injury);
        assertNull(injury.getDescription());
        assertNull(injury.getInjuryDate());
        assertNull(injury.getInjuryTime());
    }

    @Test
    @DisplayName("Injury: correctly initializes with zero IDs")
    void testInjury_ZeroIds() {
        Injury injury = new Injury(0, 0, 0, 0, "Injury", LocalDate.of(2026, 11, 20), false, 0, LocalTime.MIDNIGHT);

        assertEquals(0, injury.getId());
        assertEquals(0, injury.getPlayerId());
        assertEquals(0, injury.getMatchId());
        assertEquals(0, injury.getTrainingId());
        assertEquals(0, injury.getDoctorId());
    }

    @Test
    @DisplayName("Injury: correctly initializes with negative IDs")
    void testInjury_NegativeIds() {
        Injury injury = new Injury(-1, -2, -3, -4, "Injury", LocalDate.of(2026, 11, 20), false, -5, LocalTime.MIDNIGHT);

        assertEquals(-1, injury.getId());
        assertEquals(-2, injury.getPlayerId());
        assertEquals(-3, injury.getMatchId());
        assertEquals(-4, injury.getTrainingId());
        assertEquals(-5, injury.getDoctorId());
    }

    @Test
    @DisplayName("Injury: different instances store independent values")
    void testInjury_IndependentInstances() {
        Injury injury1 = new Injury(1, 10, 20, 30, "Sprained ankle", LocalDate.of(2026, 11, 20), false, 40,
            LocalTime.of(10, 0));

        Injury injury2 = new Injury(2, 11, 21, 31, "Broken arm", LocalDate.of(2026, 11, 21), true, 41,
            LocalTime.of(11, 30));

        assertEquals(1, injury1.getId());
        assertEquals("Sprained ankle", injury1.getDescription());
        assertFalse(injury1.isHealed());

        assertEquals(2, injury2.getId());
        assertEquals("Broken arm", injury2.getDescription());
        assertTrue(injury2.isHealed());
    }

    @Test
    @DisplayName("Injury: correctly stores different dates and times")
    void testInjury_DifferentDatesAndTimes() {
        LocalDate date = LocalDate.of(2000, 1, 1);
        LocalTime time = LocalTime.of(0, 0);

        Injury injury = new Injury(1, 10, 20, 30, "Injury", date, false, 40, time);

        assertEquals(date, injury.getInjuryDate());
        assertEquals(time, injury.getInjuryTime());
    }
}