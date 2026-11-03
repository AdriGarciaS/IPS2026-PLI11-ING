package com.ips2026.pl11.view.market;

import com.ips2026.pl11.model.market.ShareListing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table of the current sales of the share market. The last two
 * columns hold the listing itself and are drawn as buttons ("Add pack" and
 * "Choose shares...").
 */
public class ListingsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = { "Seller", "Shares", "Price/share", "Pack price", "Listed", "Pack", "Individual" };
    private static final Class<?>[] COLUMN_CLASSES = { String.class, Integer.class, BigDecimal.class, Object.class,
            LocalDateTime.class, ShareListing.class, ShareListing.class };

    public static final int COLUMN_PRICE = 2;
    public static final int COLUMN_PACK_PRICE = 3;
    public static final int COLUMN_LISTED = 4;
    public static final int COLUMN_PACK = 5;
    public static final int COLUMN_INDIVIDUAL = 6;

    /** Pack price of a listing whose pack no longer applies (a share was already sold). */
    public static final String NOT_APPLICABLE = "n/a";
    /** Pack price of a listing without pack. */
    public static final String NO_PACK = "—";

    private transient List<ShareListing> listings = new ArrayList<>();

    public void setListings(List<ShareListing> listings) {
        this.listings = new ArrayList<>(listings);
        fireTableDataChanged();
    }

    public ShareListing getListingAt(int modelRow) {
        return listings.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return listings.size();
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
        ShareListing listing = listings.get(row);
        return switch (column) {
            case 0 -> listing.getSellerName();
            case 1 -> listing.getAvailableShares();
            case 2 -> listing.getPricePerShare();
            case 3 -> packPrice(listing);
            case 4 -> listing.getListedAt();
            case 5, 6 -> listing;
            default -> throw new IllegalArgumentException("Unknown column " + column);
        };
    }

    /** The pack price, "n/a" if it no longer applies, or "—" if the listing has no pack. */
    private static Object packPrice(ShareListing listing) {
        if (listing.getPackPrice() == null) {
            return NO_PACK;
        }
        return listing.isPackAvailable() ? listing.getPackPrice() : NOT_APPLICABLE;
    }
}
