package com.ips2026.pl11.model.slots;

import java.time.LocalDate;
import java.time.LocalTime;

public class InterviewSlotRecord {

    private final int id;
    private final String playerName;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String status;

    public InterviewSlotRecord(int id, String playerName, LocalDate date, LocalTime startTime, LocalTime endTime, String status) {
        this.id = id;
        this.playerName = playerName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
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

    public String getStatus() {
        return status;
    }
}