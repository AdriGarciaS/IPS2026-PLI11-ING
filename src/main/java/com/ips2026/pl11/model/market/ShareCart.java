package com.ips2026.pl11.model.market;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Cart of the share market: packs and individual shares of one or more
 * listings. The pack of a listing and individual shares of that same listing
 * cannot be in the cart at the same time.
 */
public class ShareCart {

    private final List<CartItem> items = new ArrayList<>();

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /** Number of items (a pack counts as one). */
    public int getItemCount() {
        return items.size();
    }

    /** Number of shares of all the items. */
    public int getShareCount() {
        return items.stream().mapToInt(item -> item.getShareIds().size()).sum();
    }

    public BigDecimal getTotal() {
        return items.stream().map(CartItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Whether the pack of the listing is in the cart. */
    public boolean hasPack(ShareListing listing) {
        return items.stream().anyMatch(item -> item.getType() == CartItem.Type.PACK && sameListing(item, listing));
    }

    /** IDs of the shares of the listing that are in the cart individually. */
    public Set<Integer> getSingleShareIds(ShareListing listing) {
        Set<Integer> ids = new TreeSet<>();
        items.stream().filter(item -> item.getType() == CartItem.Type.SINGLE && sameListing(item, listing))
                .forEach(item -> ids.addAll(item.getShareIds()));
        return ids;
    }

    /**
     * Adds the pack of the listing.
     *
     * @throws IllegalStateException if the pack cannot be bought, is already in the cart, or there are
     *                               individual shares of the listing in the cart
     */
    public void addPack(ShareListing listing) {
        if (!listing.isPackAvailable()) {
            throw new IllegalStateException("The pack of this sale is not available.");
        }
        if (hasPack(listing)) {
            throw new IllegalStateException("The pack of this sale is already in the cart.");
        }
        if (!getSingleShareIds(listing).isEmpty()) {
            throw new IllegalStateException("There are individual shares of this sale in the cart: remove them to buy the pack.");
        }
        items.add(CartItem.pack(listing));
    }

    /**
     * Adds shares of the listing one by one; the ones already in the cart are skipped.
     *
     * @throws IllegalStateException    if the pack of the listing is in the cart
     * @throws IllegalArgumentException if a share is not available in the listing
     */
    public void addShares(ShareListing listing, Collection<Integer> shareIds) {
        if (hasPack(listing)) {
            throw new IllegalStateException("The pack of this sale is in the cart: remove it to choose shares.");
        }
        for (int shareId : shareIds) {
            if (!listing.getAvailableShareIds().contains(shareId)) {
                throw new IllegalArgumentException("Share " + shareId + " is not available in this sale.");
            }
        }
        Set<Integer> inCart = getSingleShareIds(listing);
        new TreeSet<>(shareIds).stream().filter(id -> !inCart.contains(id))
                .forEach(id -> items.add(CartItem.single(listing, id)));
    }

    public void remove(CartItem item) {
        items.remove(item);
    }

    public void clear() {
        items.clear();
    }

    private static boolean sameListing(CartItem item, ShareListing listing) {
        return item.getListing().getId() == listing.getId();
    }
}
