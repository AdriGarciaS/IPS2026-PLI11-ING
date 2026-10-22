package com.ips2026.pl11.controller.shareholder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.shareholder.MarketPriceDAO;
import com.ips2026.pl11.data.shareholder.ShareHoldingDAO;
import com.ips2026.pl11.model.shareholder.MarketPrice;
import com.ips2026.pl11.model.shareholder.ShareHolding;
import com.ips2026.pl11.model.shareholder.Shareholder;
import com.ips2026.pl11.model.shareholder.SharesWallet;

/**
 * Tests of the shares wallet controller with fake DAOs (no database).
 */
@DisplayName("SharesWalletController")
class SharesWalletControllerTest {

    private static final Shareholder LAURA = new Shareholder(1, "Laura", "Pérez Gil", "11111111H");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 22);

    /** Fake holdings: shareholder 1 has 2 holdings, the rest none. */
    private static class FakeHoldingDAO extends ShareHoldingDAO {
        int requestedShareholder = -1;

        @Override
        public List<ShareHolding> findByShareholder(int shareholderId) {
            requestedShareholder = shareholderId;
            if (shareholderId != 1) {
                return List.of();
            }
            return List.of(
                    new ShareHolding(1, "Founding shares 2023", LocalDate.of(2023, 2, 10), new BigDecimal("9.50"), range(1, 500)),
                    new ShareHolding(2, "Capital increase 2024", LocalDate.of(2024, 3, 12), new BigDecimal("10.00"), range(1001, 1300)));
        }
    }

    /** Fake prices: returns the prices it was given, the newest first. */
    private static class FakePriceDAO extends MarketPriceDAO {
        final List<MarketPrice> prices = new ArrayList<>();

        @Override
        public List<MarketPrice> findLatest(int count) {
            return prices.subList(0, Math.min(count, prices.size()));
        }
    }

    private static List<Integer> range(int from, int to) {
        List<Integer> ids = new ArrayList<>();
        for (int id = from; id <= to; id++) {
            ids.add(id);
        }
        return ids;
    }

    @Test
    @DisplayName("the wallet has the holdings of the shareholder, valued with the latest price")
    void loadsWalletWithLatestPrice() throws SQLException {
        FakeHoldingDAO holdingDAO = new FakeHoldingDAO();
        FakePriceDAO priceDAO = new FakePriceDAO();
        priceDAO.prices.add(new MarketPrice(TODAY, new BigDecimal("12.50")));
        priceDAO.prices.add(new MarketPrice(TODAY.minusDays(1), new BigDecimal("12.30")));

        SharesWallet wallet = new SharesWalletController(holdingDAO, priceDAO).loadWallet(LAURA);

        assertEquals(1, holdingDAO.requestedShareholder);
        assertEquals(LAURA, wallet.getShareholder());
        assertEquals(2, wallet.getHoldings().size());
        assertEquals(TODAY, wallet.getCurrentPrice().getDate());
        assertEquals(TODAY.minusDays(1), wallet.getPreviousPrice().getDate());
        assertEquals(0, new BigDecimal("10000.00").compareTo(wallet.getTotalValue()));
    }

    @Test
    @DisplayName("with only one price there is no previous price")
    void onlyOnePrice() throws SQLException {
        FakePriceDAO priceDAO = new FakePriceDAO();
        priceDAO.prices.add(new MarketPrice(TODAY, new BigDecimal("12.50")));

        SharesWallet wallet = new SharesWalletController(new FakeHoldingDAO(), priceDAO).loadWallet(LAURA);

        assertNull(wallet.getPreviousPrice());
        assertNull(wallet.getDailyChangePercent());
    }

    @Test
    @DisplayName("without any market price the wallet can not be valued")
    void noPriceAtAll() {
        SharesWalletController controller = new SharesWalletController(new FakeHoldingDAO(), new FakePriceDAO());

        assertThrows(IllegalStateException.class, () -> controller.loadWallet(LAURA));
    }

    @Test
    @DisplayName("a shareholder without shares gets an empty wallet")
    void shareholderWithoutShares() throws SQLException {
        FakePriceDAO priceDAO = new FakePriceDAO();
        priceDAO.prices.add(new MarketPrice(TODAY, new BigDecimal("12.50")));

        SharesWallet wallet = new SharesWalletController(new FakeHoldingDAO(), priceDAO)
                .loadWallet(new Shareholder(4, "Javier", "Ortega Lima", "44444444A"));

        assertEquals(0, wallet.getHoldings().size());
        assertEquals(0, BigDecimal.ZERO.compareTo(wallet.getTotalValue()));
    }
}
