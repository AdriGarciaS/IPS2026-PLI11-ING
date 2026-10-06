package com.ips2026.pl11.model.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A period in which a team of the club uses a facility (table
 * FACILITY_TEAM_USE).
 */
public final class TeamUse {

    private final int facilityId;
    private final String teamName;
    private final LocalDate date;
    private final LocalTime start;
    private final LocalTime end;

    public TeamUse(int facilityId, String teamName, LocalDate date, LocalTime start, LocalTime end) {
        this.facilityId = facilityId;
        this.teamName = teamName;
        this.date = date;
        this.start = start;
        this.end = end;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public String getTeamName() {
        return teamName;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }
}
