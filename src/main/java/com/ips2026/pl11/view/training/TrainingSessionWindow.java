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
import java.text.SimpleDateFormat;
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
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.controller.reservation.FacilityReservationController;
import com.ips2026.pl11.controller.team.TeamController;
import com.ips2026.pl11.controller.training.TrainingSessionController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.data.team.TeamDAO;
import com.ips2026.pl11.data.team.TeamDAO.TeamItem;
import com.ips2026.pl11.data.training.TrainingSessionDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.model.training.TrainingSession;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MainWindow;
import com.toedter.calendar.JDateChooser;

public class TrainingSessionWindow extends JFrame {

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
    private JDateChooser date;

    private EmployeeController employeeController = new EmployeeController(new EmployeeDAO());
    private TeamController teamController = new TeamController(new TeamDAO());
    private TrainingSessionController trainingSessionController = new TrainingSessionController(
        new TrainingSessionDAO());
    private FacilityReservationController facilityController = new FacilityReservationController(new FacilityDAO(),
        new TeamUseDAO(), new ReservationDAO());

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

    /**
     * Create the frame.
     */
    public TrainingSessionWindow(MainWindow mainWindow) {
        setResizable(false);
        setTitle("Football Club Management");
        this.mw = mainWindow;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 494, 531);
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
            btnReturn.setBounds(232, 339, 85, 27);
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
                }
            });
            btnSchedule.setFont(new Font("Tahoma", Font.BOLD, 11));
            btnSchedule.setBackground(UIManager.getColor("Button.background"));
            btnSchedule.setBounds(334, 339, 85, 27);
        }
        return btnSchedule;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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

    // ---- Auxiliary methods ----

    public void initialize() {
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
            List<Facility> facilities = facilityController.getFacilities();
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
        String date = new SimpleDateFormat("yyyy-MM-dd").format(getDate().getDate());
        String startTime = new SimpleDateFormat("HH:mm").format(getSpStart().getValue());
        String endTime = new SimpleDateFormat("HH:mm").format(getSpEnd().getValue());

        long facilityId = getFacilityIdFromCb();
        long coachId = getCoachIdFromCb();
        long teamId = getTeamIdFromCb();

        TrainingSession session = new TrainingSession(0L, date, startTime, endTime, facilityId, coachId, teamId);
        try {
            trainingSessionController.scheduleTrainingSession(session);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error scheduling training session:\n" + e.getMessage(),
                "Training Session Scheduling", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Training session scheduled successfully!", "Training Session Scheduling",
            JOptionPane.INFORMATION_MESSAGE);
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
