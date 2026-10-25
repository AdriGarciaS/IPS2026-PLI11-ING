package com.ips2026.pl11.model.shareholder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MarketPriceGenerator")
class MarketPriceGeneratorTest {

    private static final BigDecimal LAST = new BigDecimal("12.30");

    @Test
    @DisplayName("the new price is never more than 5 % up or down from the last price")
    void staysWithinFivePercent() {
        BigDecimal min = new BigDecimal("11.68"); // 12.30 x 0.95 = 11.685, rounded down by cents
        BigDecimal max = new BigDecimal("12.92"); // 12.30 x 1.05 = 12.915, rounded up by cents
        Random random = new Random(1234);
        for (int i = 0; i < 10_000; i++) {
            BigDecimal price = MarketPriceGenerator.nextPrice(LAST, random);
            assertTrue(price.compareTo(min) >= 0 && price.compareTo(max) <= 0, () -> "out of range: " + price);
        }
    }

    @Test
    @DisplayName("the price can go up and can go down")
    void goesUpAndDown() {
        Random random = new Random(42);
        boolean up = false;
        boolean down = false;
        for (int i = 0; i < 200; i++) {
            int compare = MarketPriceGenerator.nextPrice(LAST, random).compareTo(LAST);
            up |= compare > 0;
            down |= compare < 0;
        }
        assertTrue(up && down);
    }

    @Test
    @DisplayName("the price is random (not always the same)")
    void isRandom() {
        Random random = new Random(7);
        Set<BigDecimal> prices = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            prices.add(MarketPriceGenerator.nextPrice(LAST, random));
        }
        assertTrue(prices.size() > 10);
    }

    @Test
    @DisplayName("prices have exactly 2 decimals (cents)")
    void roundedToCents() {
        assertEquals(2, MarketPriceGenerator.nextPrice(LAST, new Random(3)).scale());
    }

    @Test
    @DisplayName("without a last price it starts from the initial price (12.00)")
    void initialPrice() {
        BigDecimal price = MarketPriceGenerator.nextPrice(null, new Random(5));

        assertTrue(price.compareTo(new BigDecimal("11.40")) >= 0 && price.compareTo(new BigDecimal("12.60")) <= 0);
    }

    @Test
    @DisplayName("the same random numbers give the same price (so it can be reproduced)")
    void reproducibleWithSameSeed() {
        assertEquals(MarketPriceGenerator.nextPrice(LAST, new Random(99)), MarketPriceGenerator.nextPrice(LAST, new Random(99)));
    }

    @Test
    @DisplayName("the price never goes below 0.01")
    void neverBelowOneCent() {
        Random random = new Random(11);
        for (int i = 0; i < 100; i++) {
            assertTrue(MarketPriceGenerator.nextPrice(new BigDecimal("0.01"), random).compareTo(new BigDecimal("0.01")) >= 0);
        }
    }
}
