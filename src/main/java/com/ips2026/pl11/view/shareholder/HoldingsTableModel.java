package com.ips2026.pl11.view.shareholder;

import com.ips2026.pl11.model.shareholder.ShareHolding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table model of the share holdings of a wallet (one row per
 * campaign), valued with the current market price. The last column gives
 * access to the IDs of the shares.
 */
public class HoldingsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = { "Campaign", "Quantity", "Purchase date", "Purchase price",
            "Current price", "Current value", "Variation", "Variation %", "Share IDs" };
    private static final Class<?>[] COLUMN_CLASSES = { String.class, Integer.class, LocalDate.class,
            BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, ShareHolding.class };

    public static final int COLUMN_PURCHASE_DATE = 2;
    public static final int COLUMN_PURCHASE_PRICE = 3;
    public static final int COLUMN_CURRENT_PRICE = 4;
    public static final int COLUMN_CURRENT_VALUE = 5;
    public static final int COLUMN_VARIATION = 6;
    public static final int COLUMN_VARIATION_PERCENT = 7;
    /** Last column: the holding itself, shown as a button that opens the list of its share IDs. */
    public static final int COLUMN_SHARE_IDS = 8;

    private transient List<ShareHolding> holdings = new ArrayList<>();
    private BigDecimal marketPrice = BigDecimal.ZERO;

    public void setHoldings(List<ShareHolding> holdings, BigDecimal marketPrice) {
        this.holdings = new ArrayList<>(holdings);
        this.marketPrice = marketPrice;
        fireTableDataChanged();
    }

    public ShareHolding getHoldingAt(int modelRow) {
        return holdings.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return holdings.size();
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
        ShareHolding holding = holdings.get(row);
        return switch (column) {
            case 0 -> holding.getCampaignName();
            case 1 -> holding.getQuantity();
            case 2 -> holding.getPurchaseDate();
            case 3 -> holding.getPurchasePrice();
            case 4 -> marketPrice;
            case 5 -> holding.getValue(marketPrice);
            case 6 -> holding.getVariation(marketPrice);
            case 7 -> holding.getVariationPercent(marketPrice);
            case 8 -> holding;
            default -> throw new IllegalArgumentException("Unknown column " + column);
        };
    }
}
