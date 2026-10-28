package com.ips2026.pl11.controller.ticket;

import java.sql.SQLException;
import java.util.List;

import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.data.ticket.MatchTicketDAO;
import com.ips2026.pl11.model.ticket.HomeMatch;

public class MatchTicketController {

    private final MatchTicketDAO ticketDAO;
    private final MatchDAO matchDAO;

    public MatchTicketController() {
        this.ticketDAO = new MatchTicketDAO();
        this.matchDAO = new MatchDAO();
    }

    public List<HomeMatch> getHomeMatches() throws SQLException {
        return matchDAO.getHomeMatches();
    }

    public List<int[]> sellTickets(int matchId, String grandstand, String section, int quantity) throws Exception {
        List<int[]> assigned = ticketDAO.bookContiguousSeats(matchId, grandstand, section, quantity);
        if (assigned.isEmpty()) {
            throw new IllegalStateException("There is no row in section " + section + 
                    " of grandstand " + grandstand + " with " + quantity + " contiguous free seats.");
        }
        return assigned;
    }
    
    public boolean[][] getOccupiedSeats(int matchId, String grandstand, String section) throws Exception {
        return ticketDAO.getOccupiedSeats(matchId, grandstand, section);
    }
}