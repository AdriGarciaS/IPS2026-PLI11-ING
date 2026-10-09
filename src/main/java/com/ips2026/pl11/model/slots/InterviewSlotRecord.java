package com.ips2026.pl11.model.slots;

public class InterviewSlotRecord {

	private final int id;
    private final String playerName;
    private final String date;
    private final String startTime;
    private final String endTime;
    private final String status;

    public InterviewSlotRecord(int id, String playerName, String date, String startTime, String endTime, String status) {
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

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }
}
