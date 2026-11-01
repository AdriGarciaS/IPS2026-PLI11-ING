package com.ips2026.pl11.view.schedule.manager;

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
import java.time.LocalTime;
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
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.controller.gardening.GardeningSessionController;
import com.ips2026.pl11.controller.reservation.FacilityReservationController;
import com.ips2026.pl11.controller.schedule.DayScheduleController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.gardening.GardeningSessionDAO;
import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.data.schedule.DayScheduleDAO;
import com.ips2026.pl11.data.schedule.WorkScheduleDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.gardening.GardeningSession;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.model.reservation.TimeSlot;
import com.ips2026.pl11.model.schedule.WeeklyShift;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.toedter.calendar.IDateEvaluator;
import com.toedter.calendar.JDateChooser;

public class GardeningScheduleWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    private List<Employee> gardenersList = new ArrayList<>();

    private OptionWindowSchedules wsw;
    private HeaderPanel pnHeader;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JPanel pnButtons;
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
    private JComboBox<String> cbSessionType;
    private JLabel lblType;
    private JLabel lblAddedGardeners;
    private JDateChooser dateSession;
    private JSeparator separator;

    private DayScheduleController dsController = new DayScheduleController(new DayScheduleDAO(), new WorkScheduleDAO());
    private GardeningSessionController gardeningSessionController = new GardeningSessionController(
        new GardeningSessionDAO());

    private JButton btnRemoveGardener;
    private GardenerDateEvaluator evaluator;
    private JTextField txtSelectedTimeslot;
    private JLabel lblSelectedTimeslot;
    private JTextArea txtaDescrption;
    private JLabel lblDescription;

    /**
     * Create the frame.
     */
    public GardeningScheduleWindow(OptionWindowSchedules workScheduleWindow) {
        setResizable(false);
        setTitle("Football Club Management");
        this.wsw = workScheduleWindow;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 629, 638);
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
            pnButtons.add(getBtnRemoveGardener());
            pnButtons.add(getTxtSelectedTimeslot());
            pnButtons.add(getLblSelectedTimeslot());
            pnButtons.add(getTxtaDescrption());
            pnButtons.add(getLblDescription());
        }
        return pnButtons;
    }

    private JLabel getLblGardeners() {
        if (lblGardeners == null) {
            lblGardeners = new JLabel("Gardeners:");
            lblGardeners.setLabelFor(getSpGardeners());
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
            btnReturn.setBounds(408, 463, 85, 27);
        }
        return btnReturn;
    }

    private JButton getBtnSchedule() {
        if (btnSchedule == null) {
            btnSchedule = new JButton("Schedule");
            btnSchedule.setEnabled(false);
            btnSchedule.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addGardeningSession();
                    btnSchedule.setEnabled(false);
                    txtaDescrption.setText("");
                    txtSelectedTimeslot.setText("");
                    timeslotsModel.setRowCount(0);
                    gardenersModel.setRowCount(0);
                }
            });
            btnSchedule.setFont(new Font("Tahoma", Font.BOLD, 11));
            btnSchedule.setBackground(UIManager.getColor("Button.background"));
            btnSchedule.setBounds(503, 463, 85, 27);
        }
        return btnSchedule;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JScrollPane getSpnTimeslots() {
        if (spnTimeslots == null) {
            spnTimeslots = new JScrollPane();
            spnTimeslots.setBounds(407, 98, 181, 236);
            spnTimeslots.setViewportView(getTableTimeslots());
        }
        return spnTimeslots;
    }

    private JTable getTableTimeslots() {
        if (tableTimeslots == null) {
            tableTimeslots = new JTable();
            tableTimeslots.setModel(timeslotsModel);
            tableTimeslots.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    btnSchedule.setEnabled(true);
                    int row = tableTimeslots.getSelectedRow();

                    if (row != -1) {
                        String start = ((LocalTime) tableTimeslots.getValueAt(row, 0)).toString();
                        String end = ((LocalTime) tableTimeslots.getValueAt(row, 1)).toString();
                        getTxtSelectedTimeslot().setText(String.valueOf(start + " - " + end));
                    }
                }
            });

            tableTimeslots.getTableHeader().setResizingAllowed(false);
            tableTimeslots.getTableHeader().setEnabled(false);

        }
        return tableTimeslots;
    }

    private JTable getTableGardeners() {
        if (tableGardeners == null) {
            tableGardeners = new JTable();
            tableGardeners.setModel(gardenersModel);

            TableColumn first = tableGardeners.getColumnModel().getColumn(0);
            first.setMaxWidth(50);
            first.setPreferredWidth(50);

            tableGardeners.getTableHeader().setResizingAllowed(false);
            tableGardeners.getTableHeader().setEnabled(false);

        }
        return tableGardeners;
    }

    private JLabel getLblTimeslots() {
        if (lblTimeslots == null) {
            lblTimeslots = new JLabel("Available timeslots:");
            lblTimeslots.setDisplayedMnemonic('A');
            lblTimeslots.setLabelFor(getSpnTimeslots());
            lblTimeslots.setBounds(407, 84, 220, 14);
        }
        return lblTimeslots;
    }

    private JScrollPane getSpGardeners() {
        if (spGardeners == null) {
            spGardeners = new JScrollPane();
            spGardeners.setBounds(21, 179, 331, 188);
            spGardeners.setViewportView(getTableGardeners());
        }
        return spGardeners;
    }

    private JLabel getLblDate() {
        if (lblDate == null) {
            lblDate = new JLabel("Date:");
            lblDate.setBounds(407, 21, 48, 14);
        }
        return lblDate;
    }

    private JComboBox<String> getCbGardeners() {
        if (cbGardeners == null) {
            cbGardeners = new JComboBox();
            cbGardeners.setBounds(21, 135, 181, 27);
        }
        return cbGardeners;
    }

    private JButton getBtnRemoveGardener() {
        if (btnRemoveGardener == null) {
            btnRemoveGardener = new JButton("Remove");
            btnRemoveGardener.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (Employee gardener : gardenersList) {
                        String gardenerName = gardener.getFirstName() + " " + gardener.getLastName();
                        if (gardenerName.equals(cbGardeners.getSelectedItem())) {
                            for (int i = 0; i < gardenersModel.getRowCount(); i++) {
                                if (gardenersModel.getValueAt(i, 0).equals(gardener.getId())) {
                                    gardenersModel.removeRow(i);
                                    refreshGardenerAvailability();
                                }
                            }
                        }
                    }
                }
            });
            btnRemoveGardener.setMnemonic('R');
            btnRemoveGardener.setFont(new Font("Tahoma", Font.PLAIN, 11));
            btnRemoveGardener.setBackground(UIManager.getColor("Button.background"));
            btnRemoveGardener.setBounds(282, 135, 70, 27);
        }
        return btnRemoveGardener;
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
                                gardenersModel.addRow(
                                    new Object[] { gardener.getId(), gardener.getFirstName(), gardener.getLastName() });
                                refreshGardenerAvailability();
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
            btnAddGardener.setBounds(208, 135, 70, 27);
        }
        return btnAddGardener;
    }

    private JComboBox<Facility> getCbFacilities() {
        if (cbFacilities == null) {
            cbFacilities = new JComboBox();
            cbFacilities.addActionListener(e -> {
                if (evaluator != null) {
                    evaluator.clearMap();
                }

                updateTimeslotsTable();

                if (dateSession != null) {
                    dateSession.getJCalendar().getDayChooser().repaint();
                }
            });
            cbFacilities.setBounds(21, 39, 181, 27);
        }
        return cbFacilities;
    }

    private JLabel getLblFacility() {
        if (lblFacility == null) {
            lblFacility = new JLabel("Facility:");
            lblFacility.setBounds(21, 21, 85, 23);
        }
        return lblFacility;
    }

    private JComboBox getCbSessionType() {
        if (cbSessionType == null) {
            cbSessionType = new JComboBox();
            cbSessionType.setBounds(21, 87, 181, 27);
            cbSessionType.setModel(new DefaultComboBoxModel<>(new String[] { "Interior", "Exterior" }));
        }
        return cbSessionType;
    }

    private JLabel getLblType() {
        if (lblType == null) {
            lblType = new JLabel("Session type:");
            lblType.setBounds(21, 65, 130, 23);
        }
        return lblType;
    }

    private JLabel getLblAddedGardeners() {
        if (lblAddedGardeners == null) {
            lblAddedGardeners = new JLabel("Added gardeners:");
            lblAddedGardeners.setDisplayedMnemonic('W');
            lblAddedGardeners.setBounds(21, 160, 176, 23);
        }
        return lblAddedGardeners;
    }

    private JDateChooser getDate() {
        if (dateSession == null) {
            dateSession = new JDateChooser();
            dateSession.addPropertyChangeListener("date", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateTimeslotsTable();
                    getTxtSelectedTimeslot().setText("");
                }
            });
            evaluator = new GardenerDateEvaluator();
            dateSession.getJCalendar().getDayChooser().addDateEvaluator(evaluator);
            dateSession.setFont(new Font("Tahoma", Font.PLAIN, 13));
            dateSession.setDateFormatString("yyyy-MM-dd");
            dateSession.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Branding.BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            dateSession.setBackground(Color.WHITE);
            dateSession.setBounds(407, 38, 181, 38);
        }
        return dateSession;
    }

    private JSeparator getSeparator() {
        if (separator == null) {
            separator = new JSeparator();
            separator.setOrientation(SwingConstants.VERTICAL);
            separator.setForeground(new Color(126, 126, 126));
            separator.setBounds(373, 21, 8, 346);
        }
        return separator;
    }

    private JTextField getTxtSelectedTimeslot() {
        if (txtSelectedTimeslot == null) {
            txtSelectedTimeslot = new JTextField();
            txtSelectedTimeslot.setHorizontalAlignment(SwingConstants.CENTER);
            txtSelectedTimeslot.setEditable(false);
            txtSelectedTimeslot.setBounds(483, 340, 94, 27);
            txtSelectedTimeslot.setColumns(10);
        }
        return txtSelectedTimeslot;
    }

    private JLabel getLblSelectedTimeslot() {
        if (lblSelectedTimeslot == null) {
            lblSelectedTimeslot = new JLabel("Selected:");
            lblSelectedTimeslot.setLabelFor(getTxtSelectedTimeslot());
            lblSelectedTimeslot.setBounds(418, 341, 85, 26);
        }
        return lblSelectedTimeslot;
    }

    private JTextArea getTxtaDescrption() {
        if (txtaDescrption == null) {
            txtaDescrption = new JTextArea();
            txtaDescrption.setFont(new Font("Tahoma", Font.PLAIN, 13));
            txtaDescrption.setBounds(21, 390, 561, 64);
        }
        return txtaDescrption;
    }

    private JLabel getLblDescription() {
        if (lblDescription == null) {
            lblDescription = new JLabel("Task details: (Optional)");
            lblDescription.setDisplayedMnemonic('T');
            lblDescription.setLabelFor(getTxtaDescrption());
            lblDescription.setBounds(21, 369, 257, 26);
        }
        return lblDescription;
    }

    // ---- Auxiliary methods ----

    public void loadFacilities() {
        FacilityReservationController controller = new FacilityReservationController(new FacilityDAO(),
            new TeamUseDAO(), new ReservationDAO());
        try {
            controller.loadFacilities();
            List<Facility> facilities = controller.getFacilities();
            getCbFacilities().setModel(new DefaultComboBoxModel<>(facilities.toArray(new Facility[0])));
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Facilities could not be loaded:\n" + exception.getMessage(),
                "Gardening Sessions Scheduling", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadGardeners() {
        EmployeeController controller = new EmployeeController(new EmployeeDAO());
        try {
            gardenersList = controller.getEmployeesByRole("Gardener");
            List<String> gardenerNames = new ArrayList<>();
            for (Employee gardener : gardenersList) {
                gardenerNames.add(gardener.getFirstName() + " " + gardener.getLastName());
            }
            getCbGardeners().setModel(new DefaultComboBoxModel<>(gardenerNames.toArray(new String[0])));
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Gardeners could not be loaded:\n" + exception.getMessage(),
                "Gardening Sessions Scheduling", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTimeslotsTable() {
        timeslotsModel.setRowCount(0);
        txtSelectedTimeslot.setText("");
        btnSchedule.setEnabled(false);

        if (gardenersModel.getRowCount() == 0 || dateSession.getDate() == null
            || cbFacilities.getSelectedItem() == null) {
            return;
        }

        try {
            LocalDate day = dateSession.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate monday = day.with(DayOfWeek.MONDAY);
            int weekDay = day.getDayOfWeek().getValue();

            FacilityReservationController facilityController = new FacilityReservationController(new FacilityDAO(),
                new TeamUseDAO(), new ReservationDAO());
            facilityController.selectDay((Facility) cbFacilities.getSelectedItem(), day);

            List<TimeSlot> commonPeriods = new ArrayList<>(facilityController.getAvailability().getFreePeriods());
            for (int i = 0; i < gardenersModel.getRowCount(); i++) {
                long gardenerId = ((Number) gardenersModel.getValueAt(i, 0)).longValue();

                List<WeeklyShift> shifts = dsController.getWeekShifts(gardenerId, monday);
                commonPeriods = intersectWithShifts(commonPeriods, shifts, weekDay);

                if (commonPeriods.isEmpty()) {
                    break;
                }
            }

            long facilityId = ((Facility) cbFacilities.getSelectedItem()).getId();
            commonPeriods = removeOccupiedSessions(day, facilityId, commonPeriods);
            for (TimeSlot period : commonPeriods) {
                timeslotsModel.addRow(new Object[] { period.getStart(), period.getEnd() });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Something went wrong while showing the selected day's timeslots:\n" + e.getMessage(),
                "Gardening Sessions Scheduling", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshGardenerAvailability() {
        if (evaluator != null) {
            evaluator.clearMap();
        }
        if (dateSession != null && dateSession.getDate() != null) {
            updateTimeslotsTable();
            dateSession.getJCalendar().getDayChooser().repaint();
        }
    }

    private void addGardeningSession() {
        String sessionType = (String) cbSessionType.getSelectedItem();
        int facilityId = ((Facility) cbFacilities.getSelectedItem()).getId();
        String date = dateSession.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
        String startTime = tableTimeslots.getValueAt(tableTimeslots.getSelectedRow(), 0).toString();
        String endTime = tableTimeslots.getValueAt(tableTimeslots.getSelectedRow(), 1).toString();

        if (txtaDescrption.getText().trim().isEmpty()) {
            txtaDescrption.setText("No description provided.");
        }
        String description = txtaDescrption.getText().trim();

        try {
            List<GardeningSession> sessions = gardeningSessionController.getAllGardeningSessions();
            for (GardeningSession s : sessions) {
                if (s.getFacilityId() == facilityId && s.getDate().equals(date)) {
                    LocalTime existingStart = LocalTime.parse(s.getStartTime());
                    LocalTime existingEnd = LocalTime.parse(s.getEndTime());
                    LocalTime newStart = LocalTime.parse(startTime);
                    LocalTime newEnd = LocalTime.parse(endTime);

                    if ((newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart))) {
                        JOptionPane.showMessageDialog(this,
                            "The selected timeslot overlaps with an existing gardening session:\n"
                                + "Existing session: " + existingStart + " - " + existingEnd,
                            "Gardening Session Scheduling", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                }
            }

            gardeningSessionController.scheduleGardeningSession(
                new GardeningSession(0L, facilityId, description, sessionType, date, startTime, endTime));

            JOptionPane.showMessageDialog(this,
                "Gardening session scheduled successfully!\n\n" + "Facility ID: " + facilityId + "\n" + "Description: "
                    + sessionType + "\n" + "Date: " + date + "\n" + "Start Time: " + startTime + "\n" + "End Time: "
                    + endTime,
                "Gardening Session Scheduled", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error scheduling gardening session:\n" + e.getMessage(),
                "Gardening Session Scheduling", JOptionPane.ERROR_MESSAGE);
        }

    }

    List<TimeSlot> removeOccupiedSessions(LocalDate day, long facilityId, List<TimeSlot> commonPeriods)
        throws SQLException {

        List<TimeSlot> availablePeriods = new ArrayList<>(commonPeriods);
        for (GardeningSession session : gardeningSessionController.getAllGardeningSessions()) {
            if (session.getFacilityId() != facilityId || !session.getDate().equals(day.toString())) {
                continue;
            }
            LocalTime existingStart = LocalTime.parse(session.getStartTime());
            LocalTime existingEnd = LocalTime.parse(session.getEndTime());
            List<TimeSlot> updatedPeriods = new ArrayList<>();

            for (TimeSlot period : availablePeriods) {
                if (!existingEnd.isAfter(period.getStart()) || !existingStart.isBefore(period.getEnd())) {
                    updatedPeriods.add(period);

                } else {
                    if (existingStart.isAfter(period.getStart())) {
                        updatedPeriods.add(new TimeSlot(period.getStart(), existingStart, TimeSlot.Type.FREE, ""));
                    }
                    if (existingEnd.isBefore(period.getEnd())) {
                        updatedPeriods.add(new TimeSlot(existingEnd, period.getEnd(), TimeSlot.Type.FREE, ""));
                    }
                }
            }

            availablePeriods = updatedPeriods;
            if (availablePeriods.isEmpty()) {
                break;
            }
        }

        return availablePeriods;
    }

    private List<TimeSlot> intersectWithShifts(List<TimeSlot> availablePeriods, List<WeeklyShift> shifts, int weekDay) {
        List<TimeSlot> intersections = new ArrayList<>();

        for (WeeklyShift shift : shifts) {
            if (shift.getDayOfWeek() == weekDay) {
                for (TimeSlot period : availablePeriods) {
                    LocalTime start = shift.getStartTime().isAfter(period.getStart()) ? shift.getStartTime()
                        : period.getStart();

                    LocalTime end = shift.getEndTime().isBefore(period.getEnd()) ? shift.getEndTime() : period.getEnd();

                    if (start.isBefore(end)) {
                        intersections.add(new TimeSlot(start, end, TimeSlot.Type.FREE, ""));
                    }
                }
            }
        }

        return intersections;
    }

    class GardenerDateEvaluator implements IDateEvaluator {
        private FacilityReservationController facilityController = new FacilityReservationController(new FacilityDAO(),
            new TeamUseDAO(), new ReservationDAO());

        // key: gardenerId + "|" + monday
        private final Map<String, List<WeeklyShift>> weekShiftsCache = new HashMap<>();
        private final Map<LocalDate, Boolean> availabilityMap = new HashMap<>();
        private boolean errorShown = false;

        public void clearMap() {
            availabilityMap.clear();
            weekShiftsCache.clear();
            errorShown = false;
        }

        public Map<String, List<WeeklyShift>> getWeekShiftsCache() {
            return weekShiftsCache;
        }

        @Override
        public boolean isInvalid(Date date) {
            LocalDate day = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (day.isBefore(LocalDate.now()) || gardenersModel.getRowCount() == 0) {
                return true;
            }

            Boolean isAvailable = availabilityMap.get(day);
            if (isAvailable == null) {
                isAvailable = checkAvailability(day);
                availabilityMap.put(day, isAvailable);
            }
            return !isAvailable;
        }

        private boolean checkAvailability(LocalDate day) {
            try {
                facilityController.selectDay((Facility) cbFacilities.getSelectedItem(), day);

                List<TimeSlot> commonPeriods = new ArrayList<>(facilityController.getAvailability().getFreePeriods());

                if (commonPeriods.isEmpty()) {
                    return false;
                }

                LocalDate monday = day.with(DayOfWeek.MONDAY);
                int weekDay = day.getDayOfWeek().getValue();
                for (int i = 0; i < gardenersModel.getRowCount(); i++) {
                    long gardenerId = ((Number) gardenersModel.getValueAt(i, 0)).longValue();
                    String key = gardenerId + "|" + monday;
                    List<WeeklyShift> shifts = weekShiftsCache.get(key);

                    if (shifts == null) {
                        shifts = dsController.getWeekShifts(gardenerId, monday);
                        weekShiftsCache.put(key, shifts);
                    }

                    commonPeriods = intersectWithShifts(commonPeriods, shifts, weekDay);
                    if (commonPeriods.isEmpty()) {
                        return false;
                    }
                }

                commonPeriods = removeOccupiedSessions(day, ((Facility) cbFacilities.getSelectedItem()).getId(), commonPeriods);

                return !commonPeriods.isEmpty();
            } catch (SQLException e) {
                if (!errorShown) {
                    errorShown = true;
                    showError("Availability could not be loaded:\n" + e.getMessage());
                }
                return false;
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

}
