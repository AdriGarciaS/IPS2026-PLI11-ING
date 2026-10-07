package com.ips2026.pl11.controller.schedule;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import com.ips2026.pl11.data.schedule.WorkScheduleDAO;
import com.ips2026.pl11.model.schedule.WorkSchedule;

public class WorkScheduleController {

    private final static Duration MAX_DAILY = Duration.ofHours(8);
    private final static Duration MAX_WEEKLY = Duration.ofHours(40);

    private final WorkScheduleDAO workScheduleDAO;

    public WorkScheduleController(WorkScheduleDAO wsDAO) {
        this.workScheduleDAO = wsDAO;
    }

    public List<WorkSchedule> getWorkSchedules() throws SQLException {
        return workScheduleDAO.getAllSchedules();
    }
    
    public List<WorkSchedule> getWorkSchedulesByEmployee(long employeeId) throws SQLException {
        return workScheduleDAO.getSchedulesByEmployee(employeeId);
    }
    
    public WorkSchedule getWorkScheduleById(long id) throws SQLException {
        return workScheduleDAO.getScheduleById(id);
    }

    public void addShift(WorkSchedule newShift) throws SQLException {
        // Validates the new shift length (must start before it ends)
        if (newShift.getEndTime().isBefore(newShift.getStartTime())) {
            throw new IllegalArgumentException("End of shift cannot be before its start.");
        }

        // Gets the worker's shifts
        List<WorkSchedule> existingWorkSchedules = workScheduleDAO.getSchedulesByEmployee(newShift.getEmployeeId());

        Duration dayTotal = newShift.duration();
        Duration weekTotal = newShift.duration();

        // Traverse the worker's shifts
        for (WorkSchedule ws : existingWorkSchedules) {
            // Add every shift's duration to check we stay under 40 weekly hours.
            weekTotal = weekTotal.plus(ws.duration());

            // If the current shift belongs to the same day of the week as the new one,
            // then we add it to the day's hour counter to not exceed 8 hours.
            if (ws.getWeekDay() == newShift.getWeekDay()) {
                dayTotal = dayTotal.plus(ws.duration());

                // If the shift overlaps with another one then it cannot be added.
                boolean shiftOverlaps =
                    newShift.getStartTime().isBefore(ws.getEndTime())
                    && ws.getStartTime().isBefore(newShift.getEndTime());
                if (shiftOverlaps) {
                    throw new IllegalArgumentException("New shift overlaps with an existing one.");
                }
            }
        }

        // Validate weekly and daily hours
        if (dayTotal.compareTo(MAX_DAILY) > 0) {
            throw new IllegalArgumentException("Worker cannot exceed 8 hours a day");
        }
        if (weekTotal.compareTo(MAX_WEEKLY) > 0) {
            throw new IllegalArgumentException("Worker cannot exceed 40 hours a week");
        }
        
        // If the new shift meets all the requirements, it gets added to the worker's schedule
        workScheduleDAO.insertSchedule(newShift);

    }
}
