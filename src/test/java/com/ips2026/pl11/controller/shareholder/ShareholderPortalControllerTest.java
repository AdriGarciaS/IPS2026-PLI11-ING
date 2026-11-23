package com.ips2026.pl11.controller.shareholder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.shareholder.ShareholderDAO;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * Tests of the search of the shareholder who logs in, with a fake DAO (no
 * database).
 */
@DisplayName("ShareholderPortalController")
class ShareholderPortalControllerTest {

    private static final Shareholder LAURA = new Shareholder(1, "Laura", "Pérez Gil", "11111111H", "shareholder", "x");
    private static final Shareholder MARCOS = new Shareholder(2, "Marcos", "Ruiz Soto", "22222222J", "marcos_ruiz", "x");
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
    @DisplayName("a shareholder is found by the exact username, ignoring case and outer spaces")
    void findByUsername() {
        assertEquals(LAURA, controller.findShareholderByUsername("shareholder").orElseThrow());
        assertEquals(MARCOS, controller.findShareholderByUsername("  Marcos_Ruiz ").orElseThrow());
    }

    @Test
    @DisplayName("an unknown, partial or empty username finds nobody")
    void unknownUsername() {
        assertTrue(controller.findShareholderByUsername("nobody").isEmpty());
        assertTrue(controller.findShareholderByUsername("marcos").isEmpty());
        assertTrue(controller.findShareholderByUsername("").isEmpty());
        assertTrue(controller.findShareholderByUsername(null).isEmpty());
    }
}
