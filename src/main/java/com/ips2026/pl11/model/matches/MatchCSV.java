package com.ips2026.pl11.model.matches;

import java.time.LocalTime;

public class MatchCSV {

	private int teamId;
    private String matchDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String rival;
    private boolean isHome;
    private boolean isSpecialMatch;
    private int facilityId;

    public MatchCSV(int teamId, String matchDate, LocalTime startTime, LocalTime endTime, String rival, boolean isHome, boolean isSpecialMatch, int facilityId) {
        this.teamId = teamId;
        this.matchDate = matchDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.rival = rival;
        this.isHome = isHome;
        this.isSpecialMatch = isSpecialMatch;
        this.facilityId = facilityId;
    }

    // Getters
    public int getTeamId() { 
    	return teamId; 
    }
    
    public String getMatchDate() { 
    	return matchDate; 
    }
    
    public LocalTime getStartTime() { 
    	return startTime; 
    }
    
    public LocalTime getEndTime() { 
    	return endTime; 
    }
    
    public String getRival() { 
    	return rival; 
    }
    
    public boolean isHome() { 
    	return isHome; 
    }
    
    public boolean isSpecialMatch() { 
    	return isSpecialMatch; 
    }
    
    public int getFacilityId() { 
    	return facilityId; 
    }
}
