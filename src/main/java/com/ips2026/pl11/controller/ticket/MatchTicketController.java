package com.ips2026.pl11.controller.ticket;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.ips2026.pl11.data.seasonpass.SeasonPassDAO;
import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.data.ticket.MatchTicketDAO;
import com.ips2026.pl11.model.seasonpass.SeasonDateUtils;
import com.ips2026.pl11.model.ticket.HomeMatch;
import com.ips2026.pl11.model.ticket.SeatStatus;

/**
 * Controller for US 35082 & US 36757: Managing seating map colors and ticket sales.
 * Handles special match member locks only within the active current season bounds.
 */
public class MatchTicketController {

    private final MatchTicketDAO ticketDAO;
    private final SeasonPassDAO seasonPassDAO;
    private final MatchDAO matchDAO;

    public MatchTicketController() {
        this.ticketDAO = new MatchTicketDAO();
        this.seasonPassDAO = new SeasonPassDAO();
        this.matchDAO = new MatchDAO();
    }

    public MatchTicketController(MatchTicketDAO ticketDAO, SeasonPassDAO seasonPassDAO, MatchDAO matchDAO) {
        this.ticketDAO = ticketDAO;
        this.seasonPassDAO = seasonPassDAO;
        this.matchDAO = matchDAO;
    }

    /**
     * Builds the 10x15 SeatStatus matrix for UI rendering according to US 36757 rules:
     * - Regular Match (Current Season): Member seats locked directly as OCCUPIED (Red).
     * - Special Match (Current Season): Member seats locked as SEASON_PASS_RESERVED (Yellow/Orange).
     * - Future Season Match (Regular or Special): Season pass locks are NOT applied (all unsold seats remain Green / AVAILABLE).
     */
    public SeatStatus[][] getSeatMatrix(int matchId, String grandstand, String section) throws SQLException {
        SeatStatus[][] matrix = new SeatStatus[10][15];

        // 1. Initialize all grid seats default to AVAILABLE (Green)
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 15; c++) {
                matrix[r][c] = SeatStatus.AVAILABLE;
            }
        }

        // 2. Load seats already paid/secured via MATCH_TICKET -> OCCUPIED (Red)
        boolean[][] ticketsOccupied = ticketDAO.getOccupiedSeats(matchId, grandstand, section);
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 15; c++) {
                if (ticketsOccupied[r][c]) {
                    matrix[r][c] = SeatStatus.OCCUPIED;
                }
            }
        }

        // 3. Inspect match metadata and season date bounds
        HomeMatch match = matchDAO.getMatchById(matchId);
        if (match != null) {
            boolean isSpecial = match.isSpecialMatch();
            
            // Check if the match belongs to the current active season
            boolean isCurrentSeasonMatch = isMatchInCurrentSeason(match.getMatchDate());

            // 4. Season pass seat locks ONLY apply if the match is within the CURRENT season
            if (isCurrentSeasonMatch) {
                boolean[][] seasonPassOccupied = seasonPassDAO.getOccupiedSeasonPassSeats(grandstand, section);

                for (int r = 0; r < 10; r++) {
                    for (int c = 0; c < 15; c++) {
                        if (seasonPassOccupied[r][c] && matrix[r][c] == SeatStatus.AVAILABLE) {
                            if (isSpecial) {
                                // Special Match (Current Season): Yellow/Orange reservation lock
                                matrix[r][c] = SeatStatus.SEASON_PASS_RESERVED;
                            } else {
                                // Regular Match (Current Season): Red occupied lock
                                matrix[r][c] = SeatStatus.OCCUPIED;
                            }
                        }
                    }
                }
            }
            // If it is a Future Season match, season pass locks are completely bypassed!
        }

        return matrix;
    }

    /**
     * Helper method to verify if a match date falls within the current active season date range.
     */
    private boolean isMatchInCurrentSeason(String matchDateStr) {
        if (matchDateStr == null || matchDateStr.isEmpty()) {
            return true;
        }

        try {
            LocalDate matchDate = LocalDate.parse(matchDateStr);
            String[] bounds = SeasonDateUtils.getCurrentSeasonBounds();
            LocalDate seasonStart = LocalDate.parse(bounds[0]);
            LocalDate seasonEnd = LocalDate.parse(bounds[1]);

            // Match is in current season if matchDate is between seasonStart and seasonEnd
            return !matchDate.isBefore(seasonStart) && !matchDate.isAfter(seasonEnd);
        } catch (Exception e) {
            // Default fallback to true if date parsing fails
            return true;
        }
    }

    /**
     * Converts status matrix into a binary boolean grid (true = unavailable for public sale).
     */
    public boolean[][] getOccupiedSeats(int matchId, String grandstand, String section) throws SQLException {
        boolean[][] occupied = new boolean[10][15];
        SeatStatus[][] matrix = getSeatMatrix(matchId, grandstand, section);

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 15; c++) {
                occupied[r][c] = matrix[r][c] != SeatStatus.AVAILABLE;
            }
        }

        return occupied;
    }

    /**
     * Sells requested quantity of tickets allocated contiguously in the same row.
     */
    public List<int[]> sellTickets(int matchId, String grandstand, String section, int quantity, double price) throws Exception {
        if (quantity < 1 || quantity > 15) {
            throw new IllegalArgumentException("You can only request between 1 and 15 tickets.");
        }

        boolean[][] occupiedMatrix = getOccupiedSeats(matchId, grandstand, section);

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
                        return ticketDAO.bookContiguousSeats(matchId, grandstand, section, r + 1, startCol + 1, quantity, price);
                    }
                } else {
                    consecutiveFree = 0;
                }
            }
        }

        throw new IllegalStateException("There is no row in the chosen section with " + quantity + " contiguous available seats.");
    }
}