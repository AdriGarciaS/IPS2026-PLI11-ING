package com.ips2026.pl11.view.schedule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.ips2026.pl11.controller.schedule.WorkScheduleController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.schedule.WorkScheduleDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.schedule.WorkSchedule;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MainWindow;

public class GMWorkScheduleWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    private static final String[] DAYS = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
        "Sunday" };

    private WorkScheduleController wsController = new WorkScheduleController(new WorkScheduleDAO());
    private EmployeeController eController = new EmployeeController(new EmployeeDAO());
    
    private List<WorkSchedule> schedulesList = new ArrayList<>();
    private List<Employee> workersList = new ArrayList<>();

    private MainWindow mw;
    private HeaderPanel pnHeader;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JPanel pnButtons;
    private JTextField txtWorkerId;
    private JComboBox cbDay;
    private JSpinner spStart;
    private JSpinner spEnd;
    private JLabel lblWorkers;
    private JLabel lblDay;
    private JLabel lblStart;
    private JLabel lblEnd;
    private JButton btnReturn;
    private JButton btnAdd;
    private JScrollPane spnShifts;
    private JTable tableShifts;
    
    private DefaultTableModel shiftsModel = new DefaultTableModel(
        new Object[][] {
        },
        new String[] {
            "ID", "Day", "Start", "End"
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
    

    /**
     * Create the frame.
     */
    public GMWorkScheduleWindow(MainWindow ventanaPrincipal) {
        setTitle("Football Club Management");
        this.mw = ventanaPrincipal;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1000, 559);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        contentPane.add(getLblFooter(), BorderLayout.SOUTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
        rootPane.setDefaultButton(btnAdd);
        setLocationRelativeTo(ventanaPrincipal);
        
        initializeShiftsTable();
        initializeWorkersTable();

    }
    
    //
    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Weekly Work Schedule Manager",
                    "Add schedules for non sporting employees", false);
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
            pnButtons.add(getCbDay());
            pnButtons.add(getSpStart());
            pnButtons.add(getSpEnd());
            pnButtons.add(getLblWorkers());
            pnButtons.add(getLblDay());
            pnButtons.add(getLblStart());
            pnButtons.add(getLblEnd());
            pnButtons.add(getBtnReturn());
            pnButtons.add(getBtnAdd());
            pnButtons.add(getSpnShifts());
            pnButtons.add(getLblCurrentShifts());
            pnButtons.add(getSpnWorkers());
            pnButtons.add(getLblSelectedWorkerId());
        }
        return pnButtons;
    }

    private JTextField getTxtWorkerId() {
        if (txtWorkerId == null) {
            txtWorkerId = new JTextField();
            txtWorkerId.setEditable(false);
            txtWorkerId.setBounds(505, 49, 63, 25);
            txtWorkerId.setColumns(10);
        }
        return txtWorkerId;
    }

    private JComboBox getCbDay() {
        if (cbDay == null) {
            cbDay = new JComboBox(DAYS);
            cbDay.setBounds(484, 85, 108, 25);
        }
        return cbDay;
    }

    private JSpinner getSpStart() {
        if (spStart == null) {
            spStart = new JSpinner(new SpinnerDateModel());
            spStart.setEditor(new JSpinner.DateEditor(spStart, "HH:mm"));
            spStart.setBounds(451, 121, 75, 25);
        }
        return spStart;
    }

    private JSpinner getSpEnd() {
        if (spEnd == null) {
            spEnd = new JSpinner(new SpinnerDateModel());
            spEnd.setEditor(new JSpinner.DateEditor(spEnd, "HH:mm"));
            spEnd.setBounds(446, 155, 75, 25);
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

    private JLabel getLblDay() {
        if (lblDay == null) {
            lblDay = new JLabel("Day of the week:");
            lblDay.setToolTipText("D");
            lblDay.setLabelFor(getCbDay());
            lblDay.setDisplayedMnemonic('W');
            lblDay.setBounds(393, 86, 101, 23);
        }
        return lblDay;
    }

    private JLabel getLblStart() {
        if (lblStart == null) {
            lblStart = new JLabel("Start time:");
            lblStart.setLabelFor(lblStart);
            lblStart.setDisplayedMnemonic('S');
            lblStart.setBounds(393, 122, 63, 23);
        }
        return lblStart;
    }

    private JLabel getLblEnd() {
        if (lblEnd == null) {
            lblEnd = new JLabel("End time:");
            lblEnd.setDisplayedMnemonic('W');
            lblEnd.setBounds(393, 156, 63, 23);
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
            btnReturn.setBounds(785, 386, 85, 27);
        }
        return btnReturn;
    }

    private JButton getBtnAdd() {
        if (btnAdd == null) {
            btnAdd = new JButton("Add shift");
            btnAdd.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addShift();
                }
            });
            btnAdd.setFont(new Font("Tahoma", Font.BOLD, 11));
            btnAdd.setBackground(UIManager.getColor("Button.background"));
            btnAdd.setBounds(880, 386, 85, 27);
        }
        return btnAdd;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Error", JOptionPane.ERROR_MESSAGE);
    }
    private JScrollPane getSpnShifts() {
        if (spnShifts == null) {
        	spnShifts = new JScrollPane();
        	spnShifts.setBounds(630, 49, 315, 315);
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
        	lblCurrentShifts.setBounds(630, 27, 108, 14);
        }
        return lblCurrentShifts;
    }
    private JScrollPane getSpnWorkers() {
        if (spnWorkers == null) {
        	spnWorkers = new JScrollPane();
        	spnWorkers.setBounds(31, 49, 315, 315);
        	spnWorkers.setViewportView(getTableWorkers());
        }
        return spnWorkers;
    }
    
    
    //////////////////////////////////////// METHODS /////////////////////////////////////////////
    
    private void refreshShiftsTable() {
        try {
            long employeeId = Long.parseLong(txtWorkerId.getText().trim());
            DefaultTableModel model = (DefaultTableModel) tableShifts.getModel();
            for (WorkSchedule s : wsController.getWorkSchedulesByEmployee(employeeId)) {
                model.addRow(new Object[] {
                        s.getEmployeeId(), DAYS[s.getWeekDay() - 1], s.getStartTime(), s.getEndTime()});
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
            for (WorkSchedule s: schedulesList) {
                shiftsModel.addRow(new Object[]{
                        s.getEmployeeId(),
                        DAYS[s.getWeekDay()-1],
                        s.getStartTime(),
                        s.getEndTime()
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
            int weekDay = cbDay.getSelectedIndex() + 1;
            
            
            LocalTime start = ((Date) spStart.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
            LocalTime end = ((Date) spEnd.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
            
            wsController.addShift(new WorkSchedule(0L, employeeId, weekDay, start, end));

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
    
    
    private JLabel getLblSelectedWorkerId() {
        if (lblSelectedWorkerId == null) {
        	lblSelectedWorkerId = new JLabel("Selected Worker ID:");
        	lblSelectedWorkerId.setLabelFor(getTxtWorkerId());
        	lblSelectedWorkerId.setBounds(393, 54, 108, 14);
        }
        return lblSelectedWorkerId;
    }
}
