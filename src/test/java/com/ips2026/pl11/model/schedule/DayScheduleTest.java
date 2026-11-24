package com.ips2026.pl11.model.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DayScheduleTest {

    @Test
    @DisplayName("DaySchedule: correctly initializes and exposes all fields via getters")
    void testDaySchedule_GettersMatchConstructorValues() {
        long id = 1L;
        long workScheduleId = 100L;
        LocalDate date = LocalDate.of(2026, 11, 21);
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        DaySchedule schedule = new DaySchedule(
                id, workScheduleId, date, startTime, endTime
        );

        assertNotNull(schedule);
        assertEquals(id, schedule.getId());
        assertEquals(workScheduleId, schedule.getWorkScheduleId());
        assertEquals(date, schedule.getDate());
        assertEquals(startTime, schedule.getStartTime());
        assertEquals(endTime, schedule.getEndTime());
    }

    @Test
    @DisplayName("DaySchedule: correctly calculates duration for a normal workday")
    void testDaySchedule_DurationNormalWorkday() {
        DaySchedule schedule = new DaySchedule(
                1L, 100L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        assertEquals(Duration.ofHours(8), schedule.duration());
    }

    @Test
    @DisplayName("DaySchedule: correctly calculates duration for a short schedule")
    void testDaySchedule_DurationShortSchedule() {
        DaySchedule schedule = new DaySchedule(
                1L, 100L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(10, 15),
                LocalTime.of(11, 45)
        );

        assertEquals(Duration.ofMinutes(90), schedule.duration());
    }

    @Test
    @DisplayName("DaySchedule: correctly calculates duration with minutes")
    void testDaySchedule_DurationWithMinutes() {
        DaySchedule schedule = new DaySchedule(
                1L, 100L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(8, 30),
                LocalTime.of(16, 45)
        );

        assertEquals(Duration.ofHours(8).plusMinutes(15), schedule.duration());
    }

    @Test
    @DisplayName("DaySchedule: duration is zero when start and end times are equal")
    void testDaySchedule_DurationZero() {
        LocalTime time = LocalTime.of(12, 0);

        DaySchedule schedule = new DaySchedule(
                1L, 100L,
                LocalDate.of(2026, 11, 21),
                time,
                time
        );

        assertEquals(Duration.ZERO, schedule.duration());
    }

    @Test
    @DisplayName("DaySchedule: correctly calculates duration starting at midnight")
    void testDaySchedule_DurationStartingAtMidnight() {
        DaySchedule schedule = new DaySchedule(
                1L, 100L,
                LocalDate.of(2026, 11, 21),
                LocalTime.MIDNIGHT,
                LocalTime.of(8, 0)
        );

        assertEquals(Duration.ofHours(8), schedule.duration());
    }

    @Test
    @DisplayName("DaySchedule: correctly calculates duration ending at midnight")
    void testDaySchedule_DurationEndingAtMidnight() {
        DaySchedule schedule = new DaySchedule(
                1L, 100L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(16, 0),
                LocalTime.MIDNIGHT
        );

        assertEquals(Duration.ofHours(-16), schedule.duration());
    }

    @Test
    @DisplayName("DaySchedule: correctly initializes with zero IDs")
    void testDaySchedule_ZeroIds() {
        DaySchedule schedule = new DaySchedule(
                0L, 0L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        assertEquals(0L, schedule.getId());
        assertEquals(0L, schedule.getWorkScheduleId());
    }

    @Test
    @DisplayName("DaySchedule: correctly initializes with negative IDs")
    void testDaySchedule_NegativeIds() {
        DaySchedule schedule = new DaySchedule(
                -1L, -100L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        assertEquals(-1L, schedule.getId());
        assertEquals(-100L, schedule.getWorkScheduleId());
    }

    @Test
    @DisplayName("DaySchedule: correctly initializes with null values")
    void testDaySchedule_NullValues() {
        DaySchedule schedule = new DaySchedule(
                1L, 100L, null, null, null
        );

        assertNotNull(schedule);
        assertEquals(null, schedule.getDate());
        assertEquals(null, schedule.getStartTime());
        assertEquals(null, schedule.getEndTime());
    }

    @Test
    @DisplayName("DaySchedule: correctly calculates duration for one minute")
    void testDaySchedule_DurationOneMinute() {
        DaySchedule schedule = new DaySchedule(
                1L, 100L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(9, 0),
                LocalTime.of(9, 1)
        );

        assertEquals(Duration.ofMinutes(1), schedule.duration());
    }

    @Test
    @DisplayName("DaySchedule: different instances store independent values")
    void testDaySchedule_IndependentInstances() {
        DaySchedule schedule1 = new DaySchedule(
                1L, 100L,
                LocalDate.of(2026, 11, 21),
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        DaySchedule schedule2 = new DaySchedule(
                2L, 200L,
                LocalDate.of(2026, 11, 22),
                LocalTime.of(14, 0),
                LocalTime.of(18, 0)
        );

        assertEquals(1L, schedule1.getId());
        assertEquals(100L, schedule1.getWorkScheduleId());
        assertEquals(Duration.ofHours(3), schedule1.duration());

        assertEquals(2L, schedule2.getId());
        assertEquals(200L, schedule2.getWorkScheduleId());
        assertEquals(Duration.ofHours(4), schedule2.duration());
    }
}