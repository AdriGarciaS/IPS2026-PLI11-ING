package com.ips2026.pl11.model.market;

/**
 * Sale type of the share market filter.
 */
public enum SaleType {

    /** Only the listings whose pack can still be bought. */
    PACKS,
    /** The listings with shares that can be bought one by one (all of them). */
    INDIVIDUAL,
    /** Packs and individual shares. */
    BOTH
}
