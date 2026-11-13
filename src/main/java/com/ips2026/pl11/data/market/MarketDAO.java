package com.ips2026.pl11.data.market;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.data.shareholder.ShareholderDAO;
import com.ips2026.pl11.model.market.CartItem;
import com.ips2026.pl11.model.market.MarketPurchase;
import com.ips2026.pl11.model.market.ShareListing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SQL operations of the share market: the listings that can be bought and
 * the purchase of shares, which changes their owner (tables SHARE_LISTING,
 * SHARE_LISTING_ITEM, SHARE_TRANSFER, SHARE_HOLDING and SHARE).
 */
public class MarketDAO {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // A share of a listing is available while it is not sold in that listing and still belongs to the seller.
    private static final String AVAILABLE = "NOT EXISTS (SELECT 1 FROM SHARE_TRANSFER t "
            + "WHERE t.listing_id = i.listing_id AND t.share_id = i.share_id) AND h.shareholder_id = l.seller_id";

    private static final String SQL_LISTINGS = "SELECT l.id, l.seller_id, s.first_name || ' ' || s.last_name AS seller, "
            + "l.price_per_share, l.pack_price, l.listed_at, "
            + "(SELECT COUNT(*) FROM SHARE_TRANSFER t WHERE t.listing_id = l.id) AS sold "
            + "FROM SHARE_LISTING l JOIN SHAREHOLDER s ON s.id = l.seller_id "
            + "WHERE l.seller_id <> ? ORDER BY l.listed_at DESC, l.id DESC";

    private static final String SQL_AVAILABLE_SHARES = "SELECT i.listing_id, i.share_id FROM SHARE_LISTING_ITEM i "
            + "JOIN SHARE_LISTING l ON l.id = i.listing_id JOIN SHARE x ON x.id = i.share_id "
            + "JOIN SHARE_HOLDING h ON h.id = x.holding_id WHERE l.seller_id <> ? AND " + AVAILABLE
            + " ORDER BY i.listing_id, i.share_id";

    // For the purchase: the holding and campaign of a share that is still available in the listing.
    private static final String SQL_AVAILABLE_SHARE = "SELECT x.holding_id, h.campaign_id, l.seller_id "
            + "FROM SHARE_LISTING_ITEM i JOIN SHARE_LISTING l ON l.id = i.listing_id JOIN SHARE x ON x.id = i.share_id "
            + "JOIN SHARE_HOLDING h ON h.id = x.holding_id WHERE i.listing_id = ? AND i.share_id = ? AND " + AVAILABLE;

    private static final String SQL_SOLD_IN_LISTING = "SELECT COUNT(*) FROM SHARE_TRANSFER WHERE listing_id = ?";

    private static final String SQL_NEXT_PURCHASE = "SELECT COALESCE(MAX(purchase_number), 0) + 1 FROM SHARE_TRANSFER";

    private static final String SQL_INSERT_HOLDING = "INSERT INTO SHARE_HOLDING "
            + "(shareholder_id, campaign_id, purchase_date, purchase_price) VALUES (?, ?, ?, ?)";

    private static final String SQL_INSERT_TRANSFER = "INSERT INTO SHARE_TRANSFER (purchase_number, listing_id, share_id, "
            + "seller_id, buyer_id, from_holding_id, to_holding_id, sale_type, price, transferred_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_MOVE_SHARE = "UPDATE SHARE SET holding_id = ? WHERE id = ?";

