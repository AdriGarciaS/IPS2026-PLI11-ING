package com.ips2026.pl11.model.log;

import java.time.LocalDate;

/**
 * US 36717: filters of the System Log window. Every filter is optional (null
 * means "all").
 */
public class LogFilter {

    /** Value of {@link #getUsername()} to see only the guest sessions (nobody logged in). */
    public static final String GUESTS = "";

    private final LocalDate from;
    private final LocalDate to;
    private final String username;
    private final LogAction action;

    /**
     * @param from     first day (included), or null
     * @param to       last day (included), or null
     * @param username only this user, {@link #GUESTS} for the guests, or null for everybody
     * @param action   only this action, or null for all of them
     */
    public LogFilter(LocalDate from, LocalDate to, String username, LogAction action) {
        this.from = from;
        this.to = to;
        this.username = username;
        this.action = action;
    }

    /** No filter: every record. */
    public static LogFilter all() {
        return new LogFilter(null, null, null, null);
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    public String getUsername() {
        return username;
    }

    public LogAction getAction() {
        return action;
    }

    /** Whether "from" is after "to" (then nothing can match). */
    public boolean hasWrongDates() {
        return from != null && to != null && from.isAfter(to);
    }
}
