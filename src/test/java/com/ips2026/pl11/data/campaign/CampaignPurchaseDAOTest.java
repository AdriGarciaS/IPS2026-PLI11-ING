package com.ips2026.pl11.data.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignPurchase;
import com.ips2026.pl11.model.campaign.CampaignPurchaseLimit;
import com.ips2026.pl11.model.campaign.CampaignStatus;
import com.ips2026.pl11.model.campaign.ShareholderRegistration;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * Tests of CampaignPurchaseDAO (US 35947) on a temporary SQLite database
 * created with the real schema.sql. It starts with:
 * <ul>
 *   <li>campaign 1, finished: Laura owns its shares 1-100;</li>
 *   <li>campaign 2, open in phase 1 (500 shares at 12 €): Marcos bought its
 *       shares 101-120.</li>
 * </ul>
 */
@DisplayName("CampaignPurchaseDAO")
class CampaignPurchaseDAOTest {

    private static final Shareholder LAURA = new Shareholder(1, "Laura", "Pérez Gil", "11111111H");
    private static final Shareholder MARCOS = new Shareholder(2, "Marcos", "Ruiz Soto", "22222222J");
    private static final Shareholder IRENE = new Shareholder(3, "Irene", "Castillo Mora", "33333333P");
    private static final LocalDateTime MOMENT = LocalDateTime.of(2026, 11, 20, 10, 30);

    @TempDir
    Path folder;

    private final CampaignPurchaseDAO dao = new CampaignPurchaseDAO();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
        TestDatabase.execute("INSERT INTO SHAREHOLDER (id, first_name, last_name, national_id) VALUES "
                + "(1, 'Laura', 'Pérez Gil', '11111111H'), (2, 'Marcos', 'Ruiz Soto', '22222222J'), "
                + "(3, 'Irene', 'Castillo Mora', '33333333P')");
        TestDatabase.execute("INSERT INTO SHARE_CAMPAIGN (id, name, nominal_price, total_shares, status) VALUES "
                + "(1, 'Capital increase 2024', 10.0, 1000, 'FINISHED'), "
                + "(2, 'Capital increase 2027', 12.0, 500, 'PHASE_1')");
        TestDatabase.execute("INSERT INTO SHARE_HOLDING (id, shareholder_id, campaign_id, purchase_date, purchase_price) "
                + "VALUES (1, 1, 1, '2024-03-01 10:00:00', 10.0), (2, 2, 2, '2026-11-19 09:00:00', 12.0)");
        TestDatabase.execute("WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 120) "
                + "INSERT INTO SHARE (id, holding_id) SELECT i, CASE WHEN i <= 100 THEN 1 ELSE 2 END FROM n");
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    private static Campaign openCampaign(CampaignStatus status) {
        return new Campaign(2, "Capital increase 2027", new BigDecimal("12.00"), 500, status);
    }

    @Test
    @DisplayName("the limit counts the remaining shares, the shares owned and the ones bought in the campaign")
    void findLimit() throws SQLException {
        CampaignPurchaseLimit laura = dao.findLimit(2, LAURA.getId());
        assertEquals(CampaignStatus.PHASE_1, laura.getStatus());
        assertEquals(480, laura.getRemainingShares());
        assertEquals(100, laura.getOwnedShares());
        assertEquals(100, laura.getOwnedOutsideCampaign());
        assertEquals(0, laura.getBoughtInCampaign());
        assertEquals(100, laura.getMax());

        CampaignPurchaseLimit marcos = dao.findLimit(2, MARCOS.getId());
        assertEquals(20, marcos.getOwnedShares());
        assertEquals(0, marcos.getOwnedOutsideCampaign());
        assertEquals(20, marcos.getBoughtInCampaign());
        assertEquals(0, marcos.getMax());
    }

