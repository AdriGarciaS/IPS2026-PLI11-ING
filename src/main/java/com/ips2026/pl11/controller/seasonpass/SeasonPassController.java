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
     * Attempts to purchase a season pass.
     * Checks if the seat is already taken for that team/season before saving.
     */
    public boolean purchaseSeasonPass(int fanId, int teamId, String grandstand, String section,int rowNum, int seatNum, String ageCategory, double basePrice,String seasonStartDate, String seasonEndDate) throws SQLException {

        // 1. Verify seat availability for this team's season
        boolean isTaken = seasonPassDAO.isSeatPassOccupied(teamId, grandstand, section, rowNum, seatNum, seasonStartDate);
        if (isTaken) {
            throw new IllegalStateException("Seat " + seatNum + " in Row " + rowNum + 
                    " is already reserved for another season pass holder.");
        }

        // 2. Calculate final adjusted price
        double finalPrice = calculatePrice(basePrice, ageCategory);

        // 3. Register season pass in database
        return seasonPassDAO.createSeasonPass(fanId, teamId, grandstand, section, rowNum, seatNum, ageCategory, finalPrice, seasonStartDate, seasonEndDate);
    }
    
    /**
     * Validates input parameters and purchases a season pass using dynamic season bounds.
     */
    public boolean purchaseSeasonPass(int fanId, int teamId, String grandstand, String section, 
                                     int rowNum, int seatNum, String ageCategory, double basePrice) throws SQLException {

        // 1. Boundary validations
        if (rowNum < 1 || rowNum > 10 || seatNum < 1 || seatNum > 15) {
            throw new IllegalArgumentException("Invalid seat location. Rows must be 1-10 and seats 1-15.");
        }

        if (!List.of("Adults", "Seniors", "Children", "Under 26").contains(ageCategory)) {
            throw new IllegalArgumentException("Invalid age category provided.");
        }

        // 2. Compute dynamic July 1st - June 30th season range
        String[] seasonBounds = SeasonDateUtils.getCurrentSeasonBounds();
        String startDate = seasonBounds[0];
        String endDate = seasonBounds[1];

        // 3. Check seat availability for this team and season
        boolean isTaken = seasonPassDAO.isSeatPassOccupied(teamId, grandstand, section, rowNum, seatNum, startDate);
        if (isTaken) {
            throw new IllegalStateException("Seat " + seatNum + " in Row " + rowNum + 
                    " (" + grandstand + "-" + section + ") is already occupied by a season pass holder.");
        }

        // 4. Calculate final discounted price
        double finalPrice = calculatePrice(basePrice, ageCategory);

        // 5. Save transaction
        return seasonPassDAO.createSeasonPass(fanId, teamId, grandstand, section,rowNum, seatNum, ageCategory, finalPrice, startDate, endDate);
    }

}
