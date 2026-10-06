package com.ips2026.pl11.model.reservation;

import java.time.Duration;
import java.time.LocalTime;

/**
 * A period of the day of a facility and what happens in it (free, used by a
 * team, blocked after a team use, reserved or already past).
 */
public final class TimeSlot {

    public enum Type {
        FREE, TEAM_USE, BLOCKED_AFTER_TEAM, RESERVED, PAST
    }

    private final LocalTime start;
    private final LocalTime end;
    private final Type type;
    private final String description;

    /**
     * @param description extra information to show (for example the team
     *                    name); empty if there is none
     */
    public TimeSlot(LocalTime start, LocalTime end, Type type, String description) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.description = description;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public Type getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public long getMinutes() {
        return Duration.between(start, end).toMinutes();
    }

    /** Whole hours that fit in the slot (a free slot of 2 h 30 min allows 2 hours). */
    public int getWholeHours() {
        return (int) (getMinutes() / 60);
    }

    public boolean contains(LocalTime time) {
        return !time.isBefore(start) && time.isBefore(end);
    }
}
