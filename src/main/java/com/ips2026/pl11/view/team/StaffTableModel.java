package com.ips2026.pl11.view.team;

import com.ips2026.pl11.model.team.StaffAssignment;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table model of the extra technical staff of the new team: name
 * and task.
 */
public class StaffTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = { "Name", "Task" };

    private transient List<StaffAssignment> staff = new ArrayList<>();

    public void setStaff(List<StaffAssignment> staff) {
        this.staff = new ArrayList<>(staff);
        fireTableDataChanged();
    }

    public StaffAssignment getAssignmentAt(int modelRow) {
        return staff.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return staff.size();
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
        StaffAssignment assignment = staff.get(row);
        return column == 0 ? assignment.getEmployee().getFullName() : assignment.getTask();
    }
}
