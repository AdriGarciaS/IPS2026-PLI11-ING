package com.ips2026.pl11.view.gardening.gardener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.controller.gardening.GardeningSessionController;
import com.ips2026.pl11.controller.reservation.FacilityReservationController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.gardening.GardeningSessionDAO;
import com.ips2026.pl11.data.gardening.GardeningSessionEmployeeDAO;
import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.gardening.GardeningSession;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.gardening.GardeningSessionWindow;
import com.toedter.calendar.IDateEvaluator;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;

public class ViewGardeningSessions extends JDialog {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    private List<Employee> gardenersList = new ArrayList<>();

    private GardeningSessionWindow gsw;
    private HeaderPanel pnHeader;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JPanel pnButtons;
    private JTextField txtGardenerId;
    private GardenerDateEvaluator evaluator;

    private JLabel lblGardeners;
    private JButton btnReturn;

    private DefaultTableModel shiftsModel = new DefaultTableModel(new Object[][] {},
        new String[] { "ID", "Date", "Day", "Start", "End" }) {
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

    private JLabel lblCurrentShifts;
    private JScrollPane spnGardeners;
    private JTable tableGardeners;
    private JLabel lblSelectedGardenerId;
    private JDateChooser dateSession;
    private JLabel lblDate;
    private JSeparator separator;
    private JLabel lblWeekSessions;
    private JComboBox<String> cbWeekSessions;
    private JTextArea txtaDescription;
    private JLabel lblSelectedSessionInfo;
    private JTextField txtFacility;
    private JTextField txtSessionType;
    private JTextField txtDate;
    private JTextField txtStartTime;
    private JTextField txtEndTime;
    private JLabel lblTo;
    private JLabel lblFrom;
    private JLabel lblDateInfo;
    private JLabel lblSessionType;
    private JLabel lblFacility;

    private GardeningSessionController gardeningSessionController = new GardeningSessionController(
        new GardeningSessionDAO(), new GardeningSessionEmployeeDAO());
    private JScrollPane scrollPane;

    /**
     * Create the frame.
     */
    public ViewGardeningSessions(GardeningSessionWindow gsw) {
        setResizable(false);
        setTitle("Football Club Management");
        this.gsw = gsw;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 900, 554);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        contentPane.add(getLblFooter(), BorderLayout.SOUTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
        setLocationRelativeTo(gsw);
        setModal(true);

    }

    //
    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("View Gardening Sessions", "View your weekly assigned gardening sessions",
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
            pnButtons.add(getTxtGardenerId());
            pnButtons.add(getLblGardeners());
            pnButtons.add(getBtnReturn());
            pnButtons.add(getLblCurrentShifts());
            pnButtons.add(getSpnGardeners());
            pnButtons.add(getLblSelectedGardenerId());
            pnButtons.add(getDateSession());
            pnButtons.add(getLblDate());
            pnButtons.add(getSeparator());
            pnButtons.add(getLblWeekSessions());
            pnButtons.add(getCbWeekSessions());
            pnButtons.add(getLblSelectedSessionInfo());
            pnButtons.add(getTxtFacility());
            pnButtons.add(getTxtSessionType());
            pnButtons.add(getTxtDate());
            pnButtons.add(getTxtStartTime());
            pnButtons.add(getTxtEndTime());
            pnButtons.add(getLblTo());
            pnButtons.add(getLblFrom());
            pnButtons.add(getLblDateInfo());
            pnButtons.add(getLblSessionType());
            pnButtons.add(getLblFacility());
            pnButtons.add(getScrollPane());
        }
        return pnButtons;
    }

    private JTextField getTxtGardenerId() {
        if (txtGardenerId == null) {
            txtGardenerId = new JTextField();
            txtGardenerId.setEditable(false);
            txtGardenerId.setBounds(143, 262, 63, 27);
            txtGardenerId.setColumns(10);
        }
        return txtGardenerId;
    }

