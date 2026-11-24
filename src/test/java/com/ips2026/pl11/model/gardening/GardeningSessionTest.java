package com.ips2026.pl11.model.gardening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GardeningSessionTest {

    @Test
    @DisplayName("GardeningSession: correctly initializes and exposes all fields via getters")
    void testGardeningSession_GettersMatchConstructorValues() {
        long id = 1L;
        long facilityId = 101L;
        String description = "Spring Planting Session";
        String sessionType = "PLANTING";
        String date = "2024-04-15";
        String startTime = "09:00";
        String endTime = "12:00";

        GardeningSession session = new GardeningSession(id, facilityId, description, sessionType, date, startTime,
            endTime);

        assertNotNull(session);
        assertEquals(id, session.getId());
        assertEquals(facilityId, session.getFacilityId());
        assertEquals(description, session.getDescription());
        assertEquals(sessionType, session.getSessionType());
        assertEquals(date, session.getDate());
        assertEquals(startTime, session.getStartTime());
        assertEquals(endTime, session.getEndTime());
    }

    @Test
    @DisplayName("GardeningSession: correctly initializes with zero IDs")
    void testGardeningSession_ZeroIds() {
        GardeningSession session = new GardeningSession(0L, 0L, "Session", "PLANTING", "2024-04-15", "09:00", "12:00");

        assertEquals(0L, session.getId());
        assertEquals(0L, session.getFacilityId());
    }

    @Test
    @DisplayName("GardeningSession: correctly initializes with negative IDs")
    void testGardeningSession_NegativeIds() {
        GardeningSession session = new GardeningSession(-1L, -100L, "Session", "PLANTING", "2024-04-15", "09:00",
            "12:00");

        assertEquals(-1L, session.getId());
        assertEquals(-100L, session.getFacilityId());
    }

    @Test
    @DisplayName("GardeningSession: correctly initializes with empty strings")
    void testGardeningSession_EmptyStrings() {
        GardeningSession session = new GardeningSession(1L, 101L, "", "", "", "", "");

        assertEquals("", session.getDescription());
        assertEquals("", session.getSessionType());
        assertEquals("", session.getDate());
        assertEquals("", session.getStartTime());
        assertEquals("", session.getEndTime());
    }

    @Test
    @DisplayName("GardeningSession: correctly initializes with null values")
    void testGardeningSession_NullFields() {
        GardeningSession session = new GardeningSession(1L, 101L, null, null, null, null, null);

        assertNotNull(session);
        assertEquals(null, session.getDescription());
        assertEquals(null, session.getSessionType());
        assertEquals(null, session.getDate());
        assertEquals(null, session.getStartTime());
        assertEquals(null, session.getEndTime());
    }

    @Test
    @DisplayName("GardeningSession: different instances store independent values")
    void testGardeningSession_IndependentInstances() {
        GardeningSession session1 = new GardeningSession(1L, 101L, "Session 1", "PLANTING", "2024-04-15", "09:00",
            "12:00");

        GardeningSession session2 = new GardeningSession(2L, 202L, "Session 2", "PRUNING", "2024-05-20", "15:00",
            "17:00");

        assertEquals(1L, session1.getId());
        assertEquals(101L, session1.getFacilityId());
        assertEquals("Session 1", session1.getDescription());

        assertEquals(2L, session2.getId());
        assertEquals(202L, session2.getFacilityId());
        assertEquals("Session 2", session2.getDescription());

        assertEquals("PLANTING", session1.getSessionType());
        assertEquals("PRUNING", session2.getSessionType());
    }

    @Test
    @DisplayName("GardeningSession: correctly stores long descriptions")
    void testGardeningSession_LongDescription() {
        String description = "This is a long gardening session dedicated to planting, "
            + "maintaining and taking care of the garden.";

        GardeningSession session = new GardeningSession(1L, 101L, description, "PLANTING", "2024-04-15", "09:00",
            "12:00");

        assertEquals(description, session.getDescription());
    }

    @Test
    @DisplayName("GardeningSession: correctly stores different session types")
    void testGardeningSession_DifferentSessionTypes() {
        GardeningSession planting = new GardeningSession(1L, 101L, "Planting", "PLANTING", "2024-04-15", "09:00",
            "12:00");

        GardeningSession pruning = new GardeningSession(2L, 101L, "Pruning", "PRUNING", "2024-04-15", "13:00", "15:00");

        assertEquals("PLANTING", planting.getSessionType());
        assertEquals("PRUNING", pruning.getSessionType());
    }
}