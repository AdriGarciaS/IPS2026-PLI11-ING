package com.ips2026.pl11.model.reservation;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

/**
 * Business rules of the facility reservations, all in one place so they are
 * easy to change.
 */
public final class ReservationRules {

    /** Facilities can be booked from this time... */
    public static final LocalTime OPENING_TIME = LocalTime.of(8, 0);

    /** ...until this time (a reservation must end at this time at the latest). */
    public static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);

    /** Time in which the facility can not be booked after a team of the club uses it. */
    public static final Duration BLOCKED_AFTER_TEAM_USE = Duration.ofMinutes(90);

    /** A reservation lasts a whole number of hours, at least this many. */
    public static final int MIN_HOURS = 1;

    /** Price charged for each booked hour. */
    public static final BigDecimal PRICE_PER_HOUR = new BigDecimal("50.00");

    /**
     * How far ahead a facility can be booked, counting from today.
     * Change it here (for example {@code Period.ofWeeks(2)}) and the whole
     * application uses the new value.
     */
    public static final Period BOOKING_WINDOW = Period.ofMonths(1);

    private ReservationRules() {
        // Constants only: not instantiated.
    }

    /** Last day that can be booked if today is {@code today}. */
    public static LocalDate lastBookableDay(LocalDate today) {
        return today.plus(BOOKING_WINDOW);
    }

    /** Price of a reservation of {@code hours} hours. */
    public static BigDecimal priceFor(int hours) {
        return PRICE_PER_HOUR.multiply(BigDecimal.valueOf(hours));
    }
}
