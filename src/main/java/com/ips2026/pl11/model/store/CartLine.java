package com.ips2026.pl11.model.store;

import java.math.BigDecimal;

/**
 * A line of the cart: a product and how many units of it are being bought.
 */
public final class CartLine {

    private final Merchandise merchandise;
    private final int units;

    public CartLine(Merchandise merchandise, int units) {
        this.merchandise = merchandise;
        this.units = units;
    }

    public Merchandise getMerchandise() {
        return merchandise;
    }

    public int getUnits() {
        return units;
    }

    /** Price of the line: units x unit price. */
    public BigDecimal getSubtotal() {
        return merchandise.getPrice().multiply(BigDecimal.valueOf(units));
    }
}
