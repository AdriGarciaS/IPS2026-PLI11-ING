package com.ips2026.pl11.controller.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.campaign.CampaignDAO;
import com.ips2026.pl11.data.campaign.CampaignPurchaseDAO;
import com.ips2026.pl11.data.shareholder.ShareholderDAO;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignPurchase;
import com.ips2026.pl11.model.campaign.CampaignPurchaseLimit;
import com.ips2026.pl11.model.campaign.CampaignStatus;
import com.ips2026.pl11.model.campaign.ShareholderRegistration;

/**
 * Tests of the controller for external users who buy campaign shares and
 * register as shareholders (US 35828), with fake DAOs (no database).
 */
@DisplayName("ExternalSharePurchaseController")
class ExternalSharePurchaseControllerTest {

    private static final LocalDateTime MOMENT = LocalDateTime.of(2026, 11, 20, 10, 30);
    private static final String VALID_CARD = "4111 1111 1111 1111";
    private static final ShareholderRegistration ANA = new ShareholderRegistration("Ana", "López Vidal",
            "ana.lopez@example.com", "12345678Z", "ana_lopez", "secret1");

    private static class FakeCampaignDAO extends CampaignDAO {
        Campaign open = new Campaign(2, "Capital increase 2027", new BigDecimal("12.00"), 500, CampaignStatus.PHASE_3);

        @Override
        public Optional<Campaign> findOpen() {
            return Optional.ofNullable(open);
        }
    }

    /** 430 shares remain. Remembers the last registration. */
    private static class FakePurchaseDAO extends CampaignPurchaseDAO {
        CampaignStatus status = CampaignStatus.PHASE_3;
        int remaining = 430;
        ShareholderRegistration registered;
        String storedPassword;
        LocalDateTime boughtAt;

        @Override
        public CampaignPurchaseLimit findExternalLimit(int campaignId) {
            return CampaignPurchaseLimit.forExternalUser(status, remaining);
        }

        @Override
        public CampaignPurchase registerAndBuy(Campaign campaign, ShareholderRegistration registration,
                String passwordHash, int quantity, LocalDateTime moment) {
            registered = registration;
            storedPassword = passwordHash;
            boughtAt = moment;
            remaining -= quantity;
            return new CampaignPurchase(registration.getFullName(), campaign.getName(), quantity, 3071,
                    3070 + quantity, campaign.getNominalPrice());
        }
    }

    /** Laura is a shareholder (laura@example.com, 11111111H, username "shareholder"); "pgomez" is an employee. */
    private static class FakeShareholderDAO extends ShareholderDAO {
        @Override
        public boolean existsEmail(String email) {
            return email.equalsIgnoreCase("laura@example.com");
        }

        @Override
        public boolean existsNationalId(String nationalId) {
            return nationalId.equalsIgnoreCase("11111111H");
        }

        @Override
        public boolean existsUsername(String username) {
            return username.equalsIgnoreCase("shareholder") || username.equalsIgnoreCase("pgomez");
        }
    }

    private final FakeCampaignDAO campaignDAO = new FakeCampaignDAO();
    private final FakePurchaseDAO purchaseDAO = new FakePurchaseDAO();
    private final ExternalSharePurchaseController controller = new ExternalSharePurchaseController(campaignDAO,
            purchaseDAO, new FakeShareholderDAO(), () -> MOMENT, password -> "hashed:" + password);

    @Test
    @DisplayName("the active campaign is shown in any phase, but external users only buy in phase 3")
    void loadInEveryPhase() throws SQLException {
        assertTrue(controller.load());
        assertEquals(430, controller.getLimit().getMax());

        purchaseDAO.status = CampaignStatus.PHASE_2;
        assertTrue(controller.load());
        assertEquals("Capital increase 2027", controller.getCampaign().getName());
        assertEquals(0, controller.getLimit().getMax());
        assertTrue(controller.findQuantityProblem("1").orElseThrow().contains("phase 3"));
    }

    @Test
    @DisplayName("without an active campaign there is nothing to show")
    void loadWithoutCampaign() throws SQLException {
        campaignDAO.open = null;
        assertFalse(controller.load());
        assertNull(controller.getCampaign());
    }

    @Test
    @DisplayName("the quantity can be any number of the remaining shares, and the total is price times quantity")
    void quantityAndTotal() throws SQLException {
        controller.load();
        assertTrue(controller.findQuantityProblem("430").isEmpty());
        assertEquals("You can buy at most 430 shares.", controller.findQuantityProblem("431").orElseThrow());
        assertEquals(new BigDecimal("240.00"), controller.totalFor(20));
    }

    @Test
    @DisplayName("valid data: the user is registered with the password hashed and buys the shares")
    void registerAndBuy() throws SQLException {
        controller.load();
        CampaignPurchase purchase = controller.registerAndBuy(" 20 ", ANA, VALID_CARD);

        assertEquals(ANA, purchaseDAO.registered);
        assertEquals("hashed:secret1", purchaseDAO.storedPassword);
        assertEquals(MOMENT, purchaseDAO.boughtAt);
        assertEquals("Ana López Vidal", purchase.getShareholderName());
        assertEquals(new BigDecimal("240.00"), purchase.getTotal());
        assertEquals(410, controller.getLimit().getRemainingShares()); // read again after buying
    }

    @Test
    @DisplayName("an email, national ID or username already registered (also an employee's username) is rejected")
    void alreadyRegistered() throws SQLException {
        ShareholderRegistration laura = new ShareholderRegistration("Laura", "Pérez", "LAURA@example.com",
                "11111111h", "pgomez", "secret1");

        assertEquals(List.of("There is already a shareholder with this email.",
                "There is already a shareholder with this national ID: log in to buy shares.",
                "This username is already taken. Please choose another one."),
                controller.findRegistrationProblems(laura, VALID_CARD));
        assertTrue(controller.findRegistrationProblems(ANA, VALID_CARD).isEmpty());
    }

    @Test
    @DisplayName("format problems and a wrong card are reported together")
    void formatAndCardProblems() throws SQLException {
        ShareholderRegistration wrong = new ShareholderRegistration("Ana", "", "ana@example", "12345678Z",
                "ana_lopez", "secret1");

        assertEquals(List.of("The last name is required.", "The email is not valid, for example name@example.com.",
                "A card number has between 13 and 19 digits."), controller.findRegistrationProblems(wrong, "1234"));
    }

    @Test
    @DisplayName("with any problem nothing is stored, and the quantity problem comes first")
    void registerAndBuyWithProblems() throws SQLException {
        controller.load();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.registerAndBuy("500", ANA, "4111 1111 1111 1112"));

        assertEquals("You can buy at most 430 shares." + System.lineSeparator()
                + "The card number is not valid. Please check the digits.", exception.getMessage());
        assertNull(purchaseDAO.registered);
    }

    @Test
    @DisplayName("outside phase 3 nobody can be registered")
    void registerAndBuyOutsidePhase3() throws SQLException {
        purchaseDAO.status = CampaignStatus.PHASE_1;
        controller.load();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.registerAndBuy("10", ANA, VALID_CARD));
        assertTrue(exception.getMessage().startsWith("External users can buy shares when the campaign reaches phase 3"));
        assertNull(purchaseDAO.registered);
    }
}
