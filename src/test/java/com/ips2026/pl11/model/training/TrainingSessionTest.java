package com.ips2026.pl11.model.training;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TrainingSessionTest {

    @Test
    @DisplayName("TrainingSession: correctly initializes and exposes all fields via getters")
    void testTrainingSession_GettersMatchConstructorValues() {
        long id = 1L;
        LocalDate date = LocalDate.of(2026, 11, 21);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(12, 0);
        long facilityId = 100L;
        long coachId = 200L;
        long teamId = 300L;

        TrainingSession session = new TrainingSession(
                id, date, startTime, endTime,
                facilityId, coachId, teamId
        );

        assertNotNull(session);
        assertEquals(id, session.getId());
        assertEquals(date, session.getDate());
        assertEquals(startTime, session.getStartTime());
        assertEquals(endTime, session.getEndTime());
        assertEquals(facilityId, session.getFacility());
        assertEquals(coachId, session.getCoach());
        assertEquals(teamId, session.getTeam());
    }

    @Test
    @DisplayName("TrainingSession: correctly initializes with zero IDs")
    void testTrainingSession_ZeroIds() {
        TrainingSession session = new TrainingSession(
                0L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                0L, 0L, 0L
        );

        assertEquals(0L, session.getId());
        assertEquals(0L, session.getFacility());
        assertEquals(0L, session.getCoach());
        assertEquals(0L, session.getTeam());
    }

    @Test
    @DisplayName("TrainingSession: correctly initializes with negative IDs")
    void testTrainingSession_NegativeIds() {
        TrainingSession session = new TrainingSession(
                -1L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                -10L, -20L, -30L
        );

        assertEquals(-1L, session.getId());
        assertEquals(-10L, session.getFacility());
        assertEquals(-20L, session.getCoach());
        assertEquals(-30L, session.getTeam());
    }

    @Test
    @DisplayName("TrainingSession: correctly initializes with null date")
    void testTrainingSession_NullDate() {
        TrainingSession session = new TrainingSession(
                1L, null,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                100L, 200L, 300L
        );

        assertNotNull(session);
        assertEquals(null, session.getDate());
    }

    @Test
    @DisplayName("TrainingSession: correctly initializes with null times")
    void testTrainingSession_NullTimes() {
        TrainingSession session = new TrainingSession(
                1L,
                LocalDate.of(2026, 11, 21),
                null, null,
                100L, 200L, 300L
        );

        assertNotNull(session);
        assertEquals(null, session.getStartTime());
        assertEquals(null, session.getEndTime());
    }

    @Test
    @DisplayName("TrainingSession: correctly initializes with all nullable fields null")
    void testTrainingSession_NullDateAndTimes() {
        TrainingSession session = new TrainingSession(
                1L, null, null, null,
                100L, 200L, 300L
        );

        assertNotNull(session);
        assertEquals(null, session.getDate());
        assertEquals(null, session.getStartTime());
        assertEquals(null, session.getEndTime());
    }

    @Test
    @DisplayName("TrainingSession: correctly initializes with midnight times")
    void testTrainingSession_MidnightTimes() {
        TrainingSession session = new TrainingSession(
                1L,
                LocalDate.of(2026, 11, 21),
                LocalTime.MIDNIGHT,
                LocalTime.of(8, 0),
                100L, 200L, 300L
        );

        assertEquals(LocalTime.MIDNIGHT, session.getStartTime());
        assertEquals(LocalTime.of(8, 0), session.getEndTime());
    }

    @Test
    @DisplayName("TrainingSession: correctly initializes with equal start and end times")
    void testTrainingSession_EqualTimes() {
        LocalTime time = LocalTime.of(12, 0);

        TrainingSession session = new TrainingSession(
                1L,
                LocalDate.of(2026, 11, 21),
                time, time,
                100L, 200L, 300L
        );

        assertEquals(time, session.getStartTime());
        assertEquals(time, session.getEndTime());
    }

    @Test
    @DisplayName("TrainingSession: correctly initializes with different dates")
    void testTrainingSession_DifferentDates() {
        LocalDate date = LocalDate.of(2000, 1, 1);

        TrainingSession session = new TrainingSession(
                1L, date,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                100L, 200L, 300L
        );

        assertEquals(date, session.getDate());
    }

    @Test
    @DisplayName("TrainingSession: correctly initializes with different times")
    void testTrainingSession_DifferentTimes() {
        LocalTime startTime = LocalTime.of(23, 59);
        LocalTime endTime = LocalTime.MIDNIGHT;

        TrainingSession session = new TrainingSession(
                1L,
                LocalDate.of(2026, 11, 21),
                startTime, endTime,
                100L, 200L, 300L
        );

        assertEquals(startTime, session.getStartTime());
        assertEquals(endTime, session.getEndTime());
    }

    @Test
    @DisplayName("TrainingSession: different instances store independent values")
    void testTrainingSession_IndependentInstances() {
        TrainingSession session1 = new TrainingSession(
                1L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                100L, 200L, 300L
        );

        TrainingSession session2 = new TrainingSession(
                2L,
                LocalDate.of(2026, 11, 22),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0),
                400L, 500L, 600L
        );

        assertEquals(1L, session1.getId());
        assertEquals(LocalDate.of(2026, 11, 21), session1.getDate());
        assertEquals(100L, session1.getFacility());
        assertEquals(200L, session1.getCoach());
        assertEquals(300L, session1.getTeam());

        assertEquals(2L, session2.getId());
        assertEquals(LocalDate.of(2026, 11, 22), session2.getDate());
        assertEquals(400L, session2.getFacility());
        assertEquals(500L, session2.getCoach());
        assertEquals(600L, session2.getTeam());
    }
}