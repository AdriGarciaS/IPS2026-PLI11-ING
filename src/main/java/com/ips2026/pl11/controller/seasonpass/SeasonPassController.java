package com.ips2026.pl11.controller.seasonpass;

import java.sql.SQLException;
import java.util.List;

import com.ips2026.pl11.data.seasonpass.SeasonPassDAO;
import com.ips2026.pl11.model.seasonpass.SeasonDateUtils;

public class SeasonPassController {
	
	private final SeasonPassDAO seasonPassDAO;

    public SeasonPassController() {
        this.seasonPassDAO = new SeasonPassDAO();
    }

    /**
     * Calculates final pass price based on base location price and age category discount.
     */
    public double calculatePrice(double basePrice, String ageCategory) {
        return switch (ageCategory) {
            case "Children" -> basePrice * 0.50;
            case "Under 26"  -> basePrice * 0.80;
            case "Seniors"   -> basePrice * 0.70;
            default         -> basePrice; // "Adults"
        };
    }

    /**
     * Gets 10x15 grid of occupied season pass seats for a given team, grandstand, section, and season.
     */
    public boolean[][] getOccupiedSeasonPassSeats(String teamName, String grandstand, String section) throws SQLException {
        boolean[][] grid = new boolean[10][15];
        String startDate = SeasonDateUtils.getCurrentSeasonBounds()[0];

        List<int[]> passSeats = seasonPassDAO.getActivePassSeatsForMatch(teamName, grandstand, section, startDate);
        for (int[] seat : passSeats) {
            int r = seat[0];
            int c = seat[1];
            if (r >= 0 && r < 10 && c >= 0 && c < 15) {
                grid[r][c] = true;
            }
        }
        return grid;
    }

    /**
     * Purchases a season pass for the selected seat and fan details.
     */
    public boolean purchaseSeasonPass(String dni, String fullName, String email, String teamName, String grandstand, String section, int rowNum, int seatNum, String ageCategory, double basePrice) throws Exception {

        if (dni.isBlank() || fullName.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException("All pass holder details (DNI, Name, Email) are required.");
        }

        String[] seasonBounds = SeasonDateUtils.getCurrentSeasonBounds();
        String startDate = seasonBounds[0];
        String endDate = seasonBounds[1];

        // Check if seat is already taken for this team & season
        boolean isTaken = seasonPassDAO.isSeatPassOccupiedByName(teamName, grandstand, section, rowNum, seatNum, startDate);
        if (isTaken) {
            throw new IllegalStateException("Seat Row " + rowNum + ", Seat " + seatNum + " is already occupied by another season pass holder.");
        }

        double finalPrice = calculatePrice(basePrice, ageCategory);

        return seasonPassDAO.createSeasonPassWithHolder(dni, fullName, email, teamName, grandstand, section, rowNum, seatNum, ageCategory, finalPrice, startDate, endDate);
    }

}