    @Test
    @DisplayName("shares of the campaign sold in the market still count as bought by the first buyer, not by the second")
    void marketSalesOfCampaignShares() throws SQLException {
        // Marcos sells 5 shares of the campaign (101-105) to Laura in the market.
        TestDatabase.execute("INSERT INTO SHARE_HOLDING (id, shareholder_id, campaign_id, purchase_date, purchase_price) "
                + "VALUES (3, 1, 2, '2026-11-19 18:00:00', 13.0)");
        TestDatabase.execute("INSERT INTO SHARE_LISTING (id, seller_id, price_per_share, listed_at) "
                + "VALUES (1, 2, 13.0, '2026-11-19 12:00:00')");
        TestDatabase.execute("INSERT INTO SHARE_LISTING_ITEM (listing_id, share_id) "
                + "SELECT 1, id FROM SHARE WHERE id BETWEEN 101 AND 105");
        TestDatabase.execute("INSERT INTO SHARE_TRANSFER (purchase_number, listing_id, share_id, seller_id, buyer_id, "
                + "from_holding_id, to_holding_id, sale_type, price, transferred_at) "
                + "SELECT 1, 1, id, 2, 1, 2, 3, 'SINGLE', 13.0, '2026-11-19 18:00:00' FROM SHARE WHERE id BETWEEN 101 AND 105");
        TestDatabase.execute("UPDATE SHARE SET holding_id = 3 WHERE id BETWEEN 101 AND 105");

        CampaignPurchaseLimit laura = dao.findLimit(2, LAURA.getId());
        assertEquals(105, laura.getOwnedShares());
        assertEquals(100, laura.getOwnedOutsideCampaign());
        assertEquals(0, laura.getBoughtInCampaign());
        assertEquals(480, laura.getRemainingShares());

        assertEquals(20, dao.findLimit(2, MARCOS.getId()).getBoughtInCampaign());
    }

