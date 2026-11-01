package com.ips2026.pl11.controller.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.history.IncomeDAO;
import com.ips2026.pl11.model.history.Income;
import com.ips2026.pl11.model.history.IncomeType;
import com.ips2026.pl11.model.history.SaleLine;
import com.ips2026.pl11.model.history.SalesHistory;

/**
 * Tests of the sales history controller with a fake DAO (no database).
 */
@DisplayName("SalesHistoryController")
class SalesHistoryControllerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 28);

    private static final Income SALE = income(IncomeType.MERCHANDISING, "2026-10-27T18:20", "164.78", 7);
    private static final Income TICKETS = income(IncomeType.TICKET, "2026-10-26T10:15", "90.00", 1);
    private static final Income SEASON_TICKET = income(IncomeType.SEASON_TICKET, "2026-10-25T09:45", "350.00", 1);

    private static Income income(IncomeType type, String moment, String amount, int reference) {
        return new Income(type, LocalDateTime.parse(moment), "concept", new BigDecimal(amount), reference);
    }

    /** Fake income: always the same three operations; remembers what it was asked. */
    private static class FakeIncomeDAO extends IncomeDAO {
        LocalDate requestedFrom;
        LocalDate requestedTo;
        int requestedSale = -1;

        @Override
        public List<Income> findBetween(LocalDate from, LocalDate to) {
            requestedFrom = from;
            requestedTo = to;
            return List.of(TICKETS, SEASON_TICKET, SALE);
        }

        @Override
        public List<SaleLine> findSaleLines(int saleNumber) {
            requestedSale = saleNumber;
            return List.of(new SaleLine("Home Shirt 26/27", 2, new BigDecimal("59.99"), new BigDecimal("119.98")));
        }
    }

    private final FakeIncomeDAO dao = new FakeIncomeDAO();
    private final SalesHistoryController controller = new SalesHistoryController(dao, () -> TODAY);

    @Test
    @DisplayName("by default it shows the last 30 days, until today, of every type")
    void defaultIsLast30Days() throws SQLException {
        assertEquals(LocalDate.of(2026, 9, 28), controller.getDefaultFrom());
        assertEquals(TODAY, controller.getDefaultTo());

        SalesHistory history = controller.searchDefault();
        assertEquals(LocalDate.of(2026, 9, 28), dao.requestedFrom);
        assertEquals(TODAY, dao.requestedTo);
        assertEquals(3, history.getIncomes().size());
    }

    @Test
    @DisplayName("the search uses the dates given and shows the newest operations first")
    void searchBetweenDates() throws SQLException {
        SalesHistory history = controller.search(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 27), null);

        assertEquals(LocalDate.of(2026, 10, 1), dao.requestedFrom);
        assertEquals(LocalDate.of(2026, 10, 27), dao.requestedTo);
        assertEquals(LocalDate.of(2026, 10, 1), history.getFrom());
        assertEquals(List.of(SALE, TICKETS, SEASON_TICKET), history.getIncomes());
        assertEquals(new BigDecimal("604.78"), history.getTotal());
    }

    @Test
    @DisplayName("a search of a single day is allowed (From = To)")
    void singleDay() throws SQLException {
        controller.search(TODAY, TODAY, null);
        assertEquals(TODAY, dao.requestedFrom);
        assertEquals(TODAY, dao.requestedTo);
    }

    @Test
    @DisplayName("with a type, only the operations of that type and their total")
    void filterByType() throws SQLException {
        SalesHistory history = controller.search(TODAY.minusDays(10), TODAY, IncomeType.SEASON_TICKET);

        assertEquals(List.of(SEASON_TICKET), history.getIncomes());
        assertEquals(new BigDecimal("350.00"), history.getTotal());
        assertEquals(0, history.getCount(IncomeType.MERCHANDISING));
    }

    @Test
    @DisplayName("both dates are required")
    void datesRequired() {
        assertThrows(IllegalArgumentException.class, () -> controller.search(null, TODAY, null));
        assertThrows(IllegalArgumentException.class, () -> controller.search(TODAY, null, null));
        assertEquals(null, dao.requestedFrom, "the database is not queried");
    }

    @Test
    @DisplayName("'From' cannot be after 'To'")
    void fromAfterTo() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.search(TODAY, TODAY.minusDays(1), null));
        assertTrue(exception.getMessage().contains("From"));
        assertEquals(null, dao.requestedFrom, "the database is not queried");
    }

    @Test
    @DisplayName("the details of a merchandising sale are its products, searched by its sale number")
    void saleDetails() throws SQLException {
        List<SaleLine> lines = controller.getSaleLines(SALE);

        assertEquals(7, dao.requestedSale);
        assertEquals(1, lines.size());
        assertEquals("Home Shirt 26/27", lines.get(0).getProductName());
    }

    @Test
    @DisplayName("the rest of the concepts have no details")
    void noDetailsForOtherConcepts() {
        assertThrows(IllegalArgumentException.class, () -> controller.getSaleLines(TICKETS));
        assertThrows(IllegalArgumentException.class, () -> controller.getSaleLines(SEASON_TICKET));
        assertEquals(-1, dao.requestedSale, "the database is not queried");
    }
}
