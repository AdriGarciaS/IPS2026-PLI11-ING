package com.ips2026.pl11.model.ticket;

public class HomeMatch {
   
	private final int id;
    private final String teamName;
    private final String rival;
    private final String matchDate;
    private final String startTime;
    private final boolean specialMatch; // Added field

    public HomeMatch(int id, String teamName, String rival, String matchDate, String startTime, boolean specialMatch) {
        this.id = id;
        this.teamName = teamName;
        this.rival = rival;
        this.matchDate = matchDate;
        this.startTime = startTime;
        this.specialMatch = specialMatch;
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

    public boolean isSpecialMatch() { 
        return specialMatch; 
    }

    public String getDisplayName() {
        return teamName + " vs " + rival + " (" + matchDate + " " + startTime + ")";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}