    @Test
    @DisplayName("the shares bought get new consecutive IDs and go to a new holding at the nominal price")
    void buyCreatesSharesInTheWallet() throws SQLException {
        CampaignPurchase purchase = dao.buy(openCampaign(CampaignStatus.PHASE_1), LAURA, 30, MOMENT);

        assertEquals(30, purchase.getQuantity());
        assertEquals(121, purchase.getFirstShareId());
        assertEquals(150, purchase.getLastShareId());
        assertEquals(new BigDecimal("360.00"), purchase.getTotal());
        assertEquals("Laura Pérez Gil", purchase.getShareholderName());
        assertEquals("Capital increase 2027", purchase.getCampaignName());

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_HOLDING WHERE shareholder_id = 1 "
                + "AND campaign_id = 2 AND purchase_date = '2026-11-20 10:30:00' AND purchase_price = 12.0"));
        assertEquals(30, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE s JOIN SHARE_HOLDING h "
                + "ON h.id = s.holding_id WHERE h.shareholder_id = 1 AND h.campaign_id = 2 AND s.id BETWEEN 121 AND 150"));

        CampaignPurchaseLimit after = dao.findLimit(2, LAURA.getId());
        assertEquals(450, after.getRemainingShares());
        assertEquals(30, after.getBoughtInCampaign());
        assertEquals(70, after.getMax());
    }

    @Test
    @DisplayName("phase 1: buying more than allowed stores nothing")
    void buyOverPhase1Limit() throws SQLException {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> dao.buy(openCampaign(CampaignStatus.PHASE_1), LAURA, 101, MOMENT));
        assertEquals("You can no longer buy 101 shares: the maximum now is 100.", exception.getMessage());

        assertThrows(IllegalStateException.class, () -> dao.buy(openCampaign(CampaignStatus.PHASE_1), IRENE, 1, MOMENT));
        assertEquals(120, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE"));
        assertEquals(2, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_HOLDING"));
    }

    @Test
    @DisplayName("phase 3: a shareholder without other shares can buy all the remaining ones, but not more")
    void buyInPhase3() throws SQLException {
        TestDatabase.execute("UPDATE SHARE_CAMPAIGN SET status = 'PHASE_3' WHERE id = 2");

        assertThrows(IllegalStateException.class, () -> dao.buy(openCampaign(CampaignStatus.PHASE_3), IRENE, 481, MOMENT));
        CampaignPurchase purchase = dao.buy(openCampaign(CampaignStatus.PHASE_3), IRENE, 480, MOMENT);

        assertEquals(480, purchase.getQuantity());
        assertEquals(0, dao.findLimit(2, IRENE.getId()).getRemainingShares());
    }

    @Test
    @DisplayName("the phase is read again when buying: if the campaign has finished meanwhile, nothing is bought")
    void buyAfterCampaignFinished() throws SQLException {
        TestDatabase.execute("UPDATE SHARE_CAMPAIGN SET status = 'FINISHED' WHERE id = 2");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> dao.buy(openCampaign(CampaignStatus.PHASE_1), LAURA, 10, MOMENT));
        assertEquals("The campaign is not open for purchases (Finished).", exception.getMessage());
        assertEquals(120, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE"));
    }

    // ------------------------------------------------------------------
    // US 35828: external users
    // ------------------------------------------------------------------

    private static final ShareholderRegistration ANA = new ShareholderRegistration("Ana", "López Vidal",
            "ana.lopez@example.com", "12345678Z", "ana_lopez", "secret1");

    @Test
    @DisplayName("the limit of an external user only counts the remaining shares, and only in phase 3")
    void findExternalLimit() throws SQLException {
        CampaignPurchaseLimit inPhase1 = dao.findExternalLimit(2);
        assertTrue(inPhase1.isExternalUser());
        assertEquals(480, inPhase1.getRemainingShares());
        assertEquals(0, inPhase1.getMax());

        TestDatabase.execute("UPDATE SHARE_CAMPAIGN SET status = 'PHASE_3' WHERE id = 2");
        assertEquals(480, dao.findExternalLimit(2).getMax());
    }

    @Test
    @DisplayName("an external user is registered as a shareholder and gets the shares in a new holding")
    void registerAndBuy() throws SQLException {
        TestDatabase.execute("UPDATE SHARE_CAMPAIGN SET status = 'PHASE_3' WHERE id = 2");

        CampaignPurchase purchase = dao.registerAndBuy(openCampaign(CampaignStatus.PHASE_3), ANA, "HASH", 25, MOMENT);

        assertEquals("Ana López Vidal", purchase.getShareholderName());
        assertEquals(25, purchase.getQuantity());
        assertEquals(121, purchase.getFirstShareId());
        assertEquals(145, purchase.getLastShareId());
        assertEquals(new BigDecimal("300.00"), purchase.getTotal());

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHAREHOLDER WHERE first_name = 'Ana' "
                + "AND last_name = 'López Vidal' AND email = 'ana.lopez@example.com' AND national_id = '12345678Z' "
                + "AND username = 'ana_lopez' AND password = 'HASH'"));
        assertEquals(25, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE s JOIN SHARE_HOLDING h ON h.id = s.holding_id "
                + "JOIN SHAREHOLDER sh ON sh.id = h.shareholder_id WHERE sh.username = 'ana_lopez' AND h.campaign_id = 2 "
                + "AND h.purchase_price = 12.0 AND h.purchase_date = '2026-11-20 10:30:00'"));
        assertEquals(455, dao.findExternalLimit(2).getRemainingShares());
    }

    @Test
    @DisplayName("outside phase 3 an external user is not registered and buys nothing")
    void registerAndBuyOutsidePhase3() throws SQLException {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> dao.registerAndBuy(openCampaign(CampaignStatus.PHASE_3), ANA, "HASH", 10, MOMENT));

        assertEquals("External users can buy shares when the campaign reaches phase 3 (now: Phase 1).",
                exception.getMessage());
        assertEquals(3, TestDatabase.queryInt("SELECT COUNT(*) FROM SHAREHOLDER"));
        assertEquals(120, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE"));
    }

    @Test
    @DisplayName("if the national ID is registered meanwhile, nothing is stored (the whole transaction is undone)")
    void registerAndBuyRepeatedNationalId() throws SQLException {
        TestDatabase.execute("UPDATE SHARE_CAMPAIGN SET status = 'PHASE_3' WHERE id = 2");
        ShareholderRegistration repeated = new ShareholderRegistration("Laura", "Pérez", "laura@example.com",
                "11111111h", "laura2", "secret1");

        assertThrows(SQLException.class,
                () -> dao.registerAndBuy(openCampaign(CampaignStatus.PHASE_3), repeated, "HASH", 10, MOMENT));
        assertEquals(3, TestDatabase.queryInt("SELECT COUNT(*) FROM SHAREHOLDER"));
        assertEquals(2, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_HOLDING"));
    }
}
