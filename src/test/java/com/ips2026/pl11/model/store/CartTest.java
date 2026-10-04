package com.ips2026.pl11.model.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Cart")
class CartTest {

    private static final Merchandise SHIRT = new Merchandise(1, "Home Shirt", "Clothing", 5, new BigDecimal("59.99"));
    private static final Merchandise SCARF = new Merchandise(2, "Club Scarf", "Accessories", 10, new BigDecimal("14.95"));

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
    }

    /** Compares amounts by value, so 0 and 0.00 are equal. */
    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "expected " + expected + " but was " + actual);
    }

    @Test
    @DisplayName("an empty cart has no lines and a total of 0")
    void emptyCartHasNoLinesAndZeroTotal() {
        assertTrue(cart.isEmpty());
        assertTrue(cart.getLines().isEmpty());
        assertAmount("0", cart.getTotal());
    }

    @Test
    @DisplayName("adding a product creates a line with its units and subtotal")
    void addCreatesLineWithUnitsAndSubtotal() {
        cart.add(SHIRT, 2);

        List<CartLine> lines = cart.getLines();
        assertEquals(1, lines.size());
        assertEquals(SHIRT, lines.get(0).getMerchandise());
        assertEquals(2, lines.get(0).getUnits());
        assertAmount("119.98", lines.get(0).getSubtotal());
        assertAmount("119.98", cart.getTotal());
    }

    @Test
    @DisplayName("adding a product already in the cart increases its units instead of adding a new line")
    void addingSameProductIncreasesUnits() {
        cart.add(SHIRT, 1);
        cart.add(SHIRT, 2);

        assertEquals(1, cart.getLines().size());
        assertEquals(3, cart.getUnitsOf(SHIRT.getId()));
        assertAmount("179.97", cart.getTotal());
    }

    @Test
    @DisplayName("the total is the sum of the subtotals of every line")
    void totalIsSumOfSubtotals() {
        cart.add(SHIRT, 2);
        cart.add(SCARF, 3);

        // 2 x 59.99 + 3 x 14.95 = 119.98 + 44.85
        assertAmount("164.83", cart.getTotal());
    }

    @Test
    @DisplayName("lines keep the order in which the products were added")
    void linesKeepInsertionOrder() {
        cart.add(SCARF, 1);
        cart.add(SHIRT, 1);
        cart.add(SCARF, 1);

        List<CartLine> lines = cart.getLines();
        assertEquals(SCARF, lines.get(0).getMerchandise());
        assertEquals(SHIRT, lines.get(1).getMerchandise());
    }

    @Test
    @DisplayName("all the available units can be added (upper limit)")
    void canAddAllAvailableUnits() {
        cart.add(SHIRT, 5);

        assertEquals(5, cart.getUnitsOf(SHIRT.getId()));
        assertEquals(0, cart.getRemainingUnits(SHIRT));
    }

    @Test
    @DisplayName("adding more units than the available ones is rejected")
    void cannotAddMoreThanAvailable() {
        assertThrows(IllegalArgumentException.class, () -> cart.add(SHIRT, 6));
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("several adds can not exceed the available units either")
    void severalAddsCannotExceedAvailable() {
        cart.add(SHIRT, 3);

        assertThrows(IllegalArgumentException.class, () -> cart.add(SHIRT, 3));
        assertEquals(3, cart.getUnitsOf(SHIRT.getId()), "the cart must not change after a rejected add");
    }

    @Test
    @DisplayName("adding zero or negative units is rejected")
    void cannotAddZeroOrNegativeUnits() {
        assertThrows(IllegalArgumentException.class, () -> cart.add(SHIRT, 0));
        assertThrows(IllegalArgumentException.class, () -> cart.add(SHIRT, -1));
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("the units of a line can be changed between 1 and the available units")
    void setUnitsAcceptsLimits() {
        cart.add(SHIRT, 2);

        cart.setUnits(SHIRT.getId(), 1);
        assertEquals(1, cart.getUnitsOf(SHIRT.getId()));
        assertAmount("59.99", cart.getTotal());

        cart.setUnits(SHIRT.getId(), 5);
        assertEquals(5, cart.getUnitsOf(SHIRT.getId()));
        assertAmount("299.95", cart.getTotal());
    }

    @Test
    @DisplayName("changing the units to 0 or to more than the available ones is rejected")
    void setUnitsRejectsOutOfRange() {
        cart.add(SHIRT, 2);

        assertThrows(IllegalArgumentException.class, () -> cart.setUnits(SHIRT.getId(), 0));
        assertThrows(IllegalArgumentException.class, () -> cart.setUnits(SHIRT.getId(), 6));
        assertEquals(2, cart.getUnitsOf(SHIRT.getId()));
    }

    @Test
    @DisplayName("changing the units of a product that is not in the cart is rejected")
    void setUnitsOfMissingProductIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> cart.setUnits(SHIRT.getId(), 1));
    }

    @Test
    @DisplayName("removing a product deletes its whole line and updates the total")
    void removeDeletesWholeLine() {
        cart.add(SHIRT, 3);
        cart.add(SCARF, 1);

        cart.remove(SHIRT.getId());

        assertEquals(1, cart.getLines().size());
        assertEquals(0, cart.getUnitsOf(SHIRT.getId()));
        assertAmount("14.95", cart.getTotal());
    }

    @Test
    @DisplayName("removing a product that is not in the cart does nothing")
    void removeMissingProductDoesNothing() {
        cart.add(SCARF, 1);

        cart.remove(SHIRT.getId());

        assertEquals(1, cart.getLines().size());
    }

    @Test
    @DisplayName("the remaining units are the available units minus the ones in the cart")
    void remainingUnitsSubtractCartUnits() {
        assertEquals(5, cart.getRemainingUnits(SHIRT));

        cart.add(SHIRT, 2);

        assertEquals(3, cart.getRemainingUnits(SHIRT));
        assertEquals(10, cart.getRemainingUnits(SCARF));
    }

    @Test
    @DisplayName("clear empties the cart")
    void clearEmptiesCart() {
        cart.add(SHIRT, 1);
        cart.add(SCARF, 1);

        cart.clear();

        assertTrue(cart.isEmpty());
        assertAmount("0", cart.getTotal());
    }

    @Test
    @DisplayName("the list of lines is a copy: changing it does not change the cart")
    void getLinesReturnsCopy() {
        cart.add(SHIRT, 1);

        cart.getLines().clear();

        assertEquals(1, cart.getLines().size());
    }
}
