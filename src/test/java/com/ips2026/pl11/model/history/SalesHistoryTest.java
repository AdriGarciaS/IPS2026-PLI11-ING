package com.ips2026.pl11.model.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests of the sales history model: order of the operations and totals.
 */
@DisplayName("SalesHistory")
class SalesHistoryTest {

    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 30);

    private static Income income(IncomeType type, String moment, String amount) {
        return new Income(type, LocalDateTime.parse(moment), "concept", new BigDecimal(amount), 1);
    }

    private static SalesHistory sample() {
        return new SalesHistory(FROM, TO, List.of(
                income(IncomeType.TICKET, "2026-10-05T10:15", "90.00"),
                income(IncomeType.MERCHANDISING, "2026-10-20T18:20", "164.78"),
                income(IncomeType.TICKET, "2026-10-20T18:21", "30.00"),
                income(IncomeType.CAMPAIGN, "2026-10-02T12:30", "3550.00"),
                income(IncomeType.MERCHANDISING, "2026-10-12T11:05", "29.95")));
    }

    @Test
    @DisplayName("the operations are sorted from the newest to the oldest, down to the minute")
    void newestFirst() {
        List<Income> incomes = sample().getIncomes();

        assertEquals(LocalDateTime.parse("2026-10-20T18:21"), incomes.get(0).getMoment());
        assertEquals(LocalDateTime.parse("2026-10-20T18:20"), incomes.get(1).getMoment());
        assertEquals(LocalDateTime.parse("2026-10-12T11:05"), incomes.get(2).getMoment());
        assertEquals(LocalDateTime.parse("2026-10-05T10:15"), incomes.get(3).getMoment());
        assertEquals(LocalDateTime.parse("2026-10-02T12:30"), incomes.get(4).getMoment());
    }

    @Test
    @DisplayName("the total is the sum of every operation")
    void total() {
        assertEquals(new BigDecimal("3864.73"), sample().getTotal());
    }

    @Test
    @DisplayName("the total and the number of operations of each type")
    void totalsByType() {
        SalesHistory history = sample();

        assertEquals(new BigDecimal("194.73"), history.getTotal(IncomeType.MERCHANDISING));
        assertEquals(2, history.getCount(IncomeType.MERCHANDISING));
        assertEquals(new BigDecimal("120.00"), history.getTotal(IncomeType.TICKET));
        assertEquals(2, history.getCount(IncomeType.TICKET));
        assertEquals(new BigDecimal("3550.00"), history.getTotal(IncomeType.CAMPAIGN));
        assertEquals(1, history.getCount(IncomeType.CAMPAIGN));
    }

    @Test
    @DisplayName("a type without operations has 0 operations and 0 €")
    void typeWithoutOperations() {
        SalesHistory history = sample();

        assertEquals(0, history.getCount(IncomeType.SEASON_TICKET));
        assertEquals(0, BigDecimal.ZERO.compareTo(history.getTotal(IncomeType.SEASON_TICKET)));
    }

    @Test
    @DisplayName("an empty period has no operations and a total of 0 €")
    void emptyPeriod() {
        SalesHistory history = new SalesHistory(FROM, TO, List.of());

        assertTrue(history.getIncomes().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(history.getTotal()));
        assertEquals(FROM, history.getFrom());
        assertEquals(TO, history.getTo());
    }

    @Test
    @DisplayName("only merchandising sales have details")
    void onlyMerchandisingHasDetails() {
        for (IncomeType type : IncomeType.values()) {
            assertEquals(type == IncomeType.MERCHANDISING, income(type, "2026-10-05T10:15", "1").hasDetails(), type.name());
        }
        assertFalse(income(IncomeType.TICKET, "2026-10-05T10:15", "1").hasDetails());
    }
}
