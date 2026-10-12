package com.ips2026.pl11.model.interview;

public class AvailableSlotRecord {

	private final int id;
    private final int playerId;
    private final String playerName;
    private final String date;
    private final String startTime;
    private final String endTime;

    public AvailableSlotRecord(int id, int playerId, String playerName, String date, String startTime, String endTime) {
        this.id = id;
        this.playerId = playerId;
        this.playerName = playerName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public int getPlayerId() {
        return playerId;
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
}
