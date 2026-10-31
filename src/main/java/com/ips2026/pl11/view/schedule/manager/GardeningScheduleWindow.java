package com.ips2026.pl11.view.schedule.manager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.controller.reservation.FacilityReservationController;
import com.ips2026.pl11.controller.schedule.DayScheduleController;
import com.ips2026.pl11.controller.schedule.WorkScheduleController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.data.schedule.DayScheduleDAO;
import com.ips2026.pl11.data.schedule.WorkScheduleDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.toedter.calendar.JDateChooser;

public class GardeningScheduleWindow extends JFrame {

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

    
    private List<Employee> gardenersList = new ArrayList<>();

    private OptionWindowSchedules wsw;
    private HeaderPanel pnHeader;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JPanel pnButtons;
    private JSpinner spStart;
    private JSpinner spEnd;
    private JLabel lblGardeners;
    private JButton btnReturn;
    private JButton btnSchedule;
    private JScrollPane spnTimeslots;
    private JTable tableTimeslots;
    private JDateChooser date;

    private DefaultTableModel timeslotsModel = new DefaultTableModel(new Object[][] {},
        new String[] { "Start", "End" }) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private DefaultTableModel gardenersModel = new DefaultTableModel(new Object[][] {},
        new String[] { "ID", "Name", "Surname" }) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JLabel lblTimeslots;
    private JScrollPane spGardeners;
    private JTable tableGardeners;
    private JLabel lblDate;
    private JComboBox cbGardeners;
    private JButton btnAddGardener;
    private JComboBox cbFacilities;
    private JLabel lblFacility;
    private JComboBox cbSessionType;
    private JLabel lblType;
    private JLabel lblAddedGardeners;
    private JDateChooser dateBirth;
    private JSeparator separator;

