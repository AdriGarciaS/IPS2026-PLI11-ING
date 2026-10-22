package com.ips2026.pl11.model.ticket;

public class HomeMatch {
    private final int id;
    private final String teamName;
    private final String rival;
    private final String matchDate;
    private final String startTime;

    public HomeMatch(int id, String teamName, String rival, String matchDate, String startTime) {
        this.id = id;
        this.teamName = teamName;
        this.rival = rival;
        this.matchDate = matchDate;
        this.startTime = startTime;
    }

    public int getId() { 
        return id; 
    }

    public String getTeamName() { 
        return teamName; 
    }

    public String getRival() { 
        return rival; 
    }

    public String getMatchDate() { 
        return matchDate; 
    }

    public String getStartTime() { 
        return startTime; 
    }

    public String getDisplayName() {
        return teamName + " vs " + rival + " (" + matchDate + " " + startTime + ")";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}