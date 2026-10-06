package com.ips2026.pl11.model.schedule;

import java.time.Duration;
import java.time.LocalTime;

public class WorkSchedule {
    private long id;
    private long employeeId;

    /**
     * Each day of the week is represented by an integer between 1 and 7. 1 being
     * Monday and 7 being Sunday.
     */
    private int weekDay;
    private LocalTime startTime;
    private LocalTime endTime;

    public WorkSchedule(long id, long employeeId, int weekDay, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.employeeId = employeeId;
        this.weekDay = weekDay;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Duration duration() {
        return Duration.between(startTime, endTime);
    }

    public long getId() {
        return id;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

}
