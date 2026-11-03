package com.ips2026.pl11.controller.schedule;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ips2026.pl11.data.schedule.DayScheduleDAO;
import com.ips2026.pl11.data.schedule.WorkScheduleDAO;
import com.ips2026.pl11.model.schedule.DaySchedule;
import com.ips2026.pl11.model.schedule.WeeklyShift;
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
        if (!newShift.getEndTime().isAfter(newShift.getStartTime())) {
            throw new IllegalArgumentException("End of shift cannot be before its start.");
        }
        // Get the work schedule to be modified
        WorkSchedule recurringSchedule = getRecurringSchedule(newShift.getWorkScheduleId());
        long employeeId = recurringSchedule.getEmployeeId();

        Duration dayTotal = computeDayTotal(newShift, employeeId);
        if (dayTotal.compareTo(MAX_DAILY) > 0) {
            throw new IllegalArgumentException("Worker cannot exceed 8 hours a day.");
        }

        Duration weekTotal = computeWeekTotal(employeeId, dayTotal, newShift);
        if (weekTotal.compareTo(MAX_WEEKLY) > 0) {
            throw new IllegalArgumentException("Worker cannot exceed 40 hours a week.");
        }

        dayScheduleDAO.insertSchedule(newShift);
    }

    private WorkSchedule getRecurringSchedule(long workScheduleId) throws SQLException {
        WorkSchedule result = workScheduleDAO.getScheduleById(workScheduleId);
        if (result == null) {
            throw new IllegalArgumentException("The referenced recurring schedule does not exist.");
        }

        return result;
    }

    private Duration computeWeekTotal(long employeeId, Duration dayTotal, DaySchedule newShift) throws SQLException {
        List<WorkSchedule> weeklySchedules = workScheduleDAO.getSchedulesByEmployee(employeeId);
        Duration weekTotal = Duration.ZERO;
        LocalDate monday = newShift.getDate().with(DayOfWeek.MONDAY);

        // traverse the whole week
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);

            // if the current day of the week is the same as the newShift to be added, we
            // already got the amount -> add it
            if (date.equals(newShift.getDate())) {
                weekTotal = weekTotal.plus(dayTotal);

            } else {
                boolean hasDaySchedules = false;
                List<DaySchedule> dailySchedules = dayScheduleDAO.getSchedulesByDate(date);

                for (DaySchedule ds : dailySchedules) {
                    WorkSchedule ws = workScheduleDAO.getScheduleById(ds.getWorkScheduleId());

                    if (ws.getEmployeeId() == employeeId) {
                        hasDaySchedules = true;
                        weekTotal = weekTotal.plus(ds.duration());
                    }
                }
                if (hasDaySchedules) {

                } else {
                    int weekDay = date.getDayOfWeek().getValue();

                    for (WorkSchedule ws : weeklySchedules) {
                        if (ws.getWeekDay() == weekDay) {
                            weekTotal = weekTotal.plus(ws.duration());
                        }
                    }
                }
            }
        }
        return weekTotal;
    }

    private Duration computeDayTotal(DaySchedule newShift, long employeeId) throws SQLException {
        List<DaySchedule> existingDaySchedules = dayScheduleDAO.getSchedulesByDate(newShift.getDate());
        Duration dayTotal = newShift.duration();

        for (DaySchedule ds : existingDaySchedules) {
            WorkSchedule ws = workScheduleDAO.getScheduleById(ds.getWorkScheduleId());
            // If the schedule is for the correct employee
            if (ws.getEmployeeId() == employeeId) {
                // if the shift overlaps -> throw exception
                if (shiftOverlaps(newShift, ds)) {
                    throw new IllegalArgumentException("New shift overlaps with an existing one");
                }
                // add its length to the day's total time
                dayTotal = dayTotal.plus(ds.duration());

            }
        }
        return dayTotal;
    }

    private boolean shiftOverlaps(DaySchedule oneShift, DaySchedule otherShift) {
        return oneShift.getStartTime().isBefore(otherShift.getEndTime())
            && otherShift.getStartTime().isBefore(oneShift.getEndTime());
    }

    public List<DaySchedule> getAllSchedules() throws SQLException {
        return dayScheduleDAO.getAllSchedules();
    }

    public List<DaySchedule> getSchedulesByEmployee(int gardenerId) {
        return dayScheduleDAO.getSchedulesByEmployee(gardenerId);
    }

    public List<WeeklyShift> getWeekShifts(long employeeId, LocalDate monday) throws SQLException {
        List<WeeklyShift> result = new ArrayList<>();
        List<WeeklyShift> allShifts = getAllWeekShifts(monday);
        
        for(WeeklyShift s : allShifts) {
            WorkSchedule ws = workScheduleDAO.getScheduleById(s.getWorkScheduleId());
            if(ws.getEmployeeId() == employeeId) {
                result.add(s);
            }
        }

        return result;
    }
    
    public List<WeeklyShift> getAllWeekShifts(LocalDate monday) throws SQLException {
        LocalDate sunday = monday.plusDays(6);

        List<WorkSchedule> recurring = workScheduleDAO.getAllSchedules();
        Set<Long> employeeScheduleIds = new HashSet<>();
        for (WorkSchedule ws : recurring) {
            employeeScheduleIds.add(ws.getId());
        }

        List<WeeklyShift> result = new ArrayList<>();
        Set<Long> overridden = new HashSet<>();

        for (DaySchedule ds : dayScheduleDAO.getAllSchedules()) {

            WorkSchedule ws = workScheduleDAO.getScheduleById(ds.getWorkScheduleId());
            boolean inWeek = !ds.getDate().isBefore(monday) && !ds.getDate().isAfter(sunday);
            
            int wsWeekDay = ws.getWeekDay();
            int dsWeekDay = ds.getDate().getDayOfWeek().getValue();
            if (inWeek && employeeScheduleIds.contains(ds.getWorkScheduleId())) {
                result.add(new WeeklyShift(ds.getWorkScheduleId(), ds.getDate().getDayOfWeek().getValue(),
                    ds.getStartTime(), ds.getEndTime(), true));
                
                if(wsWeekDay == dsWeekDay) {
                    overridden.add(ds.getWorkScheduleId());                    
                }
            }         
        }

        for (WorkSchedule ws : recurring) {
            if (!overridden.contains(ws.getId())) {
                result.add(new WeeklyShift(ws.getId(), ws.getWeekDay(), ws.getStartTime(), ws.getEndTime(), false));
            }
        }
        return result;
    }
}