    private JLabel getLblGardeners() {
        if (lblGardeners == null) {
            lblGardeners = new JLabel("Gardeners:");
            lblGardeners.setLabelFor(getSpnGardeners());
            lblGardeners.setDisplayedMnemonic('W');
            lblGardeners.setBounds(31, 23, 63, 23);
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
            btnReturn.setBounds(759, 376, 85, 27);
        }
        return btnReturn;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JTable getTableGardeners() {
        if (tableGardeners == null) {
            tableGardeners = new JTable();
            tableGardeners.setModel(gardenersModel);
            tableGardeners.getSelectionModel().addListSelectionListener(e -> {
                cbWeekSessions.setEnabled(false);
                
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int row = tableGardeners.getSelectedRow();
                if (row < 0) {
                    return;
                }

                clearSessionInfo();
                txtGardenerId.setText(tableGardeners.getValueAt(row, 0).toString());
                dateSession.setEnabled(true);
                refreshCalendar();
            });

            TableColumn first = tableGardeners.getColumnModel().getColumn(0);
            first.setMaxWidth(50);
            first.setPreferredWidth(50);

            tableGardeners.getTableHeader().setResizingAllowed(false);
            tableGardeners.getTableHeader().setEnabled(false);
        }
        return tableGardeners;
    }

    private JDateChooser getDateSession() {
        if (dateSession == null) {
            dateSession = new JDateChooser();
            dateSession.getCalendarButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cbWeekSessions.setEnabled(true);
                }
            });
            dateSession.setFont(new Font("Tahoma", Font.PLAIN, 13));
            evaluator = new GardenerDateEvaluator();
            dateSession.addPropertyChangeListener("date", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    try {
                        LocalDate date = LocalDate.ofInstant(dateSession.getDate().toInstant(), ZoneId.systemDefault())
                            .with(DayOfWeek.MONDAY);
                        long gardenerId = Long.parseLong(txtGardenerId.getText());

                        List<GardeningSession> sessions = gardeningSessionController
                            .getGardenerWeeklySessions(gardenerId, date);
                        List<String> sessionDescriptions = new ArrayList<>();
                        for (GardeningSession session : sessions) {
                            sessionDescriptions.add(sessions.indexOf(session) + 1 + ": " + session.getDate()+ " / "+ session.getStartTime());
                        }

                        cbWeekSessions.setModel(new DefaultComboBoxModel<>(sessionDescriptions.toArray(new String[0])));
                        updateCbWeeks();
                    } catch (SQLException | NumberFormatException e) {
                        showError("Sessions could not be loaded:\n" + e.getMessage());
                    }
                }
            });
            dateSession.getJCalendar().getDayChooser().addDateEvaluator(evaluator);
            dateSession.setDateFormatString("yyyy-MM-dd");
            dateSession.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Branding.BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            dateSession.setBackground(Color.WHITE);
            dateSession.setBounds(143, 294, 181, 38);

            dateSession.setEnabled(false);
        }
        return dateSession;
    }

    private JLabel getLblDate() {
        if (lblDate == null) {
            lblDate = new JLabel("Date:");
            lblDate.setBounds(31, 306, 48, 14);
        }
        return lblDate;
    }

    private JLabel getLblCurrentShifts() {
        if (lblCurrentShifts == null) {
            lblCurrentShifts = new JLabel("Session description:");
            lblCurrentShifts.setBounds(398, 215, 201, 14);
        }
        return lblCurrentShifts;
    }

    private JScrollPane getSpnGardeners() {
        if (spnGardeners == null) {
            spnGardeners = new JScrollPane();
            spnGardeners.setBounds(31, 49, 322, 192);
            spnGardeners.setViewportView(getTableGardeners());
        }
        return spnGardeners;
    }

    private JLabel getLblSelectedGardenerId() {
        if (lblSelectedGardenerId == null) {
            lblSelectedGardenerId = new JLabel("Selected Gardener ID:");
            lblSelectedGardenerId.setLabelFor(getTxtGardenerId());
            lblSelectedGardenerId.setBounds(31, 267, 108, 14);
        }
        return lblSelectedGardenerId;
    }

    private JSeparator getSeparator() {
        if (separator == null) {
            separator = new JSeparator();
            separator.setOrientation(SwingConstants.VERTICAL);
            separator.setForeground(new Color(126, 126, 126));
            separator.setBounds(374, 23, 8, 346);
        }
        return separator;
    }

    private JLabel getLblWeekSessions() {
        if (lblWeekSessions == null) {
            lblWeekSessions = new JLabel("Week's sessions:");
            lblWeekSessions.setBounds(31, 343, 108, 14);
        }
        return lblWeekSessions;
    }

    private JComboBox getCbWeekSessions() {
        if (cbWeekSessions == null) {
            cbWeekSessions = new JComboBox();
            cbWeekSessions.setEnabled(false);
            cbWeekSessions.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateCbWeeks();
                }
            });
            cbWeekSessions.setBounds(143, 337, 210, 27);
        }
        return cbWeekSessions;
    }

    private JTextArea getTxtaDescription() {
        if (txtaDescription == null) {
            txtaDescription = new JTextArea();
            txtaDescription.setWrapStyleWord(true);
            txtaDescription.setLineWrap(true);
            txtaDescription.setFont(new Font("Tahoma", Font.PLAIN, 13));
            txtaDescription.setEditable(false);
        }
        return txtaDescription;
    }

    private JLabel getLblSelectedSessionInfo() {
        if (lblSelectedSessionInfo == null) {
            lblSelectedSessionInfo = new JLabel("Selected session info:");
            lblSelectedSessionInfo.setBounds(398, 23, 201, 14);
        }
        return lblSelectedSessionInfo;
    }

    private JTextField getTxtFacility() {
        if (txtFacility == null) {
            txtFacility = new JTextField();
            txtFacility.setEditable(false);
            txtFacility.setColumns(10);
            txtFacility.setBounds(471, 55, 172, 27);
        }
        return txtFacility;
    }

    private JTextField getTxtSessionType() {
        if (txtSessionType == null) {
            txtSessionType = new JTextField();
            txtSessionType.setEditable(false);
            txtSessionType.setColumns(10);
            txtSessionType.setBounds(471, 93, 172, 27);
        }
        return txtSessionType;
    }

    private JTextField getTxtDate() {
        if (txtDate == null) {
            txtDate = new JTextField();
            txtDate.setEditable(false);
            txtDate.setColumns(10);
            txtDate.setBounds(471, 130, 92, 27);
        }
        return txtDate;
    }

    private JTextField getTxtStartTime() {
        if (txtStartTime == null) {
            txtStartTime = new JTextField();
            txtStartTime.setHorizontalAlignment(SwingConstants.CENTER);
            txtStartTime.setEditable(false);
            txtStartTime.setColumns(10);
            txtStartTime.setBounds(471, 167, 63, 27);
        }
        return txtStartTime;
    }

    private JTextField getTxtEndTime() {
        if (txtEndTime == null) {
            txtEndTime = new JTextField();
            txtEndTime.setHorizontalAlignment(SwingConstants.CENTER);
            txtEndTime.setEditable(false);
            txtEndTime.setColumns(10);
            txtEndTime.setBounds(565, 167, 63, 27);
        }
        return txtEndTime;
    }

    private JLabel getLblTo() {
        if (lblTo == null) {
            lblTo = new JLabel("to");
            lblTo.setHorizontalAlignment(SwingConstants.CENTER);
            lblTo.setBounds(531, 168, 32, 26);
        }
        return lblTo;
    }

    private JLabel getLblFrom() {
        if (lblFrom == null) {
            lblFrom = new JLabel("From");
            lblFrom.setHorizontalAlignment(SwingConstants.CENTER);
            lblFrom.setBounds(398, 168, 63, 26);
        }
        return lblFrom;
    }

    private JLabel getLblDateInfo() {
        if (lblDateInfo == null) {
            lblDateInfo = new JLabel("Date:");
            lblDateInfo.setHorizontalAlignment(SwingConstants.CENTER);
            lblDateInfo.setBounds(398, 131, 63, 26);
        }
        return lblDateInfo;
    }

    private JLabel getLblSessionType() {
        if (lblSessionType == null) {
            lblSessionType = new JLabel("Type:");
            lblSessionType.setHorizontalAlignment(SwingConstants.CENTER);
            lblSessionType.setBounds(398, 94, 63, 26);
        }
        return lblSessionType;
    }

    private JLabel getLblFacility() {
        if (lblFacility == null) {
            lblFacility = new JLabel("Facility:");
            lblFacility.setHorizontalAlignment(SwingConstants.CENTER);
            lblFacility.setBounds(401, 57, 71, 26);
        }
        return lblFacility;
    }

    // ---- auxiliary methods ----

    public void refreshCalendar() {
        if (evaluator != null) {
            evaluator.clearMap();
        }
        JCalendar jc = dateSession.getJCalendar();
        jc.getDayChooser().setMonth(jc.getMonthChooser().getMonth());
    }

    public void loadGardeners() {

        gardenersModel.setRowCount(0);
        EmployeeController controller = new EmployeeController(new EmployeeDAO());
        try {
            gardenersList = controller.getEmployeesByRole("Gardener");
            for (Employee gardener : gardenersList) {
                gardenersModel
                    .addRow(new Object[] { gardener.getId(), gardener.getFirstName(), gardener.getLastName() });
            }

        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Gardeners could not be loaded:\n" + exception.getMessage(),
                "Gardening Sessions Scheduling", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCbWeeks() {
        LocalDate date = LocalDate.ofInstant(dateSession.getDate().toInstant(), ZoneId.systemDefault())
            .with(DayOfWeek.MONDAY);
        long gardenerId = Long.parseLong(txtGardenerId.getText());
        List<GardeningSession> sessions;
        try {
            sessions = gardeningSessionController.getGardenerWeeklySessions(gardenerId, date);
        } catch (SQLException e) {
            showError("Sessions could not be loaded:\n" + e.getMessage());
            return;
        }

        long sessionId = sessions.get(cbWeekSessions.getSelectedIndex()).getId();
        for (GardeningSession session : sessions) {
            if (session.getId() == sessionId) {

                FacilityReservationController frc = new FacilityReservationController(new FacilityDAO(),
                    new TeamUseDAO(), new ReservationDAO());
                try {
                    frc.loadFacilities();
                } catch (SQLException e) {
                    showError("Facilities could not be loaded:\n" + e.getMessage());
                    return;
                }
                List<Facility> facilities = frc.getFacilities();
                for (Facility facility : facilities) {
                    if (facility.getId() == session.getFacilityId()) {
                        txtFacility.setText(facility.getName());
                        break;
                    }
                }

                txtSessionType.setText(session.getSessionType());
                txtDate.setText(session.getDate());
                txtStartTime.setText(session.getStartTime());
                txtEndTime.setText(session.getEndTime());
                txtaDescription.setText(session.getDescription());
            }
        }
    }
    
    private void clearSessionInfo() {
        txtFacility.setText("");
        txtSessionType.setText("");
        txtDate.setText("");
        txtStartTime.setText("");
        txtEndTime.setText("");
        txtaDescription.setText("");
    }

    class GardenerDateEvaluator implements IDateEvaluator {

        private final Map<String, Boolean> availabilityCache = new HashMap<>();

        private boolean errorShown = false;

        public void clearMap() {
            availabilityCache.clear();
            errorShown = false;
        }

        @Override
        public boolean isInvalid(Date date) {
            LocalDate day = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (day.isBefore(LocalDate.now()) || gardenersModel.getRowCount() == 0) {
                return true;
            }

            try {
                String gardener = txtGardenerId.getText();
                if (gardener == null || gardener.isBlank()) {
                    return true;
                }

                long gardenerId = Long.parseLong(gardener);

                LocalDate monday = day.with(DayOfWeek.MONDAY);
                String cacheKey = gardenerId + "|" + monday;

                Boolean available = availabilityCache.get(cacheKey);

                if (available == null) {
                    List<GardeningSession> sessions = gardeningSessionController.getGardenerWeeklySessions(gardenerId,
                        monday);

                    available = !sessions.isEmpty();
                    availabilityCache.put(cacheKey, available);
                }

                return !available;

            } catch (SQLException | NumberFormatException e) {
                if (!errorShown) {
                    errorShown = true;
                    showError("Availability could not be loaded:\n" + e.getMessage());
                }

                return true;
            }
        }

        @Override
        public boolean isSpecial(Date date) {
            return false;
        }

        @Override
        public Color getSpecialForegroundColor() {
            return null;
        }

        @Override
        public Color getSpecialBackroundColor() {
            return null;
        }

        @Override
        public String getSpecialTooltip() {
            return null;
        }

        @Override
        public Color getInvalidForegroundColor() {
            return null;
        }

        @Override
        public Color getInvalidBackroundColor() {
            return null;
        }

        @Override
        public String getInvalidTooltip() {
            return null;
        }
    }

    private JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane();
            scrollPane.setBounds(400, 234, 434, 125);
            scrollPane.setViewportView(getTxtaDescription());
        }
        return scrollPane;
    }
}
