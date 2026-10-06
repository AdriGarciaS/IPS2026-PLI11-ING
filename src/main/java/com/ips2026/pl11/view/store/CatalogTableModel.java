package com.ips2026.pl11.view.store;

import com.ips2026.pl11.model.store.Cart;
import com.ips2026.pl11.model.store.Merchandise;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table model of the catalog: name, type, price and the units that
 * can still be added (available units minus the ones already in the cart).
 */
public class CatalogTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = { "Name", "Type", "Price", "Available" };
    private static final Class<?>[] COLUMN_CLASSES = { String.class, String.class, BigDecimal.class, Integer.class };

    public static final int COLUMN_PRICE = 2;

    private final transient Cart cart;
    private transient List<Merchandise> products = new ArrayList<>();

    public CatalogTableModel(Cart cart) {
        this.cart = cart;
    }

    public void setProducts(List<Merchandise> products) {
        this.products = new ArrayList<>(products);
        fireTableDataChanged();
    }

    public Merchandise getProductAt(int modelRow) {
        return products.get(modelRow);
    }

    /** Repaints the "Available" column after the cart changes, keeping the selection. */
    public void refreshAvailable() {
        if (!products.isEmpty()) {
            fireTableRowsUpdated(0, products.size() - 1);
        }
    }

    @Override
    public int getRowCount() {
        return products.size();
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
        Merchandise product = products.get(row);
        return switch (column) {
            case 0 -> product.getName();
            case 1 -> product.getType();
            case 2 -> product.getPrice();
            case 3 -> cart.getRemainingUnits(product);
            default -> throw new IllegalArgumentException("Unknown column " + column);
        };
    }
}
