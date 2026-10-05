package com.ips2026.pl11.model.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DayAvailability")
class DayAvailabilityTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    /** "Now" used in the tests: today at 09:00. */
    private static final LocalDateTime NOW = TODAY.atTime(9, 0);

    private static final Facility PITCH = new Facility(2, "Training Pitch 1");

    private static LocalTime t(String time) {
        return LocalTime.parse(time);
    }

    private static TeamUse teamUse(String team, String start, String end) {
        return new TeamUse(PITCH.getId(), team, TOMORROW, t(start), t(end));
    }

    private static Reservation reservation(String start, int hours) {
        return new Reservation(1, PITCH, "Laura Perez", "**** **** **** 1111", TOMORROW, t(start), hours,
                ReservationRules.priceFor(hours), NOW);
    }

    /** Tomorrow with one team use from 10:00 to 12:00 (blocked until 13:30). */
    private static DayAvailability tomorrowWithTeamUse() {
        return new DayAvailability(TOMORROW, List.of(teamUse("First Team", "10:00", "12:00")), List.of(), NOW);
    }

    private static DayAvailability emptyDay(LocalDate date) {
        return new DayAvailability(date, List.of(), List.of(), NOW);
    }

    private static void assertSlot(TimeSlot slot, String start, String end, TimeSlot.Type type) {
        assertEquals(t(start), slot.getStart(), "start");
        assertEquals(t(end), slot.getEnd(), "end");
        assertEquals(type, slot.getType(), "type");
    }

    private static void assertAvailable(Optional<String> problem) {
        assertTrue(problem.isEmpty(), () -> "expected available but was: " + problem.orElse(""));
    }

    private static void assertNotAvailable(Optional<String> problem, String expectedText) {
        assertTrue(problem.isPresent(), "expected a problem but it was available");
        assertTrue(problem.get().contains(expectedText),
                () -> "expected a message with '" + expectedText + "' but was: " + problem.get());
    }

    @Nested
    @DisplayName("timeline and free periods")
    class Timeline {

        @Test
        @DisplayName("a day without team uses or reservations is free from 08:00 to 22:00")
        void emptyDayIsFree() {
            DayAvailability day = emptyDay(TOMORROW);

            assertEquals(1, day.getTimeline().size());
            assertSlot(day.getTimeline().get(0), "08:00", "22:00", TimeSlot.Type.FREE);
            assertEquals(14, day.maxHoursFrom(t("08:00")));
        }

        @Test
        @DisplayName("after a team use the facility is blocked for 1 h 30 min")
        void teamUseIsFollowedByBlockedTime() {
            List<TimeSlot> timeline = tomorrowWithTeamUse().getTimeline();

            assertEquals(4, timeline.size());
            assertSlot(timeline.get(0), "08:00", "10:00", TimeSlot.Type.FREE);
            assertSlot(timeline.get(1), "10:00", "12:00", TimeSlot.Type.TEAM_USE);
            assertSlot(timeline.get(2), "12:00", "13:30", TimeSlot.Type.BLOCKED_AFTER_TEAM);
            assertSlot(timeline.get(3), "13:30", "22:00", TimeSlot.Type.FREE);
            assertEquals("First Team", timeline.get(1).getDescription());
        }

        @Test
        @DisplayName("the blocked time after a team use that ends late is cut at 22:00")
        void blockedTimeIsCutAtClosing() {
            DayAvailability day = new DayAvailability(TOMORROW, List.of(teamUse("U19 Team", "20:00", "21:00")), List.of(), NOW);

            List<TimeSlot> timeline = day.getTimeline();
            assertSlot(timeline.get(timeline.size() - 1), "21:00", "22:00", TimeSlot.Type.BLOCKED_AFTER_TEAM);
        }

        @Test
        @DisplayName("if two team uses are close, the second one is shown as team use (not as blocked)")
        void teamUseInsideBlockedTimeOfAnother() {
            DayAvailability day = new DayAvailability(TOMORROW,
                    List.of(teamUse("First Team", "10:00", "11:00"), teamUse("U19 Team", "12:00", "13:00")), List.of(), NOW);

            List<TimeSlot> timeline = day.getTimeline();
            assertSlot(timeline.get(1), "10:00", "11:00", TimeSlot.Type.TEAM_USE);
            assertSlot(timeline.get(2), "11:00", "12:00", TimeSlot.Type.BLOCKED_AFTER_TEAM);
            assertSlot(timeline.get(3), "12:00", "13:00", TimeSlot.Type.TEAM_USE);
            assertSlot(timeline.get(4), "13:00", "14:30", TimeSlot.Type.BLOCKED_AFTER_TEAM);
        }

        @Test
        @DisplayName("reservations appear as reserved, without blocked time after them")
        void reservationsAreReserved() {
            DayAvailability day = new DayAvailability(TOMORROW, List.of(), List.of(reservation("14:00", 2)), NOW);

            List<TimeSlot> timeline = day.getTimeline();
            assertSlot(timeline.get(1), "14:00", "16:00", TimeSlot.Type.RESERVED);
            assertSlot(timeline.get(2), "16:00", "22:00", TimeSlot.Type.FREE);
        }

        @Test
        @DisplayName("only free periods of at least 1 hour are offered")
        void shortFreePeriodsAreNotOffered() {
            // Free: 08:00-10:00 (2 h), 13:30-14:00 (30 min), 16:00-22:00 (6 h)
            DayAvailability day = new DayAvailability(TOMORROW,
                    List.of(teamUse("First Team", "10:00", "12:00")), List.of(reservation("14:00", 2)), NOW);

            List<TimeSlot> free = day.getFreePeriods();
            assertEquals(2, free.size());
            assertSlot(free.get(0), "08:00", "10:00", TimeSlot.Type.FREE);
            assertSlot(free.get(1), "16:00", "22:00", TimeSlot.Type.FREE);
            assertEquals(6, free.get(1).getWholeHours());
        }

        @Test
        @DisplayName("today, the time until now is past and can not be booked")
        void todayTimeUntilNowIsPast() {
            DayAvailability day = emptyDay(TODAY); // now = 09:00

            List<TimeSlot> timeline = day.getTimeline();
            assertSlot(timeline.get(0), "08:00", "09:01", TimeSlot.Type.PAST);
            assertSlot(timeline.get(1), "09:01", "22:00", TimeSlot.Type.FREE);
        }
    }

    @Nested
    @DisplayName("maximum hours from a start time")
    class MaxHours {

        @Test
        @DisplayName("counts the whole hours until the next team use")
        void untilNextTeamUse() {
            DayAvailability day = tomorrowWithTeamUse();

            assertEquals(2, day.maxHoursFrom(t("08:00")));
            assertEquals(1, day.maxHoursFrom(t("08:13"))); // 1 h 47 min until 10:00
            assertEquals(0, day.maxHoursFrom(t("09:01"))); // 59 min until 10:00
        }

        @Test
        @DisplayName("is 0 inside a team use or the blocked time")
        void zeroWhenNotFree() {
            DayAvailability day = tomorrowWithTeamUse();

            assertEquals(0, day.maxHoursFrom(t("11:00")));
            assertEquals(0, day.maxHoursFrom(t("13:29")));
            assertEquals(8, day.maxHoursFrom(t("13:30")));
        }

        @Test
        @DisplayName("is 0 outside the opening hours")
        void zeroOutsideOpeningHours() {
            DayAvailability day = emptyDay(TOMORROW);

            assertEquals(0, day.maxHoursFrom(t("07:59")));
            assertEquals(0, day.maxHoursFrom(t("22:00")));
            assertEquals(1, day.maxHoursFrom(t("21:00")));
        }
    }

    @Nested
    @DisplayName("checking a reservation")
    class FindProblem {

        @Test
        @DisplayName("a reservation can start at any minute (16:13 - 17:13)")
        void canStartAtAnyMinute() {
            assertAvailable(tomorrowWithTeamUse().findProblem(t("16:13"), 1));
        }

        @Test
        @DisplayName("a reservation can end exactly when a team use starts")
        void canEndWhenTeamUseStarts() {
            assertAvailable(tomorrowWithTeamUse().findProblem(t("09:00"), 1));
        }

        @Test
        @DisplayName("a reservation can not overlap a team use")
        void cannotOverlapTeamUse() {
            assertNotAvailable(tomorrowWithTeamUse().findProblem(t("09:01"), 1), "used by First Team");
            assertNotAvailable(tomorrowWithTeamUse().findProblem(t("10:30"), 1), "used by First Team");
        }

        @Test
        @DisplayName("a reservation can start exactly 1 h 30 min after a team use, not a minute before")
        void blockedTimeLimits() {
            assertAvailable(tomorrowWithTeamUse().findProblem(t("13:30"), 1));
            assertNotAvailable(tomorrowWithTeamUse().findProblem(t("13:29"), 1), "until 13:30");
        }

        @Test
        @DisplayName("a reservation can not overlap another reservation")
        void cannotOverlapReservation() {
            DayAvailability day = new DayAvailability(TOMORROW, List.of(), List.of(reservation("14:00", 2)), NOW);

            assertNotAvailable(day.findProblem(t("13:30"), 1), "already reserved");
            assertNotAvailable(day.findProblem(t("15:59"), 1), "already reserved");
            assertAvailable(day.findProblem(t("13:00"), 1)); // ends 14:00
            assertAvailable(day.findProblem(t("16:00"), 1)); // starts when it ends
        }

        @Test
        @DisplayName("a reservation must be between 08:00 and 22:00")
        void openingHoursLimits() {
            DayAvailability day = emptyDay(TOMORROW);

            assertAvailable(day.findProblem(t("08:00"), 1));
            assertAvailable(day.findProblem(t("21:00"), 1)); // ends exactly at 22:00
            assertAvailable(day.findProblem(t("08:00"), 14)); // the whole day
            assertNotAvailable(day.findProblem(t("07:59"), 1), "from 08:00");
            assertNotAvailable(day.findProblem(t("21:01"), 1), "end at 22:00");
            assertNotAvailable(day.findProblem(t("21:30"), 1), "end at 22:00");
        }

        @Test
        @DisplayName("a reservation lasts at least 1 hour")
        void atLeastOneHour() {
            assertNotAvailable(emptyDay(TOMORROW).findProblem(t("10:00"), 0), "at least 1 hour");
        }

        @Test
        @DisplayName("today, only start times after now can be booked")
        void todayOnlyFutureStartTimes() {
            DayAvailability day = emptyDay(TODAY); // now = 09:00

            assertNotAvailable(day.findProblem(t("08:30"), 1), "already passed");
            assertNotAvailable(day.findProblem(t("09:00"), 1), "already passed");
            assertAvailable(day.findProblem(t("09:01"), 1));
        }

        @Test
        @DisplayName("past days can not be booked")
        void pastDaysCanNotBeBooked() {
            assertNotAvailable(emptyDay(TODAY.minusDays(1)).findProblem(t("10:00"), 1), "already passed");
        }

        @Test
        @DisplayName("days can be booked until today + 1 month, not after")
        void bookingWindowLimits() {
            LocalDate lastDay = TODAY.plus(ReservationRules.BOOKING_WINDOW); // 05/11/2026

            assertAvailable(emptyDay(lastDay).findProblem(t("10:00"), 1));
            assertNotAvailable(emptyDay(lastDay.plusDays(1)).findProblem(t("10:00"), 1), "05/11/2026");
        }
    }

    @Test
    @DisplayName("the price is 50 EUR for each booked hour")
    void priceIsFiftyPerHour() {
        assertEquals(0, new BigDecimal("50").compareTo(ReservationRules.priceFor(1)));
        assertEquals(0, new BigDecimal("150").compareTo(ReservationRules.priceFor(3)));
    }
}
