package com.ips2026.pl11.model.market;

import static com.ips2026.pl11.model.market.MarketTestData.listing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests of the share market cart: packs, individual shares, total and the
 * rule that a pack and individual shares of the same listing cannot be
 * together.
 */
@DisplayName("ShareCart")
class ShareCartTest {

    private static final ShareListing LAURA = listing(1, "12.50", "230.00", 1001, 1020, 0);
    private static final ShareListing MARCOS = listing(2, "13.00", null, 1301, 1315, 0);
    private static final ShareListing IRENE = listing(3, "11.80", "110.00", 303, 310, 2);

    private final ShareCart cart = new ShareCart();

    @Test
    @DisplayName("a new cart is empty and its total is 0 €")
    void emptyCart() {
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItemCount());
        assertEquals(0, cart.getShareCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(cart.getTotal()));
    }

    @Test
    @DisplayName("a pack is one item with all the shares of the listing, at the pack price")
    void addPack() {
        cart.addPack(LAURA);

        assertEquals(1, cart.getItemCount());
        assertEquals(20, cart.getShareCount());
        assertEquals(new BigDecimal("230.00"), cart.getTotal());
        CartItem pack = cart.getItems().get(0);
        assertEquals(CartItem.Type.PACK, pack.getType());
        assertEquals("Laura Pérez Gil · 20 shares", pack.getDescription());
        assertTrue(cart.hasPack(LAURA));
    }

    @Test
    @DisplayName("each individual share is one item at the price per share")
    void addShares() {
        cart.addShares(MARCOS, List.of(1306, 1305));

        assertEquals(2, cart.getItemCount());
        assertEquals(new BigDecimal("26.00"), cart.getTotal());
        assertEquals("Laura Pérez Gil · ID 1305", cart.getItems().get(0).getDescription(), "sorted by ID");
        assertEquals(CartItem.Type.SINGLE, cart.getItems().get(0).getType());
        assertEquals(Set.of(1305, 1306), cart.getSingleShareIds(MARCOS));
    }

    @Test
    @DisplayName("the shares of several listings add up in the total")
    void severalListings() {
        cart.addPack(LAURA);
        cart.addShares(MARCOS, List.of(1301));
        cart.addShares(IRENE, List.of(303, 304));

        assertEquals(4, cart.getItemCount());
        assertEquals(23, cart.getShareCount());
        assertEquals(new BigDecimal("266.60"), cart.getTotal());
    }

    @Test
    @DisplayName("a share already in the cart is not added twice")
    void noDuplicatedShares() {
        cart.addShares(MARCOS, List.of(1301, 1302));
        cart.addShares(MARCOS, List.of(1302, 1303));

        assertEquals(3, cart.getItemCount());
        assertEquals(Set.of(1301, 1302, 1303), cart.getSingleShareIds(MARCOS));
    }

    @Test
    @DisplayName("the pack cannot be added twice")
    void packTwice() {
        cart.addPack(LAURA);
        assertThrows(IllegalStateException.class, () -> cart.addPack(LAURA));
        assertEquals(1, cart.getItemCount());
    }

    @Test
    @DisplayName("the pack cannot be added if there are individual shares of that listing in the cart")
    void packAfterSingles() {
        cart.addShares(LAURA, List.of(1001));
        assertThrows(IllegalStateException.class, () -> cart.addPack(LAURA));
        assertFalse(cart.hasPack(LAURA));
    }

    @Test
    @DisplayName("individual shares cannot be added if the pack of that listing is in the cart")
    void singlesAfterPack() {
        cart.addPack(LAURA);
        assertThrows(IllegalStateException.class, () -> cart.addShares(LAURA, List.of(1001)));
        assertEquals(1, cart.getItemCount());
    }

    @Test
    @DisplayName("a pack that no longer applies, or a listing without pack, cannot be added as a pack")
    void packNotAvailable() {
        assertThrows(IllegalStateException.class, () -> cart.addPack(IRENE));
        assertThrows(IllegalStateException.class, () -> cart.addPack(MARCOS));
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("a share that is not available in the listing cannot be added")
    void shareNotInListing() {
        assertThrows(IllegalArgumentException.class, () -> cart.addShares(IRENE, List.of(301)));
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("an item can be removed and the cart emptied")
    void removeAndClear() {
        cart.addPack(LAURA);
        cart.addShares(MARCOS, List.of(1301));

        cart.remove(cart.getItems().get(0));
        assertEquals(1, cart.getItemCount());
        assertEquals(new BigDecimal("13.00"), cart.getTotal());
        assertFalse(cart.hasPack(LAURA));

        cart.clear();
        assertTrue(cart.isEmpty());
    }
}
