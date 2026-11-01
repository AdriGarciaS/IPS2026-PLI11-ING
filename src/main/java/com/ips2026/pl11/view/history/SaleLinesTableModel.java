package com.ips2026.pl11.view.history;

import com.ips2026.pl11.model.history.SaleLine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table of the products of a merchandising purchase: product,
 * units, unit price and total.
 */
public class SaleLinesTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = { "Product", "Units", "Unit price", "Total" };
    private static final Class<?>[] COLUMN_CLASSES = { String.class, Integer.class, BigDecimal.class, BigDecimal.class };

    public static final int COLUMN_UNIT_PRICE = 2;
    public static final int COLUMN_TOTAL = 3;

    private transient List<SaleLine> lines = new ArrayList<>();

    public SaleLinesTableModel(List<SaleLine> lines) {
        this.lines = new ArrayList<>(lines);
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
        SaleLine line = lines.get(row);
        return switch (column) {
            case 0 -> line.getProductName();
            case 1 -> line.getUnits();
            case 2 -> line.getUnitPrice();
            case 3 -> line.getTotal();
            default -> throw new IllegalArgumentException("Unknown column " + column);
        };
    }
}
