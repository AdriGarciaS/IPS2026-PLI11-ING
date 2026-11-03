package com.ips2026.pl11.controller.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.market.ListingDAO;
import com.ips2026.pl11.model.market.OwnedShare;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * Tests of the sell shares controller with a fake DAO (no database).
 */
@DisplayName("SellSharesController")
class SellSharesControllerTest {

    private static final Shareholder LAURA = new Shareholder(1, "Laura", "Pérez Gil", "11111111H");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 2, 10, 15);
    private static final String VALID_CARD = "4111 1111 1111 1111";
    private static final String FOUNDING = "Founding shares 2023";
    private static final String INCREASE = "Capital increase 2024";

    /** Fake wallet: shares 1-4 (share 4 already on sale) and 1001-1003; remembers the new listing. */
    private static class FakeListingDAO extends ListingDAO {
        int loads;
        int sellerId = -1;
        List<Integer> listedIds;
        BigDecimal pricePerShare;
        BigDecimal packPrice;
        LocalDateTime listedAt;

        @Override
        public List<OwnedShare> findOwnedShares(int shareholderId) {
            loads++;
            return List.of(new OwnedShare(1, FOUNDING, false), new OwnedShare(2, FOUNDING, false),
                    new OwnedShare(3, FOUNDING, false), new OwnedShare(4, FOUNDING, true),
                    new OwnedShare(1001, INCREASE, false), new OwnedShare(1002, INCREASE, false),
                    new OwnedShare(1003, INCREASE, false));
        }

        @Override
        public int insertListing(int sellerId, Collection<Integer> shareIds, BigDecimal pricePerShare,
                BigDecimal packPrice, LocalDateTime moment) {
            this.sellerId = sellerId;
            this.listedIds = List.copyOf(shareIds);
            this.pricePerShare = pricePerShare;
            this.packPrice = packPrice;
            this.listedAt = moment;
            return 9;
        }
    }

    private final FakeListingDAO dao = new FakeListingDAO();
    private final SellSharesController controller = new SellSharesController(dao, LAURA, () -> NOW);

    @BeforeEach
    void setUp() throws SQLException {
        controller.load();
    }

    // ------------------------------------------------------------------
    // Wallet and selection
    // ------------------------------------------------------------------

    @Test
    @DisplayName("the campaigns of the wallet, once each and in alphabetical order")
    void campaigns() {
        assertEquals(List.of(INCREASE, FOUNDING), controller.getCampaigns());
    }

    @Test
    @DisplayName("the shares of one campaign, or of all of them")
    void sharesByCampaign() {
        assertEquals(7, controller.getShares(null).size());
        assertEquals(List.of(1001, 1002, 1003), controller.getShares(INCREASE).stream().map(OwnedShare::getShareId).toList());
    }

    @Test
    @DisplayName("a range selects the shares of the campaign between both IDs, skipping the ones already on sale")
    void range() {
        assertEquals(List.of(2, 3), controller.getIdsInRange(FOUNDING, "2", "4"), "share 4 is already on sale");
        assertEquals(List.of(1, 2, 3, 1001), controller.getIdsInRange(null, "1", "1001"));
        assertEquals(List.of(), controller.getIdsInRange(INCREASE, "1", "3"), "those IDs are of another campaign");
    }

    @Test
    @DisplayName("a range needs two whole numbers, the first one not greater than the second")
    void wrongRange() {
        assertThrows(IllegalArgumentException.class, () -> controller.getIdsInRange(null, "", "10"));
        assertThrows(IllegalArgumentException.class, () -> controller.getIdsInRange(null, "a", "10"));
        assertThrows(IllegalArgumentException.class, () -> controller.getIdsInRange(null, "10", "2"));
    }

    @Test
    @DisplayName("the sum of individual prices is the shares x the price, or nothing while the price is not valid")
    void sumOfPrices() {
        assertEquals(new BigDecimal("38.70"), controller.getSumOfPrices(3, "12,90"));
        assertEquals(new BigDecimal("37.50"), controller.getSumOfPrices(3, "12.5"));
        assertNull(controller.getSumOfPrices(3, ""));
        assertNull(controller.getSumOfPrices(3, "abc"));
    }

    // ------------------------------------------------------------------
    // Problems
    // ------------------------------------------------------------------

    @Test
    @DisplayName("valid data has no problems, with or without pack")
    void noProblems() {
        assertTrue(controller.findProblems(Set.of(1, 2), VALID_CARD, "12,90", "").isEmpty());
        assertTrue(controller.findProblems(Set.of(1, 2), VALID_CARD, "12,90", "20").isEmpty(),
                "the pack price does not need to match the sum");
    }

    @Test
    @DisplayName("at least one share must be selected")
    void noShares() {
        assertEquals(List.of("Select at least one share."), controller.findProblems(Set.of(), VALID_CARD, "12,90", ""));
    }

    @Test
    @DisplayName("a share already in another sale cannot be put up for sale again")
    void shareAlreadyOnSale() {
        assertEquals(List.of("Share 4 is already on sale."), controller.findProblems(Set.of(1, 4), VALID_CARD, "12,90", ""));
    }

    @Test
    @DisplayName("a share that is not in the wallet cannot be sold")
    void shareNotInWallet() {
        assertEquals(List.of("Share 50 is not in your wallet."), controller.findProblems(Set.of(50), VALID_CARD, "12,90", ""));
    }

    @Test
    @DisplayName("the card must be valid")
    void wrongCard() {
        assertEquals(1, controller.findProblems(Set.of(1), "1234", "12,90", "").size());
        assertEquals(1, controller.findProblems(Set.of(1), "4111 1111 1111 1112", "12,90", "").size());
        assertEquals(1, controller.findProblems(Set.of(1), "", "12,90", "").size());
    }

    @Test
    @DisplayName("the price per share is required, a number greater than 0 with at most 2 decimals")
    void wrongPricePerShare() {
        for (String price : List.of("", "abc", "0", "-3", "12,999")) {
            List<String> problems = controller.findProblems(Set.of(1), VALID_CARD, price, "");
            assertEquals(1, problems.size(), price);
            assertTrue(problems.get(0).contains("price per share"), problems.get(0));
        }
    }

    @Test
    @DisplayName("the pack price is optional, but if given it must be valid")
    void wrongPackPrice() {
        for (String pack : List.of("abc", "0", "10,555")) {
            List<String> problems = controller.findProblems(Set.of(1, 2), VALID_CARD, "12,90", pack);
            assertEquals(1, problems.size(), pack);
            assertTrue(problems.get(0).contains("pack price"), problems.get(0));
        }
    }

    @Test
    @DisplayName("a pack price needs at least 2 shares")
    void packOfOneShare() {
        assertEquals(List.of("A pack price needs at least 2 shares."), controller.findProblems(Set.of(1), VALID_CARD, "12,90", "12"));
    }

    // ------------------------------------------------------------------
    // Put up for sale
    // ------------------------------------------------------------------

    @Test
    @DisplayName("the shares are put up for sale as one listing of the seller, and the wallet is read again")
    void putUpForSale() throws SQLException {
        int listingId = controller.putUpForSale(Set.of(1001, 2, 1), VALID_CARD, " 12,9 ", "120");

        assertEquals(9, listingId);
        assertEquals(1, dao.sellerId);
        assertEquals(List.of(1, 2, 1001), dao.listedIds, "sorted by ID");
        assertEquals(new BigDecimal("12.90"), dao.pricePerShare);
        assertEquals(new BigDecimal("120.00"), dao.packPrice);
        assertEquals(NOW, dao.listedAt);
        assertEquals(2, dao.loads);
    }

    @Test
    @DisplayName("without pack price the listing has no pack")
    void putUpForSaleWithoutPack() throws SQLException {
        controller.putUpForSale(Set.of(1), VALID_CARD, "13", "  ");
        assertNull(dao.packPrice);
    }

    @Test
    @DisplayName("with problems nothing is saved, and the message has every problem")
    void putUpForSaleWithProblems() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.putUpForSale(Set.of(4), "1234", "", "50"));
        assertEquals(4, exception.getMessage().lines().count(), exception.getMessage());
        assertNull(dao.listedIds, "nothing is saved");
    }
}
