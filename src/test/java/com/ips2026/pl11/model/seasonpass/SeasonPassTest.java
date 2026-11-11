package com.ips2026.pl11.model.seasonpass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Test Suite for SeasonPass domain model.
 * Verifies constructor field assignments, ID mutation via setter, and getter methods.
 */
public class SeasonPassTest {

    @Test
    @DisplayName("SeasonPass: correctly initializes and exposes all fields via getters")
    void testSeasonPass_GettersMatchConstructorValues() {
        int fanId = 42;
        int teamId = 1;
        String grandstand = "A";
        String section = "B";
        int rowNum = 7;
        int seatNum = 12;
        String ageCategory = "ADULT";
        double price = 250.00;
        String startDate = "2026-07-01";
        String endDate = "2027-06-30";

        SeasonPass pass = new SeasonPass(fanId, teamId, grandstand, section, rowNum, seatNum, ageCategory, price, startDate, endDate);

        assertNotNull(pass, "The created SeasonPass instance must not be null.");
        assertEquals(fanId, pass.getFanId(), "Fan ID must match constructor argument.");
        assertEquals(teamId, pass.getTeamId(), "Team ID must match constructor argument.");
        assertEquals(grandstand, pass.getGrandstand(), "Grandstand must match constructor argument.");
        assertEquals(section, pass.getSection(), "Section must match constructor argument.");
        assertEquals(rowNum, pass.getRowNum(), "Row number must match constructor argument.");
        assertEquals(seatNum, pass.getSeatNum(), "Seat number must match constructor argument.");
        assertEquals(ageCategory, pass.getAgeCategory(), "Age category must match constructor argument.");
        assertEquals(price, pass.getPrice(), 0.001, "Price must match constructor argument.");
        assertEquals(startDate, pass.getStartDate(), "Start date must match constructor argument.");
        assertEquals(endDate, pass.getEndDate(), "End date must match constructor argument.");
    }

    @Test
    @DisplayName("SeasonPass: setId and getId properly mutate and retrieve database identifier")
    void testSeasonPass_SetAndGetId() {
        SeasonPass pass = new SeasonPass(10, 2, "C", "D", 3, 5, "SENIOR", 180.00, "2026-07-01", "2027-06-30");

        assertEquals(0, pass.getId(), "Initial default pass ID should be 0 before setting.");

        int expectedId = 501;
        pass.setId(expectedId);

        assertEquals(expectedId, pass.getId(), "Pass ID must match updated value after setter invocation.");
    }
}
