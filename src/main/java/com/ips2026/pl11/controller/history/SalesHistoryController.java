package com.ips2026.pl11.controller.history;

import com.ips2026.pl11.data.history.IncomeDAO;
import com.ips2026.pl11.model.history.Income;
import com.ips2026.pl11.model.history.IncomeType;
import com.ips2026.pl11.model.history.SaleLine;
import com.ips2026.pl11.model.history.SalesHistory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

/**
 * Controller of the sales history: the general manager sees the income of
 * the club between two dates (by default, the last 30 days), optionally of
 * one type only, and the products of a merchandising purchase.
 */
public class SalesHistoryController {

    /** Days before today shown when the window is opened or reset. */
    public static final int DEFAULT_DAYS = 30;

    private final IncomeDAO incomeDAO;
    private final Supplier<LocalDate> today;

    public SalesHistoryController(IncomeDAO incomeDAO) {
        this(incomeDAO, LocalDate::now);
    }

    /** @param today gives the current day (the tests use a fixed day) */
    public SalesHistoryController(IncomeDAO incomeDAO, Supplier<LocalDate> today) {
        this.incomeDAO = incomeDAO;
        this.today = today;
    }

    /** First day of the default period: 30 days before today. */
    public LocalDate getDefaultFrom() {
        return today.get().minusDays(DEFAULT_DAYS);
    }

    /** Last day of the default period: today. */
    public LocalDate getDefaultTo() {
        return today.get();
    }

    /** The income of the last 30 days, of every type. */
    public SalesHistory searchDefault() throws SQLException {
        return search(getDefaultFrom(), getDefaultTo(), null);
    }

    /**
     * The income between two dates, both days included.
     *
     * @param type only this type, or null for every type
     * @throws IllegalArgumentException if a date is missing or {@code from} is after {@code to}
     */
    public SalesHistory search(LocalDate from, LocalDate to, IncomeType type) throws SQLException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both dates are required.");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("The 'From' date must be on or before the 'To' date.");
        }
        List<Income> incomes = incomeDAO.findBetween(from, to).stream()
                .filter(income -> type == null || income.getType() == type)
                .toList();
        return new SalesHistory(from, to, incomes);
    }

    /**
     * The products sold in a merchandising purchase.
     *
     * @throws IllegalArgumentException if the income is not a merchandising purchase
     */
    public List<SaleLine> getSaleLines(Income income) throws SQLException {
        if (!income.hasDetails()) {
            throw new IllegalArgumentException("Only merchandising sales have details.");
        }
        return incomeDAO.findSaleLines(income.getReference());
    }
}
