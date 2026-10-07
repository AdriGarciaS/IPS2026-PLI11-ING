package com.ips2026.pl11.controller.schedule;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import com.ips2026.pl11.data.schedule.DayScheduleDAO;
import com.ips2026.pl11.data.schedule.WorkScheduleDAO;
import com.ips2026.pl11.model.schedule.DaySchedule;
import com.ips2026.pl11.model.schedule.WorkSchedule;

public class DayScheduleController {

    private final static Duration MAX_DAILY = Duration.ofHours(8);
    private final static Duration MAX_WEEKLY = Duration.ofHours(40);

    private final DayScheduleDAO dayScheduleDAO;
    private final WorkScheduleDAO workScheduleDAO;

    public DayScheduleController(DayScheduleDAO dsDAO, WorkScheduleDAO wsDAO) {
        this.dayScheduleDAO = dsDAO;
        this.workScheduleDAO = wsDAO;
    }

    public void addShift(DaySchedule newShift) throws SQLException {
        // Validates the new shift length (must start before it ends)
        if (newShift.getEndTime().isBefore(newShift.getStartTime())) {
            throw new IllegalArgumentException("End of shift cannot be before its start.");
        }

        // Get the work schedule to be modified
        WorkSchedule recurringSchedule = workScheduleDAO.getScheduleById(newShift.getWorkScheduleId());
        if (recurringSchedule == null) {
            throw new IllegalArgumentException("The referenced recurring schedule does not exist.");
        }

        long employeeId = recurringSchedule.getEmployeeId();

        List<DaySchedule> existingDaySchedules = dayScheduleDAO.getSchedulesByDate(newShift.getDate());
        Duration dayTotal = newShift.duration();

        for (DaySchedule ds : existingDaySchedules) {
            WorkSchedule ws = workScheduleDAO.getScheduleById(ds.getWorkScheduleId());
            if (ws.getEmployeeId() != employeeId) {
                continue;
            }

            dayTotal = dayTotal.plus(ds.duration());
            boolean shiftOverlaps = newShift.getStartTime().isBefore(ds.getEndTime())
                && ds.getStartTime().isBefore(newShift.getEndTime());

            if (shiftOverlaps) {
                throw new IllegalArgumentException("New shift overlaps with an existing one");
            }
        }

        if (dayTotal.compareTo(MAX_DAILY) > 0) {
            throw new IllegalArgumentException("Worker cannot exceed 8 hours a day.");
        }

        List<WorkSchedule> weeklySchedules = workScheduleDAO.getSchedulesByEmployee(employeeId);
        Duration weekTotal = Duration.ZERO;
        for (WorkSchedule ws : weeklySchedules) {
            weekTotal = weekTotal.plus(ws.duration());
        }

        weekTotal = weekTotal.minus(recurringSchedule.duration());
        weekTotal = weekTotal.plus(dayTotal);

        if (weekTotal.compareTo(MAX_WEEKLY) > 0) {
            throw new IllegalArgumentException("Worker cannot exceed 40 hours a week.");
        }
        
        dayScheduleDAO.insertSchedule(newShift);

    }
    
    public List<DaySchedule> getAllSchedules() throws SQLException{
        return dayScheduleDAO.getAllSchedules();
    }
}
