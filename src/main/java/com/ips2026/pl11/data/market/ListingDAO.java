package com.ips2026.pl11.data.market;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.market.OwnedShare;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SQL operations to put shares up for sale in the share market (tables
 * SHARE_LISTING and SHARE_LISTING_ITEM).
 */
public class ListingDAO {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // A share is on sale while it is in a listing of its owner that has not sold it yet.
    private static final String ON_SALE = "EXISTS (SELECT 1 FROM SHARE_LISTING_ITEM i "
            + "JOIN SHARE_LISTING l ON l.id = i.listing_id WHERE i.share_id = s.id AND l.seller_id = h.shareholder_id "
            + "AND NOT EXISTS (SELECT 1 FROM SHARE_TRANSFER t WHERE t.listing_id = i.listing_id AND t.share_id = i.share_id))";

    private static final String SQL_OWNED_SHARES = "SELECT s.id, c.name AS campaign_name, " + ON_SALE + " AS on_sale "
            + "FROM SHARE s JOIN SHARE_HOLDING h ON h.id = s.holding_id JOIN SHARE_CAMPAIGN c ON c.id = h.campaign_id "
            + "WHERE h.shareholder_id = ? ORDER BY s.id";

    // For the new listing: the share must still belong to the seller and not be on sale.
    private static final String SQL_SELLABLE = "SELECT 1 FROM SHARE s JOIN SHARE_HOLDING h ON h.id = s.holding_id "
            + "WHERE s.id = ? AND h.shareholder_id = ? AND NOT " + ON_SALE;

    private static final String SQL_INSERT_LISTING = "INSERT INTO SHARE_LISTING "
            + "(seller_id, price_per_share, pack_price, listed_at) VALUES (?, ?, ?, ?)";

    private static final String SQL_INSERT_ITEM = "INSERT INTO SHARE_LISTING_ITEM (listing_id, share_id) VALUES (?, ?)";

    /** Every share of the shareholder, ordered by ID, saying which ones are already on sale. */
    public List<OwnedShare> findOwnedShares(int shareholderId) throws SQLException {
        List<OwnedShare> shares = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_OWNED_SHARES)) {

            statement.setInt(1, shareholderId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    shares.add(new OwnedShare(result.getInt("id"), result.getString("campaign_name"),
                            result.getInt("on_sale") == 1));
                }
            }
        }
        return shares;
    }

    /**
     * Puts the shares up for sale as one listing.
     *
     * <p>Everything runs in a single transaction: if a share no longer
     * belongs to the seller or is already on sale, nothing is stored.</p>
     *
     * @param packPrice price of all the shares together, or null if there is no pack
     * @return the id of the new listing
     * @throws IllegalStateException if a share cannot be sold
     */
    public int insertListing(int sellerId, Collection<Integer> shareIds, BigDecimal pricePerShare, BigDecimal packPrice,
            LocalDateTime moment) throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            connection.setAutoCommit(false);
            try (PreparedStatement sellable = connection.prepareStatement(SQL_SELLABLE);
                 PreparedStatement insertListing = connection.prepareStatement(SQL_INSERT_LISTING, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement insertItem = connection.prepareStatement(SQL_INSERT_ITEM)) {

                for (int shareId : shareIds) {
                    sellable.setInt(1, shareId);
                    sellable.setInt(2, sellerId);
                    try (ResultSet result = sellable.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalStateException("Share " + shareId
                                    + " cannot be sold: it is not in your wallet or it is already on sale.");
                        }
                    }
                }

                insertListing.setInt(1, sellerId);
                insertListing.setBigDecimal(2, pricePerShare);
                if (packPrice == null) {
                    insertListing.setNull(3, Types.REAL); // no pack
                } else {
                    insertListing.setBigDecimal(3, packPrice);
                }
                insertListing.setString(4, moment.format(DATE_TIME_FORMAT));
                insertListing.executeUpdate();
                int listingId;
                try (ResultSet keys = insertListing.getGeneratedKeys()) {
                    keys.next();
                    listingId = keys.getInt(1);
                }

                for (int shareId : shareIds) {
                    insertItem.setInt(1, listingId);
                    insertItem.setInt(2, shareId);
                    insertItem.executeUpdate();
                }

                connection.commit();
                return listingId;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}
