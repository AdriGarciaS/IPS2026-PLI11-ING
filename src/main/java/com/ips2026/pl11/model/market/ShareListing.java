package com.ips2026.pl11.model.market;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Shares that a shareholder has put up for sale in the share market (table
 * SHARE_LISTING), with the shares still available. Every share can be bought
 * individually at the price per share; the pack price (optional) is for all
 * the shares together and only applies while no share has been sold.
 */
public class ShareListing {

    private final int id;
    private final int sellerId;
    private final String sellerName;
    private final BigDecimal pricePerShare;
    private final BigDecimal packPrice;
    private final LocalDateTime listedAt;
    private final List<Integer> availableShareIds;
    private final int soldShares;

    /**
     * @param packPrice         price of the whole listing, or null if the seller did not set one
     * @param availableShareIds IDs of the shares not sold yet, in order
     * @param soldShares        number of shares of the listing already sold
     */
    public ShareListing(int id, int sellerId, String sellerName, BigDecimal pricePerShare, BigDecimal packPrice,
            LocalDateTime listedAt, List<Integer> availableShareIds, int soldShares) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.pricePerShare = pricePerShare;
        this.packPrice = packPrice;
        this.listedAt = listedAt;
        this.availableShareIds = List.copyOf(availableShareIds);
        this.soldShares = soldShares;
    }

    public int getId() {
        return id;
    }

    public int getSellerId() {
        return sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    /** The pack price set by the seller (null if none), even if it no longer applies. */
    public BigDecimal getPackPrice() {
        return packPrice;
    }

    public LocalDateTime getListedAt() {
        return listedAt;
    }

    public List<Integer> getAvailableShareIds() {
        return availableShareIds;
    }

    /** Number of shares that can still be bought. */
    public int getAvailableShares() {
        return availableShareIds.size();
    }

    /** The whole listing can be bought as a pack: it has a pack price and no share has been sold yet. */
    public boolean isPackAvailable() {
        return packPrice != null && soldShares == 0 && !availableShareIds.isEmpty();
    }

    /** Price of each share when the pack is bought (pack price / shares). */
    public BigDecimal getPackPricePerShare() {
        return packPrice.divide(BigDecimal.valueOf(availableShareIds.size()), 4, RoundingMode.HALF_UP);
    }
}
