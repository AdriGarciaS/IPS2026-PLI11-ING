package com.ips2026.pl11.controller.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.ips2026.pl11.data.campaign.CampaignDAO;
import com.ips2026.pl11.data.campaign.CampaignPurchaseDAO;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignPurchase;
import com.ips2026.pl11.model.campaign.CampaignPurchaseLimit;
import com.ips2026.pl11.model.campaign.CampaignStatus;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * Tests of the controller to buy shares of the active campaign (US 35947)
 * with fake DAOs (no database).
 */
@DisplayName("BuyCampaignSharesController")
class BuyCampaignSharesControllerTest {

    private static final Shareholder LAURA = new Shareholder(1, "Laura", "Pérez Gil", "11111111H");
    private static final LocalDateTime MOMENT = LocalDateTime.of(2026, 11, 20, 10, 30);
    private static final String VALID_CARD = "4111 1111 1111 1111";

    private static class FakeCampaignDAO extends CampaignDAO {
        Campaign open = new Campaign(2, "Capital increase 2027", new BigDecimal("12.00"), 500, CampaignStatus.PHASE_1);

        @Override
        public Optional<Campaign> findOpen() {
            return Optional.ofNullable(open);
        }
    }

    /** Laura owns 100 shares of other campaigns; 480 shares remain. Remembers the last purchase. */
    private static class FakePurchaseDAO extends CampaignPurchaseDAO {
        CampaignStatus status = CampaignStatus.PHASE_1;
        int remaining = 480;
        int bought;
        Integer boughtQuantity;
        LocalDateTime boughtAt;

        @Override
        public CampaignPurchaseLimit findLimit(int campaignId, int shareholderId) {
            return new CampaignPurchaseLimit(status, remaining, 100 + bought, 100, bought);
        }

        @Override
        public CampaignPurchase buy(Campaign campaign, Shareholder shareholder, int quantity, LocalDateTime moment) {
            boughtQuantity = quantity;
            boughtAt = moment;
            bought += quantity;
            remaining -= quantity;
            return new CampaignPurchase(shareholder.getFullName(), campaign.getName(), quantity, 121,
                    120 + quantity, campaign.getNominalPrice());
        }
    }

    private final FakeCampaignDAO campaignDAO = new FakeCampaignDAO();
    private final FakePurchaseDAO purchaseDAO = new FakePurchaseDAO();
    private final BuyCampaignSharesController controller =
            new BuyCampaignSharesController(campaignDAO, purchaseDAO, LAURA, () -> MOMENT);

    @Test
    @DisplayName("load reads the active campaign and the limit of the shareholder")
    void loadActiveCampaign() throws SQLException {
        assertTrue(controller.load());
        assertSame(campaignDAO.open, controller.getCampaign());
        assertEquals(100, controller.getLimit().getMax());
        assertSame(LAURA, controller.getShareholder());
    }

    @ParameterizedTest
    @EnumSource(value = CampaignStatus.class, names = { "INACTIVE", "FINISHED" })
    @DisplayName("nothing can be bought while the open campaign is inactive (or finished)")
    void loadCampaignNotInPhase(CampaignStatus status) throws SQLException {
        campaignDAO.open = new Campaign(2, "Capital increase 2027", new BigDecimal("12.00"), 500, status);
        assertFalse(controller.load());
        assertNull(controller.getCampaign());
    }

    @Test
    @DisplayName("nothing can be bought when there is no open campaign")
    void loadWithoutCampaign() throws SQLException {
        campaignDAO.open = null;
        assertFalse(controller.load());
    }

    @Test
    @DisplayName("the quantity must be a whole number between 1 and the maximum")
    void quantityProblems() throws SQLException {
        controller.load();
        assertTrue(controller.findQuantityProblem("1").isEmpty());
        assertTrue(controller.findQuantityProblem(" 100 ").isEmpty());
        assertEquals("Enter the number of shares to buy.", controller.findQuantityProblem(" ").orElseThrow());
        assertEquals("You must buy at least 1 share.", controller.findQuantityProblem("0").orElseThrow());
        assertEquals("You can buy at most 100 shares.", controller.findQuantityProblem("101").orElseThrow());
        assertEquals("You can buy at most 100 shares.", controller.findQuantityProblem("99999999999").orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = { "abc", "-5", "2.5", "1e3" })
    @DisplayName("a quantity that is not a whole number is not valid")
    void quantityNotWholeNumber(String text) throws SQLException {
        controller.load();
        assertEquals("The number of shares must be a whole number.", controller.findQuantityProblem(text).orElseThrow());
    }

    @Test
    @DisplayName("when the shareholder can buy nothing, the quantity problem says why")
    void quantityWhenNothingCanBeBought() throws SQLException {
        purchaseDAO.bought = 100;
        controller.load();
        assertTrue(controller.findQuantityProblem("1").orElseThrow().contains("already bought 100"));
    }

    @Test
    @DisplayName("the total is the nominal price times the quantity")
    void total() throws SQLException {
        controller.load();
        assertEquals(new BigDecimal("240.00"), controller.totalFor(20));
    }

    @Test
    @DisplayName("a valid purchase is stored with the moment, and the limit is read again")
    void buy() throws SQLException {
        controller.load();
        CampaignPurchase purchase = controller.buy(" 20 ", VALID_CARD);

        assertEquals(20, purchaseDAO.boughtQuantity);
        assertEquals(MOMENT, purchaseDAO.boughtAt);
        assertEquals(new BigDecimal("240.00"), purchase.getTotal());
        assertEquals(80, controller.getLimit().getMax());
        assertEquals(460, controller.getLimit().getRemainingShares());
    }

    @Test
    @DisplayName("a wrong quantity and a wrong card are both reported, and nothing is bought")
    void buyWithProblems() throws SQLException {
        controller.load();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.buy("500", "1234"));

        assertEquals("You can buy at most 100 shares." + System.lineSeparator()
                + "A card number has between 13 and 19 digits.", exception.getMessage());
        assertNull(purchaseDAO.boughtQuantity);
    }

    @Test
    @DisplayName("a wrong card alone is reported, and nothing is bought")
    void buyWithWrongCard() throws SQLException {
        controller.load();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.buy("10", "4111 1111 1111 1112"));
        assertEquals("The card number is not valid. Please check the digits.", exception.getMessage());
        assertNull(purchaseDAO.boughtQuantity);
    }
}
