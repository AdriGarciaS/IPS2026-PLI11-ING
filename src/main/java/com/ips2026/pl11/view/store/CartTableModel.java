package com.ips2026.pl11.view.store;

import com.ips2026.pl11.model.store.Cart;
import com.ips2026.pl11.model.store.CartLine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table model of the cart: name, unit price, units and subtotal of
 * each line.
 */
public class CartTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = { "Name", "Price", "Units", "Subtotal" };
    private static final Class<?>[] COLUMN_CLASSES = { String.class, BigDecimal.class, Integer.class, BigDecimal.class };

    public static final int COLUMN_PRICE = 1;
    public static final int COLUMN_SUBTOTAL = 3;

    private final transient Cart cart;
    private transient List<CartLine> lines = new ArrayList<>();

    public CartTableModel(Cart cart) {
        this.cart = cart;
    }

    /** Reloads every line from the cart (the selection is lost). */
    public void reload() {
        lines = cart.getLines();
        fireTableDataChanged();
    }

    /** Reloads the line in {@code modelRow} after its units change, keeping the selection. */
    public void reloadRow(int modelRow) {
        lines = cart.getLines();
        fireTableRowsUpdated(modelRow, modelRow);
    }

    public CartLine getLineAt(int modelRow) {
        return lines.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return lines.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return COLUMN_CLASSES[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        CartLine line = lines.get(row);
        return switch (column) {
            case 0 -> line.getMerchandise().getName();
            case 1 -> line.getMerchandise().getPrice();
            case 2 -> line.getUnits();
            case 3 -> line.getSubtotal();
            default -> throw new IllegalArgumentException("Unknown column " + column);
        };
    }
}
