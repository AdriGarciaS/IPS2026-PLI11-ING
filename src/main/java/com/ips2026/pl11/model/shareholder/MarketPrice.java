package com.ips2026.pl11.model.shareholder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Market price of one share of the club on a day (table MARKET_PRICE).
 */
public final class MarketPrice {

    private final LocalDate date;
    private final BigDecimal price;

    public MarketPrice(LocalDate date, BigDecimal price) {
        this.date = date;
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
