package com.ips2026.pl11.model.market;

import static com.ips2026.pl11.model.market.MarketTestData.listing;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests of the share market filter: price range (pack and individual),
 * number of shares and sale type.
 */
@DisplayName("MarketFilter")
class MarketFilterTest {

    /** 20 shares at 12,50 € each, or the pack for 230 €. */
    private static final ShareListing WITH_PACK = listing(1, "12.50", "230.00", 1001, 1020, 0);
    /** 15 shares at 13 € each, no pack. */
    private static final ShareListing WITHOUT_PACK = listing(2, "13.00", null, 1301, 1315, 0);
    /** 8 shares at 11,80 €; its pack (110 €) no longer applies. */
    private static final ShareListing PACK_BROKEN = listing(3, "11.80", "110.00", 303, 310, 2);

    private static MarketFilter filter(String minPrice, String maxPrice, Integer minShares, Integer maxShares, SaleType type) {
        return new MarketFilter(minPrice == null ? null : new BigDecimal(minPrice),
                maxPrice == null ? null : new BigDecimal(maxPrice), minShares, maxShares, type);
    }

    @Test
    @DisplayName("without limits every listing is shown")
    void noLimits() {
        assertTrue(MarketFilter.NONE.matches(WITH_PACK));
        assertTrue(MarketFilter.NONE.matches(WITHOUT_PACK));
        assertTrue(MarketFilter.NONE.matches(PACK_BROKEN));
    }

    @Test
    @DisplayName("'Packs' only shows the listings whose pack can still be bought")
    void onlyPacks() {
        MarketFilter packs = filter(null, null, null, null, SaleType.PACKS);

        assertTrue(packs.matches(WITH_PACK));
        assertFalse(packs.matches(WITHOUT_PACK));
        assertFalse(packs.matches(PACK_BROKEN));
    }

    @Test
    @DisplayName("'Individual shares' shows every listing")
    void onlyIndividual() {
        MarketFilter individual = filter(null, null, null, null, SaleType.INDIVIDUAL);

        assertTrue(individual.matches(WITH_PACK));
        assertTrue(individual.matches(WITHOUT_PACK));
        assertTrue(individual.matches(PACK_BROKEN));
    }

    @Test
    @DisplayName("the price range applies to the pack price for packs")
    void priceOfPacks() {
        assertTrue(filter("200", "250", null, null, SaleType.PACKS).matches(WITH_PACK));
        assertFalse(filter("10", "15", null, null, SaleType.PACKS).matches(WITH_PACK), "12,50 € is the price per share, not of the pack");
    }

    @Test
    @DisplayName("the price range applies to the price per share for individual shares")
    void priceOfIndividualShares() {
        assertTrue(filter("12", "13", null, null, SaleType.INDIVIDUAL).matches(WITH_PACK));
        assertFalse(filter("200", "250", null, null, SaleType.INDIVIDUAL).matches(WITH_PACK));
    }

    @Test
    @DisplayName("with 'Both', the pack price or the price per share in the range is enough")
    void priceOfBoth() {
        assertTrue(filter("200", "250", null, null, SaleType.BOTH).matches(WITH_PACK), "by its pack");
        assertTrue(filter("12", "13", null, null, SaleType.BOTH).matches(WITH_PACK), "by its price per share");
        assertFalse(filter("100", "120", null, null, SaleType.BOTH).matches(PACK_BROKEN), "its pack no longer applies");
    }

    @Test
    @DisplayName("the limits of the price range are included")
    void priceLimitsIncluded() {
        assertTrue(filter("13.00", "13.00", null, null, SaleType.INDIVIDUAL).matches(WITHOUT_PACK));
        assertFalse(filter("13.01", null, null, null, SaleType.INDIVIDUAL).matches(WITHOUT_PACK));
        assertFalse(filter(null, "12.99", null, null, SaleType.INDIVIDUAL).matches(WITHOUT_PACK));
    }

    @Test
    @DisplayName("the range of shares applies to the shares still available, limits included")
    void numberOfShares() {
        assertTrue(filter(null, null, 15, 20, SaleType.BOTH).matches(WITHOUT_PACK));
        assertTrue(filter(null, null, 8, 8, SaleType.BOTH).matches(PACK_BROKEN));
        assertFalse(filter(null, null, 9, null, SaleType.BOTH).matches(PACK_BROKEN));
        assertFalse(filter(null, null, null, 14, SaleType.BOTH).matches(WITHOUT_PACK));
    }

    @Test
    @DisplayName("negative limits or a minimum greater than the maximum are rejected")
    void wrongLimits() {
        assertThrows(IllegalArgumentException.class, () -> filter("-1", null, null, null, SaleType.BOTH));
        assertThrows(IllegalArgumentException.class, () -> filter("20", "10", null, null, SaleType.BOTH));
        assertThrows(IllegalArgumentException.class, () -> filter(null, null, -1, null, SaleType.BOTH));
        assertThrows(IllegalArgumentException.class, () -> filter(null, null, 10, 5, SaleType.BOTH));
    }
}
