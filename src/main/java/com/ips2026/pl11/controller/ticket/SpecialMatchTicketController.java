package com.ips2026.pl11.controller.ticket;

import java.sql.SQLException;
import java.time.LocalDate;

import com.ips2026.pl11.data.seasonpass.SeasonPassDAO;
import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.data.ticket.MatchTicketDAO;
import com.ips2026.pl11.model.seasonpass.SeasonDateUtils;
import com.ips2026.pl11.model.ticket.HomeMatch;

/**
 * Controller for managing season pass member seat claims and ticket confirmations during Special Matches ("Día del Club").
 */
public class SpecialMatchTicketController {

    private final SeasonPassDAO seasonPassDAO;
    private final MatchDAO matchDAO;
    private final MatchTicketDAO ticketDAO;

    public SpecialMatchTicketController() {
        this.seasonPassDAO = new SeasonPassDAO();
        this.matchDAO = new MatchDAO();
        this.ticketDAO = new MatchTicketDAO();
    }

    public SpecialMatchTicketController(SeasonPassDAO seasonPassDAO, MatchDAO matchDAO, MatchTicketDAO ticketDAO) {
        this.seasonPassDAO = seasonPassDAO;
        this.matchDAO = matchDAO;
        this.ticketDAO = ticketDAO;
    }

    /**
     * Verifies member eligibility and returns their active season pass seat coordinates for a Special Match.
     * Enforces that member seat claims are strictly restricted to the current active season (US 36757).
     */
    public String[] verifyAndClaimMemberSeat(int matchId, String memberDni) throws Exception {
        HomeMatch match = matchDAO.getMatchById(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found.");
        }

        if (!match.isSpecialMatch()) {
            throw new IllegalStateException("This match is not designated as a Special Match.");
        }

        // US 36757: Season pass seat reservations do NOT carry over to future seasons
        if (!isMatchInCurrentSeason(match.getMatchDate())) {
            throw new IllegalStateException("Season pass holder seat reservations are not applicable for future season matches.");
        }

        LocalDate matchDate = LocalDate.parse(match.getMatchDate());
        LocalDate cutoffDate = matchDate.minusDays(2);
        LocalDate today = LocalDate.now();

        if (today.isAfter(cutoffDate)) {
            throw new IllegalStateException("The payment deadline (" + cutoffDate + ") for season pass members to claim their usual seat has expired.");
        }

        // Pass the match date so SeasonPassDAO checks pass validity for this specific match date
        String[] seatInfo = seasonPassDAO.getMemberRegularSeatForMatchDate(memberDni, match.getTeamName(), match.getMatchDate());
        if (seatInfo == null) {
            throw new IllegalArgumentException("No active season pass found for DNI: " + memberDni + " on match date: " + match.getMatchDate());
        }

        return seatInfo; // [grandstand, section, row, seat]
    }

    /**
     * Helper method checking if the match date falls within the current active season.
     */
    private boolean isMatchInCurrentSeason(String matchDateStr) {
        if (matchDateStr == null || matchDateStr.isEmpty()) {
            return false;
        }

        try {
            LocalDate matchDate = LocalDate.parse(matchDateStr);
            String[] bounds = SeasonDateUtils.getCurrentSeasonBounds();
            LocalDate seasonStart = LocalDate.parse(bounds[0]);
            LocalDate seasonEnd = LocalDate.parse(bounds[1]);

            return !matchDate.isBefore(seasonStart) && !matchDate.isAfter(seasonEnd);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Confirms the special match ticket payment for the member's claimed seat and inserts the ticket record.
     */
    public boolean confirmMemberSpecialTicket(int matchId, String grandstand, String section, int row, int seat, double price) throws SQLException {
        boolean[][] occupied = ticketDAO.getOccupiedSeats(matchId, grandstand, section);
        if (occupied[row - 1][seat - 1]) {
            throw new IllegalStateException("This seat has already been paid and secured for this match.");
        }

        return ticketDAO.bookSpecificSeat(matchId, grandstand, section, row, seat, price);
    }
}