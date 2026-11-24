package com.ips2026.pl11.view.log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.ips2026.pl11.model.log.LogAction;
import com.ips2026.pl11.model.log.LogRecord;

/**
 * US 36717: read-only table of the records of the system log. The date
 * column keeps the {@link LocalDateTime} (so it sorts by date) and is shown
 * with a renderer.
 */
public class LogRecordsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int COLUMN_DATE = 0;

    private static final String[] COLUMNS = { "Date & time", "User", "Action", "Description", "Session" };
    private static final Class<?>[] TYPES = { LocalDateTime.class, String.class, LogAction.class, String.class,
            Integer.class };

    private transient List<LogRecord> records = new ArrayList<>();

    public void setRecords(List<LogRecord> records) {
        this.records = new ArrayList<>(records);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return records.size();
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
        return TYPES[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int column) {
        LogRecord record = records.get(row);
        switch (column) {
        case 0:
            return record.getLoggedAt();
        case 1:
            return record.getUserLabel();
        case 2:
            return record.getAction();
        case 3:
            return record.getDescription();
        default:
            return record.getSessionId();
        }
    }
}
