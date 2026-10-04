package com.ips2026.pl11.model.reservation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Availability of one facility on one day, following the rules of
 * {@link ReservationRules}:
 * <ul>
 *   <li>facilities can be booked from 08:00 to 22:00;</li>
 *   <li>not while a team of the club uses the facility, nor during the
 *       1 h 30 min after that use;</li>
 *   <li>not while there is another reservation;</li>
 *   <li>a reservation lasts whole hours and can start at any minute, but not
 *       in the past.</li>
 * </ul>
 *
 * <p>The day is divided in minutes and each minute gets what happens in it;
 * consecutive minutes with the same use form a {@link TimeSlot}.</p>
 */
public final class DayAvailability {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int OPENING_MINUTE = ReservationRules.OPENING_TIME.toSecondOfDay() / 60;
    private static final int CLOSING_MINUTE = ReservationRules.CLOSING_TIME.toSecondOfDay() / 60;
    private static final int DAY_MINUTES = CLOSING_MINUTE - OPENING_MINUTE;

    private final LocalDate date;
    private final LocalDateTime now;
    private final TimeSlot.Type[] typeOfMinute = new TimeSlot.Type[DAY_MINUTES];
    private final String[] descriptionOfMinute = new String[DAY_MINUTES];
    private final List<TimeSlot> timeline;

    /**
     * @param now current date and time (passed in so the class can be tested
     *            with any "now")
     */
    public DayAvailability(LocalDate date, List<TeamUse> teamUses, List<Reservation> reservations, LocalDateTime now) {
        this.date = date;
        this.now = now;

        for (int minute = 0; minute < DAY_MINUTES; minute++) {
            typeOfMinute[minute] = TimeSlot.Type.FREE;
            descriptionOfMinute[minute] = "";
        }
        markPast();
        int blockedMinutes = (int) ReservationRules.BLOCKED_AFTER_TEAM_USE.toMinutes();
        for (TeamUse use : teamUses) {
            int end = toIndex(use.getEnd());
            mark(end, end + blockedMinutes, TimeSlot.Type.BLOCKED_AFTER_TEAM, use.getTeamName());
        }
        for (Reservation reservation : reservations) {
            mark(toIndex(reservation.getStart()), toIndex(reservation.getStart()) + reservation.getHours() * 60,
                    TimeSlot.Type.RESERVED, "");
        }
        for (TeamUse use : teamUses) {
            mark(toIndex(use.getStart()), toIndex(use.getEnd()), TimeSlot.Type.TEAM_USE, use.getTeamName());
        }
        timeline = buildTimeline();
    }

    public LocalDate getDate() {
        return date;
    }

    /** Every period of the day from 08:00 to 22:00, in order, with what happens in it. */
    public List<TimeSlot> getTimeline() {
        return List.copyOf(timeline);
    }

    /** Free periods in which at least one whole hour can be booked. */
    public List<TimeSlot> getFreePeriods() {
        List<TimeSlot> free = new ArrayList<>();
        for (TimeSlot slot : timeline) {
            if (slot.getType() == TimeSlot.Type.FREE && slot.getWholeHours() >= ReservationRules.MIN_HOURS) {
                free.add(slot);
            }
        }
        return free;
    }

    /** Maximum whole hours that can be booked starting at {@code start} (0 if it is not free). */
    public int maxHoursFrom(LocalTime start) {
        int first = minuteOfDay(start) - OPENING_MINUTE;
        if (first < 0 || first >= DAY_MINUTES || typeOfMinute[first] != TimeSlot.Type.FREE) {
            return 0;
        }
        int last = first;
        while (last < DAY_MINUTES && typeOfMinute[last] == TimeSlot.Type.FREE) {
            last++;
        }
        return (last - first) / 60;
    }

