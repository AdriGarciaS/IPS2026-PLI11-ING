package com.ips2026.pl11.controller.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.store.MerchandiseDAO;
import com.ips2026.pl11.data.store.MerchandiseSaleDAO;
import com.ips2026.pl11.model.store.CartLine;
import com.ips2026.pl11.model.store.Merchandise;
import com.ips2026.pl11.model.store.SaleReceipt;

/**
 * Tests of the store sales controller without a database: the DAOs are
 * replaced by small fakes that return fixed data and remember what they
 * were asked to store.
 */
@DisplayName("StoreSalesController")
class StoreSalesControllerTest {

    private static final Merchandise HOME_SHIRT = new Merchandise(1, "Home Shirt 26/27", "Clothing", 25, new BigDecimal("59.99"));
    private static final Merchandise TRAINING_SHIRT = new Merchandise(2, "Training Shirt", "Clothing", 40, new BigDecimal("34.95"));
    private static final Merchandise SCARF = new Merchandise(3, "Club Scarf", "Accessories", 50, new BigDecimal("14.95"));
    private static final Merchandise BALL = new Merchandise(4, "Official Match Ball", "Equipment", 12, new BigDecimal("29.95"));

    /** Fake catalog: always returns the same products; counts how many times it is loaded. */
    private static class FakeMerchandiseDAO extends MerchandiseDAO {
        int loads;

        @Override
        public List<Merchandise> findAll() {
            loads++;
            return List.of(HOME_SHIRT, TRAINING_SHIRT, SCARF, BALL);
        }

        @Override
        public List<String> findTypes() {
            return List.of("Accessories", "Clothing", "Equipment");
        }
    }

    /** Fake sales table: remembers the registered lines, or fails if asked to. */
    private static class FakeSaleDAO extends MerchandiseSaleDAO {
        List<CartLine> registeredLines;
        LocalDate registeredDate;
        boolean fail;

        @Override
        public int registerSale(List<CartLine> lines, LocalDate date) throws SQLException {
            if (fail) {
                throw new SQLException("There are not enough available units of 'Home Shirt 26/27'");
            }
            registeredLines = new ArrayList<>(lines);
            registeredDate = date;
            return 7;
        }
    }

    private FakeMerchandiseDAO merchandiseDAO;
    private FakeSaleDAO saleDAO;
    private StoreSalesController controller;

    @BeforeEach
    void setUp() throws SQLException {
        merchandiseDAO = new FakeMerchandiseDAO();
        saleDAO = new FakeSaleDAO();
        controller = new StoreSalesController(merchandiseDAO, saleDAO);
        controller.loadCatalog();
    }

    private static List<String> names(List<Merchandise> products) {
        return products.stream().map(Merchandise::getName).toList();
    }

    // ------------------------------------------------------------------
    // Catalog: search by name and filter by type
    // ------------------------------------------------------------------

    @Test
    @DisplayName("without search text and with all types, the whole catalog is shown")
    void noFilterShowsWholeCatalog() {
        assertEquals(4, controller.filterCatalog("", null).size());
        assertEquals(4, controller.filterCatalog(null, null).size());
        assertEquals(4, controller.filterCatalog("   ", null).size());
    }

    @Test
    @DisplayName("the search by name is not case sensitive")
    void searchIgnoresCase() {
        assertEquals(List.of("Home Shirt 26/27", "Training Shirt"), names(controller.filterCatalog("SHIRT", null)));
        assertEquals(List.of("Home Shirt 26/27", "Training Shirt"), names(controller.filterCatalog("shirt", null)));
        assertEquals(List.of("Home Shirt 26/27", "Training Shirt"), names(controller.filterCatalog("sHiRt", null)));
    }

    @Test
    @DisplayName("the search finds a text in any part of the name")
    void searchMatchesAnyPartOfName() {
        assertEquals(List.of("Official Match Ball"), names(controller.filterCatalog("match", null)));
        assertEquals(List.of("Home Shirt 26/27"), names(controller.filterCatalog("26/27", null)));
    }

    @Test
    @DisplayName("spaces before and after the search text are ignored")
    void searchIgnoresSurroundingSpaces() {
        assertEquals(List.of("Club Scarf"), names(controller.filterCatalog("  scarf  ", null)));
    }

