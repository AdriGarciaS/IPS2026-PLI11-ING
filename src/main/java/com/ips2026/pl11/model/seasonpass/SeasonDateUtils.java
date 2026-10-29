package com.ips2026.pl11.model.seasonpass;

import java.time.LocalDate;

public class SeasonDateUtils {

	/**
     * Calculates the season start date (July 1st) and end date (June 30th)
     * based on the current date.
     * 
     * @return String array where [0] = startDate ('YYYY-07-01') and [1] = endDate ('YYYY-06-30').
     */
    public static String[] getCurrentSeasonBounds() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        int startYear;
        int endYear;

        // If purchased between January and June, the season started July 1st of the previous year
        if (now.getMonthValue() < 7) {
            startYear = currentYear - 1;
            endYear = currentYear;
        } else {
            // If purchased between July and December, the season ends June 30th of the next year
            startYear = currentYear;
            endYear = currentYear + 1;
        }

        String startDate = startYear + "-07-01";
        String endDate = endYear + "-06-30";

        return new String[]{startDate, endDate};
    }
	
}
