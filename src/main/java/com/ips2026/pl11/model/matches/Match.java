package com.ips2026.pl11.model.matches;

import java.time.LocalTime;

public class Match {
    private int id;
    private int teamId;
    private String matchDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String rival;
    private boolean isHome;
    private boolean isSpecialMatch;
    private int facilityId;
    
    public Match(int id, int teamId, String matchDate, LocalTime startTime, LocalTime endTime, String rival, boolean isHome,
            boolean isSpecialMatch, int facilityId) {
        this.id = id;
        this.teamId = teamId;
        this.matchDate = matchDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.rival = rival;
        this.isHome = isHome;
        this.isSpecialMatch = isSpecialMatch;
        this.facilityId = facilityId;
    }
    
    public int getId() {
        return id;
    }
    
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
