package com.ips2026.pl11.view.schedule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
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

public class ManagerWorkScheduleWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    
    private static final String[] DAYS = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
    "Sunday" };
    
    private static final String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    
    private static final String RECURRING = "Weekly";

    private WorkScheduleDAO wsDAO = new WorkScheduleDAO();
    private WorkScheduleController wsController = new WorkScheduleController(wsDAO);
    private DayScheduleController dsController = new DayScheduleController(new DayScheduleDAO(), wsDAO);
    private EmployeeController eController = new EmployeeController(new EmployeeDAO());
    
    private List<WorkSchedule> schedulesList = new ArrayList<>();
    private List<DaySchedule> daySchedulesList = new ArrayList<>();
    private List<Employee> workersList = new ArrayList<>();

    private WorkScheduleWindow wsw;
    private HeaderPanel pnHeader;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JPanel pnButtons;
    private JTextField txtWorkerId;
    private JComboBox cbMonth;
    private JSpinner spStart;
    private JSpinner spEnd;
    private JLabel lblWorkers;
    private JLabel lblStart;
    private JLabel lblEnd;
    private JButton btnReturn;
    private JButton btnModify;
    private JScrollPane spnShifts;
    private JTable tableShifts;
    
    private DefaultTableModel shiftsModel = new DefaultTableModel(
        new Object[][] {
        },
        new String[] {
            "ID","Date", "Day", "Start", "End"
        }
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    
    private DefaultTableModel workersModel = new DefaultTableModel(
        new Object[][] {
        },
        new String[] {
            "ID", "Name", "Surname", "Position"
        }
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    
    private JLabel lblCurrentShifts;
    private JScrollPane spnWorkers;
    private JTable tableWorkers;
    private JLabel lblSelectedWorkerId;
    private JComboBox cbDay;
    private JComboBox cbYear;
    private JLabel lblDate;
    

    /**
     * Create the frame.
     */
    public ManagerWorkScheduleWindow(WorkScheduleWindow workScheduleWindow) {
        setTitle("Football Club Management");
        this.wsw = workScheduleWindow;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1128, 559);
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
            pnHeader = new HeaderPanel("Specific Work Schedule Management",
                    "Modify specific days from non sporting employees", false);
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
            pnButtons.add(getSpStart());
            pnButtons.add(getSpEnd());
            pnButtons.add(getLblWorkers());
            pnButtons.add(getLblStart());
            pnButtons.add(getLblEnd());
            pnButtons.add(getBtnReturn());
            pnButtons.add(getBtnModify());
            pnButtons.add(getSpnShifts());
            pnButtons.add(getLblCurrentShifts());
            pnButtons.add(getSpnWorkers());
            pnButtons.add(getLblSelectedWorkerId());
            pnButtons.add(getLblDate());
            pnButtons.add(getCbYear());
            pnButtons.add(getCbMonth());
            pnButtons.add(getCbDay());
        }
        return pnButtons;
    }

    private JTextField getTxtWorkerId() {
        if (txtWorkerId == null) {
            txtWorkerId = new JTextField();
            txtWorkerId.setEditable(false);
            txtWorkerId.setBounds(549, 69, 63, 25);
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
                }
            });
            cbYear.setBounds(468, 105, 67, 25);
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
                }
            });
            cbMonth.setBounds(540, 105, 64, 25);
        }
        return cbMonth;
    }
    
    private JComboBox getCbDay() {
        if (cbDay == null) {
            cbDay = new JComboBox(determineDays());
            cbDay.setBounds(611, 105, 63, 25);
        }
        return cbDay;
    }

    private JSpinner getSpStart() {
        if (spStart == null) {
            spStart = new JSpinner(new SpinnerDateModel());
            spStart.setEditor(new JSpinner.DateEditor(spStart, "HH:mm"));
            spStart.setBounds(495, 142, 75, 25);
        }
        return spStart;
    }

    private JSpinner getSpEnd() {
        if (spEnd == null) {
            spEnd = new JSpinner(new SpinnerDateModel());
            spEnd.setEditor(new JSpinner.DateEditor(spEnd, "HH:mm"));
            spEnd.setBounds(495, 176, 75, 25);
        }
        return spEnd;
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

    private JLabel getLblStart() {
        if (lblStart == null) {
            lblStart = new JLabel("Start time:");
            lblStart.setLabelFor(lblStart);
            lblStart.setDisplayedMnemonic('S');
            lblStart.setBounds(437, 143, 63, 23);
        }
        return lblStart;
    }

    private JLabel getLblEnd() {
        if (lblEnd == null) {
            lblEnd = new JLabel("End time:");
            lblEnd.setDisplayedMnemonic('W');
            lblEnd.setBounds(437, 177, 63, 23);
        }
        return lblEnd;
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
            btnReturn.setBounds(900, 388, 85, 27);
        }
        return btnReturn;
    }

    private JButton getBtnModify() {
        if (btnModify == null) {
            btnModify = new JButton("Modify");
            btnModify.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addShift();
                }
            });
            btnModify.setFont(new Font("Tahoma", Font.BOLD, 11));
            btnModify.setBackground(UIManager.getColor("Button.background"));
            btnModify.setBounds(995, 388, 85, 27);
        }
        return btnModify;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Error", JOptionPane.ERROR_MESSAGE);
    }
    private JScrollPane getSpnShifts() {
        if (spnShifts == null) {
        	spnShifts = new JScrollPane();
        	spnShifts.setBounds(695, 52, 385, 315);
        	spnShifts.setViewportView(getTableShifts());
        }
        return spnShifts;
    }
    private JTable getTableShifts() {
        if (tableShifts == null) {
        	tableShifts = new JTable();
        	tableShifts.setModel(shiftsModel);
        	customizeHeader(tableShifts);
        }
        return tableShifts;
    }
    
    private JTable getTableWorkers() {
        if (tableWorkers == null) {
            tableWorkers = new JTable();
            tableWorkers.setModel(workersModel);
            tableWorkers.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = tableWorkers.getSelectedRow();
                    
                    if(row != -1) {
                        int id = (int) tableWorkers.getValueAt(row, 0);
                        getTxtWorkerId().setText(String.valueOf(id));
                    }
                }
            });
            customizeHeader(tableWorkers);
            tableWorkers.getColumnModel().getColumn(2).setPreferredWidth(50);
            tableWorkers.getColumnModel().getColumn(1).setPreferredWidth(50);
        }
        return tableWorkers;
    }
    
    private JLabel getLblCurrentShifts() {
        if (lblCurrentShifts == null) {
        	lblCurrentShifts = new JLabel("Current Shifts:");
        	lblCurrentShifts.setLabelFor(getSpnShifts());
        	lblCurrentShifts.setBounds(695, 30, 108, 14);
        }
        return lblCurrentShifts;
    }
    private JScrollPane getSpnWorkers() {
        if (spnWorkers == null) {
        	spnWorkers = new JScrollPane();
        	spnWorkers.setBounds(31, 49, 385, 315);
        	spnWorkers.setViewportView(getTableWorkers());
        }
        return spnWorkers;
    }
    
    private JLabel getLblSelectedWorkerId() {
        if (lblSelectedWorkerId == null) {
            lblSelectedWorkerId = new JLabel("Selected Worker ID:");
            lblSelectedWorkerId.setLabelFor(getTxtWorkerId());
            lblSelectedWorkerId.setBounds(437, 74, 108, 14);
        }
        return lblSelectedWorkerId;
    }
    
    private JLabel getLblDate() {
        if (lblDate == null) {
            lblDate = new JLabel("Date:");
            lblDate.setBounds(436, 110, 48, 14);
        }
        return lblDate;
    }
    
    
    //////////////////////////////////////// METHODS /////////////////////////////////////////////
    
    public void refreshShiftsTable() {
        try {
            DefaultTableModel model = (DefaultTableModel) tableShifts.getModel();
            model.setRowCount(0);
            for (WorkSchedule s : wsController.getWorkSchedules()) {
                model.addRow(new Object[] {
                        s.getEmployeeId(), RECURRING, DAYS[s.getWeekDay() - 1], s.getStartTime(), s.getEndTime()
                });
            }
            for (DaySchedule ds : dsController.getAllSchedules()) {
                model.addRow(new Object[] {
                    wsController.getWorkScheduleById(ds.getWorkScheduleId()).getEmployeeId(),
                        ds.getDate(), DAYS[ds.getDate().getDayOfWeek().getValue()-1], ds.getStartTime(), ds.getEndTime()
                });
            }
            
        } catch (NumberFormatException e) {
            showError("Employee id must be a number");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }
    
    private void initializeShiftsTable() {
        try {
            schedulesList = wsController.getWorkSchedules();
            daySchedulesList = dsController.getAllSchedules();
            for (WorkSchedule s: schedulesList) {
                shiftsModel.addRow(new Object[]{
                        s.getEmployeeId(), RECURRING,
                        DAYS[s.getWeekDay()-1],
                        s.getStartTime(),
                        s.getEndTime()
                });
            }
            
            for(DaySchedule ds : daySchedulesList) {
                shiftsModel.addRow(new Object[] {
                    wsController.getWorkScheduleById(ds.getWorkScheduleId()).getEmployeeId(),
                    ds.getDate(),
                    DAYS[ds.getDate().getDayOfWeek().getValue() -1],
                    ds.getStartTime(),
                    ds.getEndTime()
                });
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading work schedules: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initializeWorkersTable() {
        try {
            workersList = eController.getEmployees();
            for (Employee e: workersList) {
                if(e.getCategory().equals("NON_SPORTS")) {
                    workersModel.addRow(new Object[]{
                            e.getId(),
                            e.getFirstName(),
                            e.getLastName(),
                            e.getPosition()
                    });
                }
            }
            tableWorkers.setRowSelectionInterval(0, 0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading employees: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    private void customizeHeader(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(18);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setEnabled(false);
    }
    
    private void addShift() {
        try {
            long employeeId = Long.parseLong(txtWorkerId.getText().trim());
            
            LocalDate date = LocalDate.of((Integer)cbYear.getSelectedItem(),
                obtainMonthNumber((String) cbMonth.getSelectedItem()), (Integer)cbDay.getSelectedItem());
            
            long workScheduleId = obtainWorkScheduleId(employeeId, date);
            
            LocalTime start = ((Date) spStart.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
            LocalTime end = ((Date) spEnd.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
            
            dsController.addShift(new DaySchedule(0L, workScheduleId, date, start, end));

            refreshShiftsTable();
            JOptionPane.showMessageDialog(this, "Shift added");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Employee id must be a number");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }
    
    private int obtainMonthNumber(String monthString) {
        return Month.from(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH).parse(monthString)).getValue();
    }

    private long obtainWorkScheduleId(long employeeId, LocalDate date) throws SQLException, IllegalArgumentException {
        List<WorkSchedule> workSchedules = wsController.getWorkSchedulesByEmployee(employeeId);
        int dayOfWeek = date.getDayOfWeek().getValue();
        for(WorkSchedule ws : workSchedules) {
            if(ws.getWeekDay() == dayOfWeek) {
                return ws.getId();
            }
        }
        throw new IllegalArgumentException("There's no work schedule assigned to " + date.getDayOfWeek().toString());
    }
    
    
    private Object[] years() {
        List<Object> years = new ArrayList<>();
        int currentYear = Year.now().getValue();
        for(int i = currentYear; i < currentYear + 80; i++) {
            years.add(i);
        }
        
        return years.toArray();
        
    }
    
    private Object[] determineDays() {
        int highestDay = 31;
        int lowestDay = 1;
        if(cbMonth.getSelectedItem().equals("Feb")) {
            int year = (Integer) cbYear.getSelectedItem();
            if((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                highestDay = 29;
            } else {
                highestDay = 28;
            }
        }
        if(cbMonth.getSelectedIndex() % 2 == 0 && cbMonth.getSelectedIndex() < 8) { // Jan, Mar, May, Jul, 
            highestDay = 31;
        }
        if(cbMonth.getSelectedIndex() % 2 == 0 && cbMonth.getSelectedIndex() >= 8) { // Sep, Nov
            highestDay = 30;
        }
        List<Object> res = new ArrayList<>();
        
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        int selectedMonth = cbMonth.getSelectedIndex();

        if (cbYear.getSelectedIndex() == 0) {
            selectedMonth += LocalDate.now().getMonthValue();
        }
        if(selectedMonth == month && cbYear.getSelectedIndex() == 0) {
            lowestDay = LocalDate.now().getDayOfMonth();
        }
        
        for(int i = highestDay; i>= lowestDay; i--) {
            res.add(i);
        }
        
        return res.reversed().toArray();
        
    }
    
    private Object[] determineMonths() {
        if(cbYear.getSelectedIndex() == 0) {
            int month = LocalDate.now().getMonthValue();
            List<Object> res = new ArrayList<>();
            for(int i = month-1; i<MONTHS.length; i++) {
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
    
}
