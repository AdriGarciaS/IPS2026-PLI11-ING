package com.ips2026.pl11.data.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.store.Merchandise;

@DisplayName("MerchandiseDAO")
class MerchandiseDAOTest {

    @TempDir
    Path folder;

    private final MerchandiseDAO dao = new MerchandiseDAO();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    @Test
    @DisplayName("an empty catalog returns no products and no types")
    void emptyCatalog() throws SQLException {
        assertTrue(dao.findAll().isEmpty());
        assertTrue(dao.findTypes().isEmpty());
    }

    @Test
    @DisplayName("findAll returns every product with its data, ordered by name")
    void findAllReturnsProductsOrderedByName() throws SQLException {
        TestDatabase.execute("INSERT INTO MERCHANDISING (id, name, type, available_units, price) VALUES "
                + "(1, 'Home Shirt', 'Clothing', 25, 59.99), "
                + "(2, 'Club Scarf', 'Accessories', 50, 14.95)");

        List<Merchandise> products = dao.findAll();

        assertEquals(2, products.size());
        Merchandise scarf = products.get(0);
        assertEquals(2, scarf.getId());
        assertEquals("Club Scarf", scarf.getName());
        assertEquals("Accessories", scarf.getType());
        assertEquals(50, scarf.getAvailableUnits());
        assertEquals(new BigDecimal("14.95"), scarf.getPrice());
        assertEquals("Home Shirt", products.get(1).getName());
    }

    @Test
    @DisplayName("prices are read with exactly 2 decimals")
    void pricesHaveTwoDecimals() throws SQLException {
        TestDatabase.execute("INSERT INTO MERCHANDISING (name, type, available_units, price) VALUES "
                + "('Club Keyring', 'Souvenirs', 100, 4.5), ('Club Pin', 'Souvenirs', 100, 3)");

        List<Merchandise> products = dao.findAll();

        assertEquals(new BigDecimal("4.50"), products.get(0).getPrice());
        assertEquals(new BigDecimal("3.00"), products.get(1).getPrice());
    }

    @Test
    @DisplayName("findTypes returns each type once, in alphabetical order")
    void findTypesReturnsDistinctSortedTypes() throws SQLException {
        TestDatabase.execute("INSERT INTO MERCHANDISING (name, type, available_units, price) VALUES "
                + "('Home Shirt', 'Clothing', 1, 1), ('Club Mug', 'Souvenirs', 1, 1), "
                + "('Away Shirt', 'Clothing', 1, 1), ('Club Scarf', 'Accessories', 1, 1)");

        assertEquals(List.of("Accessories", "Clothing", "Souvenirs"), dao.findTypes());
    }
}
