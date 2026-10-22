package com.ips2026.pl11.model.shareholder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Shares of a shareholder bought in one campaign (table SHARE_HOLDING), with
 * the ID of every share (table SHARE). The quantity is the number of shares.
 */
public final class ShareHolding {

    private final int id;
    private final String campaignName;
    private final LocalDate purchaseDate;
    private final BigDecimal purchasePrice;
    private final List<Integer> shareIds;

    /**
     * @param purchasePrice price of ONE share when it was bought
     * @param shareIds      IDs of the shares of the holding, in ascending order
     */
    public ShareHolding(int id, String campaignName, LocalDate purchaseDate, BigDecimal purchasePrice,
            List<Integer> shareIds) {
        this.id = id;
        this.campaignName = campaignName;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
        this.shareIds = List.copyOf(shareIds);
    }

    public int getId() {
        return id;
    }

    public String getCampaignName() {
        return campaignName;
    }

    /** Number of shares of the holding. */
    public int getQuantity() {
        return shareIds.size();
    }

    /** IDs of the shares, in ascending order. */
    public List<Integer> getShareIds() {
        return shareIds;
    }

    /**
     * The share IDs as ranges of consecutive numbers, for example
     * "1 - 300, 601 - 800" (a single share is shown alone, for example "42").
     */
    public String getShareIdRanges() {
        List<String> ranges = new ArrayList<>();
        int i = 0;
        while (i < shareIds.size()) {
            int first = shareIds.get(i);
            int last = first;
            while (i + 1 < shareIds.size() && shareIds.get(i + 1) == last + 1) {
                last = shareIds.get(++i);
            }
            ranges.add(first == last ? String.valueOf(first) : first + " - " + last);
            i++;
        }
        return String.join(", ", ranges);
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    /** Price of one share when it was bought. */
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    /** What the shares cost: quantity x purchase price. */
    public BigDecimal getCost() {
        return purchasePrice.multiply(BigDecimal.valueOf(getQuantity()));
    }

    /** What the shares are worth now: quantity x market price. */
    public BigDecimal getValue(BigDecimal marketPrice) {
        return marketPrice.multiply(BigDecimal.valueOf(getQuantity()));
    }

    /** Money won (positive) or lost (negative) since the purchase. */
    public BigDecimal getVariation(BigDecimal marketPrice) {
        return getValue(marketPrice).subtract(getCost());
    }

    /** Variation as a percentage of the cost, for example 24.00 (= +24 %). */
    public BigDecimal getVariationPercent(BigDecimal marketPrice) {
        return percent(getVariation(marketPrice), getCost());
    }

    /** {@code part} as a percentage of {@code whole}, with 2 decimals (0 if whole is 0). */
    static BigDecimal percent(BigDecimal part, BigDecimal whole) {
        if (whole.signum() == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(whole, 2, RoundingMode.HALF_UP);
    }
}
