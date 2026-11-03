package com.ips2026.pl11.model.market;

import java.math.BigDecimal;
import java.util.List;

/**
 * Result of a purchase in the share market: its number, the IDs of the
 * shares that now belong to the buyer and the amount paid.
 */
public class MarketPurchase {

    private final int purchaseNumber;
    private final List<Integer> shareIds;
    private final BigDecimal total;

    public MarketPurchase(int purchaseNumber, List<Integer> shareIds, BigDecimal total) {
        this.purchaseNumber = purchaseNumber;
        this.shareIds = List.copyOf(shareIds);
        this.total = total;
    }

    public int getPurchaseNumber() {
        return purchaseNumber;
    }

    public List<Integer> getShareIds() {
        return shareIds;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
