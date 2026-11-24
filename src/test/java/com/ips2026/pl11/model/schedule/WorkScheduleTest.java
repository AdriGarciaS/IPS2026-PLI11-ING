package com.ips2026.pl11.model.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WorkScheduleTest {

    @Test
    @DisplayName("WorkSchedule: correctly initializes and exposes all fields via getters")
    void testWorkSchedule_GettersMatchConstructorValues() {
        long id = 1L;
        long employeeId = 100L;
        int weekDay = 1;
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        WorkSchedule schedule = new WorkSchedule(id, employeeId, weekDay, startTime, endTime);

        assertNotNull(schedule);
        assertEquals(id, schedule.getId());
        assertEquals(employeeId, schedule.getEmployeeId());
        assertEquals(weekDay, schedule.getWeekDay());
        assertEquals(startTime, schedule.getStartTime());
        assertEquals(endTime, schedule.getEndTime());
    }

    @Test
    @DisplayName("WorkSchedule: correctly calculates duration for a normal workday")
    void testWorkSchedule_DurationNormalWorkday() {
        WorkSchedule schedule = new WorkSchedule(1L, 100L, 1, LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertEquals(Duration.ofHours(8), schedule.duration());
    }

    @Test
    @DisplayName("WorkSchedule: correctly calculates duration for a short shift")
    void testWorkSchedule_DurationShortShift() {
        WorkSchedule schedule = new WorkSchedule(1L, 100L, 1, LocalTime.of(10, 15), LocalTime.of(11, 45));

        assertEquals(Duration.ofMinutes(90), schedule.duration());
    }

    @Test
    @DisplayName("WorkSchedule: correctly calculates duration with minutes")
    void testWorkSchedule_DurationWithMinutes() {
        WorkSchedule schedule = new WorkSchedule(1L, 100L, 1, LocalTime.of(8, 30), LocalTime.of(16, 45));

        assertEquals(Duration.ofHours(8).plusMinutes(15), schedule.duration());
    }

    @Test
    @DisplayName("WorkSchedule: duration is zero when start and end times are equal")
    void testWorkSchedule_DurationZero() {
        LocalTime time = LocalTime.of(12, 0);

        WorkSchedule schedule = new WorkSchedule(1L, 100L, 1, time, time);

        assertEquals(Duration.ZERO, schedule.duration());
    }

    @Test
    @DisplayName("WorkSchedule: correctly calculates duration starting at midnight")
    void testWorkSchedule_DurationStartingAtMidnight() {
        WorkSchedule schedule = new WorkSchedule(1L, 100L, 1, LocalTime.MIDNIGHT, LocalTime.of(8, 0));

        assertEquals(Duration.ofHours(8), schedule.duration());
    }

    @Test
    @DisplayName("WorkSchedule: correctly calculates duration ending at midnight")
    void testWorkSchedule_DurationEndingAtMidnight() {
        WorkSchedule schedule = new WorkSchedule(1L, 100L, 1, LocalTime.of(16, 0), LocalTime.MIDNIGHT);

        assertEquals(Duration.ofHours(-16), schedule.duration());
    }

    @Test
    @DisplayName("WorkSchedule: correctly initializes with Monday")
    void testWorkSchedule_Monday() {
        WorkSchedule schedule = new WorkSchedule(1L, 100L, 1, LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertEquals(1, schedule.getWeekDay());
    }

    @Test
    @DisplayName("WorkSchedule: correctly initializes with Sunday")
    void testWorkSchedule_Sunday() {
        WorkSchedule schedule = new WorkSchedule(1L, 100L, 7, LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertEquals(7, schedule.getWeekDay());
    }

    @Test
    @DisplayName("WorkSchedule: correctly initializes with zero IDs")
    void testWorkSchedule_ZeroIds() {
        WorkSchedule schedule = new WorkSchedule(0L, 0L, 1, LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertEquals(0L, schedule.getId());
        assertEquals(0L, schedule.getEmployeeId());
    }

    @Test
    @DisplayName("WorkSchedule: correctly initializes with negative IDs")
    void testWorkSchedule_NegativeIds() {
        WorkSchedule schedule = new WorkSchedule(-1L, -100L, 1, LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertEquals(-1L, schedule.getId());
        assertEquals(-100L, schedule.getEmployeeId());
    }

    @Test
    @DisplayName("WorkSchedule: correctly initializes with null times")
    void testWorkSchedule_NullTimes() {
        WorkSchedule schedule = new WorkSchedule(1L, 100L, 1, null, null);

        assertNotNull(schedule);
        assertEquals(null, schedule.getStartTime());
        assertEquals(null, schedule.getEndTime());
    }

    @Test
    @DisplayName("WorkSchedule: correctly calculates duration for one minute")
    void testWorkSchedule_DurationOneMinute() {
        WorkSchedule schedule = new WorkSchedule(1L, 100L, 1, LocalTime.of(9, 0), LocalTime.of(9, 1));

        assertEquals(Duration.ofMinutes(1), schedule.duration());
    }

    @Test
    @DisplayName("WorkSchedule: different instances store independent values")
    void testWorkSchedule_IndependentInstances() {
        WorkSchedule schedule1 = new WorkSchedule(1L, 100L, 1, LocalTime.of(9, 0), LocalTime.of(12, 0));

        WorkSchedule schedule2 = new WorkSchedule(2L, 200L, 5, LocalTime.of(14, 0), LocalTime.of(18, 0));

        assertEquals(1L, schedule1.getId());
        assertEquals(100L, schedule1.getEmployeeId());
        assertEquals(1, schedule1.getWeekDay());
        assertEquals(Duration.ofHours(3), schedule1.duration());

        assertEquals(2L, schedule2.getId());
        assertEquals(200L, schedule2.getEmployeeId());
        assertEquals(5, schedule2.getWeekDay());
        assertEquals(Duration.ofHours(4), schedule2.duration());
    }
}