package com.ips2026.pl11.model.history;

import java.math.BigDecimal;

/**
 * One product of a merchandising purchase, as it was sold: units, price of
 * one unit at the moment of the sale and total of the line.
 */
public class SaleLine {

    private final String productName;
    private final int units;
    private final BigDecimal unitPrice;
    private final BigDecimal total;

    public SaleLine(String productName, int units, BigDecimal unitPrice, BigDecimal total) {
        this.productName = productName;
        this.units = units;
        this.unitPrice = unitPrice;
        this.total = total;
    }

    public String getProductName() {
        return productName;
    }

    public int getUnits() {
        return units;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
