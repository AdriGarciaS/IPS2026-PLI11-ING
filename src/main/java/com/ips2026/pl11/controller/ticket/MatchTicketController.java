package com.ips2026.pl11.controller.ticket;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.seasonpass.SeasonPassDAO;
import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.data.ticket.MatchTicketDAO;
import com.ips2026.pl11.model.ticket.HomeMatch;
import com.ips2026.pl11.model.ticket.MatchTicket;

public class MatchTicketController {

    private final MatchTicketDAO ticketDAO;
    private final MatchDAO matchDAO;
    private final SeasonPassDAO seasonPassDAO;

    public MatchTicketController() {
        this.ticketDAO = new MatchTicketDAO();
        this.matchDAO = new MatchDAO();
        this.seasonPassDAO = new SeasonPassDAO();
    }

    public List<HomeMatch> getHomeMatches() throws SQLException {
        return matchDAO.getHomeMatches();
    }

    public List<int[]> sellTickets(int matchId, String grandstand, String section, int quantity) throws Exception {
        List<MatchTicket> tickets = ticketDAO.bookContiguousSeats(matchId, grandstand, section, quantity);
        if (tickets.isEmpty()) {
            throw new IllegalStateException("There is no row in section " + section + 
                    " of grandstand " + grandstand + " with " + quantity + " contiguous free seats.");
        }

        // Convierte la lista de objetos MatchTicket a int[] (fila, asiento) para la vista UI
        List<int[]> assigned = new ArrayList<>();
        for (MatchTicket ticket : tickets) {
            assigned.add(new int[]{ticket.getRowNum(), ticket.getSeatNum()});
        }
        return assigned;
    }

    public boolean[][] getOccupiedSeats(int matchId, String grandstand, String section) throws Exception {
        // 1. Fetch match details
        HomeMatch match = matchDAO.getMatchById(matchId);

        // 2. Fetch seats sold directly for this specific match
        boolean[][] grid = ticketDAO.getOccupiedSeats(matchId, grandstand, section);

     // 3. Lock season pass seats ONLY if it is a STANDARD match (not Special)
        if (match != null && !match.isSpecialMatch()) {
            List<int[]> passSeats = seasonPassDAO.getActivePassSeatsForMatch(
                    match.getTeamName(), grandstand, section, match.getMatchDate());

            for (int[] seat : passSeats) {
                int rowIndex = seat[0];
                int columnIndex = seat[1];
                if (rowIndex >= 0 && rowIndex < 10 && columnIndex >= 0 && columnIndex < 15) {
                    grid[rowIndex][columnIndex] = true;
                }
            }
        }

        return grid;
    }
}