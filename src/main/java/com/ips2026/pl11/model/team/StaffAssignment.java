package com.ips2026.pl11.model.team;

/**
 * An extra technical sports employee of a team and the task they do there
 * (for example "Goalkeepers" or "Physical trainer").
 */
public final class StaffAssignment {

    private final SportsEmployee employee;
    private final String task;

    public StaffAssignment(SportsEmployee employee, String task) {
        this.employee = employee;
        this.task = task;
    }

    public SportsEmployee getEmployee() {
        return employee;
    }

    public String getTask() {
        return task;
    }
}
