package com.ips2026.pl11.view.training;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.controller.gardening.GardeningSessionController;
import com.ips2026.pl11.controller.interview.InterviewController;
import com.ips2026.pl11.controller.reservation.FacilityReservationController;
import com.ips2026.pl11.controller.slots.InterviewSlotsController;
import com.ips2026.pl11.controller.team.TeamController;
import com.ips2026.pl11.controller.training.TrainingSessionController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.gardening.GardeningSessionDAO;
import com.ips2026.pl11.data.gardening.GardeningSessionEmployeeDAO;
import com.ips2026.pl11.data.interview.InterviewDAO;
import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.data.slots.InterviewSlotDAO;
import com.ips2026.pl11.data.team.TeamDAO;
import com.ips2026.pl11.data.team.TeamDAO.TeamItem;
import com.ips2026.pl11.data.training.TrainingSessionDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.gardening.GardeningSession;
import com.ips2026.pl11.model.interview.AvailableSlotRecord;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.model.reservation.TimeSlot;
import com.ips2026.pl11.model.training.TrainingSession;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MainWindow;
import com.toedter.calendar.JDateChooser;

public class TrainingSessionWindow extends JDialog {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    private MainWindow mw;
    private HeaderPanel pnHeader;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JPanel pnButtons;
    private JLabel lblTeam;
    private JButton btnReturn;
    private JButton btnSchedule;

    private EmployeeController employeeController = new EmployeeController(new EmployeeDAO());
    private TeamController teamController = new TeamController(new TeamDAO());
    private TrainingSessionController trainingSessionController = new TrainingSessionController(
        new TrainingSessionDAO());
    private FacilityReservationController facilityController = new FacilityReservationController(new FacilityDAO(),
        new TeamUseDAO(), new ReservationDAO());
    private GardeningSessionController gardeningSessionController = new GardeningSessionController(
        new GardeningSessionDAO(), new GardeningSessionEmployeeDAO());
    private InterviewController interviewController = new InterviewController(new InterviewDAO(), new EmployeeDAO());
    private InterviewSlotsController interviewSlotsController = new InterviewSlotsController(new InterviewSlotDAO());

    private JLabel lblDate;
    private JComboBox cbTeams;
    private JComboBox cbFacilities;
    private JLabel lblFacility;
    private JDateChooser dateSession;
    private JComboBox<String> cbCoaches;
    private JLabel lblCoach;
    private JLabel lblEnd;
    private JLabel lblStart;
    private JSpinner spStart;
    private JSpinner spEnd;
    private JSeparator separator;
    private JScrollPane spnInterviews;
    private JScrollPane spnFreeTimeslots;
    private JTable tableInterviews;
    private JTable tableFreeTimeslots;
    private JLabel lblInterviews;
    private JLabel lblFreeTimeslots;

    private DefaultTableModel timeslotsModel=new DefaultTableModel(new Object[][]{},new String[]{"Start","End"}){@Override public boolean isCellEditable(int row,int column){return false;}};

    private DefaultTableModel interviewsModel=new DefaultTableModel(new Object[][]{},new String[]{"Player","Start","End"}){@Override public boolean isCellEditable(int row,int column){return false;}};

    private List<Facility> facilities = new ArrayList<>();

