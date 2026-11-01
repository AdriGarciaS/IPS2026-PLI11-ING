package com.ips2026.pl11.controller.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.market.MarketDAO;
import com.ips2026.pl11.model.market.CartItem;
import com.ips2026.pl11.model.market.MarketFilter;
import com.ips2026.pl11.model.market.MarketPurchase;
import com.ips2026.pl11.model.market.SaleType;
import com.ips2026.pl11.model.market.ShareListing;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * Tests of the share market controller with a fake DAO (no database).
 */
@DisplayName("ShareMarketController")
class ShareMarketControllerTest {

    private static final Shareholder JAVIER = new Shareholder(4, "Javier", "Ortega Lima", "44444444A");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 1, 18, 30);
    private static final String VALID_CARD = "4111 1111 1111 1111";

    private static final ShareListing LAURA = listing(1, "Laura Pérez Gil", "12.50", "230.00", 1001, 1020);
    private static final ShareListing MARCOS = listing(2, "Marcos Ruiz Soto", "13.00", null, 1301, 1315);
    private static final ShareListing IRENE = listing(5, "Irene Castillo Mora", "15.20", null, 2251, 2253);

    private static ShareListing listing(int id, String seller, String price, String pack, int from, int to) {
        List<Integer> ids = new ArrayList<>();
        for (int shareId = from; shareId <= to; shareId++) {
            ids.add(shareId);
        }
        return new ShareListing(id, id, seller, new BigDecimal(price), pack == null ? null : new BigDecimal(pack),
                LocalDateTime.of(2026, 10, 24, 10, 30), ids, 0);
    }

    /** Fake market: three listings; remembers the purchase and can fail as if a share had been sold. */
    private static class FakeMarketDAO extends MarketDAO {
        int requestedShareholder = -1;
        int loads;
        int buyerId = -1;
        List<CartItem> boughtItems;
        LocalDateTime boughtAt;
        boolean shareAlreadySold;

        @Override
        public List<ShareListing> findOpenListings(int shareholderId) {
            requestedShareholder = shareholderId;
            loads++;
            return List.of(IRENE, MARCOS, LAURA);
        }

        @Override
        public MarketPurchase buy(int buyerId, List<CartItem> items, LocalDateTime moment) {
            if (shareAlreadySold) {
                throw new IllegalStateException("Share 1301 of Marcos Ruiz Soto is no longer available.");
            }
            this.buyerId = buyerId;
            this.boughtItems = items;
            this.boughtAt = moment;
            return new MarketPurchase(7, List.of(1301), new BigDecimal("13.00"));
        }
    }

    private final FakeMarketDAO dao = new FakeMarketDAO();
    private final ShareMarketController controller = new ShareMarketController(dao, JAVIER, () -> NOW);

    @BeforeEach
    void setUp() throws SQLException {
        controller.load();
    }

    @Test
    @DisplayName("loads the listings of the other shareholders for the buyer")
    void loadsListingsOfTheBuyer() {
        assertEquals(4, dao.requestedShareholder);
        assertEquals(List.of(IRENE, MARCOS, LAURA), controller.getListings(MarketFilter.NONE));
    }

    @Test
    @DisplayName("the filter is created from the texts of the window (comma or dot, empty = no limit)")
    void filterFromTexts() {
        MarketFilter packs = controller.createFilter("200", "250,00", "", " ", SaleType.PACKS);
        assertEquals(List.of(LAURA), controller.getListings(packs));

        MarketFilter cheap = controller.createFilter("", "13.00", "", "", SaleType.INDIVIDUAL);
        assertEquals(List.of(MARCOS, LAURA), controller.getListings(cheap));

        MarketFilter fewShares = controller.createFilter("", "", "", "5", SaleType.BOTH);
        assertEquals(List.of(IRENE), controller.getListings(fewShares));
    }

    @Test
    @DisplayName("texts that are not numbers are rejected in the filter")
    void wrongFilterTexts() {
        assertThrows(IllegalArgumentException.class, () -> controller.createFilter("abc", "", "", "", SaleType.BOTH));
        assertThrows(IllegalArgumentException.class, () -> controller.createFilter("", "-5", "", "", SaleType.BOTH));
        assertThrows(IllegalArgumentException.class, () -> controller.createFilter("", "", "1.5", "", SaleType.BOTH));
        assertThrows(IllegalArgumentException.class, () -> controller.createFilter("20", "10", "", "", SaleType.BOTH));
    }

    @Test
    @DisplayName("a valid checkout buys the cart for the buyer, empties it and reloads the listings")
    void checkout() throws SQLException {
        controller.addPack(LAURA);
        controller.addShares(MARCOS, List.of(1301));

        MarketPurchase purchase = controller.checkout(" 44444444a ", VALID_CARD);

        assertEquals(7, purchase.getPurchaseNumber());
        assertEquals(4, dao.buyerId);
        assertEquals(NOW, dao.boughtAt);
        assertEquals(2, dao.boughtItems.size());
        assertTrue(controller.getCart().isEmpty());
        assertEquals(2, dao.loads, "the listings are read again after the purchase");
    }

    @Test
    @DisplayName("an empty cart cannot be bought")
    void emptyCart() {
        assertThrows(IllegalArgumentException.class, () -> controller.checkout("44444444A", VALID_CARD));
        assertNull(dao.boughtItems);
    }

    @Test
    @DisplayName("the ID must be the one of the shareholder of the portal, and the card valid: every problem at once")
    void wrongIdAndCard() {
        controller.addShares(MARCOS, List.of(1301));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.checkout("11111111H", "1234"));
        assertEquals(2, exception.getMessage().lines().count(), exception.getMessage());
        assertTrue(exception.getMessage().contains("Javier Ortega Lima"));
        assertNull(dao.boughtItems, "nothing is bought");
        assertEquals(1, controller.getCart().getItemCount(), "the cart is kept to correct the data");
    }

    @Test
    @DisplayName("the ID is required and the card must pass the check digit")
    void idRequiredAndCardChecked() {
        controller.addShares(MARCOS, List.of(1301));

        assertThrows(IllegalArgumentException.class, () -> controller.checkout("  ", VALID_CARD));
        assertThrows(IllegalArgumentException.class, () -> controller.checkout("44444444A", "4111 1111 1111 1112"));
        assertNull(dao.boughtItems);
    }

    @Test
    @DisplayName("if a share was bought by someone else, nothing is bought: the cart is emptied and the listings reloaded")
    void shareNoLongerAvailable() {
        controller.addShares(MARCOS, List.of(1301));
        dao.shareAlreadySold = true;

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> controller.checkout("44444444A", VALID_CARD));
        assertTrue(exception.getMessage().contains("no longer available"));
        assertTrue(exception.getMessage().contains("Nothing has been bought"));
        assertTrue(controller.getCart().isEmpty());
        assertEquals(2, dao.loads);
    }

    @Test
    @DisplayName("items can be removed from the cart and the cart emptied")
    void cartOperations() {
        controller.addPack(LAURA);
        controller.addShares(MARCOS, List.of(1301, 1302));

        controller.removeFromCart(controller.getCart().getItems().get(0));
        assertEquals(new BigDecimal("26.00"), controller.getCart().getTotal());

        controller.emptyCart();
        assertTrue(controller.getCart().isEmpty());
    }
}
