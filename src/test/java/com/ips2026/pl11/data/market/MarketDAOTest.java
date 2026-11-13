package com.ips2026.pl11.data.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.market.CartItem;
import com.ips2026.pl11.model.market.MarketPurchase;
import com.ips2026.pl11.model.market.ShareListing;

/**
 * Tests of MarketDAO on a temporary SQLite database created with the real
 * schema.sql.
 *
 * <p>Shareholders: 1 Laura, 2 Marcos, 3 Irene, 4 Javier. Listings:</p>
 * <ul>
 *   <li>1 (Laura): shares 1, 2, 3 (campaign 1) and 6 (campaign 2), 12,50 € or 40 € the pack.</li>
 *   <li>2 (Marcos): shares 20-24, 13 €, no pack.</li>
 *   <li>3 (Irene): shares 30-33, 11,80 € or 40 € the pack; share 30 was already sold to Marcos.</li>
 *   <li>4 (Laura): share 5, which is no longer hers (so it cannot be bought).</li>
 * </ul>
 */
@DisplayName("MarketDAO")
class MarketDAOTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 1, 18, 30, 15);
    private static final int LAURA = 1;
    private static final int MARCOS = 2;
    private static final int JAVIER = 4;

    @TempDir
    Path folder;

    private final MarketDAO dao = new MarketDAO();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
        TestDatabase.execute("INSERT INTO SHAREHOLDER (id, first_name, last_name, national_id) VALUES "
                + "(1, 'Laura', 'Pérez Gil', '11111111H'), (2, 'Marcos', 'Ruiz Soto', '22222222J'), "
                + "(3, 'Irene', 'Castillo Mora', '33333333P'), (4, 'Javier', 'Ortega Lima', '44444444A')");
        TestDatabase.execute("INSERT INTO SHARE_CAMPAIGN (id, name, nominal_price, total_shares, status) VALUES "
                + "(1, 'Founding shares 2023', 9.5, 1000, 'FINISHED'), (2, 'Capital increase 2024', 10.0, 1000, 'FINISHED')");
        TestDatabase.execute("INSERT INTO SHARE_HOLDING (id, shareholder_id, campaign_id, purchase_date, purchase_price) VALUES "
                + "(10, 1, 1, '2023-02-10 10:15:00', 9.5), (11, 1, 2, '2024-03-12 12:40:00', 10.0), "
                + "(12, 2, 2, '2024-04-02 09:05:00', 15.0), (13, 3, 1, '2023-03-01 17:30:00', 9.8), "
                + "(14, 2, 1, '2026-10-31 19:20:00', 11.8), (15, 4, 1, '2026-10-30 10:00:00', 9.5)");
        TestDatabase.execute("INSERT INTO SHARE (id, holding_id) VALUES "
                + "(1, 10), (2, 10), (3, 10), (4, 10), (5, 15), (6, 11), (7, 11), "
                + "(20, 12), (21, 12), (22, 12), (23, 12), (24, 12), (30, 14), (31, 13), (32, 13), (33, 13)");
        TestDatabase.execute("INSERT INTO SHARE_LISTING (id, seller_id, price_per_share, pack_price, listed_at) VALUES "
                + "(1, 1, 12.5, 40.0, '2026-10-24 10:30:00'), (2, 2, 13.0, NULL, '2026-10-26 17:15:00'), "
                + "(3, 3, 11.8, 40.0, '2026-10-27 09:00:00'), (4, 1, 9.0, NULL, '2026-10-20 08:00:00')");
        TestDatabase.execute("INSERT INTO SHARE_LISTING_ITEM (listing_id, share_id) VALUES "
                + "(1, 1), (1, 2), (1, 3), (1, 6), (2, 20), (2, 21), (2, 22), (2, 23), (2, 24), "
                + "(3, 30), (3, 31), (3, 32), (3, 33), (4, 5)");
        // Irene (3) has shares 31, 32 and 33 left: selling them removes her (US 36748)
        TestDatabase.execute("INSERT INTO SHARE_TRANSFER (purchase_number, listing_id, share_id, seller_id, buyer_id, "
                + "from_holding_id, to_holding_id, sale_type, price, transferred_at) VALUES "
                + "(1, 3, 30, 3, 2, 13, 14, 'SINGLE', 11.8, '2026-10-31 19:20:00')");
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    private ShareListing listing(int buyerId, int listingId) throws SQLException {
        return dao.findOpenListings(buyerId).stream().filter(l -> l.getId() == listingId).findFirst().orElseThrow();
    }

    /** The shareholder that owns the share now. */
    private static int owner(int shareId) throws SQLException {
        return TestDatabase.queryInt("SELECT h.shareholder_id FROM SHARE s JOIN SHARE_HOLDING h ON h.id = s.holding_id WHERE s.id = " + shareId);
    }

    private static int count(String table) throws SQLException {
        return TestDatabase.queryInt("SELECT COUNT(*) FROM " + table);
    }

    // ------------------------------------------------------------------
    // Listings
    // ------------------------------------------------------------------

    @Test
    @DisplayName("lists the sales of the others with shares to buy, from the newest to the oldest")
    void openListingsNewestFirst() throws SQLException {
        List<ShareListing> listings = dao.findOpenListings(JAVIER);

        assertEquals(List.of(3, 2, 1), listings.stream().map(ShareListing::getId).toList(),
                "listing 4 is not shown: its share is no longer of the seller");
        ShareListing laura = listings.get(2);
        assertEquals("Laura Pérez Gil", laura.getSellerName());
        assertEquals(new BigDecimal("12.50"), laura.getPricePerShare());
        assertEquals(new BigDecimal("40.00"), laura.getPackPrice());
        assertEquals(LocalDateTime.of(2026, 10, 24, 10, 30), laura.getListedAt());
        assertEquals(List.of(1, 2, 3, 6), laura.getAvailableShareIds());
        assertTrue(laura.isPackAvailable());
        assertNull(listings.get(1).getPackPrice(), "Marcos sells without pack");
    }

    @Test
    @DisplayName("the shares already sold are not available, and the pack no longer applies")
    void soldSharesNotAvailable() throws SQLException {
        ShareListing irene = listing(JAVIER, 3);

        assertEquals(List.of(31, 32, 33), irene.getAvailableShareIds());
        assertFalse(irene.isPackAvailable());
    }

    @Test
    @DisplayName("a shareholder does not see their own sales")
    void ownListingsNotShown() throws SQLException {
        assertEquals(List.of(3, 2), dao.findOpenListings(LAURA).stream().map(ShareListing::getId).toList());
    }

    // ------------------------------------------------------------------
    // Purchase
    // ------------------------------------------------------------------

    @Test
    @DisplayName("buying individual shares changes their owner and records each change")
    void buySingles() throws SQLException {
        ShareListing irene = listing(JAVIER, 3);
        ShareListing marcos = listing(JAVIER, 2);

        MarketPurchase purchase = dao.buy(JAVIER, List.of(CartItem.single(irene, 31), CartItem.single(irene, 32),
                CartItem.single(marcos, 20)), NOW);

        assertEquals(2, purchase.getPurchaseNumber(), "the next number after the existing transfer");
        assertEquals(List.of(31, 32, 20), purchase.getShareIds());
        assertEquals(new BigDecimal("36.60"), purchase.getTotal());
        assertEquals(JAVIER, owner(31));
        assertEquals(JAVIER, owner(32));
        assertEquals(JAVIER, owner(20));
        assertEquals(MARCOS, owner(21), "the rest stay with the seller");

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_TRANSFER WHERE purchase_number = 2 AND share_id = 31 "
                + "AND listing_id = 3 AND seller_id = 3 AND buyer_id = 4 AND from_holding_id = 13 AND sale_type = 'SINGLE' "
                + "AND price = 11.8 AND transferred_at = '2026-11-01 18:30:15'"));
        assertEquals(3, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_TRANSFER WHERE purchase_number = 2"));
    }

    @Test
    @DisplayName("the buyer gets one new holding per campaign and price, with the moment of the purchase")
    void newHoldingsOfTheBuyer() throws SQLException {
        dao.buy(JAVIER, List.of(CartItem.single(listing(JAVIER, 3), 31), CartItem.single(listing(JAVIER, 2), 20)), NOW);

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_HOLDING WHERE shareholder_id = 4 AND campaign_id = 1 "
                + "AND purchase_price = 11.8 AND purchase_date = '2026-11-01 18:30:15'"));
        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_HOLDING WHERE shareholder_id = 4 AND campaign_id = 2 "
                + "AND purchase_price = 13.0"));
        assertEquals(3, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_HOLDING WHERE shareholder_id = 4"),
                "the old holding and the two new ones");
    }

    @Test
    @DisplayName("buying a pack takes all its shares, each at the pack price divided by its shares")
    void buyPack() throws SQLException {
        MarketPurchase purchase = dao.buy(JAVIER, List.of(CartItem.pack(listing(JAVIER, 1))), NOW);

        assertEquals(List.of(1, 2, 3, 6), purchase.getShareIds());
        assertEquals(new BigDecimal("40.00"), purchase.getTotal());
        for (int shareId : List.of(1, 2, 3, 6)) {
            assertEquals(JAVIER, owner(shareId));
        }
        assertEquals(4, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_TRANSFER WHERE listing_id = 1 "
                + "AND sale_type = 'PACK' AND price = 10.0 AND buyer_id = 4"));
        assertEquals(2, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_HOLDING WHERE shareholder_id = 4 "
                + "AND purchase_price = 10.0"), "the shares of the pack are of two campaigns");
        assertTrue(dao.findOpenListings(JAVIER).stream().noneMatch(l -> l.getId() == 1), "nothing left to buy");
    }

    @Test
    @DisplayName("a pack cannot be bought once one of its shares has been sold: nothing is stored")
    void packAfterASale() throws SQLException {
        ShareListing lauraBefore = listing(JAVIER, 1);
        dao.buy(MARCOS, List.of(CartItem.single(lauraBefore, 1)), NOW);
        int transfers = count("SHARE_TRANSFER");
        int holdings = count("SHARE_HOLDING");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> dao.buy(JAVIER, List.of(CartItem.pack(lauraBefore)), NOW));
        assertTrue(exception.getMessage().contains("pack"));
        assertEquals(transfers, count("SHARE_TRANSFER"));
        assertEquals(holdings, count("SHARE_HOLDING"));
        assertEquals(LAURA, owner(2));
    }

    @Test
    @DisplayName("if a share of the cart was already sold, nothing of the cart is bought")
    void shareAlreadySold() throws SQLException {
        ShareListing marcos = listing(JAVIER, 2);
        dao.buy(LAURA, List.of(CartItem.single(marcos, 20)), NOW);
        int transfers = count("SHARE_TRANSFER");
        int holdings = count("SHARE_HOLDING");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> dao.buy(JAVIER, List.of(CartItem.single(marcos, 21), CartItem.single(marcos, 20)), NOW));
        assertTrue(exception.getMessage().contains("Share 20"));
        assertEquals(MARCOS, owner(21), "share 21 was before share 20 in the cart, but it is not bought either");
        assertEquals(transfers, count("SHARE_TRANSFER"));
        assertEquals(holdings, count("SHARE_HOLDING"));
    }

    @Test
    @DisplayName("a shareholder cannot buy their own shares")
    void ownShares() throws SQLException {
        ShareListing laura = listing(JAVIER, 1);

        assertThrows(IllegalStateException.class, () -> dao.buy(LAURA, List.of(CartItem.single(laura, 1)), NOW));
        assertEquals(LAURA, owner(1));
        assertEquals(1, count("SHARE_TRANSFER"));
    }

    @Test
    @DisplayName("a share whose seller no longer owns it cannot be bought")
    void shareNoLongerOfTheSeller() throws SQLException {
        ShareListing stale = new ShareListing(4, LAURA, "Laura Pérez Gil", new BigDecimal("9.00"), null,
                LocalDateTime.of(2026, 10, 20, 8, 0), List.of(5), 0);

        assertThrows(IllegalStateException.class, () -> dao.buy(MARCOS, List.of(CartItem.single(stale, 5)), NOW));
        assertEquals(JAVIER, owner(5));
    }

    // ------------------------------------------------------------------
    // US 36748: a shareholder that sells all their shares is removed
    // ------------------------------------------------------------------

    private static boolean exists(int shareholderId) throws SQLException {
        return TestDatabase.queryInt("SELECT COUNT(*) FROM SHAREHOLDER WHERE id = " + shareholderId) == 1;
    }

    @Test
    @DisplayName("US 36748: a seller that sells their last share is removed from the system")
    void sellerOfAllSharesRemoved() throws SQLException {
        ShareListing irene = listing(JAVIER, 3); // Irene only has shares 31, 32 and 33 left

        dao.buy(JAVIER, List.of(CartItem.single(irene, 31), CartItem.single(irene, 32), CartItem.single(irene, 33)), NOW);

        assertFalse(exists(3), "Irene has no shares now");
        assertEquals(3, count("SHAREHOLDER"));
        assertTrue(exists(JAVIER), "the buyer is not removed");
        assertTrue(exists(MARCOS), "a shareholder that bought from Irene before is not removed");
        assertTrue(dao.findOpenListings(JAVIER).stream().noneMatch(l -> l.getId() == 3));
    }

    @Test
    @DisplayName("US 36748: the history of a removed shareholder is kept, without them")
    void historyOfRemovedShareholderKept() throws SQLException {
        ShareListing irene = listing(JAVIER, 3);

        dao.buy(JAVIER, List.of(CartItem.single(irene, 31), CartItem.single(irene, 32), CartItem.single(irene, 33)), NOW);

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_HOLDING WHERE id = 13 AND shareholder_id IS NULL"),
                "the campaign purchase of Irene is still there (it is income of the club)");
        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_LISTING WHERE id = 3 AND seller_id IS NULL"));
        assertEquals(4, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_TRANSFER WHERE seller_id IS NULL"),
                "the old sale to Marcos and the three new ones");
        assertEquals(0, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_TRANSFER WHERE seller_id = 3 OR buyer_id = 3"));
        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_TRANSFER WHERE buyer_id = 2"),
                "Marcos is still the buyer of his old purchase");
    }

    @Test
    @DisplayName("US 36748: a seller that still has shares is not removed")
    void sellerWithSharesLeftNotRemoved() throws SQLException {
        ShareListing irene = listing(JAVIER, 3);
        ShareListing laura = listing(JAVIER, 1);

        dao.buy(JAVIER, List.of(CartItem.single(irene, 31), CartItem.single(irene, 32), CartItem.pack(laura)), NOW);

        assertTrue(exists(3), "Irene still has share 33");
        assertTrue(exists(LAURA), "Laura sold her listing but still has shares 4 and 7");
        assertEquals(4, count("SHAREHOLDER"));
    }

    @Test
    @DisplayName("US 36748: if the purchase fails, nobody is removed")
    void failedPurchaseRemovesNobody() throws SQLException {
        ShareListing irene = listing(JAVIER, 3);
        ShareListing stale = new ShareListing(4, LAURA, "Laura Pérez Gil", new BigDecimal("9.00"), null,
                LocalDateTime.of(2026, 10, 20, 8, 0), List.of(5), 0);

        assertThrows(IllegalStateException.class, () -> dao.buy(MARCOS, List.of(CartItem.single(irene, 31),
                CartItem.single(irene, 32), CartItem.single(irene, 33), CartItem.single(stale, 5)), NOW));

        assertTrue(exists(3));
        assertEquals(3, owner(31));
    }
}
