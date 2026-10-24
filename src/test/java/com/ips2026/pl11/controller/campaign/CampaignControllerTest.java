package com.ips2026.pl11.controller.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ips2026.pl11.data.campaign.CampaignDAO;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignStatus;

/**
 * Tests of the campaigns controller with a fake DAO (no database).
 */
@DisplayName("CampaignController")
class CampaignControllerTest {

    /** Fake campaigns: an optional open campaign, one existing name, and remembers the last insert. */
    private static class FakeCampaignDAO extends CampaignDAO {
        Campaign open;
        String existingName = "Capital increase 2024";
        String insertedName;
        BigDecimal insertedPrice;
        int insertedShares = -1;

        @Override
        public Optional<Campaign> findOpen() {
            return Optional.ofNullable(open);
        }

        @Override
        public boolean existsName(String name) {
            return existingName.equalsIgnoreCase(name);
        }

        @Override
        public Campaign insert(String name, BigDecimal nominalPrice, int totalShares) {
            insertedName = name;
            insertedPrice = nominalPrice;
            insertedShares = totalShares;
            return new Campaign(7, name, nominalPrice, totalShares, CampaignStatus.INACTIVE);
        }
    }

    private final FakeCampaignDAO dao = new FakeCampaignDAO();
    private final CampaignController controller = new CampaignController(dao);

    /** Message of the problems found when creating with these values (nothing must be saved). */
    private String problems(String name, String price, String shares) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.create(name, price, shares));
        assertNull(dao.insertedName, "nothing is saved when there are problems");
        return exception.getMessage();
    }

    @Test
    @DisplayName("a valid campaign is saved inactive, with the name trimmed and the price with 2 decimals")
    void createsInactiveCampaign() throws SQLException {
        Campaign campaign = controller.create("  Capital increase 2027 ", "10,5", " 1000 ");

        assertEquals("Capital increase 2027", dao.insertedName);
        assertEquals(new BigDecimal("10.50"), dao.insertedPrice);
        assertEquals(1000, dao.insertedShares);
        assertEquals(CampaignStatus.INACTIVE, campaign.getStatus());
        assertEquals(1000, campaign.getTotalShares());
    }

    @Test
    @DisplayName("the price is accepted with a comma or a dot")
    void priceWithCommaOrDot() throws SQLException {
        controller.create("A", "12.75", "1");
        assertEquals(new BigDecimal("12.75"), dao.insertedPrice);

        controller.create("B", "12,75", "1");
        assertEquals(new BigDecimal("12.75"), dao.insertedPrice);
    }

    @Test
    @DisplayName("the smallest values are accepted: 0,01 € and 1 share")
    void smallestValues() throws SQLException {
        controller.create("Small", "0,01", "1");
        assertEquals(new BigDecimal("0.01"), dao.insertedPrice);
        assertEquals(1, dao.insertedShares);
    }

    @Test
    @DisplayName("no campaign can be created while another one is not finished")
    void blockedWhileAnotherIsOpen() {
        dao.open = new Campaign(3, "Capital increase 2026", new BigDecimal("14.00"), 1000, CampaignStatus.PHASE_2);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> controller.create("Capital increase 2027", "10", "1000"));
        assertTrue(exception.getMessage().contains("Capital increase 2026"));
        assertTrue(exception.getMessage().contains("Phase 2"));
        assertNull(dao.insertedName);
    }

    @Test
    @DisplayName("an inactive campaign also blocks creating a new one")
    void blockedByInactiveCampaign() {
        dao.open = new Campaign(3, "Next campaign", new BigDecimal("14.00"), 1000, CampaignStatus.INACTIVE);

        assertThrows(IllegalStateException.class, () -> controller.create("Other", "10", "1000"));
        assertNull(dao.insertedName);
    }

    @Test
    @DisplayName("empty fields: one message with every problem")
    void allFieldsRequired() {
        String message = problems("   ", "", null);

        assertTrue(message.contains("name is required"));
        assertTrue(message.contains("price per share is required"));
        assertTrue(message.contains("number of shares is required"));
        assertEquals(3, message.lines().count());
    }

    @Test
    @DisplayName("the name must be unique, ignoring upper/lower case")
    void nameMustBeUnique() {
        assertTrue(problems("CAPITAL INCREASE 2024", "10", "1000").contains("already a campaign"));
    }

    @ParameterizedTest(name = "price \"{0}\" is rejected")
    @ValueSource(strings = { "0", "0,00", "-5", "abc", "10€", "1,2,3", "10,555", "1.000,50" })
    @DisplayName("wrong nominal prices are rejected")
    void wrongPrices(String price) {
        String message = problems("Capital increase 2027", price, "1000");
        assertTrue(message.contains("nominal price"), message);
        assertFalse(message.contains("shares"), "only the price is wrong: " + message);
    }

    @ParameterizedTest(name = "total shares \"{0}\" is rejected")
    @ValueSource(strings = { "0", "-5", "1.5", "1,5", "abc", "99999999999" })
    @DisplayName("wrong numbers of shares are rejected")
    void wrongShares(String shares) {
        String message = problems("Capital increase 2027", "10", shares);
        assertTrue(message.contains("number of shares"), message);
        assertFalse(message.contains("price"), "only the shares are wrong: " + message);
    }

    @Test
    @DisplayName("findOpenCampaign returns the campaign that is not finished, if any")
    void findOpenCampaign() throws SQLException {
        assertTrue(controller.findOpenCampaign().isEmpty());

        dao.open = new Campaign(3, "Next campaign", new BigDecimal("14.00"), 1000, CampaignStatus.INACTIVE);
        assertEquals("Next campaign", controller.findOpenCampaign().orElseThrow().getName());
    }
}
