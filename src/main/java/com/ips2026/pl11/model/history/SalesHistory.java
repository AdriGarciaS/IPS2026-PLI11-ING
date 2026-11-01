package com.ips2026.pl11.model.history;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Income operations between two dates (both days included), sorted from the
 * newest to the oldest, with the totals of the summary.
 */
public class SalesHistory {

    private final LocalDate from;
    private final LocalDate to;
    private final List<Income> incomes;

    public SalesHistory(LocalDate from, LocalDate to, List<Income> incomes) {
        this.from = from;
        this.to = to;
        this.incomes = incomes.stream()
                .sorted(Comparator.comparing(Income::getMoment).reversed())
                .toList();
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    /** The operations, newest first. */
    public List<Income> getIncomes() {
        return incomes;
    }

    /** Total income of all the operations. */
    public BigDecimal getTotal() {
        return incomes.stream().map(Income::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Total income of the operations of one type. */
    public BigDecimal getTotal(IncomeType type) {
        return incomes.stream().filter(income -> income.getType() == type)
                .map(Income::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Number of operations of one type. */
    public int getCount(IncomeType type) {
        return (int) incomes.stream().filter(income -> income.getType() == type).count();
    }
}