    @Test
    @DisplayName("a search with no matches returns an empty list")
    void searchWithoutMatchesIsEmpty() {
        assertTrue(controller.filterCatalog("mug", null).isEmpty());
    }

    @Test
    @DisplayName("the type filter only shows products of that type")
    void typeFilterShowsOnlyThatType() {
        assertEquals(List.of("Home Shirt 26/27", "Training Shirt"), names(controller.filterCatalog("", "Clothing")));
        assertEquals(List.of("Official Match Ball"), names(controller.filterCatalog("", "Equipment")));
    }

    @Test
    @DisplayName("the search and the type filter work together")
    void searchAndTypeFilterCombine() {
        assertEquals(List.of("Training Shirt"), names(controller.filterCatalog("training", "Clothing")));
        assertTrue(controller.filterCatalog("shirt", "Accessories").isEmpty());
    }

    @Test
    @DisplayName("the types of the type filter come from the catalog")
    void typesComeFromCatalog() {
        assertEquals(List.of("Accessories", "Clothing", "Equipment"), controller.getTypes());
    }

    // ------------------------------------------------------------------
    // Cart operations through the controller
    // ------------------------------------------------------------------

    @Test
    @DisplayName("adding, changing units and removing update the cart")
    void cartOperationsUpdateCart() {
        controller.addToCart(HOME_SHIRT, 2);
        controller.addToCart(SCARF, 1);
        controller.changeUnits(SCARF.getId(), 4);
        controller.removeFromCart(HOME_SHIRT.getId());

        assertEquals(1, controller.getCart().getLines().size());
        assertEquals(4, controller.getCart().getUnitsOf(SCARF.getId()));
    }

    @Test
    @DisplayName("more units than the available ones can not be added through the controller")
    void controllerDoesNotAllowMoreThanAvailable() {
        assertThrows(IllegalArgumentException.class, () -> controller.addToCart(BALL, 13));
    }

    // ------------------------------------------------------------------
    // Purchase
    // ------------------------------------------------------------------

    @Test
    @DisplayName("purchasing an empty cart is rejected and nothing is stored")
    void purchaseEmptyCartIsRejected() {
        assertThrows(IllegalStateException.class, () -> controller.purchase());
        assertNull(saleDAO.registeredLines);
    }

    @Test
    @DisplayName("the purchase summary has the sale number, today's date, the lines and the final price")
    void purchaseReturnsSummary() throws SQLException {
        controller.addToCart(HOME_SHIRT, 2);
        controller.addToCart(SCARF, 1);

        SaleReceipt receipt = controller.purchase();

        assertEquals(7, receipt.getSaleNumber());
        assertEquals(LocalDate.now(), receipt.getDate());
        assertEquals(2, receipt.getLines().size());
        // 2 x 59.99 + 1 x 14.95
        assertEquals(0, new BigDecimal("134.93").compareTo(receipt.getFinalPrice()));
    }

    @Test
    @DisplayName("the purchase stores every cart line with today's date")
    void purchaseStoresLines() throws SQLException {
        controller.addToCart(HOME_SHIRT, 2);
        controller.addToCart(SCARF, 1);

        controller.purchase();

        assertEquals(2, saleDAO.registeredLines.size());
        assertEquals(HOME_SHIRT, saleDAO.registeredLines.get(0).getMerchandise());
        assertEquals(2, saleDAO.registeredLines.get(0).getUnits());
        assertEquals(LocalDate.now(), saleDAO.registeredDate);
    }

    @Test
    @DisplayName("after a purchase the cart is empty and the catalog is reloaded (new stock)")
    void purchaseEmptiesCartAndReloadsCatalog() throws SQLException {
        controller.addToCart(HOME_SHIRT, 1);
        int loadsBefore = merchandiseDAO.loads;

        controller.purchase();

        assertTrue(controller.getCart().isEmpty());
        assertEquals(loadsBefore + 1, merchandiseDAO.loads);
    }

    @Test
    @DisplayName("if the purchase can not be stored, the cart is kept so the employee can fix it")
    void failedPurchaseKeepsCart() {
        controller.addToCart(HOME_SHIRT, 2);
        saleDAO.fail = true;

        assertThrows(SQLException.class, () -> controller.purchase());

        assertFalse(controller.getCart().isEmpty());
        assertEquals(2, controller.getCart().getUnitsOf(HOME_SHIRT.getId()));
    }
}
