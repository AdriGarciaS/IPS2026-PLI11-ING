package com.ips2026.pl11.controller.shareholder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.shareholder.ShareholderDAO;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * Tests of the shareholders portal controller with a fake DAO (no database).
 */
@DisplayName("ShareholderPortalController")
class ShareholderPortalControllerTest {

    private static final Shareholder LAURA = new Shareholder(1, "Laura", "Pérez Gil", "11111111H");
    private static final Shareholder MARCOS = new Shareholder(2, "Marcos", "Ruiz Soto", "22222222J");
    private static final Shareholder IRENE = new Shareholder(3, "Irene", "Castillo Mora", "33333333P");

    private static class FakeShareholderDAO extends ShareholderDAO {
        @Override
        public List<Shareholder> findAll() {
            return List.of(IRENE, LAURA, MARCOS);
        }
    }

    private ShareholderPortalController controller;

    @BeforeEach
    void setUp() throws SQLException {
        controller = new ShareholderPortalController(new FakeShareholderDAO());
        controller.load();
    }

    @Test
    @DisplayName("a shareholder can be found by the national ID")
    void searchByNationalId() {
        assertEquals(List.of(MARCOS), controller.search("22222222J"));
        assertEquals(List.of(MARCOS), controller.search("2222")); // part of it
    }

    @Test
    @DisplayName("a shareholder can be found by name or surname, in any part of it")
    void searchByName() {
        assertEquals(List.of(LAURA), controller.search("Laura"));
        assertEquals(List.of(IRENE), controller.search("castillo"));
        assertEquals(List.of(MARCOS), controller.search("ruiz so"));
    }

    @Test
    @DisplayName("the search ignores upper/lower case and accents")
    void searchIgnoresCaseAndAccents() {
        assertEquals(List.of(LAURA), controller.search("PEREZ"));
        assertEquals(List.of(LAURA), controller.search("pérez"));
        assertEquals(List.of(MARCOS), controller.search("22222222j"));
    }

    @Test
    @DisplayName("an empty search finds nobody, and a search without matches is empty")
    void emptySearch() {
        assertTrue(controller.search("").isEmpty());
        assertTrue(controller.search("   ").isEmpty());
        assertTrue(controller.search(null).isEmpty());
        assertTrue(controller.search("zzz").isEmpty());
    }

    @Test
    @DisplayName("a complete national ID identifies exactly one shareholder")
    void findByNationalId() {
        assertEquals(LAURA, controller.findByNationalId(" 11111111h ").orElseThrow());
        assertTrue(controller.findByNationalId("1111").isEmpty()); // only part of it
    }

    @Test
    @DisplayName("the shareholder chosen is kept for the portal options")
    void selectShareholder() {
        assertNull(controller.getSelected());

        controller.select(IRENE);
        assertEquals(IRENE, controller.getSelected());

        controller.select(null);
        assertNull(controller.getSelected());
    }
}
