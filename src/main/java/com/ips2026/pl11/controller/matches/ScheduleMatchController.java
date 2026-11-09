package com.ips2026.pl11.controller.matches;

import com.ips2026.pl11.data.matches.MatchPriceDAO;
import com.ips2026.pl11.data.ticket.MatchDAO;

public class ScheduleMatchController {

    private final MatchDAO matchDAO;
    private final MatchPriceDAO matchPriceDAO;

    public ScheduleMatchController() {
        this.matchDAO = new MatchDAO();
        this.matchPriceDAO = new MatchPriceDAO();
    }

    public int scheduleMatch(int teamId, String rival, String date, String startTime, 
                             boolean isSpecial, String grandstand, String section, double price, int facilityId) throws Exception {
        
        // Fixed match window duration: 2 hours (120 minutes)
        String endTime = calculateEndTime(startTime, 120);

        // Schedule match and enforce training conflict validation
        int matchId = matchDAO.createHomeMatchWithConflictsCleared(teamId, rival, date, startTime, endTime, isSpecial, facilityId);

        // Save active price configuration in MATCH_PRICE
        matchPriceDAO.saveMatchPrice(matchId, grandstand, section, price);

        return matchId;
    }

    private String calculateEndTime(String startTime, int durationMinutes) {
        String[] parts = startTime.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]) + durationMinutes;

        hours += minutes / 60;
        minutes = minutes % 60;
        hours = hours % 24;

        return String.format("%02d:%02d", hours, minutes);
    }
}