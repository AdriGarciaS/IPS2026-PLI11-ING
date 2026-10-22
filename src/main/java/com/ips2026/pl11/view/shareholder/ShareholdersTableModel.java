package com.ips2026.pl11.view.shareholder;

import com.ips2026.pl11.model.shareholder.Shareholder;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table model of the shareholders found in the portal: name and
 * national ID.
 */
public class ShareholdersTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = { "Name", "National ID" };

    private transient List<Shareholder> shareholders = new ArrayList<>();

    public void setShareholders(List<Shareholder> shareholders) {
        this.shareholders = new ArrayList<>(shareholders);
        fireTableDataChanged();
    }

    public Shareholder getShareholderAt(int modelRow) {
        return shareholders.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return shareholders.size();
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
    public Object getValueAt(int row, int column) {
        Shareholder shareholder = shareholders.get(row);
        return column == 0 ? shareholder.getFullName() : shareholder.getNationalId();
    }
}
