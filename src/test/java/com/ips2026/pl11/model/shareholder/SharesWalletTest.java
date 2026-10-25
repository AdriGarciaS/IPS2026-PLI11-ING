package com.ips2026.pl11.model.shareholder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SharesWallet")
class SharesWalletTest {

    private static final Shareholder LAURA = new Shareholder(1, "Laura", "Pérez Gil", "11111111H");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 22);

    private static MarketPrice price(String value) {
        return new MarketPrice(TODAY, new BigDecimal(value));
    }

    private static MarketPrice yesterday(String value) {
        return new MarketPrice(TODAY.minusDays(1), new BigDecimal(value));
    }

    private static ShareHolding holding(String campaign, String purchasePrice, int firstId, int quantity) {
        List<Integer> ids = new ArrayList<>();
        for (int id = firstId; id < firstId + quantity; id++) {
            ids.add(id);
        }
        return new ShareHolding(firstId, campaign, LocalDate.of(2024, 3, 12), new BigDecimal(purchasePrice), ids);
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    /** Laura in the sample data: 500 shares at 9.50 and 300 at 10.00. */
    private static SharesWallet laura(String marketPrice) {
        return new SharesWallet(LAURA, List.of(
                holding("Founding shares 2023", "9.50", 1, 500),
                holding("Capital increase 2024", "10.00", 1001, 300)), price(marketPrice), yesterday("12.30"));
    }

    @Test
    @DisplayName("the total portfolio value is the sum of the current value of every holding")
    void totalValueIsSumOfValues() {
        assertAmount("10000.00", laura("12.50").getTotalValue()); // 800 x 12.50
    }

    @Test
    @DisplayName("total cost, variation and percentage of all the holdings")
    void totalCostAndVariation() {
        SharesWallet wallet = laura("12.50");

        assertAmount("7750.00", wallet.getTotalCost());        // 500 x 9.50 + 300 x 10.00
        assertAmount("2250.00", wallet.getTotalVariation());
        assertAmount("29.03", wallet.getTotalVariationPercent());
        assertEquals(800, wallet.getTotalShares());
    }

    @Test
    @DisplayName("a portfolio worth more than it cost is up (green)")
    void portfolioUp() {
        assertTrue(laura("12.50").isUp());
    }

    @Test
    @DisplayName("a portfolio worth less than it cost is down (red)")
    void portfolioDown() {
        SharesWallet wallet = laura("9.00");

        assertFalse(wallet.isUp());
        assertAmount("-550.00", wallet.getTotalVariation()); // 7200 - 7750
    }

    @Test
    @DisplayName("a portfolio worth exactly what it cost counts as up (green)")
    void portfolioEvenIsUp() {
        SharesWallet wallet = new SharesWallet(LAURA, List.of(holding("Capital increase 2024", "10.00", 1, 100)),
                price("10.00"), null);

        assertAmount("0", wallet.getTotalVariation());
        assertTrue(wallet.isUp());
    }

    @Test
    @DisplayName("one holding up and another down: the total uses both")
    void mixedHoldings() {
        SharesWallet wallet = new SharesWallet(LAURA, List.of(
                holding("Founding shares 2023", "9.80", 301, 200),     // +540.00 at 12.50
                holding("Capital increase 2026", "14.80", 2251, 350)), // -805.00 at 12.50
                price("12.50"), null);

        assertAmount("-265.00", wallet.getTotalVariation());
        assertFalse(wallet.isUp());
    }

    @Test
    @DisplayName("a shareholder without shares has a value of 0 and is not down")
    void emptyWallet() {
        SharesWallet wallet = new SharesWallet(LAURA, List.of(), price("12.50"), yesterday("12.30"));

        assertTrue(wallet.isEmpty());
        assertAmount("0", wallet.getTotalValue());
        assertAmount("0", wallet.getTotalVariationPercent());
        assertEquals(0, wallet.getTotalShares());
        assertEquals(0, wallet.getCampaignCount());
        assertTrue(wallet.isUp());
    }

    @Test
    @DisplayName("the campaigns are counted once each")
    void campaignCount() {
        assertEquals(2, laura("12.50").getCampaignCount());
    }

    @Test
    @DisplayName("the change from the previous day is a percentage of the previous price")
    void dailyChange() {
        assertAmount("1.63", laura("12.50").getDailyChangePercent());  // (12.50 - 12.30) / 12.30
        assertAmount("-3.90", laura("11.82").getDailyChangePercent()); // (11.82 - 12.30) / 12.30
    }

    @Test
    @DisplayName("without a previous price there is no daily change")
    void noPreviousPrice() {
        SharesWallet wallet = new SharesWallet(LAURA, List.of(), price("12.50"), null);

        assertNull(wallet.getDailyChangePercent());
    }
}
