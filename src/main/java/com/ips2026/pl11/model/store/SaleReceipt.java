package com.ips2026.pl11.model.store;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Summary of a completed purchase, shown to the employee after buying.
 */
public final class SaleReceipt {

    private final int saleNumber;
    private final LocalDate date;
    private final List<CartLine> lines;
    private final BigDecimal finalPrice;

    public SaleReceipt(int saleNumber, LocalDate date, List<CartLine> lines, BigDecimal finalPrice) {
        this.saleNumber = saleNumber;
        this.date = date;
        this.lines = List.copyOf(lines);
        this.finalPrice = finalPrice;
    }

    public int getSaleNumber() {
        return saleNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<CartLine> getLines() {
        return lines;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }
}
