package com.ips2026.pl11.model.market;

import java.math.BigDecimal;
import java.util.List;

/**
 * One item of the share cart: the whole pack of a listing, or one share of
 * a listing bought individually.
 */
public class CartItem {

    /** How the shares of the item are bought. */
    public enum Type {
        PACK("Pack"),
        SINGLE("Single");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final ShareListing listing;
    private final Type type;
    private final List<Integer> shareIds;

    private CartItem(ShareListing listing, Type type, List<Integer> shareIds) {
        this.listing = listing;
        this.type = type;
        this.shareIds = List.copyOf(shareIds);
    }

    /** All the available shares of the listing at its pack price. */
    public static CartItem pack(ShareListing listing) {
        return new CartItem(listing, Type.PACK, listing.getAvailableShareIds());
    }

    /** One share of the listing at its price per share. */
    public static CartItem single(ShareListing listing, int shareId) {
        return new CartItem(listing, Type.SINGLE, List.of(shareId));
    }

    public ShareListing getListing() {
        return listing;
    }

    public Type getType() {
        return type;
    }

    public List<Integer> getShareIds() {
        return shareIds;
    }

    /** Price of the item: the pack price, or the price of one share. */
    public BigDecimal getPrice() {
        return type == Type.PACK ? listing.getPackPrice() : listing.getPricePerShare();
    }

    /** Text of the cart, for example "Laura Pérez Gil · 20 shares" or "Laura Pérez Gil · ID 1005". */
    public String getDescription() {
        return listing.getSellerName() + " · "
                + (type == Type.PACK ? shareIds.size() + " shares" : "ID " + shareIds.get(0));
    }
}
