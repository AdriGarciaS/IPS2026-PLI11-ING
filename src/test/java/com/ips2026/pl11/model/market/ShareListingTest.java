package com.ips2026.pl11.model.market;

import static com.ips2026.pl11.model.market.MarketTestData.listing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests of a share market listing: when its pack can be bought.
 */
@DisplayName("ShareListing")
class ShareListingTest {

    @Test
    @DisplayName("the pack can be bought while no share has been sold")
    void packAvailable() {
        ShareListing listing = listing(1, "12.50", "230.00", 1001, 1020, 0);

        assertTrue(listing.isPackAvailable());
        assertEquals(20, listing.getAvailableShares());
    }

    @Test
    @DisplayName("once a share has been sold, the pack price no longer applies")
    void packNotAvailableAfterASale() {
        ShareListing listing = listing(3, "11.80", "110.00", 303, 310, 2);

        assertFalse(listing.isPackAvailable());
        assertEquals(new BigDecimal("110.00"), listing.getPackPrice(), "the price set by the seller is kept");
    }

    @Test
    @DisplayName("a listing without pack price has no pack")
    void listingWithoutPack() {
        assertFalse(listing(2, "13.00", null, 1301, 1315, 0).isPackAvailable());
    }

    @Test
    @DisplayName("each share of the pack costs the pack price divided by its shares")
    void packPricePerShare() {
        assertEquals(0, new BigDecimal("11.50").compareTo(listing(1, "12.50", "230.00", 1001, 1020, 0).getPackPricePerShare()));
        assertEquals(new BigDecimal("33.3333"), listing(4, "14.90", "100.00", 1, 3, 0).getPackPricePerShare());
    }
}
