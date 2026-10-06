package com.ips2026.pl11.model.reservation;

/**
 * A facility of the club (table FACILITY).
 */
public final class Facility {

    private final int id;
    private final String name;

    public Facility(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** The name, so it can be shown directly in a combo box. */
    @Override
    public String toString() {
        return name;
    }
}
