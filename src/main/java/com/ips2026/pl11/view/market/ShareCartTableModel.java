package com.ips2026.pl11.view.market;

import com.ips2026.pl11.model.market.CartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table of the share cart: item, type, price and a last column
 * drawn as a button to remove the item.
 */
public class ShareCartTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = { "Item", "Type", "Price", "" };
    private static final Class<?>[] COLUMN_CLASSES = { String.class, String.class, BigDecimal.class, CartItem.class };

    public static final int COLUMN_PRICE = 2;
    public static final int COLUMN_REMOVE = 3;

    private transient List<CartItem> items = new ArrayList<>();

    public void setItems(List<CartItem> items) {
        this.items = new ArrayList<>(items);
        fireTableDataChanged();
    }

    public CartItem getItemAt(int modelRow) {
        return items.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return items.size();
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
        CartItem item = items.get(row);
        return switch (column) {
            case 0 -> item.getDescription();
            case 1 -> item.getType().getLabel();
            case 2 -> item.getPrice();
            case 3 -> item;
            default -> throw new IllegalArgumentException("Unknown column " + column);
        };
    }
}
