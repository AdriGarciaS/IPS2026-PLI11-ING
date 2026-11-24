package com.ips2026.pl11.controller.interview;

import com.ips2026.pl11.controller.log.SystemLog;
import com.ips2026.pl11.model.log.LogAction;
import java.sql.SQLException;
import java.util.List;

import com.ips2026.pl11.data.interview.InterviewBookingDAO;
import com.ips2026.pl11.model.interview.AvailableSlotRecord;

public class BookInterviewController {

	private final InterviewBookingDAO bookingDAO;

    public BookInterviewController() {
        this.bookingDAO = new InterviewBookingDAO();
    }

    /**
     * Loads all slots currently marked as AVAILABLE.
     */
    public List<AvailableSlotRecord> loadAvailableSlots() throws SQLException {
        List<AvailableSlotRecord> slots = bookingDAO.getAvailableSlots();
        SystemLog.record(LogAction.QUERY, "Viewed available interview slots (" + slots.size() + ")");
        return slots;
    }

    /**
     * Validates input parameters and books the interview slot.
     */
    public void bookInterview(AvailableSlotRecord slot, String mediaName, String mediaContact) throws SQLException {
        if (slot == null) {
            throw new IllegalArgumentException("Please select an available slot from the table.");
        }
        if (mediaName == null || mediaName.trim().isEmpty()) {
            throw new IllegalArgumentException("Media name cannot be empty.");
        }

        bookingDAO.bookSlotAndRemoveRemaining(
                slot.getId(),
                slot.getPlayerId(),
                slot.getDate(),
                mediaName.trim(),
                mediaContact != null ? mediaContact.trim() : ""
        );
        SystemLog.record(LogAction.INSERT, "Booked interview with " + slot.getPlayerName() + " on " + slot.getDate()
                + " " + slot.getStartTime() + " for " + mediaName.trim());
    }
}
