package com.ips2026.pl11.model.slots;

import java.time.LocalDate;
import java.time.LocalTime;

public class TeamActivityRecord {

	private final String type; // "MATCH" or "TRAINING"
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public TeamActivityRecord(String type, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.type = type;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