    /**
     * Checks a reservation of {@code hours} hours starting at {@code start}.
     *
     * @return the reason why it can not be booked, or empty if it can
     */
    public Optional<String> findProblem(LocalTime start, int hours) {
        LocalDate today = now.toLocalDate();
        LocalDate lastDay = ReservationRules.lastBookableDay(today);

        if (hours < ReservationRules.MIN_HOURS) {
            return Optional.of("A reservation must last at least " + ReservationRules.MIN_HOURS + " hour.");
        }
        if (date.isBefore(today)) {
            return Optional.of("That day has already passed.");
        }
        if (date.isAfter(lastDay)) {
            return Optional.of("Reservations can only be made until " + lastDay.format(DATE_FORMAT) + ".");
        }
        int startMinute = minuteOfDay(start);
        int endMinute = startMinute + hours * 60;
        if (startMinute < OPENING_MINUTE) {
            return Optional.of("Facilities can be booked from " + ReservationRules.OPENING_TIME + ".");
        }
        if (endMinute > CLOSING_MINUTE) {
            return Optional.of("A reservation must end at " + ReservationRules.CLOSING_TIME + " at the latest.");
        }

        LocalTime end = start.plusHours(hours);
        for (TimeSlot slot : timeline) {
            boolean overlaps = slot.getStart().isBefore(end) && start.isBefore(slot.getEnd());
            if (overlaps && slot.getType() != TimeSlot.Type.FREE) {
                return Optional.of(describeConflict(slot));
            }
        }
        return Optional.empty();
    }

    private static String describeConflict(TimeSlot slot) {
        return switch (slot.getType()) {
            case TEAM_USE -> "The facility is used by " + slot.getDescription() + " from "
                    + slot.getStart() + " to " + slot.getEnd() + ".";
            case BLOCKED_AFTER_TEAM -> "The facility can not be booked until " + slot.getEnd()
                    + " (" + formatDuration(ReservationRules.BLOCKED_AFTER_TEAM_USE) + " after the use of "
                    + slot.getDescription() + ").";
            case RESERVED -> "The facility is already reserved from " + slot.getStart() + " to " + slot.getEnd() + ".";
            case PAST -> "The start time has already passed.";
            case FREE -> "";
        };
    }

    /** For example "1 h 30 min" or "2 h". */
    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return minutes == 0 ? hours + " h" : hours + " h " + minutes + " min";
    }

    /** On the current day, the minutes until now (included) can not be booked. */
    private void markPast() {
        LocalDate today = now.toLocalDate();
        if (date.isBefore(today)) {
            mark(0, DAY_MINUTES, TimeSlot.Type.PAST, "");
        } else if (date.equals(today)) {
            mark(0, minuteOfDay(now.toLocalTime()) - OPENING_MINUTE + 1, TimeSlot.Type.PAST, "");
        }
    }

    /** Marks the minutes [from, to) of the day (indexes from 08:00), ignoring the part outside 08:00-22:00. */
    private void mark(int from, int to, TimeSlot.Type type, String description) {
        for (int minute = Math.max(0, from); minute < Math.min(DAY_MINUTES, to); minute++) {
            typeOfMinute[minute] = type;
            descriptionOfMinute[minute] = description;
        }
    }

    private List<TimeSlot> buildTimeline() {
        List<TimeSlot> slots = new ArrayList<>();
        int start = 0;
        for (int minute = 1; minute <= DAY_MINUTES; minute++) {
            boolean endOfRun = minute == DAY_MINUTES
                    || typeOfMinute[minute] != typeOfMinute[start]
                    || !descriptionOfMinute[minute].equals(descriptionOfMinute[start]);
            if (endOfRun) {
                slots.add(new TimeSlot(toTime(start), toTime(minute), typeOfMinute[start], descriptionOfMinute[start]));
                start = minute;
            }
        }
        return slots;
    }

    private static int minuteOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private static int toIndex(LocalTime time) {
        return minuteOfDay(time) - OPENING_MINUTE;
    }

    private static LocalTime toTime(int index) {
        return LocalTime.of(0, 0).plusMinutes(OPENING_MINUTE + index);
    }
}
