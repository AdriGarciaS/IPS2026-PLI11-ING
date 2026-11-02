package com.ips2026.pl11.model.training;

import java.time.LocalDate;
import java.time.LocalTime;

public class TrainingSession {
    private long id;
    private long facilityId;
    private long coachId;
    private long teamId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    
    public TrainingSession(long id, LocalDate date, LocalTime startTime, LocalTime endTime, long facilityId, long coachId, long teamId) {
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
    
    public LocalDate getDate() {
        return date;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public LocalTime getEndTime() {
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