    /**
     * The listings of the other shareholders that still have shares to buy,
     * from the newest to the oldest.
     *
     * @param shareholderId the shareholder that is buying (their own listings are not returned)
     */
    public List<ShareListing> findOpenListings(int shareholderId) throws SQLException {
        Map<Integer, List<Integer>> availableIds = new HashMap<>();
        List<ShareListing> listings = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion()) {
            try (PreparedStatement statement = connection.prepareStatement(SQL_AVAILABLE_SHARES)) {
                statement.setInt(1, shareholderId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        availableIds.computeIfAbsent(result.getInt("listing_id"), id -> new ArrayList<>())
                                .add(result.getInt("share_id"));
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(SQL_LISTINGS)) {
                statement.setInt(1, shareholderId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        List<Integer> ids = availableIds.get(result.getInt("id"));
                        if (ids == null) {
                            continue; // everything sold
                        }
                        double packPrice = result.getDouble("pack_price");
                        BigDecimal pack = result.wasNull() ? null : money(packPrice);
                        listings.add(new ShareListing(result.getInt("id"), result.getInt("seller_id"),
                                result.getString("seller"), money(result.getDouble("price_per_share")), pack,
                                LocalDateTime.parse(result.getString("listed_at").replace(' ', 'T')),
                                ids, result.getInt("sold")));
                    }
                }
            }
        }
        return listings;
    }

    /**
     * Buys the items of the cart: every share changes its owner to the buyer.
     * The buyer gets a new holding for each campaign and price of the shares
     * bought, and each change of owner is stored in SHARE_TRANSFER. A seller
     * that is left without shares is removed from the system (US 36748).
     *
     * <p>Everything runs in a single transaction: if a share is no longer
     * available (or a pack is no longer complete), nothing is stored.</p>
     *
     * @throws IllegalStateException if a share or a pack is no longer available
     */
    public MarketPurchase buy(int buyerId, List<CartItem> items, LocalDateTime moment) throws SQLException {
        String when = moment.format(DATE_TIME_FORMAT);

        try (Connection connection = ConexionBD.obtenerConexion()) {
            connection.setAutoCommit(false);
            try (PreparedStatement findShare = connection.prepareStatement(SQL_AVAILABLE_SHARE);
                 PreparedStatement soldInListing = connection.prepareStatement(SQL_SOLD_IN_LISTING);
                 PreparedStatement insertHolding = connection.prepareStatement(SQL_INSERT_HOLDING, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement insertTransfer = connection.prepareStatement(SQL_INSERT_TRANSFER);
                 PreparedStatement moveShare = connection.prepareStatement(SQL_MOVE_SHARE);
                 Statement nextNumber = connection.createStatement()) {

                int purchaseNumber;
                try (ResultSet result = nextNumber.executeQuery(SQL_NEXT_PURCHASE)) {
                    result.next();
                    purchaseNumber = result.getInt(1);
                }

                Map<String, Integer> newHoldings = new LinkedHashMap<>(); // "campaign|price" -> holding of the buyer
                List<Integer> boughtIds = new ArrayList<>();
                Set<Integer> sellers = new LinkedHashSet<>();
                BigDecimal total = BigDecimal.ZERO;

                for (CartItem item : items) {
                    ShareListing listing = item.getListing();
                    boolean pack = item.getType() == CartItem.Type.PACK;
                    if (pack && countSold(soldInListing, listing.getId()) > 0) {
                        throw new IllegalStateException("The pack of " + listing.getSellerName()
                                + " is no longer available: some of its shares have been sold.");
                    }
                    BigDecimal pricePerShare = pack ? listing.getPackPricePerShare() : listing.getPricePerShare();

                    for (int shareId : item.getShareIds()) {
                        findShare.setInt(1, listing.getId());
                        findShare.setInt(2, shareId);
                        int fromHolding;
                        int campaignId;
                        int sellerId;
                        try (ResultSet result = findShare.executeQuery()) {
                            if (!result.next()) {
                                throw new IllegalStateException("Share " + shareId + " of " + listing.getSellerName()
                                        + " is no longer available.");
                            }
                            fromHolding = result.getInt("holding_id");
                            campaignId = result.getInt("campaign_id");
                            sellerId = result.getInt("seller_id");
                        }
                        if (sellerId == buyerId) {
                            throw new IllegalStateException("You cannot buy your own shares.");
                        }

                        String key = campaignId + "|" + pricePerShare.toPlainString();
                        Integer toHolding = newHoldings.get(key);
                        if (toHolding == null) {
                            insertHolding.setInt(1, buyerId);
                            insertHolding.setInt(2, campaignId);
                            insertHolding.setString(3, when);
                            insertHolding.setBigDecimal(4, pricePerShare);
                            insertHolding.executeUpdate();
                            try (ResultSet keys = insertHolding.getGeneratedKeys()) {
                                keys.next();
                                toHolding = keys.getInt(1);
                            }
                            newHoldings.put(key, toHolding);
                        }

                        insertTransfer.setInt(1, purchaseNumber);
                        insertTransfer.setInt(2, listing.getId());
                        insertTransfer.setInt(3, shareId);
                        insertTransfer.setInt(4, sellerId);
                        insertTransfer.setInt(5, buyerId);
                        insertTransfer.setInt(6, fromHolding);
                        insertTransfer.setInt(7, toHolding);
                        insertTransfer.setString(8, item.getType().name());
                        insertTransfer.setBigDecimal(9, pricePerShare);
                        insertTransfer.setString(10, when);
                        insertTransfer.executeUpdate();

                        moveShare.setInt(1, toHolding);
                        moveShare.setInt(2, shareId);
                        moveShare.executeUpdate();
                        boughtIds.add(shareId);
                        sellers.add(sellerId);
                    }
                    total = total.add(item.getPrice());
                }

                // US 36748: a seller that has just sold their last share is removed from the system
                for (int sellerId : sellers) {
                    ShareholderDAO.deleteIfWithoutShares(connection, sellerId);
                }

                connection.commit();
                return new MarketPurchase(purchaseNumber, boughtIds, total);
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static int countSold(PreparedStatement soldInListing, int listingId) throws SQLException {
        soldInListing.setInt(1, listingId);
        try (ResultSet result = soldInListing.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }

    private static BigDecimal money(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }
}
