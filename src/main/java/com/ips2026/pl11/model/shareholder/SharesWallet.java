package com.ips2026.pl11.model.shareholder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Shares wallet of a shareholder: the holdings and the current market price,
 * with the total portfolio value and its variation since the purchases.
 */
public final class SharesWallet {

    private final Shareholder shareholder;
    private final List<ShareHolding> holdings;
    private final MarketPrice currentPrice;
    private final MarketPrice previousPrice;

    /**
     * @param currentPrice  latest market price
     * @param previousPrice market price of the day before the latest one, or
     *                      null if there is none
     */
    public SharesWallet(Shareholder shareholder, List<ShareHolding> holdings, MarketPrice currentPrice,
            MarketPrice previousPrice) {
        this.shareholder = shareholder;
        this.holdings = List.copyOf(holdings);
        this.currentPrice = currentPrice;
        this.previousPrice = previousPrice;
    }

    public Shareholder getShareholder() {
        return shareholder;
    }

    public List<ShareHolding> getHoldings() {
        return holdings;
    }

    public MarketPrice getCurrentPrice() {
        return currentPrice;
    }

    /** Market price of the previous day, or null if there is none. */
    public MarketPrice getPreviousPrice() {
        return previousPrice;
    }

    public boolean isEmpty() {
        return holdings.isEmpty();
    }

    public int getTotalShares() {
        return holdings.stream().mapToInt(ShareHolding::getQuantity).sum();
    }

    /** Number of different campaigns in which the shares were bought. */
    public int getCampaignCount() {
        return (int) holdings.stream().map(ShareHolding::getCampaignName).distinct().count();
    }

    /** Total portfolio value: sum of the current market value of every holding. */
    public BigDecimal getTotalValue() {
        BigDecimal total = BigDecimal.ZERO;
        for (ShareHolding holding : holdings) {
            total = total.add(holding.getValue(currentPrice.getPrice()));
        }
        return total;
    }

    /** What all the shares cost when they were bought. */
    public BigDecimal getTotalCost() {
        BigDecimal total = BigDecimal.ZERO;
        for (ShareHolding holding : holdings) {
            total = total.add(holding.getCost());
        }
        return total;
    }

    /** Money won (positive) or lost (negative) with all the shares. */
    public BigDecimal getTotalVariation() {
        return getTotalValue().subtract(getTotalCost());
    }

    /** Total variation as a percentage of the total cost. */
    public BigDecimal getTotalVariationPercent() {
        return ShareHolding.percent(getTotalVariation(), getTotalCost());
    }

    /** True if the portfolio is worth the same or more than it cost (shown in green). */
    public boolean isUp() {
        return getTotalVariation().signum() >= 0;
    }

    /** Change of the market price from the previous day, as a percentage (null if there is no previous price). */
    public BigDecimal getDailyChangePercent() {
        if (previousPrice == null) {
            return null;
        }
        return ShareHolding.percent(currentPrice.getPrice().subtract(previousPrice.getPrice()), previousPrice.getPrice());
    }
}
