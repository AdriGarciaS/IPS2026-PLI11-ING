package com.ips2026.pl11.data.shareholder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.shareholder.MarketPrice;
import com.ips2026.pl11.model.shareholder.ShareHolding;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * Tests of ShareholderDAO, ShareHoldingDAO and MarketPriceDAO on a temporary
 * SQLite database created with the real schema.sql.
 */
@DisplayName("Shareholder DAOs")
class ShareholderDAOsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 22);

    @TempDir
    Path folder;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
        TestDatabase.execute("INSERT INTO SHAREHOLDER (id, first_name, last_name, national_id) VALUES "
                + "(1, 'Laura', 'Pérez Gil', '11111111H'), (2, 'Marcos', 'Ruiz Soto', '22222222J'), "
                + "(3, 'Irene', 'Castillo Mora', '33333333P')");
        TestDatabase.execute("INSERT INTO SHARE_CAMPAIGN (id, name, nominal_price, total_shares, status) VALUES "
                + "(1, 'Founding shares 2023', 9.5, 1000, 'FINISHED'), "
                + "(2, 'Capital increase 2024', 10.0, 1000, 'FINISHED')");
        // Laura: holding 10 (2023, IDs 1-3 and 7-8) and holding 11 (2024, IDs 20-21). Marcos: holding 12.
        TestDatabase.execute("INSERT INTO SHARE_HOLDING (id, shareholder_id, campaign_id, purchase_date, purchase_price) VALUES "
                + "(11, 1, 2, '2024-03-12', 10.0), (10, 1, 1, '2023-02-10', 9.5), (12, 2, 2, '2024-04-02', 15.0)");
        TestDatabase.execute("INSERT INTO SHARE (id, holding_id) VALUES "
                + "(8, 10), (1, 10), (3, 10), (2, 10), (7, 10), (20, 11), (21, 11), (30, 12)");
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    private static int priceCount() throws SQLException {
        return TestDatabase.queryInt("SELECT COUNT(*) FROM MARKET_PRICE");
    }

    // ------------------------------------------------------------------
    // ShareholderDAO
    // ------------------------------------------------------------------

    @Test
    @DisplayName("ShareholderDAO returns every shareholder ordered by surname")
    void shareholdersOrderedBySurname() throws SQLException {
        List<Shareholder> shareholders = new ShareholderDAO().findAll();

        assertEquals(List.of("Irene Castillo Mora", "Laura Pérez Gil", "Marcos Ruiz Soto"),
                shareholders.stream().map(Shareholder::getFullName).toList());
        assertEquals("33333333P", shareholders.get(0).getNationalId());
    }

    // ------------------------------------------------------------------
    // ShareHoldingDAO
    // ------------------------------------------------------------------

    @Test
    @DisplayName("ShareHoldingDAO returns one holding per campaign with all its share IDs, in order")
    void holdingsWithTheirShareIds() throws SQLException {
        List<ShareHolding> holdings = new ShareHoldingDAO().findByShareholder(1);

        assertEquals(2, holdings.size());
        ShareHolding founding = holdings.get(0); // ordered by purchase date
        assertEquals(10, founding.getId());
        assertEquals("Founding shares 2023", founding.getCampaignName());
        assertEquals(LocalDate.of(2023, 2, 10), founding.getPurchaseDate());
        assertEquals(new BigDecimal("9.50"), founding.getPurchasePrice());
        assertEquals(List.of(1, 2, 3, 7, 8), founding.getShareIds());
        assertEquals(5, founding.getQuantity());
        assertEquals("1 - 3, 7 - 8", founding.getShareIdRanges());

        assertEquals("Capital increase 2024", holdings.get(1).getCampaignName());
        assertEquals(List.of(20, 21), holdings.get(1).getShareIds());
    }

    @Test
    @DisplayName("ShareHoldingDAO does not mix the shares of other shareholders")
    void onlySharesOfThatShareholder() throws SQLException {
        List<ShareHolding> holdings = new ShareHoldingDAO().findByShareholder(2);

        assertEquals(1, holdings.size());
        assertEquals(List.of(30), holdings.get(0).getShareIds());
    }

    @Test
    @DisplayName("a shareholder without shares has no holdings")
    void shareholderWithoutShares() throws SQLException {
        assertTrue(new ShareHoldingDAO().findByShareholder(3).isEmpty());
    }

    @Test
    @DisplayName("the database allows only one holding per shareholder and campaign")
    void oneHoldingPerCampaign() {
        assertThrows(SQLException.class, () -> TestDatabase.execute("INSERT INTO SHARE_HOLDING "
                + "(shareholder_id, campaign_id, purchase_date, purchase_price) VALUES (1, 1, '2023-03-01', 9.8)"));
    }

    @Test
    @DisplayName("the database does not allow two shares with the same ID")
    void shareIdsAreUnique() {
        assertThrows(SQLException.class, () -> TestDatabase.execute("INSERT INTO SHARE (id, holding_id) VALUES (1, 12)"));
    }

    // ------------------------------------------------------------------
    // MarketPriceDAO
    // ------------------------------------------------------------------

    @Test
    @DisplayName("the price of today is created only once, even if the application starts several times")
    void todayPriceCreatedOnce() throws SQLException {
        TestDatabase.execute("INSERT INTO MARKET_PRICE (price_date, price) VALUES ('2026-10-21', 12.30)");
        MarketPriceDAO dao = new MarketPriceDAO();

        assertTrue(dao.ensurePriceFor(TODAY, new Random(1)));
        BigDecimal first = dao.findLatest(1).get(0).getPrice();
        assertFalse(dao.ensurePriceFor(TODAY, new Random(2)));
        assertFalse(dao.ensurePriceFor(TODAY, new Random(3)));

        assertEquals(2, priceCount());
        assertEquals(first, dao.findLatest(1).get(0).getPrice(), "the price of today must not change");
    }

    @Test
    @DisplayName("the new price is at most 5 % up or down from the last price")
    void newPriceWithinFivePercent() throws SQLException {
        TestDatabase.execute("INSERT INTO MARKET_PRICE (price_date, price) VALUES "
                + "('2026-10-19', 12.10), ('2026-10-20', 12.25), ('2026-10-21', 12.30)");

        new MarketPriceDAO().ensurePriceFor(TODAY, new Random(5));

        MarketPrice today = new MarketPriceDAO().findLatest(1).get(0);
        assertEquals(TODAY, today.getDate());
        assertTrue(today.getPrice().compareTo(new BigDecimal("11.68")) >= 0
                && today.getPrice().compareTo(new BigDecimal("12.92")) <= 0, () -> "out of range: " + today.getPrice());
    }

    @Test
    @DisplayName("without any previous price, the first one starts from 12.00 (at most 5 % up or down)")
    void firstPriceEver() throws SQLException {
        new MarketPriceDAO().ensurePriceFor(TODAY, new Random(8));

        BigDecimal price = new MarketPriceDAO().findLatest(1).get(0).getPrice();
        assertEquals(1, priceCount());
        assertTrue(price.compareTo(new BigDecimal("11.40")) >= 0 && price.compareTo(new BigDecimal("12.60")) <= 0);
    }

    @Test
    @DisplayName("findLatest returns the most recent prices, the newest first")
    void latestPricesNewestFirst() throws SQLException {
        TestDatabase.execute("INSERT INTO MARKET_PRICE (price_date, price) VALUES "
                + "('2026-10-21', 12.30), ('2026-10-19', 12.10), ('2026-10-20', 12.25)");

        List<MarketPrice> latest = new MarketPriceDAO().findLatest(2);

        assertEquals(2, latest.size());
        assertEquals(LocalDate.of(2026, 10, 21), latest.get(0).getDate());
        assertEquals(new BigDecimal("12.30"), latest.get(0).getPrice());
        assertEquals(LocalDate.of(2026, 10, 20), latest.get(1).getDate());
    }

    @Test
    @DisplayName("the database allows only one price per day")
    void onePricePerDay() throws SQLException {
        TestDatabase.execute("INSERT INTO MARKET_PRICE (price_date, price) VALUES ('2026-10-21', 12.30)");

        assertThrows(SQLException.class,
                () -> TestDatabase.execute("INSERT INTO MARKET_PRICE (price_date, price) VALUES ('2026-10-21', 13.00)"));
    }
}
