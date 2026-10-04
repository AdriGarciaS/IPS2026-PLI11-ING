package com.ips2026.pl11.model.store;

import java.math.BigDecimal;

/**
 * A merchandising product of the club store (table MERCHANDISING).
 */
public final class Merchandise {

    private final int id;
    private final String name;
    private final String type;
    private final int availableUnits;
    private final BigDecimal price;

    public Merchandise(int id, String name, String type, int availableUnits, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.availableUnits = availableUnits;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getAvailableUnits() {
        return availableUnits;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