    /**
     * Create the frame.
     */
    public GardeningScheduleWindow(OptionWindowSchedules workScheduleWindow) {
        setResizable(false);
        setTitle("Football Club Management");
        this.wsw = workScheduleWindow;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 789, 559);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        contentPane.add(getLblFooter(), BorderLayout.SOUTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
        rootPane.setDefaultButton(btnSchedule);
        setLocationRelativeTo(wsw);

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
            pnButtons.add(getLblGardeners());
            pnButtons.add(getBtnReturn());
            pnButtons.add(getBtnSchedule());
            pnButtons.add(getSpnTimeslots());
            pnButtons.add(getLblTimeslots());
            pnButtons.add(getSpGardeners());
            pnButtons.add(getLblDate());
            pnButtons.add(getCbGardeners());
            pnButtons.add(getBtnAddGardener());
            pnButtons.add(getCbFacilities());
            pnButtons.add(getLblFacility());
            pnButtons.add(getCbSessionType());
            pnButtons.add(getLblType());
            pnButtons.add(getLblAddedGardeners());
            pnButtons.add(getDate());
            pnButtons.add(getSeparator());
        }
        return pnButtons;
    }

    private JLabel getLblGardeners() {
        if (lblGardeners == null) {
            lblGardeners = new JLabel("Gardeners:");
            lblGardeners.setLabelFor(getSpGardeners());
            lblGardeners.setDisplayedMnemonic('W');
            lblGardeners.setBounds(21, 114, 176, 23);
        }
        return lblGardeners;
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
            btnReturn.setBounds(558, 388, 85, 27);
        }
        return btnReturn;
    }

    private JButton getBtnSchedule() {
        if (btnSchedule == null) {
            btnSchedule = new JButton("Schedule");
            btnSchedule.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                }
            });
            btnSchedule.setFont(new Font("Tahoma", Font.BOLD, 11));
            btnSchedule.setBackground(UIManager.getColor("Button.background"));
            btnSchedule.setBounds(653, 388, 85, 27);
        }
        return btnSchedule;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JScrollPane getSpnTimeslots() {
        if (spnTimeslots == null) {
            spnTimeslots = new JScrollPane();
            spnTimeslots.setBounds(477, 98, 262, 269);
            spnTimeslots.setViewportView(getTableTimeslots());
        }
        return spnTimeslots;
    }

    private JTable getTableTimeslots() {
        if (tableTimeslots == null) {
            tableTimeslots = new JTable();
            tableTimeslots.setModel(timeslotsModel);
            
            tableGardeners.getTableHeader().setResizingAllowed(false);
            tableGardeners.getTableHeader().setEnabled(false);
            
        }
        return tableTimeslots;
    }

    private JTable getTableGardeners() {
        if (tableGardeners == null) {
            tableGardeners = new JTable();
            tableGardeners.setModel(gardenersModel);
            
            tableGardeners.getColumnModel().getColumn(0).setPreferredWidth(18);
            tableGardeners.getTableHeader().setResizingAllowed(false);
            tableGardeners.getTableHeader().setEnabled(false);
            
            tableGardeners.getColumnModel().getColumn(2).setPreferredWidth(50);
            tableGardeners.getColumnModel().getColumn(1).setPreferredWidth(50);
        }
        return tableGardeners;
    }

    private JLabel getLblTimeslots() {
        if (lblTimeslots == null) {
            lblTimeslots = new JLabel("Available timeslots:");
            lblTimeslots.setLabelFor(getSpnTimeslots());
            lblTimeslots.setBounds(477, 84, 220, 14);
        }
        return lblTimeslots;
    }

    private JScrollPane getSpGardeners() {
        if (spGardeners == null) {
            spGardeners = new JScrollPane();
            spGardeners.setBounds(21, 179, 385, 188);
            spGardeners.setViewportView(getTableGardeners());
        }
        return spGardeners;
    }

    private JLabel getLblDate() {
        if (lblDate == null) {
            lblDate = new JLabel("Date:");
            lblDate.setBounds(477, 21, 48, 14);
        }
        return lblDate;
    }
    
    private JComboBox<String> getCbGardeners() {
        if (cbGardeners == null) {
            cbGardeners = new JComboBox();
            cbGardeners.setBounds(21, 135, 279, 27);
        }
        return cbGardeners;
    }

    private JButton getBtnAddGardener() {
        if (btnAddGardener == null) {
            btnAddGardener = new JButton("Add");
            btnAddGardener.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (Employee gardener : gardenersList) {
                        String gardenerName = gardener.getFirstName() + " " + gardener.getLastName();
                        if (gardenerName.equals(cbGardeners.getSelectedItem())) {
                            boolean alreadyAdded = false;
                            for (int i = 0; i < gardenersModel.getRowCount(); i++) {
                                if (gardenersModel.getValueAt(i, 0).equals(gardener.getId())) {
                                    alreadyAdded = true;
                                    break;
                                }
                            }
                            if (!alreadyAdded) {
                                gardenersModel.addRow(new Object[] { gardener.getId(), gardener.getFirstName(),
                                        gardener.getLastName() });
                            } else {
                                showError("This gardener is already added.");
                            }
                            break;
                        }
                    }
                }
            });
            btnAddGardener.setMnemonic('A');
            btnAddGardener.setFont(new Font("Tahoma", Font.PLAIN, 11));
            btnAddGardener.setBackground(UIManager.getColor("Button.background"));
            btnAddGardener.setBounds(321, 135, 85, 27);
        }
        return btnAddGardener;
    }

    private JComboBox<Facility> getCbFacilities() {
        if (cbFacilities == null) {
            cbFacilities = new JComboBox();
            cbFacilities.setBounds(21, 39, 279, 27);
        }
        return cbFacilities;
    }

    private JLabel getLblFacility() {
        if (lblFacility == null) {
            lblFacility = new JLabel("Facility:");
            lblFacility.setDisplayedMnemonic('W');
            lblFacility.setBounds(21, 21, 85, 23);
        }
        return lblFacility;
    }

    private JComboBox getCbSessionType() {
        if (cbSessionType == null) {
            cbSessionType = new JComboBox();
            cbSessionType.setBounds(21, 87, 279, 27);
        }
        return cbSessionType;
    }

    private JLabel getLblType() {
        if (lblType == null) {
            lblType = new JLabel("Session type:");
            lblType.setDisplayedMnemonic('W');
            lblType.setBounds(21, 65, 130, 23);
        }
        return lblType;
    }

    private JLabel getLblAddedGardeners() {
        if (lblAddedGardeners == null) {
            lblAddedGardeners = new JLabel("Added gardeners:");
            lblAddedGardeners.setDisplayedMnemonic('W');
            lblAddedGardeners.setBounds(21, 158, 176, 23);
        }
        return lblAddedGardeners;
    }

    private JDateChooser getDate() {
        if (dateBirth == null) {
            dateBirth = new JDateChooser();
            dateBirth.setFont(new Font("Tahoma", Font.PLAIN, 13));
            dateBirth.setDateFormatString("yyyy-MM-dd");
            dateBirth.setBorder(BorderFactory.createCompoundBorder(

                new LineBorder(Branding.BORDER, 1, true),

                BorderFactory.createEmptyBorder(2, 2, 2, 2)

            ));
            dateBirth.setBackground(Color.WHITE);
            dateBirth.setBounds(477, 38, 146, 38);
        }
        return dateBirth;
    }
    private JSeparator getSeparator() {
        if (separator == null) {
            separator = new JSeparator();
            separator.setOrientation(SwingConstants.VERTICAL);
            separator.setForeground(new Color(126, 126, 126));
            separator.setBounds(443, 21, 8, 346);
        }
        return separator;
    }


    // ---- Auxiliary methods ----

    public void loadFacilities() {
        FacilityReservationController controller = new FacilityReservationController(
                new FacilityDAO(), new TeamUseDAO(), new ReservationDAO());
        try {
            controller.loadFacilities();
            List<Facility> facilities = controller.getFacilities();
            getCbFacilities().setModel(new DefaultComboBoxModel<>(facilities.toArray(new Facility[0])));
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Facilities could not be loaded:\n" + exception.getMessage(),
                    "Gardening Sessions Scheduling", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void loadGardeners() {
        EmployeeController controller = new EmployeeController(new EmployeeDAO());
        try {
            gardenersList = controller.getEmployeesByRole("Gardener");
            List<String> gardenerNames = new ArrayList<>();
            for(Employee gardener : gardenersList) {
                gardenerNames.add(gardener.getFirstName() + " " + gardener.getLastName());
            }
            getCbGardeners().setModel(new DefaultComboBoxModel<>(gardenerNames.toArray(new String[0])));
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Gardeners could not be loaded:\n" + exception.getMessage(),
                    "Gardening Sessions Scheduling", JOptionPane.ERROR_MESSAGE);
        }
    }

    
}
