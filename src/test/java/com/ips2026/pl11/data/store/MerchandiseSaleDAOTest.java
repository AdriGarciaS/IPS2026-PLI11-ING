package com.ips2026.pl11.data.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.store.CartLine;
import com.ips2026.pl11.model.store.Merchandise;

@DisplayName("MerchandiseSaleDAO")
class MerchandiseSaleDAOTest {

    private static final LocalDate SALE_DATE = LocalDate.of(2026, 10, 2);

    // Same data as the rows inserted in setUp().
    private static final Merchandise SHIRT = new Merchandise(1, "Home Shirt", "Clothing", 5, new BigDecimal("59.99"));
    private static final Merchandise SCARF = new Merchandise(2, "Club Scarf", "Accessories", 10, new BigDecimal("14.95"));

    @TempDir
    Path folder;

    private final MerchandiseSaleDAO dao = new MerchandiseSaleDAO();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
        TestDatabase.execute("INSERT INTO MERCHANDISING (id, name, type, available_units, price) VALUES "
                + "(1, 'Home Shirt', 'Clothing', 5, 59.99), "
                + "(2, 'Club Scarf', 'Accessories', 10, 14.95)");
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    private static int stockOf(int merchId) throws SQLException {
        return TestDatabase.queryInt("SELECT available_units FROM MERCHANDISING WHERE id = " + merchId);
    }

    private static int saleRows() throws SQLException {
        return TestDatabase.queryInt("SELECT COUNT(*) FROM MERCHANDISING_SALE");
    }

    @Test
    @DisplayName("a purchase stores one row per line, all with the same sale number")
    void registerSaleStoresOneRowPerLine() throws SQLException {
        int saleNumber = dao.registerSale(List.of(new CartLine(SHIRT, 2), new CartLine(SCARF, 3)), SALE_DATE);

        assertEquals(1, saleNumber);
        assertEquals(2, saleRows());

        String sql = "SELECT sale_number, merch_id, units_sold, total_price, sale_date "
                + "FROM MERCHANDISING_SALE ORDER BY id";
        try (Connection connection = ConexionBD.obtenerConexion();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {

            rows.next();
            assertEquals(1, rows.getInt("sale_number"));
            assertEquals(1, rows.getInt("merch_id"));
            assertEquals(2, rows.getInt("units_sold"));
            assertEquals(119.98, rows.getDouble("total_price"), 0.001); // 2 x 59.99
            assertEquals("2026-10-02", rows.getString("sale_date"));

            rows.next();
            assertEquals(1, rows.getInt("sale_number"));
            assertEquals(2, rows.getInt("merch_id"));
            assertEquals(3, rows.getInt("units_sold"));
            assertEquals(44.85, rows.getDouble("total_price"), 0.001); // 3 x 14.95
        }
    }

    @Test
    @DisplayName("the final price of a purchase can be consulted as the sum of its lines")
    void finalPriceIsSumOfLines() throws SQLException {
        int saleNumber = dao.registerSale(List.of(new CartLine(SHIRT, 2), new CartLine(SCARF, 3)), SALE_DATE);

        int finalPriceInCents = TestDatabase.queryInt("SELECT ROUND(SUM(total_price) * 100) FROM MERCHANDISING_SALE "
                + "WHERE sale_number = " + saleNumber);
        assertEquals(16483, finalPriceInCents); // 119.98 + 44.85
    }

    @Test
    @DisplayName("a purchase subtracts the sold units from the stock")
    void registerSaleSubtractsStock() throws SQLException {
        dao.registerSale(List.of(new CartLine(SHIRT, 2), new CartLine(SCARF, 3)), SALE_DATE);

        assertEquals(3, stockOf(SHIRT.getId()));
        assertEquals(7, stockOf(SCARF.getId()));
    }

    @Test
    @DisplayName("all the available units can be sold, leaving the stock at 0 (upper limit)")
    void canSellAllUnits() throws SQLException {
        dao.registerSale(List.of(new CartLine(SHIRT, 5)), SALE_DATE);

        assertEquals(0, stockOf(SHIRT.getId()));
    }

    @Test
    @DisplayName("each purchase gets the next sale number")
    void consecutivePurchasesGetConsecutiveNumbers() throws SQLException {
        int first = dao.registerSale(List.of(new CartLine(SHIRT, 1)), SALE_DATE);
        int second = dao.registerSale(List.of(new CartLine(SCARF, 1), new CartLine(SHIRT, 1)), SALE_DATE);

        assertEquals(1, first);
        assertEquals(2, second);
        assertEquals(2, TestDatabase.queryInt("SELECT COUNT(*) FROM MERCHANDISING_SALE WHERE sale_number = 2"));
    }

    @Test
    @DisplayName("selling more units than the stock fails and the stock does not change")
    void cannotSellMoreThanStock() throws SQLException {
        assertThrows(SQLException.class,
                () -> dao.registerSale(List.of(new CartLine(SHIRT, 6)), SALE_DATE));

        assertEquals(5, stockOf(SHIRT.getId()));
        assertEquals(0, saleRows());
    }

    @Test
    @DisplayName("if one line has not enough stock, nothing of the purchase is stored")
    void failedLineCancelsWholePurchase() throws SQLException {
        // The stock changed since the products were added to the cart: only 1 shirt left.
        TestDatabase.execute("UPDATE MERCHANDISING SET available_units = 1 WHERE id = 1");

        assertThrows(SQLException.class,
                () -> dao.registerSale(List.of(new CartLine(SCARF, 3), new CartLine(SHIRT, 2)), SALE_DATE));

        assertEquals(0, saleRows(), "no line of the failed purchase must be stored");
        assertEquals(10, stockOf(SCARF.getId()), "the stock of the first line must be restored");
        assertEquals(1, stockOf(SHIRT.getId()));
    }
}
