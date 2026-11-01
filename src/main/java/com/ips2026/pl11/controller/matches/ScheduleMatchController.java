package com.ips2026.pl11.controller.matches;

import java.sql.SQLException;
import com.ips2026.pl11.data.matches.MatchPriceDAO;
import com.ips2026.pl11.data.ticket.MatchDAO;

public class ScheduleMatchController {

    private final MatchDAO matchDAO = new MatchDAO();

    public void scheduleMatch(int teamId, String rival, String date, String startTime, 
                              boolean isSpecial, String selectedGrandstand, String selectedSection, double basePrice) throws SQLException {
        
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Invalid match date format. Use YYYY-MM-DD.");
        }
        if (startTime == null || !startTime.matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("Invalid start time format. Use HH:MM.");
        }
        if (basePrice <= 0) {
            throw new IllegalArgumentException("Base price must be greater than zero.");
        }

        String endTime = calculateEndTime(startTime);
        int facilityId = 1;

        // Inserta el partido y cancela reservas/entrenamientos conflictivos
        matchDAO.createHomeMatchWithConflictsCleared(
                teamId, rival, date, startTime, endTime, isSpecial, facilityId);
    }

    private String calculateEndTime(String startTime) {
        try {
            String[] parts = startTime.split(":");
            int hour = Integer.parseInt(parts[0]) + 2;
            return String.format("%02d:%s", hour, parts[1]);
        } catch (Exception e) {
            return "20:00";
        }
    }
}