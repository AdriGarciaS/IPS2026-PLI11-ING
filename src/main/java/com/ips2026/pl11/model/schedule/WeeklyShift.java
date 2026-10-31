package com.ips2026.pl11.model.schedule;

import java.time.LocalTime;

public class WeeklyShift {

    private long workScheduleId;
    private int dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean specific;

    public WeeklyShift(long workScheduleId, int dayOfWeek, LocalTime startTime, LocalTime endTime,
        boolean specific) {
        this.workScheduleId = workScheduleId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.specific = specific;
    }
    
    public long getWorkScheduleId() {
        return workScheduleId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isSpecific() {
        return specific;
    }  
}
