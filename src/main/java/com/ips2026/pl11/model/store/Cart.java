package com.ips2026.pl11.model.store;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Software cart of the store.
 *
 * <p>Keeps one line per product (adding a product that is already in the
 * cart increases its units) and never allows more units of a product than
 * the available ones.</p>
 */
public class Cart {

    // Indexed by product id; LinkedHashMap keeps the order in which they were added.
    private final Map<Integer, CartLine> lines = new LinkedHashMap<>();

    /**
     * Adds units of a product to the cart.
     *
     * @throws IllegalArgumentException if units is not positive or the cart
     *         would contain more units than the available ones
     */
    public void add(Merchandise merchandise, int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("The number of units must be positive");
        }
        int newUnits = getUnitsOf(merchandise.getId()) + units;
        checkAvailable(merchandise, newUnits);
        lines.put(merchandise.getId(), new CartLine(merchandise, newUnits));
    }

    /**
     * Changes the units of a product that is already in the cart.
     *
     * @throws IllegalArgumentException if the product is not in the cart,
     *         units is not positive or it exceeds the available units
     */
    public void setUnits(int merchandiseId, int units) {
        CartLine line = lines.get(merchandiseId);
        if (line == null) {
            throw new IllegalArgumentException("The product is not in the cart");
        }
        if (units <= 0) {
            throw new IllegalArgumentException("The number of units must be positive");
        }
        checkAvailable(line.getMerchandise(), units);
        lines.put(merchandiseId, new CartLine(line.getMerchandise(), units));
    }

    /** Removes the whole line of a product. Does nothing if it is not in the cart. */
    public void remove(int merchandiseId) {
        lines.remove(merchandiseId);
    }

    public void clear() {
        lines.clear();
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /** Units of a product currently in the cart (0 if it is not in the cart). */
    public int getUnitsOf(int merchandiseId) {
        CartLine line = lines.get(merchandiseId);
        return line == null ? 0 : line.getUnits();
    }

    /** Units of a product that can still be added to the cart. */
    public int getRemainingUnits(Merchandise merchandise) {
        return merchandise.getAvailableUnits() - getUnitsOf(merchandise.getId());
    }

    /** Lines of the cart, in the order they were added (a copy). */
    public List<CartLine> getLines() {
        return new ArrayList<>(lines.values());
    }

    /** Total price of the cart: sum of the subtotals of every line. */
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartLine line : lines.values()) {
            total = total.add(line.getSubtotal());
        }
        return total;
    }

    private static void checkAvailable(Merchandise merchandise, int units) {
        if (units > merchandise.getAvailableUnits()) {
            throw new IllegalArgumentException("Only " + merchandise.getAvailableUnits()
                    + " units of '" + merchandise.getName() + "' are available");
        }
    }
}
