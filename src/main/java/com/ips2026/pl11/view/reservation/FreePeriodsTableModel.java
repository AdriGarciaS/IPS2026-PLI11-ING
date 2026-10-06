package com.ips2026.pl11.view.reservation;

import com.ips2026.pl11.model.reservation.TimeSlot;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table model of the free periods of a day: from, to and the
 * maximum whole hours that can be booked in each one.
 */
public class FreePeriodsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = { "From", "To", "Max. hours" };
    private static final Class<?>[] COLUMN_CLASSES = { String.class, String.class, Integer.class };

    private transient List<TimeSlot> periods = new ArrayList<>();

    public void setPeriods(List<TimeSlot> periods) {
        this.periods = new ArrayList<>(periods);
        fireTableDataChanged();
    }

    public TimeSlot getPeriodAt(int modelRow) {
        return periods.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return periods.size();
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
        TimeSlot period = periods.get(row);
        return switch (column) {
            case 0 -> period.getStart().toString();
            case 1 -> period.getEnd().toString();
            case 2 -> period.getWholeHours();
            default -> throw new IllegalArgumentException("Unknown column " + column);
        };
    }
}