    /**
     * Create the frame.
     */
    public TrainingSessionWindow(MainWindow mainWindow) {
        setResizable(false);
        setTitle("Football Club Management");
        this.mw = mainWindow;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 820, 531);
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
        setLocationRelativeTo(mw);
        setModal(true);

    }

    //
    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Training session scheduling",
                "Add training sessions as a General Manager for the team", false);
            pnHeader.setBackground(new Color(7, 51, 117));
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
            pnButtons.add(getLblTeam());
            pnButtons.add(getBtnReturn());
            pnButtons.add(getBtnSchedule());
            pnButtons.add(getLblDate());
            pnButtons.add(getCbTeams());
            pnButtons.add(getCbFacilities());
            pnButtons.add(getLblFacility());
            pnButtons.add(getDate());
            pnButtons.add(getCbCoaches());
            pnButtons.add(getLblCoach());
            pnButtons.add(getLblEnd());
            pnButtons.add(getLblStart());
            pnButtons.add(getSpStart());
            pnButtons.add(getSpEnd());
            pnButtons.add(getSeparator());
            pnButtons.add(getSpnInterviews());
            pnButtons.add(getSpnFreeTimeslots());
            pnButtons.add(getLblInterviews());
            pnButtons.add(getLblFreeTimeslots());
        }
        return pnButtons;
    }

    private JLabel getLblTeam() {
        if (lblTeam == null) {
            lblTeam = new JLabel("Team:");
            lblTeam.setForeground(new Color(0, 64, 128));
            lblTeam.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblTeam.setBounds(63, 93, 176, 23);
        }
        return lblTeam;
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
            btnReturn.setBounds(586, 339, 85, 27);
        }
        return btnReturn;
    }

    private JButton getBtnSchedule() {
        if (btnSchedule == null) {
            btnSchedule = new JButton("Schedule");
            btnSchedule.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkAllFilled();
                    dateSession.setDate(null);
                }
            });
            btnSchedule.setFont(new Font("Tahoma", Font.BOLD, 11));
            btnSchedule.setBackground(UIManager.getColor("Button.background"));
            btnSchedule.setBounds(688, 339, 85, 27);
        }
        return btnSchedule;
    }

    private JLabel getLblDate() {
        if (lblDate == null) {
            lblDate = new JLabel("Date:");
            lblDate.setForeground(new Color(0, 64, 128));
            lblDate.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblDate.setBounds(63, 190, 102, 14);
        }
        return lblDate;
    }

    private JComboBox<String> getCbTeams() {
        if (cbTeams == null) {
            cbTeams = new JComboBox();
            cbTeams.setBounds(237, 85, 181, 38);
        }
        return cbTeams;
    }

    private JComboBox<String> getCbFacilities() {
        if (cbFacilities == null) {
            cbFacilities = new JComboBox();
            cbFacilities.addActionListener(e -> {
                if (dateSession != null) {
                    dateSession.getJCalendar().getDayChooser().repaint();
                }
            });
            cbFacilities.setBounds(237, 39, 181, 38);
        }
        return cbFacilities;
    }

    private JLabel getLblFacility() {
        if (lblFacility == null) {
            lblFacility = new JLabel("Training facility:");
            lblFacility.setForeground(new Color(0, 64, 128));
            lblFacility.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblFacility.setBounds(63, 47, 162, 23);
        }
        return lblFacility;
    }

    private JDateChooser getDate() {
        if (dateSession == null) {
            dateSession = new JDateChooser();
            dateSession.addPropertyChangeListener("date", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if(dateSession.getDate() != null) {
                        updateTableFreeTimeslots();
                        loadTableInterviews();                        
                    }
                }
            });

            dateSession.setFont(new Font("Tahoma", Font.PLAIN, 13));
            dateSession.setDateFormatString("yyyy-MM-dd");
            dateSession.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Branding.BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            dateSession.setBackground(Color.WHITE);
            dateSession.setBounds(237, 177, 181, 38);
        }
        return dateSession;
    }

    private JComboBox<String> getCbCoaches() {
        if (cbCoaches == null) {
            cbCoaches = new JComboBox();
            cbCoaches.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadTableInterviews();
                }
            });
            cbCoaches.setBounds(237, 132, 181, 38);
        }
        return cbCoaches;
    }

    private JLabel getLblCoach() {
        if (lblCoach == null) {
            lblCoach = new JLabel("Coach:");
            lblCoach.setForeground(new Color(0, 64, 128));
            lblCoach.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblCoach.setBounds(63, 140, 176, 23);
        }
        return lblCoach;
    }

    private JLabel getLblEnd() {
        if (lblEnd == null) {
            lblEnd = new JLabel("End time:");
            lblEnd.setForeground(new Color(0, 64, 128));
            lblEnd.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblEnd.setBounds(63, 276, 176, 23);
        }
        return lblEnd;
    }

    private JLabel getLblStart() {
        if (lblStart == null) {
            lblStart = new JLabel("Start time:");
            lblStart.setForeground(new Color(0, 64, 128));
            lblStart.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblStart.setBounds(63, 229, 176, 23);
        }
        return lblStart;
    }

    private JSpinner getSpStart() {
        if (spStart == null) {
            spStart = new JSpinner(new SpinnerDateModel());
            spStart.setEditor(new JSpinner.DateEditor(spStart, "HH:mm"));
            spStart.setBounds(238, 222, 181, 38);
        }
        return spStart;
    }

    private JSpinner getSpEnd() {
        if (spEnd == null) {
            spEnd = new JSpinner(new SpinnerDateModel());
            spEnd.setEditor(new JSpinner.DateEditor(spEnd, "HH:mm"));
            spEnd.setBounds(238, 268, 181, 38);
        }
        return spEnd;
    }

    private JSeparator getSeparator() {
        if (separator == null) {
            separator = new JSeparator();
            separator.setOrientation(SwingConstants.VERTICAL);
            separator.setForeground(new Color(126, 126, 126));
            separator.setBounds(439, 21, 8, 306);
        }
        return separator;
    }

    private JScrollPane getSpnInterviews() {
        if (spnInterviews == null) {
            spnInterviews = new JScrollPane();
            spnInterviews.setBounds(468, 39, 282, 122);
            spnInterviews.setViewportView(getTableInterviews());
        }
        return spnInterviews;
    }

    private JScrollPane getSpnFreeTimeslots() {
        if (spnFreeTimeslots == null) {
            spnFreeTimeslots = new JScrollPane();
            spnFreeTimeslots.setBounds(468, 182, 282, 122);
            spnFreeTimeslots.setViewportView(getTableFreeTimeslots());
        }
        return spnFreeTimeslots;
    }

    private JTable getTableInterviews() {
        if (tableInterviews == null) {
            tableInterviews = new JTable();
            tableInterviews.setEnabled(false);
            tableInterviews.setModel(interviewsModel);
        }
        return tableInterviews;
    }

    private JTable getTableFreeTimeslots() {
        if (tableFreeTimeslots == null) {
            tableFreeTimeslots = new JTable();
            tableFreeTimeslots.setEnabled(false);
            tableFreeTimeslots.setModel(timeslotsModel);
        }
        return tableFreeTimeslots;
    }

    private JLabel getLblInterviews() {
        if (lblInterviews == null) {
            lblInterviews = new JLabel("Interviews:");
            lblInterviews.setFont(new Font("Tahoma", Font.PLAIN, 12));
            lblInterviews.setBounds(468, 19, 152, 26);
        }
        return lblInterviews;
    }

    private JLabel getLblFreeTimeslots() {
        if (lblFreeTimeslots == null) {
            lblFreeTimeslots = new JLabel("Facility free timeslots:");
            lblFreeTimeslots.setFont(new Font("Tahoma", Font.PLAIN, 12));
            lblFreeTimeslots.setBounds(468, 160, 231, 26);
        }
        return lblFreeTimeslots;
    }

    // ---- Auxiliary methods ----

    public void initialize() {
        interviewsModel.setRowCount(0);
        timeslotsModel.setRowCount(0);
        
        
        loadFacilities();
        loadCoaches();
        loadTeams();

    }

    private void loadTeams() {
        try {
            List<TeamItem> teams = teamController.getTeams();
            List<String> teamNames = new ArrayList<>();
            for (TeamItem team : teams) {
                teamNames.add(team.getName());
            }
            getCbTeams().setModel(new DefaultComboBoxModel<String>(teamNames.toArray(new String[0])));
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Teams could not be loaded:\n" + exception.getMessage(),
                "Training Sessions Scheduling", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void loadCoaches() {
        try {
            List<Employee> coaches = employeeController.getEmployeesByRole("Coach");
            List<String> names = new ArrayList<>();
            for (Employee e : coaches) {
                names.add(e.getFirstName() + " " + e.getLastName());
            }
            getCbCoaches().setModel(new DefaultComboBoxModel<String>(names.toArray(new String[0])));
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Coaches could not be loaded:\n" + exception.getMessage(),
                "Training Sessions Scheduling", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void loadFacilities() {

        try {
            facilityController.loadFacilities();
            facilities = facilityController.getFacilities();
            List<String> training = new ArrayList<>();
            for (Facility f : facilities) {
                if (f.getName().contains("Training")) {
                    training.add(f.getName());
                }
            }
            getCbFacilities().setModel(new DefaultComboBoxModel<>(training.toArray(new String[0])));
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Facilities could not be loaded:\n" + exception.getMessage(),
                "Gardening Sessions Scheduling", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTableFreeTimeslots() {
        try {
            long facilityId = getFacilityIdFromCb();
            Facility facility = facilities.stream().filter(f -> f.getId() == facilityId).findFirst().orElse(null);

            LocalDate date = getDate().getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            facilityController.selectDay(facility, date);

            timeslotsModel.setRowCount(0);
            List<GardeningSession> gardeningSessions = getTodayGardeningSessions(date);
            List<TimeSlot> freePeriods = intersectPeriodsWithGardeningSessions(gardeningSessions, facility);

            for (TimeSlot slot : freePeriods) {
                timeslotsModel.addRow(new Object[] { slot.getStart().toString(), slot.getEnd().toString() });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading free timeslots:\n" + e.getMessage(),
                "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<TimeSlot> intersectPeriodsWithGardeningSessions(List<GardeningSession> gardeningSessions,
        Facility facility) {

        List<TimeSlot> result = new ArrayList<>(facilityController.getAvailability().getFreePeriods());

        for (GardeningSession gardeningSession : gardeningSessions) {
            Optional<Facility> facilityOpt = getGardeningSessionFacility(gardeningSession);

            if (facilityOpt.isEmpty() || facilityOpt.get().getId() != facility.getId()) {
                continue;
            }
            LocalTime gardeningStart = LocalTime.parse(gardeningSession.getStartTime());
            LocalTime gardeningEnd = LocalTime.parse(gardeningSession.getEndTime());
            List<TimeSlot> updatedPeriods = new ArrayList<>();

            for (TimeSlot freeSlot : result) {
                LocalTime freeStart = freeSlot.getStart();
                LocalTime freeEnd = freeSlot.getEnd();
                // No overlap
                if (!gardeningStart.isBefore(freeEnd) || !gardeningEnd.isAfter(freeStart)) {

                    updatedPeriods.add(freeSlot);
                    continue;
                }
                // Free period before the gardening session
                if (gardeningStart.isAfter(freeStart)) {
                    updatedPeriods.add(new TimeSlot(freeStart, gardeningStart, TimeSlot.Type.FREE, ""));
                }
                // Free period after the gardening session
                if (gardeningEnd.isBefore(freeEnd)) {
                    updatedPeriods.add(new TimeSlot(gardeningEnd, freeEnd, TimeSlot.Type.FREE, ""));
                }
            }
            result = updatedPeriods;
        }

        return result;
    }

    private Optional<Facility> getGardeningSessionFacility(GardeningSession gardeningSession) {
        for (Facility f : facilities) {
            if (f.getId() == gardeningSession.getFacilityId()) {
                return Optional.of(f);
            }
        }
        return Optional.empty();
    }

    private List<GardeningSession> getTodayGardeningSessions(LocalDate date) throws SQLException {
        List<GardeningSession> sessions = gardeningSessionController.getAllGardeningSessions();
        List<GardeningSession> todaySessions = new ArrayList<>();
        for (GardeningSession session : sessions) {
            LocalDate sessionDate = LocalDate.parse(session.getDate());
            if (date.equals(sessionDate)) {
                todaySessions.add(session);
            }
        }
        return todaySessions;
    }

    private void loadTableInterviews() {
        interviewsModel.setRowCount(0);
        try {
            int coachId = (int) getCoachIdFromCb();
            List<Employee> players = interviewSlotsController.getPlayersByCoach(coachId);
            
            List<AvailableSlotRecord> interviews = interviewController.getInterviewSlots();
            LocalDate date = getDate().getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            for (AvailableSlotRecord slot : interviews) {
                if (players.stream().anyMatch(p -> p.getId() == slot.getPlayerId())) {
                    LocalDate interviewDate = LocalDate.parse(slot.getDate());
                    if (interviewDate.equals(date)) {
                        interviewsModel
                        .addRow(new Object[] { slot.getPlayerName(), slot.getStartTime(), slot.getEndTime() });
                    }
                }
            }

        }catch(

    SQLException e)
    {
        JOptionPane.showMessageDialog(this, "Interviews could not be loaded:\n" + e.getMessage(),
            "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
    }
    }

    private void checkAllFilled() {
        if (getCbTeams().getSelectedItem() == null || getCbFacilities().getSelectedItem() == null
            || getCbCoaches().getSelectedItem() == null || getDate().getDate() == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields before scheduling a training session.",
                "Training Session Scheduling", JOptionPane.WARNING_MESSAGE);
        } else {
            addTrainingSession();

        }
    }

    private void addTrainingSession() {
        LocalDate date = getDate().getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime startTime = ((Date) getSpStart().getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        LocalTime endTime = ((Date) getSpEnd().getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

        long facilityId = getFacilityIdFromCb();
        long coachId = getCoachIdFromCb();
        long teamId = getTeamIdFromCb();

        TrainingSession session = new TrainingSession(0L, date, startTime, endTime, facilityId, coachId, teamId);

        boolean canAdd = checkTimeConflict(session);
        try {
            if (canAdd) {
                trainingSessionController.scheduleTrainingSession(session);
                JOptionPane.showMessageDialog(this, "Training session scheduled successfully!",
                    "Training Session Scheduling", JOptionPane.INFORMATION_MESSAGE);
                initialize();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error scheduling training session:\n" + e.getMessage(),
                "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    private boolean checkTimeConflict(TrainingSession session) {
        try {
            Facility facility = facilityController.getFacilities().stream()
                .filter(f -> f.getId() == session.getFacility()).findFirst().orElse(null);
            facilityController.selectDay(facility, session.getDate());

            boolean fits = facilityController.getAvailability().getFreePeriods().stream()
                .anyMatch(slot -> !slot.getStart().isAfter(session.getStartTime())
                    && !slot.getEnd().isBefore(session.getEndTime()));

            if (!fits && !findTimeConflict(session)) {
                return false;
            }

            List<GardeningSession> overlapping = gardeningSessionController.getAllGardeningSessions().stream()
                .filter(g -> LocalDate.parse(g.getDate()).equals(session.getDate()))
                .filter(g -> session.getStartTime().isBefore(LocalTime.parse(g.getEndTime()))
                    && session.getEndTime().isAfter(LocalTime.parse(g.getStartTime())))
                .filter(g -> g.getFacilityId() == session.getFacility())
                .toList();
           

            if (!overlapping.isEmpty()) {
                String msg = overlapping.stream()
                    .map(g -> g.getDescription() + " (" + g.getStartTime() + " - " + g.getEndTime() + ")")
                    .collect(Collectors.joining("\n"));

                int option = JOptionPane.showConfirmDialog(this,
                    "The following gardening session will be cancelled:\n" + msg + "\n\nContinue?",
                    "Training Session Scheduling", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (option != JOptionPane.YES_OPTION) {
                    return false;
                }
                overlapping.forEach(g -> gardeningSessionController.cancelGardeningSession(g.getId()));
            }
            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error checking for time conflicts:\n" + e.getMessage(),
                "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean findTimeConflict(TrainingSession session) {
        try {
            Optional<String> problem = facilityController.findTimeProblem(session.getStartTime(),
                Duration.between(session.getStartTime(), session.getEndTime()).toHoursPart());

            if (problem.isPresent()) {
                if (canCancelReservation(problem)) {

                    if(!checkBookingExclusiveRestriction(problem.get())) {
                        JOptionPane.showMessageDialog(this, "The following time conflict was found:\n" + problem.get(),
                            "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }

                    int option = JOptionPane.showConfirmDialog(this,
                        "The following reservation will be cancelled:\n" + problem.get() + "\n\nContinue?",
                        "Training Session Scheduling", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                    if (option != JOptionPane.YES_OPTION) {
                        return false;
                    }

                    facilityController.cancelReservation(session.getDate(), session.getStartTime());
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, "The following time conflict was found:\n" + problem.get(),
                        "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading facility availability:\n" + e.getMessage(),
                "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private boolean checkBookingExclusiveRestriction(String problem) {
        String minDurationMsg = "A reservation must last at least ";
        String timeLimit = "Reservations can only be made until ";
        String openingTime = "Facilities can be booked from ";
        String closingTime = "A reservation must end at ";
        String pastTime = "That day has already passed.";
        String blockedAfterTeamUse = "The facility can not be booked until ";
        String startTimePassed = "The start time has already passed.";
        String training = "(training)";
                
        boolean ignoreProblem = problem.contains(minDurationMsg) || problem.contains(timeLimit) || problem.contains(openingTime)
            || problem.contains(closingTime) || problem.contains(pastTime) || problem.contains(blockedAfterTeamUse)
            || problem.contains(startTimePassed) || problem.contains(training);
        
        if (ignoreProblem) {
            return true;
        }
        return false;
    }

    private boolean canCancelReservation(Optional<String> problem) {
        boolean a = problem.get().contains("A reservation must last at least ");
        boolean b = problem.get().contains("That day has already passed.");
        boolean c = problem.get().contains("Reservations can only be made until ");
        boolean d = problem.get().contains("Facilities can be booked from ");
        boolean e = problem.get().contains("A reservation must end at ");

        return !(a || b || c || d || e);
    }

    private long getTeamIdFromCb() {
        try {
            String selectedTeamName = (String) getCbTeams().getSelectedItem();
            List<TeamItem> teams = teamController.getTeams();
            for (TeamItem t : teams) {
                if (t.getName().equals(selectedTeamName)) {
                    return t.getId();
                }
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Teams could not be loaded:\n" + exception.getMessage(),
                "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
        }
        return -1;
    }

    private long getCoachIdFromCb() {
        try {
            String selectedCoachName = (String) getCbCoaches().getSelectedItem();
            List<Employee> coaches = employeeController.getEmployeesByRole("Coach");
            for (Employee e : coaches) {
                String fullName = e.getFirstName() + " " + e.getLastName();
                if (fullName.equals(selectedCoachName)) {
                    return e.getId();
                }
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Coaches could not be loaded:\n" + exception.getMessage(),
                "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
        }
        return -1;
    }

    private long getFacilityIdFromCb() {
        try {
            String selectedFacilityName = (String) getCbFacilities().getSelectedItem();
            facilityController.loadFacilities();
            List<Facility> facilities = facilityController.getFacilities();
            for (Facility f : facilities) {
                if (f.getName().equals(selectedFacilityName)) {
                    return f.getId();
                }
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Facilities could not be loaded:\n" + exception.getMessage(),
                "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
        }
        return -1;
    }

}
