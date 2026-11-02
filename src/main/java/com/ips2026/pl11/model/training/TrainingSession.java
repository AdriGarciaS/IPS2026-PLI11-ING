package com.ips2026.pl11.model.training;

public class TrainingSession {
    private long id;
    private long facilityId;
    private long coachId;
    private long teamId;
    private String date;
    private String startTime;
    private String endTime;
    
    public TrainingSession(long id, String date, String startTime, String endTime, long facilityId, long coachId, long teamId) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.facilityId = facilityId;
        this.coachId = coachId;
        this.teamId = teamId;
    }
    
    public long getId() {
        return id;
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
    
    public long getFacility() {
        return facilityId;
    }
    
    public long getCoach() {
        return coachId;
    }
    
    public long getTeam() {
        return teamId;
    }
    
    
    
}
