package com.ips2026.pl11.controller.slots;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.ips2026.pl11.data.slots.InterviewSlotDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.slots.InterviewSlotRecord;

public class InterviewSlotsController {

    private final InterviewSlotDAO slotDAO;

    public InterviewSlotsController(InterviewSlotDAO slotDAO) {
        this.slotDAO = slotDAO;
    }

    public InterviewSlotsController() {
        this(new InterviewSlotDAO());
    }

    /**
     * Retrieves all coaches available in the system.
     */
    public List<Employee> getAllCoaches() throws SQLException {
        return slotDAO.getAllCoaches();
    }

    /**
     * Retrieves all players associated with the specified coach.
     */
    public List<Employee> getPlayersByCoach(int coachId) throws SQLException {
        return slotDAO.getPlayersByCoach(coachId);
    }

    /**
     * Validates business constraints and creates an interview slot.
     */
    public void createInterviewSlot(int playerId, int coachId, LocalDate date, LocalTime start, LocalTime end) throws Exception {
        // 1. Basic time boundary validation
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create interview slots in the past.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Slot end time must be strictly after start time.");
        }

        // 2. Rule: If player has an interview already assigned on that date, reject
        if (slotDAO.hasAssignedInterviewOnDate(playerId, date)) {
            throw new IllegalStateException("The player already has an assigned interview on " + date + 
                                            ". No new slots can be created for this day.");
        }

        // 3. Rule: Check conflict with team training sessions or matches
        if (slotDAO.hasTeamActivityConflict(playerId, date, start, end)) {
            throw new IllegalStateException("The proposed slot overlaps with a scheduled match or training session.");
        }

        // 4. Check overlap with other existing interview slots
        if (slotDAO.hasSlotOverlap(playerId, date, start, end)) {
            throw new IllegalStateException("The player already has an interview slot registered during this time interval.");
        }

        // 5. Persist slot in database
        slotDAO.createSlot(playerId, coachId, date, start, end);
    }

    /**
     * Retrieves all registered interview slots.
     */
    public List<InterviewSlotRecord> getAllSlots() throws SQLException {
        return slotDAO.getAllSlots();
    }
}