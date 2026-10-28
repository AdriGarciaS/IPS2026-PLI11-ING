package com.ips2026.pl11.view.history;

import com.ips2026.pl11.model.history.Income;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table of the income operations of the sales history: date and
 * time, type, concept and amount.
 */
public class IncomeTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = { "Date & time", "Type", "Concept", "Amount" };
    private static final Class<?>[] COLUMN_CLASSES = { LocalDateTime.class, String.class, String.class, BigDecimal.class };

    public static final int COLUMN_MOMENT = 0;
    public static final int COLUMN_AMOUNT = 3;

    private transient List<Income> incomes = new ArrayList<>();

    public void setIncomes(List<Income> incomes) {
        this.incomes = new ArrayList<>(incomes);
        fireTableDataChanged();
    }

    public Income getIncomeAt(int modelRow) {
        return incomes.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return incomes.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return COLUMN_CLASSES[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Income income = incomes.get(row);
        return switch (column) {
            case 0 -> income.getMoment();
            case 1 -> income.getType().getLabel();
            case 2 -> income.getConcept();
            case 3 -> income.getAmount();
            default -> throw new IllegalArgumentException("Unknown column " + column);
        };
    }
}
