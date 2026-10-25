package com.ips2026.pl11.model.shareholder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ShareHolding")
class ShareHoldingTest {

    private static final BigDecimal PRICE_12_50 = new BigDecimal("12.50");

    /** List with the numbers from..to (both included). */
    private static List<Integer> ids(int from, int to) {
        List<Integer> ids = new ArrayList<>();
        for (int id = from; id <= to; id++) {
            ids.add(id);
        }
        return ids;
    }

    private static ShareHolding holding(String purchasePrice, List<Integer> shareIds) {
        return new ShareHolding(1, "Founding shares 2023", LocalDate.of(2023, 2, 10), new BigDecimal(purchasePrice), shareIds);
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    @Test
    @DisplayName("the quantity is the number of shares (IDs) of the holding")
    void quantityIsNumberOfShares() {
        assertEquals(500, holding("9.50", ids(1, 500)).getQuantity());
    }

    @Test
    @DisplayName("cost = quantity x purchase price of one share")
    void cost() {
        assertAmount("4750.00", holding("9.50", ids(1, 500)).getCost());
    }

    @Test
    @DisplayName("current value = quantity x market price")
    void value() {
        assertAmount("6250.00", holding("9.50", ids(1, 500)).getValue(PRICE_12_50));
    }

    @Test
    @DisplayName("a holding that went up has a positive variation in euros and in %")
    void variationUp() {
        ShareHolding holding = holding("10.00", ids(1, 300));

        assertAmount("750.00", holding.getVariation(PRICE_12_50));
        assertAmount("25.00", holding.getVariationPercent(PRICE_12_50));
    }

    @Test
    @DisplayName("a holding that went down has a negative variation in euros and in %")
    void variationDown() {
        ShareHolding holding = holding("15.00", ids(1, 400));

        assertAmount("-1000.00", holding.getVariation(PRICE_12_50));
        assertAmount("-16.67", holding.getVariationPercent(PRICE_12_50)); // -2.50 / 15.00, rounded to 2 decimals
    }

    @Test
    @DisplayName("bought at the current price: variation 0")
    void noVariation() {
        ShareHolding holding = holding("12.50", ids(1, 10));

        assertAmount("0", holding.getVariation(PRICE_12_50));
        assertAmount("0", holding.getVariationPercent(PRICE_12_50));
    }

    @Test
    @DisplayName("consecutive IDs are shown as one range")
    void consecutiveIdsAreOneRange() {
        assertEquals("1001 - 1300", holding("10.00", ids(1001, 1300)).getShareIdRanges());
    }

    @Test
    @DisplayName("IDs with gaps are shown as several ranges")
    void idsWithGapsAreSeveralRanges() {
        List<Integer> shareIds = ids(1, 300);
        shareIds.addAll(ids(601, 800));

        ShareHolding holding = holding("9.50", shareIds);

        assertEquals("1 - 300, 601 - 800", holding.getShareIdRanges());
        assertEquals(500, holding.getQuantity());
    }

    @Test
    @DisplayName("a single share is shown alone, not as a range")
    void singleShares() {
        assertEquals("7, 9 - 10, 42", holding("10.00", List.of(7, 9, 10, 42)).getShareIdRanges());
    }

    @Test
    @DisplayName("the list of IDs can not be changed from outside")
    void shareIdsAreACopy() {
        List<Integer> shareIds = ids(1, 3);
        ShareHolding holding = holding("10.00", shareIds);

        shareIds.add(99);

        assertEquals(3, holding.getQuantity());
    }
}
