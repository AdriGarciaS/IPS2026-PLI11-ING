package com.ips2026.pl11.controller.matches;

import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.data.ticket.MatchTicketDAO;

public class ScheduleMatchController {

    private final MatchDAO matchDAO;
    private final MatchTicketDAO ticketDAO;

    public ScheduleMatchController() {
        this.matchDAO = new MatchDAO();
        this.ticketDAO = new MatchTicketDAO();
    }

    public void scheduleMatch(int teamId, String rival, String date, String startTime, 
                              boolean isSpecial, String grandstand, String section, double price, int facilityId) throws Exception {
        
        // Fixed match window duration: 2 hours (120 minutes)
        String endTime = calculateEndTime(startTime, 120);

        // Schedule match and enforce training conflict validation
        int matchId = matchDAO.createHomeMatchWithConflictsCleared(teamId, rival, date, startTime, endTime, isSpecial, facilityId);

        // Batch initialize tickets with base price
        ticketDAO.initializeMatchTicketsWithPrice(matchId, grandstand, section, price);
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