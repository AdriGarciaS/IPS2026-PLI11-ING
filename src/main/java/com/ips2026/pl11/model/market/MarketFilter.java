package com.ips2026.pl11.model.market;

import java.math.BigDecimal;

/**
 * Filter of the share market: price range, range of available shares and
 * sale type. Each limit is optional (null = no limit) and both limits are
 * included. The price range applies to what can be bought with the chosen
 * sale type: the pack price for packs and the price per share for individual
 * shares (with "both", one of them is enough).
 */
public class MarketFilter {

    /** No limits, packs and individual shares. */
    public static final MarketFilter NONE = new MarketFilter(null, null, null, null, SaleType.BOTH);

    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final Integer minShares;
    private final Integer maxShares;
    private final SaleType saleType;

    /**
     * @throws IllegalArgumentException if a limit is negative or a minimum is greater than its maximum
     */
    public MarketFilter(BigDecimal minPrice, BigDecimal maxPrice, Integer minShares, Integer maxShares, SaleType saleType) {
        if ((minPrice != null && minPrice.signum() < 0) || (maxPrice != null && maxPrice.signum() < 0)) {
            throw new IllegalArgumentException("The prices cannot be negative.");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("The minimum price cannot be greater than the maximum price.");
        }
        if ((minShares != null && minShares < 0) || (maxShares != null && maxShares < 0)) {
            throw new IllegalArgumentException("The number of shares cannot be negative.");
        }
        if (minShares != null && maxShares != null && minShares > maxShares) {
            throw new IllegalArgumentException("The minimum number of shares cannot be greater than the maximum.");
        }
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minShares = minShares;
        this.maxShares = maxShares;
        this.saleType = saleType;
    }

    public SaleType getSaleType() {
        return saleType;
    }

    /** Whether the listing is shown with this filter. */
    public boolean matches(ShareListing listing) {
        int shares = listing.getAvailableShares();
        if ((minShares != null && shares < minShares) || (maxShares != null && shares > maxShares)) {
            return false;
        }
        boolean packMatches = listing.isPackAvailable() && inPriceRange(listing.getPackPrice());
        boolean individualMatches = inPriceRange(listing.getPricePerShare());
        return switch (saleType) {
            case PACKS -> packMatches;
            case INDIVIDUAL -> individualMatches;
            case BOTH -> packMatches || individualMatches;
        };
    }

    private boolean inPriceRange(BigDecimal price) {
        return (minPrice == null || price.compareTo(minPrice) >= 0)
                && (maxPrice == null || price.compareTo(maxPrice) <= 0);
    }
}
