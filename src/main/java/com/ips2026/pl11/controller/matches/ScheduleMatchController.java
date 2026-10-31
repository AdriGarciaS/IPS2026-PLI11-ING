package com.ips2026.pl11.controller.matches;

import java.sql.SQLException;
import com.ips2026.pl11.data.matches.MatchPriceDAO;
import com.ips2026.pl11.data.ticket.MatchDAO;

public class ScheduleMatchController {

    private MatchDAO matchDAO = new MatchDAO();
    private MatchPriceDAO matchPriceDAO = new MatchPriceDAO();

    /**
     * Schedules a new home match, validates inputs, clears facility conflicts,
     * and sets category-based pricing across stadium sections.
     */
    public void scheduleMatch(int teamId, String rival, String date, String startTime, 
                              boolean isSpecial, String selectedGrandstand, String selectedSection, double basePrice) throws SQLException {
        
        // Input Validations
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
        int facilityId = 1; // Default facility ID for main stadium home matches

        // 1. Save fixture and automatically cancel conflicting facility bookings
        int matchId = matchDAO.createHomeMatchWithConflictsCleared(
                teamId, rival, date, startTime, endTime, isSpecial, facilityId);

        // 2. Register ticket rates across all stadium grandstands and sections
        if (matchId != -1) {
            String[] grandstands = {"A", "B", "C", "D"};
            String[] sections = {"A", "B", "C", "D", "E", "F"};

            for (String g : grandstands) {
                for (String s : sections) {
                    // Applies base price to all sections (or customized per selection)
                    matchPriceDAO.saveMatchPrice(matchId, g, s, basePrice);
                }
            }
        }
    }

    /**
     * Calculates the default 2-hour end time given a start time formatted as HH:MM.
     */
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