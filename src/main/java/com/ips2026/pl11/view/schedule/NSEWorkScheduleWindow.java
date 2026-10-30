package com.ips2026.pl11.view.schedule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.controller.schedule.DayScheduleController;
import com.ips2026.pl11.controller.schedule.WorkScheduleController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.schedule.DayScheduleDAO;
import com.ips2026.pl11.data.schedule.WorkScheduleDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.schedule.DaySchedule;
import com.ips2026.pl11.model.schedule.WorkSchedule;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class NSEWorkScheduleWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    private static final String[] DAYS = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
        "Sunday" };

    private static final String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
        "Nov", "Dec" };

    private static final String RECURRING = "Weekly";

    private WorkScheduleDAO wsDAO = new WorkScheduleDAO();
    private WorkScheduleController wsController = new WorkScheduleController(wsDAO);
    private DayScheduleController dsController = new DayScheduleController(new DayScheduleDAO(), wsDAO);
    private EmployeeController eController = new EmployeeController(new EmployeeDAO());

    private List<WorkSchedule> schedulesList = new ArrayList<>();
    private List<DaySchedule> daySchedulesList = new ArrayList<>();
    private List<Employee> workersList = new ArrayList<>();

    private List<String> occupiedShiftCells = new ArrayList<>();

    private ScheduleWindow wsw;
    private HeaderPanel pnHeader;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JPanel pnButtons;
    private JTextField txtWorkerId;
    private JComboBox cbMonth;
    private JSpinner spStart;
    private JSpinner spEnd;
    private JLabel lblWorkers;
    private JLabel lblSelectedWorkerId;
    private JButton btnReturn;
    private JButton btnModify;
    private JScrollPane spnShifts;
    private JTable tableShifts;

    private DefaultTableModel shiftsModel = new DefaultTableModel(new Object[][] {}, DAYS) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private DefaultTableModel workersModel = new DefaultTableModel(new Object[][] {},
        new String[] { "ID", "Name", "Surname", "Position" }) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private DefaultTableModel hoursModel = new DefaultTableModel(new Object[][] {}, new String[] { "Time" }) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private DefaultTableCellRenderer shiftsTextRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            c.setForeground(Color.WHITE);

            if (occupiedShiftCells.contains(row + "," + column)) {
                if (row % 2 == 0) {
                    c.setBackground(new Color(80, 103, 173));
                } else {
                    c.setBackground(new Color(46, 66, 128));
                }

            } else {
                c.setBackground(tableShifts.getBackground());
            }

            return c;
        }
    };

    private JScrollPane spnWorkers;
    private JTable tableWorkers;
    private JLabel lSelectedWorkerId;
    private JComboBox cbDay;
    private JComboBox cbYear;
    private JLabel lblDate;
    private JTextField txtWeek;
    private JLabel lblWeek;
    private JPanel pnHours;
    private JTable tableHours;
    private JScrollPane spnHours;

    /**
     * Create the frame.
     */
    public NSEWorkScheduleWindow(ScheduleWindow workScheduleWindow) {
        setResizable(false);
        setTitle("Football Club Management");
        this.wsw = workScheduleWindow;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1067, 611);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        contentPane.add(getLblFooter(), BorderLayout.SOUTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
        rootPane.setDefaultButton(btnModify);
        setLocationRelativeTo(wsw);

        initializeShiftsTable();
        initializeWorkersTable();

    }

    //
    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Non-sporting employees schedule viewer", "Consult any of your weekly schedules",
                false);
            pnHeader.setMinimumSize(new Dimension(564, 76));
            pnHeader.setPreferredSize(new Dimension(564, 76));
        }
        return pnHeader;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel();
            pnFooter.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
            pnFooter.setLayout(new BorderLayout(0, 0));
        }
        return pnFooter;
    }

    private JLabel getLblFooter() {
        if (lblFooter == null) {
            lblFooter = new JLabel("IPS 2026 - Team PL11");
            lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
            lblFooter.setForeground(Color.GRAY);
            lblFooter.setFont(new Font("Tahoma", Font.PLAIN, 11));
            lblFooter.setBorder(new EmptyBorder(5, 0, 0, 0));
        }
        return lblFooter;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setLayout(null);
            pnButtons.add(getTxtWorkerId());
            pnButtons.add(getLblWorkers());
            pnButtons.add(getBtnReturn());
            pnButtons.add(getSpnShifts());
            pnButtons.add(getSpnWorkers());
            pnButtons.add(getLblSelectedWorkerId());
            pnButtons.add(getLblDate());
            pnButtons.add(getCbYear());
            pnButtons.add(getCbMonth());
            pnButtons.add(getCbDay());
            pnButtons.add(getTxtWeek());
            pnButtons.add(getLblWeek());
            pnButtons.add(getSpnHours());
        }
        return pnButtons;
    }

    private JTextField getTxtWorkerId() {
        if (txtWorkerId == null) {
            txtWorkerId = new JTextField();
            txtWorkerId.setEditable(false);
            txtWorkerId.setBounds(217, 228, 63, 25);
            txtWorkerId.setColumns(10);
        }
        return txtWorkerId;
    }

    private JComboBox getCbYear() {
        if (cbYear == null) {
            cbYear = new JComboBox(years());
            cbYear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    refreshMonths();
                    refreshDays();
                    displaySelectedWeek();
                    refreshShiftsTable();
                }
            });
            cbYear.setBounds(149, 300, 63, 25);
        }
        return cbYear;
    }

    private JComboBox getCbMonth() {
        if (cbMonth == null) {
            cbMonth = new JComboBox(determineMonths());
            cbMonth.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    refreshDays();
                    displaySelectedWeek();
                    refreshShiftsTable();
                }
            });
            cbMonth.setBounds(217, 300, 63, 25);
        }
        return cbMonth;
    }

    private JComboBox getCbDay() {
        if (cbDay == null) {
            cbDay = new JComboBox(determineDays());
            cbDay.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    displaySelectedWeek();
                    refreshShiftsTable();
                }
            });
            cbDay.setBounds(286, 300, 63, 25);
        }
        return cbDay;
    }

    private JLabel getLblWorkers() {
        if (lblWorkers == null) {
            lblWorkers = new JLabel("Workers:");
            lblWorkers.setLabelFor(getSpnWorkers());
            lblWorkers.setDisplayedMnemonic('W');
            lblWorkers.setBounds(31, 23, 63, 23);
        }
        return lblWorkers;
    }

    private JButton getBtnReturn() {
        if (btnReturn == null) {
            btnReturn = new JButton("Return");
            btnReturn.setMnemonic('R');
            btnReturn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            btnReturn.setBackground(new Color(240, 240, 240));
            btnReturn.setFont(new Font("Tahoma", Font.PLAIN, 11));
            btnReturn.setBounds(933, 435, 85, 27);
        }
        return btnReturn;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JScrollPane getSpnShifts() {
        if (spnShifts == null) {
            spnShifts = new JScrollPane();
            spnShifts.setBounds(487, 11, 530, 413);
            spnShifts.setViewportView(getTableShifts());
        }
        return spnShifts;
    }

    private JTable getTableShifts() {
        if (tableShifts == null) {
            tableShifts = new JTable();
            tableShifts.setFont(new Font("Tahoma", Font.PLAIN, 11));
            tableShifts.setModel(shiftsModel);
            tableShifts.setShowVerticalLines(true);
            tableShifts.setGridColor(Color.WHITE);
            tableShifts.getTableHeader().setResizingAllowed(false);
            tableShifts.getTableHeader().setEnabled(false);
            centerShiftValues();

        }
        return tableShifts;
    }

    private void centerShiftValues() {
        shiftsTextRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < tableShifts.getModel().getColumnCount(); i++) {
            tableShifts.getColumnModel().getColumn(i).setCellRenderer(shiftsTextRenderer);
        }
    }

    private JTable getTableWorkers() {
        if (tableWorkers == null) {
            tableWorkers = new JTable();
            tableWorkers.setModel(workersModel);
            tableWorkers.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = tableWorkers.getSelectedRow();

                    if (row != -1) {
                        int id = (int) tableWorkers.getValueAt(row, 0);
                        getTxtWorkerId().setText(String.valueOf(id));
                        refreshShiftsTable();
                    }
                }
            });
            tableWorkers.getColumnModel().getColumn(0).setPreferredWidth(18);
            tableWorkers.getTableHeader().setResizingAllowed(false);
            tableWorkers.getTableHeader().setEnabled(false);
            tableWorkers.getColumnModel().getColumn(2).setPreferredWidth(50);
            tableWorkers.getColumnModel().getColumn(1).setPreferredWidth(50);
        }
        return tableWorkers;
    }

    private JScrollPane getSpnHours() {
        if (spnHours == null) {
            spnHours = new JScrollPane();
            spnHours.setFocusable(false);
            spnHours.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            spnHours.setBounds(402, 11, 75, 413);
            spnHours.setViewportView(getTableHours());
        }
        return spnHours;
    }

    private JTable getTableHours() {
        if (tableHours == null) {
            tableHours = new JTable();
            tableHours.setModel(hoursModel);
            fillHoursTable();
        }
        return tableHours;
    }

    private void fillHoursTable() {
        int hour = 0;
        for (int i = 0; i < 24; i++) {
            if (hour < 10) {
                hoursModel.addRow(new Object[] { "0" + hour + ":00" });
            } else {
                hoursModel.addRow(new Object[] { hour + ":00" });
            }
            hour++;

        }
    }

    private JScrollPane getSpnWorkers() {
        if (spnWorkers == null) {
            spnWorkers = new JScrollPane();
            spnWorkers.setBounds(31, 49, 350, 158);
            spnWorkers.setViewportView(getTableWorkers());
        }
        return spnWorkers;
    }

    private JLabel getLblSelectedWorkerId() {
        if (lblSelectedWorkerId == null) {
            lblSelectedWorkerId = new JLabel("Selected Worker ID:");
            lblSelectedWorkerId.setLabelFor(getTxtWorkerId());
            lblSelectedWorkerId.setBounds(105, 233, 108, 14);
        }
        return lblSelectedWorkerId;
    }

    private JLabel getLblDate() {
        if (lblDate == null) {
            lblDate = new JLabel("Date:");
            lblDate.setBounds(105, 305, 48, 14);
        }
        return lblDate;
    }

    //////////////////////////////////////// METHODS   /////////////////////////////////////////////////////////////////////////////////////

    public void refreshShiftsTable() {
        try {
            occupiedShiftCells.clear();

            WeekFields wf = WeekFields.ISO;
            DefaultTableModel model = (DefaultTableModel) tableShifts.getModel();
            List<Long> overridenSchedulesIds = new ArrayList<>();
            int selectedEmployeeId = Integer.parseInt(txtWorkerId.getText());

            model.setRowCount(0);
            model.setRowCount(24);
            for (DaySchedule ds : dsController.getAllSchedules()) {
                if (scheduleBelongsToSelectedWeek(ds, wf)
                    && wsController.getWorkScheduleById(ds.getWorkScheduleId()).getEmployeeId() == selectedEmployeeId) {

                    int dayOfWeek = ds.getDate().getDayOfWeek().getValue() - 1;
                    int entryHour = ds.getStartTime().getHour();
                    int entryMinute = ds.getStartTime().getMinute();
                    int exitHour = ds.getEndTime().getHour();
                    int exitMinute = ds.getEndTime().getMinute();

                    for (int i = entryHour; i <= exitHour; i++) {
                        if (i == entryHour) {
                            if (entryHour == exitHour) {
                                model.setValueAt(ds.getStartTime() + "/" + exitMinute, i, dayOfWeek);
                            } else {
                                model.setValueAt(ds.getStartTime(), i, dayOfWeek);
                            }
                            occupiedShiftCells.add(i + "," + dayOfWeek);
                        } else if (i == exitHour) {
                            model.setValueAt(ds.getEndTime(), i, dayOfWeek);
                            occupiedShiftCells.add(i + "," + dayOfWeek);
                        } else {
                            occupiedShiftCells.add(i + "," + dayOfWeek);

                        }
                    }

                    overridenSchedulesIds.add(ds.getWorkScheduleId());
                }
            }

            for (WorkSchedule s : wsController.getWorkSchedules()) {
                if (!overridenSchedulesIds.contains(s.getId()) && s.getEmployeeId() == selectedEmployeeId) {

                    int dayOfWeek = s.getWeekDay() - 1;
                    int entryHour = s.getStartTime().getHour();
                    int entryMinute = s.getStartTime().getMinute();
                    int exitHour = s.getEndTime().getHour();
                    int exitMinute = s.getEndTime().getMinute();

                    for (int i = entryHour; i <= exitHour; i++) {
                        if (i == entryHour) {
                            if (entryHour == exitHour) {
                                model.setValueAt(s.getStartTime() + "/"  + exitMinute, i, dayOfWeek);
                            } else {
                                model.setValueAt(s.getStartTime(), i, dayOfWeek);
                            }
                            occupiedShiftCells.add(i + "," + dayOfWeek);

                        } else if (i == exitHour) {
                            model.setValueAt(s.getEndTime(), i, dayOfWeek);
                            occupiedShiftCells.add(i + "," + dayOfWeek);

                        } else {
                            if (!(exitMinute == 0 && i == exitHour)) {
                                occupiedShiftCells.add(i + "," + dayOfWeek);
                            }
                        }

                    }
                }
            }

        } catch (NumberFormatException e) {
            showError("Employee id must be a number");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private boolean scheduleBelongsToSelectedWeek(DaySchedule ds, WeekFields wf) {
        LocalDate date = LocalDate.of((Integer) cbYear.getSelectedItem(),
            obtainMonthNumber((String) cbMonth.getSelectedItem()), (Integer) cbDay.getSelectedItem());
        LocalDate scheduleDate = ds.getDate();

        return date.get(wf.weekOfWeekBasedYear()) == scheduleDate.get(wf.weekOfWeekBasedYear())
            && date.get(wf.weekBasedYear()) == scheduleDate.get(wf.weekBasedYear());
    }

    private void initializeShiftsTable() {
        for (int i = 0; i < 23; i++) {
            shiftsModel.addRow(new Object[] {});
        }
    }

    private void initializeWorkersTable() {
        try {
            workersList = eController.getEmployees();
            for (Employee e : workersList) {
                if (e.getCategory().equals("NON_SPORTS")) {
                    workersModel.addRow(new Object[] { e.getId(), e.getFirstName(), e.getLastName(), e.getPosition() });
                }
            }
            tableWorkers.setRowSelectionInterval(0, 0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading employees: " + ex.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }

    }

    public void refreshWorkersTable() {
        workersModel.setRowCount(0);
        initializeWorkersTable();
    }

    private int obtainMonthNumber(String monthString) {
        return Month.from(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH).parse(monthString)).getValue();
    }

    private Object[] years() {
        List<Object> years = new ArrayList<>();
        int currentYear = Year.now().getValue();
        for (int i = currentYear; i < currentYear + 80; i++) {
            years.add(i);
        }

        return years.toArray();

    }

    private Object[] determineDays() {
        int highestDay = 31;
        int lowestDay = 1;
        if (cbMonth.getSelectedItem().equals("Feb")) {
            int year = (Integer) cbYear.getSelectedItem();
            if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                highestDay = 29;
            } else {
                highestDay = 28;
            }
        }
        if (cbMonth.getSelectedIndex() % 2 == 0 && cbMonth.getSelectedIndex() < 8) { // Jan, Mar, May, Jul,
            highestDay = 31;
        }
        if (cbMonth.getSelectedIndex() % 2 == 0 && cbMonth.getSelectedIndex() >= 8) { // Sep, Nov
            highestDay = 30;
        }
        List<Object> res = new ArrayList<>();

        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        int selectedMonth = cbMonth.getSelectedIndex();

        if (cbYear.getSelectedIndex() == 0) {
            selectedMonth += LocalDate.now().getMonthValue();
        }
        if (selectedMonth == month && cbYear.getSelectedIndex() == 0) {
            lowestDay = LocalDate.now().getDayOfMonth();
        }

        for (int i = highestDay; i >= lowestDay; i--) {
            res.add(i);
        }

        return res.reversed().toArray();

    }

    private Object[] determineMonths() {
        if (cbYear.getSelectedIndex() == 0) {
            int month = LocalDate.now().getMonthValue();
            List<Object> res = new ArrayList<>();
            for (int i = month - 1; i < MONTHS.length; i++) {
                res.add(MONTHS[i]);
            }
            return res.toArray();
        }
        return MONTHS;
    }

    private void refreshDays() {
        cbDay.setModel(new DefaultComboBoxModel(determineDays()));
    }

    private void refreshMonths() {
        cbMonth.setModel(new DefaultComboBoxModel(determineMonths()));
    }

    private JTextField getTxtWeek() {
        if (txtWeek == null) {
            txtWeek = new JTextField();
            txtWeek.setEditable(false);
            txtWeek.setBounds(217, 263, 63, 25);
            txtWeek.setColumns(10);
            displaySelectedWeek();
        }
        return txtWeek;
    }

    private JLabel getLblWeek() {
        if (lblWeek == null) {
            lblWeek = new JLabel("Week displayed:");
            lblWeek.setLabelFor(getTxtWeek());
            lblWeek.setBounds(105, 263, 92, 26);
        }
        return lblWeek;
    }

    private void displaySelectedWeek() {

        WeekFields wf = WeekFields.ISO;
        LocalDate date = LocalDate.of((Integer) cbYear.getSelectedItem(),
            obtainMonthNumber((String) cbMonth.getSelectedItem()), (Integer) cbDay.getSelectedItem());

        String selectedWeek = String.valueOf(date.get(wf.weekOfWeekBasedYear()));
        txtWeek.setText(selectedWeek);
    }

}
