package com.ips2026.pl11.controller.slots;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.data.slots.InterviewSlotDAO;
import com.ips2026.pl11.model.slots.InterviewSlotRecord;

public class InterviewSlotsController {

	private final InterviewSlotDAO slotDAO;
	
	public InterviewSlotsController(InterviewSlotDAO slotsDAO) {
		this.slotDAO = slotsDAO;
	}
	
	public void createInterviewSlot(int playerId, int coachId, LocalDate date, LocalTime start, LocalTime end) throws Exception {
        // 1. Basic time boundary validation
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create interview slots in the past.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Slot end time must be after start time.");
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

        // 4. Check overlap with other interview slots
        if (slotDAO.hasSlotOverlap(playerId, date, start, end)) {
            throw new IllegalStateException("The player already has an interview slot registered during this time interval.");
        }

        // 5. Persist
        slotDAO.createSlot(playerId, coachId, date, start, end);
    }
	
	public List<InterviewSlotRecord> getAllSlots() throws SQLException {
        return slotDAO.getAllSlots();
    }
}

