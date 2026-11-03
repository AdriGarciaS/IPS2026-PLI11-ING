package com.ips2026.pl11.controller.ticket;

import java.sql.SQLException;
import java.util.List;

import com.ips2026.pl11.data.seasonpass.SeasonPassDAO;
import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.data.ticket.MatchTicketDAO;
import com.ips2026.pl11.model.ticket.HomeMatch;

/**
 * Controller for US 35082: Selling match tickets with contiguous seat allocation rules.
 */
public class MatchTicketController {

    private final MatchTicketDAO ticketDAO;
    private final SeasonPassDAO seasonPassDAO;
    private final MatchDAO matchDAO;

    /**
     * Constructs the ticket controller initializing the necessary DAOs.
     */
    public MatchTicketController() {
        this.ticketDAO = new MatchTicketDAO();
        this.seasonPassDAO = new SeasonPassDAO();
        this.matchDAO = new MatchDAO();
    }

    /**
     * Combines general match ticket sales and active season pass member seats into a single 10x15 occupied matrix grid.
     */
    public boolean[][] getOccupiedSeats(int matchId, String grandstand, String section) throws SQLException {
        boolean[][] occupied = new boolean[10][15];

        // 1. Fetch seats occupied by general match tickets (MATCH_TICKET table)
        boolean[][] ticketsOccupied = ticketDAO.getOccupiedSeats(matchId, grandstand, section);

        // 2. Retrieve match details to check if it is flagged as a Special Match
        HomeMatch match = matchDAO.getMatchById(matchId);
        boolean isSpecial = (match != null && match.isSpecialMatch());

        // 3. Fetch seats reserved by Season Pass holders (only locks seats for regular matches)
        boolean[][] seasonPassOccupied = new boolean[10][15];
        if (!isSpecial && match != null) {
            seasonPassOccupied = seasonPassDAO.getOccupiedSeasonPassSeats(grandstand, section);
        }

        // Merge both reservation matrices
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 15; c++) {
                occupied[r][c] = ticketsOccupied[r][c] || seasonPassOccupied[r][c];
            }
        }

        return occupied;
    }

    /**
     * Sells requested quantity of tickets allocated contiguously in the same row (US 35082).
     */
    public List<int[]> sellTickets(int matchId, String grandstand, String section, int quantity, double price) throws Exception {
        if (quantity < 1 || quantity > 15) {
            throw new IllegalArgumentException("You can only request between 1 and 15 tickets.");
        }

        boolean[][] occupiedMatrix = getOccupiedSeats(matchId, grandstand, section);

        // Search for a contiguous block of available seats in the same row
        for (int r = 0; r < 10; r++) {
            int consecutiveFree = 0;
            int startCol = -1;

            for (int c = 0; c < 15; c++) {
                if (!occupiedMatrix[r][c]) {
                    if (consecutiveFree == 0) {
                        startCol = c;
                    }
                    consecutiveFree++;

                    if (consecutiveFree == quantity) {
                        // Found a contiguous free seat block in row r
                        return ticketDAO.bookContiguousSeats(matchId, grandstand, section, r + 1, startCol + 1, quantity, price);
                    }
                } else {
                    consecutiveFree = 0;
                }
            }
        }

        // Thrown if no single row contains enough contiguous seats
        throw new IllegalStateException("There is no row in the chosen section with " + quantity + " contiguous available seats.");
    }
}