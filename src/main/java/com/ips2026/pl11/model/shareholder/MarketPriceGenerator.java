package com.ips2026.pl11.model.shareholder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Creates the random market price of a new day. For now the price is
 * simulated: it moves at random, at most {@link #MAX_DAILY_CHANGE} up or
 * down from the last known price. Another user story may replace it.
 */
public final class MarketPriceGenerator {

    /** Maximum change of the price from one day to the next: 0.05 = 5 %. */
    public static final double MAX_DAILY_CHANGE = 0.05;

    /** Price used if there is no previous price at all. */
    public static final BigDecimal INITIAL_PRICE = new BigDecimal("12.00");

    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");

    private MarketPriceGenerator() {
        // Utility class: not instantiated.
    }

    /**
     * New price between lastPrice x (1 - 5 %) and lastPrice x (1 + 5 %),
     * rounded to cents (never below 0.01).
     *
     * @param lastPrice last known price, or null to use {@link #INITIAL_PRICE}
     */
    public static BigDecimal nextPrice(BigDecimal lastPrice, Random random) {
        BigDecimal base = lastPrice == null ? INITIAL_PRICE : lastPrice;
        double change = (random.nextDouble() * 2 - 1) * MAX_DAILY_CHANGE;
        BigDecimal next = base.multiply(BigDecimal.valueOf(1 + change)).setScale(2, RoundingMode.HALF_UP);
        return next.max(MIN_PRICE);
    }
}
