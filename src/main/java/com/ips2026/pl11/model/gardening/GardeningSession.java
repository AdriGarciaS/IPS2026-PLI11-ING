package com.ips2026.pl11.model.gardening;

public class GardeningSession {
    private long id;
    private long facilityId;
    private String description;
    private String sessionType;
    private String date;
    private String startTime;
    private String endTime;

    public GardeningSession(long id, long facilityId, String description, String sessionType, String date, String startTime, String endTime) {
        this.id = id;
        this.facilityId = facilityId;
        this.description = description;
        this.sessionType = sessionType;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public String getDescription() {
        return description;
    }

    public long getId() {
        return id;
    }
    
    public long getFacilityId() {
        return facilityId;
    }

    public String getSessionType() {
        return sessionType;
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
