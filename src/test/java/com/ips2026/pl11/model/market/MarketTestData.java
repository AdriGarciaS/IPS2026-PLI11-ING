package com.ips2026.pl11.model.market;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Listings used by the share market tests.
 */
final class MarketTestData {

    private MarketTestData() {
        // Utility class: not instantiated.
    }

    /** A listing of the shares from..to, with an optional pack price and some shares already sold. */
    static ShareListing listing(int id, String pricePerShare, String packPrice, int from, int to, int soldShares) {
        List<Integer> ids = new ArrayList<>();
        for (int shareId = from; shareId <= to; shareId++) {
            ids.add(shareId);
        }
        return new ShareListing(id, 1, "Laura Pérez Gil", new BigDecimal(pricePerShare),
                packPrice == null ? null : new BigDecimal(packPrice), LocalDateTime.of(2026, 10, 24, 10, 30), ids, soldShares);
    }
}
