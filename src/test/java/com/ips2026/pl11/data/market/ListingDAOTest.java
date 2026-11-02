package com.ips2026.pl11.data.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.ips2026.pl11.model.market.OwnedShare;
import com.ips2026.pl11.model.market.ShareListing;

/**
 * Tests of ListingDAO on a temporary SQLite database created with the real
 * schema.sql.
 *
 * <p>Laura (1) has shares 1-4 (campaign 1) and 6 (campaign 2); share 3 is
 * already in her open listing 1. Marcos (2) has shares 20-21; he sold share
 * 22 to Laura in his listing 2, so share 22 is now Laura's and not on sale.</p>
 */
@DisplayName("ListingDAO")
class ListingDAOTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 2, 10, 15, 30);
    private static final int LAURA = 1;
    private static final int MARCOS = 2;

    @TempDir
    Path folder;

    private final ListingDAO dao = new ListingDAO();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
        TestDatabase.execute("INSERT INTO SHAREHOLDER (id, first_name, last_name, national_id) VALUES "
                + "(1, 'Laura', 'Pérez Gil', '11111111H'), (2, 'Marcos', 'Ruiz Soto', '22222222J')");
        TestDatabase.execute("INSERT INTO SHARE_CAMPAIGN (id, name, nominal_price, total_shares, status) VALUES "
                + "(1, 'Founding shares 2023', 9.5, 1000, 'FINISHED'), (2, 'Capital increase 2024', 10.0, 1000, 'FINISHED')");
        TestDatabase.execute("INSERT INTO SHARE_HOLDING (id, shareholder_id, campaign_id, purchase_date, purchase_price) VALUES "
                + "(10, 1, 1, '2023-02-10 10:15:00', 9.5), (11, 1, 2, '2024-03-12 12:40:00', 10.0), "
                + "(12, 2, 2, '2024-04-02 09:05:00', 15.0), (13, 1, 2, '2026-10-31 19:20:00', 13.0)");
        TestDatabase.execute("INSERT INTO SHARE (id, holding_id) VALUES "
                + "(1, 10), (2, 10), (3, 10), (4, 10), (6, 11), (20, 12), (21, 12), (22, 13)");
        TestDatabase.execute("INSERT INTO SHARE_LISTING (id, seller_id, price_per_share, pack_price, listed_at) VALUES "
                + "(1, 1, 12.5, NULL, '2026-10-24 10:30:00'), (2, 2, 13.0, NULL, '2026-10-26 17:15:00')");
        TestDatabase.execute("INSERT INTO SHARE_LISTING_ITEM (listing_id, share_id) VALUES (1, 3), (2, 22)");
        TestDatabase.execute("INSERT INTO SHARE_TRANSFER (purchase_number, listing_id, share_id, seller_id, buyer_id, "
                + "from_holding_id, to_holding_id, sale_type, price, transferred_at) VALUES "
                + "(1, 2, 22, 2, 1, 12, 13, 'SINGLE', 13.0, '2026-10-31 19:20:00')");
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    private static int count(String table) throws SQLException {
        return TestDatabase.queryInt("SELECT COUNT(*) FROM " + table);
    }

    @Test
    @DisplayName("the shares of the wallet, by ID, with their campaign and whether they are already on sale")
    void ownedShares() throws SQLException {
        List<OwnedShare> shares = dao.findOwnedShares(LAURA);

        assertEquals(List.of(1, 2, 3, 4, 6, 22), shares.stream().map(OwnedShare::getShareId).toList());
        assertEquals("Founding shares 2023", shares.get(0).getCampaignName());
        assertEquals("Capital increase 2024", shares.get(4).getCampaignName());
        assertTrue(shares.get(2).isOnSale(), "share 3 is in her open listing");
        assertFalse(shares.get(5).isOnSale(), "share 22 was sold by Marcos: it is Laura's now and not on sale");
        assertTrue(shares.stream().filter(share -> share.getShareId() != 3).noneMatch(OwnedShare::isOnSale));
    }

    @Test
    @DisplayName("a new listing stores its seller, prices, moment and shares")
    void insertListing() throws SQLException {
        int listingId = dao.insertListing(LAURA, List.of(1, 2, 6), new BigDecimal("12.90"), new BigDecimal("30.00"), NOW);

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_LISTING WHERE id = " + listingId
                + " AND seller_id = 1 AND price_per_share = 12.9 AND pack_price = 30.0 AND listed_at = '2026-11-02 10:15:30'"));
        assertEquals(3, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_LISTING_ITEM WHERE listing_id = " + listingId));
        assertTrue(dao.findOwnedShares(LAURA).stream().filter(share -> List.of(1, 2, 6).contains(share.getShareId()))
                .allMatch(OwnedShare::isOnSale));
    }

    @Test
    @DisplayName("without pack price the listing has no pack (NULL)")
    void insertListingWithoutPack() throws SQLException {
        int listingId = dao.insertListing(LAURA, List.of(1), new BigDecimal("12.90"), null, NOW);

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_LISTING WHERE id = " + listingId + " AND pack_price IS NULL"));
    }

    @Test
    @DisplayName("a share already in an open listing cannot be put in another one: nothing is stored")
    void shareAlreadyOnSale() throws SQLException {
        int listings = count("SHARE_LISTING");
        int items = count("SHARE_LISTING_ITEM");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> dao.insertListing(LAURA, List.of(1, 3), new BigDecimal("12.90"), null, NOW));
        assertTrue(exception.getMessage().contains("Share 3"));
        assertEquals(listings, count("SHARE_LISTING"));
        assertEquals(items, count("SHARE_LISTING_ITEM"), "share 1 is not put up for sale either");
    }

    @Test
    @DisplayName("the same share cannot be put in two listings one after the other")
    void sameShareTwice() throws SQLException {
        dao.insertListing(LAURA, List.of(4), new BigDecimal("12.90"), null, NOW);

        assertThrows(IllegalStateException.class, () -> dao.insertListing(LAURA, List.of(4), new BigDecimal("11.00"), null, NOW));
        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_LISTING_ITEM WHERE share_id = 4"));
    }

    @Test
    @DisplayName("a share bought from another listing can be put up for sale by its new owner")
    void boughtShareCanBeSoldAgain() throws SQLException {
        dao.insertListing(LAURA, List.of(22), new BigDecimal("14.00"), null, NOW);

        assertEquals(2, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_LISTING_ITEM WHERE share_id = 22"));
    }

    @Test
    @DisplayName("a share of another shareholder cannot be put up for sale: nothing is stored")
    void shareOfAnotherShareholder() throws SQLException {
        int listings = count("SHARE_LISTING");

        assertThrows(IllegalStateException.class, () -> dao.insertListing(LAURA, List.of(1, 20), new BigDecimal("12.90"), null, NOW));
        assertEquals(listings, count("SHARE_LISTING"));
    }

    @Test
    @DisplayName("the new listing can be bought by the other shareholders in the share market")
    void newListingInTheMarket() throws SQLException {
        int listingId = dao.insertListing(LAURA, List.of(1, 2), new BigDecimal("12.90"), new BigDecimal("24.00"), NOW);

        ShareListing listing = new MarketDAO().findOpenListings(MARCOS).stream()
                .filter(l -> l.getId() == listingId).findFirst().orElseThrow();
        assertEquals(List.of(1, 2), listing.getAvailableShareIds());
        assertTrue(listing.isPackAvailable());
        assertTrue(new MarketDAO().findOpenListings(LAURA).stream().noneMatch(l -> l.getId() == listingId),
                "the seller does not see it");
    }
}
