package com.ips2026.pl11.model.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WeeklyShiftTest {

    @Test
    @DisplayName("WeeklyShift: correctly initializes and exposes all fields via getters")
    void testWeeklyShift_GettersMatchConstructorValues() {
        long workScheduleId = 100L;
        int dayOfWeek = 1;
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        boolean specific = false;

        WeeklyShift shift = new WeeklyShift(
                workScheduleId, dayOfWeek, startTime, endTime, specific
        );

        assertNotNull(shift);
        assertEquals(workScheduleId, shift.getWorkScheduleId());
        assertEquals(dayOfWeek, shift.getDayOfWeek());
        assertEquals(startTime, shift.getStartTime());
        assertEquals(endTime, shift.getEndTime());
        assertEquals(specific, shift.isSpecific());
    }

    @Test
    @DisplayName("WeeklyShift: correctly initializes a specific shift")
    void testWeeklyShift_SpecificShift() {
        WeeklyShift shift = new WeeklyShift(
                100L, 1,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                true
        );

        assertTrue(shift.isSpecific());
    }

    @Test
    @DisplayName("WeeklyShift: correctly initializes a non-specific shift")
    void testWeeklyShift_NonSpecificShift() {
        WeeklyShift shift = new WeeklyShift(
                100L, 1,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false
        );

        assertFalse(shift.isSpecific());
    }

    @Test
    @DisplayName("WeeklyShift: correctly initializes with zero workScheduleId")
    void testWeeklyShift_ZeroWorkScheduleId() {
        WeeklyShift shift = new WeeklyShift(
                0L, 1,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false
        );

        assertEquals(0L, shift.getWorkScheduleId());
    }

    @Test
    @DisplayName("WeeklyShift: correctly initializes with negative workScheduleId")
    void testWeeklyShift_NegativeWorkScheduleId() {
        WeeklyShift shift = new WeeklyShift(
                -1L, 1,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false
        );

        assertEquals(-1L, shift.getWorkScheduleId());
    }

    @Test
    @DisplayName("WeeklyShift: correctly initializes with minimum day of week")
    void testWeeklyShift_MinimumDayOfWeek() {
        WeeklyShift shift = new WeeklyShift(
                100L, 1,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false
        );

        assertEquals(1, shift.getDayOfWeek());
    }

    @Test
    @DisplayName("WeeklyShift: correctly initializes with maximum day of week")
    void testWeeklyShift_MaximumDayOfWeek() {
        WeeklyShift shift = new WeeklyShift(
                100L, 7,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false
        );

        assertEquals(7, shift.getDayOfWeek());
    }

    @Test
    @DisplayName("WeeklyShift: correctly initializes with midnight start time")
    void testWeeklyShift_MidnightStartTime() {
        WeeklyShift shift = new WeeklyShift(
                100L, 1,
                LocalTime.MIDNIGHT,
                LocalTime.of(8, 0),
                false
        );

        assertEquals(LocalTime.MIDNIGHT, shift.getStartTime());
    }

    @Test
    @DisplayName("WeeklyShift: correctly initializes with midnight end time")
    void testWeeklyShift_MidnightEndTime() {
        WeeklyShift shift = new WeeklyShift(
                100L, 7,
                LocalTime.of(16, 0),
                LocalTime.MIDNIGHT,
                false
        );

        assertEquals(LocalTime.MIDNIGHT, shift.getEndTime());
    }

    @Test
    @DisplayName("WeeklyShift: correctly initializes with equal start and end times")
    void testWeeklyShift_EqualTimes() {
        LocalTime time = LocalTime.of(12, 0);

        WeeklyShift shift = new WeeklyShift(
                100L, 3, time, time, false
        );

        assertEquals(time, shift.getStartTime());
        assertEquals(time, shift.getEndTime());
    }

    @Test
    @DisplayName("WeeklyShift: correctly initializes with null times")
    void testWeeklyShift_NullTimes() {
        WeeklyShift shift = new WeeklyShift(
                100L, 1, null, null, false
        );

        assertNotNull(shift);
        assertEquals(null, shift.getStartTime());
        assertEquals(null, shift.getEndTime());
    }

    @Test
    @DisplayName("WeeklyShift: different instances store independent values")
    void testWeeklyShift_IndependentInstances() {
        WeeklyShift shift1 = new WeeklyShift(
                100L, 1,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0),
                false
        );

        WeeklyShift shift2 = new WeeklyShift(
                200L, 5,
                LocalTime.of(15, 0),
                LocalTime.of(20, 0),
                true
        );

        assertEquals(100L, shift1.getWorkScheduleId());
        assertEquals(1, shift1.getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), shift1.getStartTime());
        assertEquals(LocalTime.of(13, 0), shift1.getEndTime());
        assertFalse(shift1.isSpecific());

        assertEquals(200L, shift2.getWorkScheduleId());
        assertEquals(5, shift2.getDayOfWeek());
        assertEquals(LocalTime.of(15, 0), shift2.getStartTime());
        assertEquals(LocalTime.of(20, 0), shift2.getEndTime());
        assertTrue(shift2.isSpecific());
    }
}