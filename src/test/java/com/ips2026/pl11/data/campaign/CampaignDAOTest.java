package com.ips2026.pl11.data.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignStatus;

/**
 * Tests of CampaignDAO on a temporary SQLite database created with the real
 * schema.sql. It starts with one finished campaign.
 */
@DisplayName("CampaignDAO")
class CampaignDAOTest {

    @TempDir
    Path folder;

    private final CampaignDAO dao = new CampaignDAO();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
        TestDatabase.execute("INSERT INTO SHARE_CAMPAIGN (id, name, nominal_price, total_shares, status) VALUES "
                + "(1, 'Capital increase 2024', 10.0, 1000, 'FINISHED')");
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    @Test
    @DisplayName("a new campaign is saved inactive with its data, and no share is created")
    void insertSavesInactiveCampaign() throws SQLException {
        Campaign campaign = dao.insert("Capital increase 2027", new BigDecimal("12.50"), 5000);

        assertEquals(CampaignStatus.INACTIVE, campaign.getStatus());
        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE_CAMPAIGN WHERE id = " + campaign.getId()
                + " AND name = 'Capital increase 2027' AND nominal_price = 12.5 AND total_shares = 5000"
                + " AND status = 'INACTIVE'"));
        assertEquals(0, TestDatabase.queryInt("SELECT COUNT(*) FROM SHARE"));
    }

    @Test
    @DisplayName("findOpen ignores finished campaigns and finds the inactive one")
    void findOpen() throws SQLException {
        assertTrue(dao.findOpen().isEmpty());

        dao.insert("Capital increase 2027", new BigDecimal("12.50"), 5000);
        Campaign open = dao.findOpen().orElseThrow();
        assertEquals("Capital increase 2027", open.getName());
        assertEquals(0, new BigDecimal("12.50").compareTo(open.getNominalPrice()));
        assertEquals(5000, open.getTotalShares());
        assertEquals(CampaignStatus.INACTIVE, open.getStatus());
    }

    @Test
    @DisplayName("findOpen also finds a campaign in a phase")
    void findOpenInPhase() throws SQLException {
        TestDatabase.execute("INSERT INTO SHARE_CAMPAIGN (name, nominal_price, total_shares, status) VALUES "
                + "('Capital increase 2026', 14.0, 1000, 'PHASE_3')");
        assertEquals(CampaignStatus.PHASE_3, dao.findOpen().orElseThrow().getStatus());
    }

    @Test
    @DisplayName("existsName ignores upper/lower case")
    void existsName() throws SQLException {
        assertTrue(dao.existsName("Capital increase 2024"));
        assertTrue(dao.existsName("CAPITAL INCREASE 2024"));
        assertFalse(dao.existsName("Capital increase 2027"));
    }

    @Test
    @DisplayName("the database allows only one campaign that is not finished")
    void onlyOneOpenCampaign() throws SQLException {
        dao.insert("Capital increase 2027", new BigDecimal("12.50"), 5000);

        assertThrows(SQLException.class, () -> dao.insert("Capital increase 2028", new BigDecimal("13.00"), 100));
        // Finished campaigns can be many
        TestDatabase.execute("INSERT INTO SHARE_CAMPAIGN (name, nominal_price, total_shares, status) VALUES "
                + "('Founding shares 2023', 9.5, 1000, 'FINISHED')");
    }

    @Test
    @DisplayName("the database does not allow two campaigns with the same name, ignoring case")
    void uniqueName() {
        assertThrows(SQLException.class, () -> TestDatabase.execute("INSERT INTO SHARE_CAMPAIGN "
                + "(name, nominal_price, total_shares, status) VALUES ('capital INCREASE 2024', 9.5, 10, 'FINISHED')"));
    }

    @Test
    @DisplayName("the database rejects a price or a number of shares that is not positive, and unknown states")
    void databaseChecks() {
        assertThrows(SQLException.class, () -> dao.insert("Zero price", BigDecimal.ZERO, 10));
        assertThrows(SQLException.class, () -> dao.insert("Zero shares", BigDecimal.TEN, 0));
        assertThrows(SQLException.class, () -> TestDatabase.execute("INSERT INTO SHARE_CAMPAIGN "
                + "(name, nominal_price, total_shares, status) VALUES ('Bad', 9.5, 10, 'ACTIVE')"));
    }
}
