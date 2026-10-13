package com.ips2026.pl11.model.schedule;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class DaySchedule{

    private LocalDate date;
    private long id;
    private long workScheduleId;
    private LocalTime startTime;
    private LocalTime endTime;
    
    public DaySchedule(long id, long workScheduleId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        
        this.id = id;
        this.workScheduleId = workScheduleId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public Duration duration() {
        return Duration.between(startTime, endTime);
    }
    
    public LocalDate getDate() {
        return this.date;
    }
    
    public long getId() {
        return id;
    }

    public long getWorkScheduleId() {
        return workScheduleId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
    
    

}
