package com.ips2026.pl11.controller.ticket;

import java.sql.SQLException;
import java.time.LocalDate;

import com.ips2026.pl11.data.seasonpass.SeasonPassDAO;
import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.data.ticket.MatchTicketDAO;
import com.ips2026.pl11.model.ticket.HomeMatch;

public class SpecialMatchTicketController {

    private final SeasonPassDAO seasonPassDAO;
    private final MatchDAO matchDAO;
    private final MatchTicketDAO ticketDAO;

    public SpecialMatchTicketController() {
        this.seasonPassDAO = new SeasonPassDAO();
        this.matchDAO = new MatchDAO();
        this.ticketDAO = new MatchTicketDAO();
    }

    
    public String[] verifyAndClaimMemberSeat(int matchId, String memberDni) throws Exception {
        HomeMatch match = matchDAO.getMatchById(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found.");
        }

        if (!match.isSpecialMatch()) {
            throw new IllegalStateException("This match is not designated as a Special Match.");
        }

        LocalDate matchDate = LocalDate.parse(match.getMatchDate());
        LocalDate cutoffDate = matchDate.minusDays(2);
        LocalDate today = LocalDate.now();

        if (today.isAfter(cutoffDate)) {
            throw new IllegalStateException("The payment deadline (" + cutoffDate + ") for season pass members to claim their usual seat has expired.");
        }

        String[] seatInfo = seasonPassDAO.getMemberRegularSeat(memberDni, match.getTeamName());
        if (seatInfo == null) {
            throw new IllegalArgumentException("No active season pass found for DNI: " + memberDni);
        }

        return seatInfo; // [grandstand, section, row, seat]
    }


    public boolean confirmMemberSpecialTicket(int matchId, String grandstand, String section, int row, int seat, double price) throws SQLException {
        boolean[][] occupied = ticketDAO.getOccupiedSeats(matchId, grandstand, section);
        if (occupied[row - 1][seat - 1]) {
            throw new IllegalStateException("This seat has already been paid and secured for this match.");
        }

        return ticketDAO.bookSpecificSeat(matchId, grandstand, section, row, seat, price);
    }
}
