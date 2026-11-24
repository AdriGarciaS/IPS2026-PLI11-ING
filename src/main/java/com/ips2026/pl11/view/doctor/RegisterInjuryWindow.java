package com.ips2026.pl11.view.doctor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.controller.injury.InjuryController;
import com.ips2026.pl11.controller.matches.ScheduleMatchController;
import com.ips2026.pl11.controller.reservation.FacilityReservationController;
import com.ips2026.pl11.controller.team.TeamController;
import com.ips2026.pl11.controller.training.TrainingSessionController;
import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.data.team.TeamDAO;
import com.ips2026.pl11.data.team.TeamDAO.TeamItem;
import com.ips2026.pl11.data.training.TrainingSessionDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.injury.Injury;
import com.ips2026.pl11.model.matches.Match;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.model.training.TrainingSession;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class RegisterInjuryWindow extends JDialog {

    private static final long serialVersionUID = 1L;
    
    private int doctorId;

    private HeaderPanel pnHeader;
    private JPanel pnButtons;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JPanel contentPane;
    private JLabel lblTeam;
    private JLabel lblPlayer;
    private JLabel lblTraining;
    private JLabel lblDescription;
    private JLabel lblMatches;
    private JComboBox<TeamItem> cbTeams;
    private JComboBox<String> cbPlayers;
    private JComboBox<String> cbTrainings;
    private JComboBox<String> cbMatches;
    private JButton btnRegister;
    private JButton btnReturn;

    private TeamController teamController = new TeamController(new TeamDAO());
    private TrainingSessionController trainingController = new TrainingSessionController(new TrainingSessionDAO());
    private FacilityReservationController facilityController = new FacilityReservationController(new FacilityDAO(),
        new TeamUseDAO(), new ReservationDAO());
    private ScheduleMatchController matchController = new ScheduleMatchController();
    private InjuryController injuryController = new InjuryController();

    private List<Employee> players = new ArrayList<>();
    private List<TrainingSession> trainings = new ArrayList<>();
    private List<Match> matches = new ArrayList<>();
    private JScrollPane scpDescription;
    private JTextArea txtrNoDescriptionProvided;

    /**
     * Create the dialog.
     */
    public RegisterInjuryWindow(JFrame parent, int doctorId) {
        this.doctorId = doctorId;
        
        setTitle("Football Club Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(550, 480));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        setLocationRelativeTo(parent);
        Branding.setInitialSize(this, 550, 610, null);
        rootPane.setDefaultButton(getBtnRegister());

        initialize();
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setBorder(new EmptyBorder(24, 24, 16, 24));
            pnButtons.setLayout(null);
            pnButtons.add(getLblTeam());
            pnButtons.add(getLblPlayer());
            pnButtons.add(getLblTraining());
            pnButtons.add(getLblDescription());
            pnButtons.add(getLblMatches());
            pnButtons.add(getCbTeams());
            pnButtons.add(getCbPlayers());
            pnButtons.add(getCbTrainings());
            pnButtons.add(getCbMatches());
            pnButtons.add(getBtnRegister());
            pnButtons.add(getBtnReturn());
            pnButtons.add(getScpDescription());

        }
        return pnButtons;
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Register a Player Injury", "Log an injury after a medical revision", false);
        }
        return pnHeader;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel();
            pnFooter.setOpaque(false);
            pnFooter.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 0, 0, Branding.BORDER),
                new EmptyBorder(8, 0, 10, 0)));
            pnFooter.setLayout(new BorderLayout(0, 0));
            pnFooter.add(getLblFooter(), BorderLayout.CENTER);
        }
        return pnFooter;
    }

    private JLabel getLblFooter() {
        if (lblFooter == null) {
            lblFooter = new JLabel("IPS 2026 · Team PL11");
            lblFooter.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 11));
            lblFooter.setForeground(Branding.TEXT_MUTED);
            lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return lblFooter;
    }

    private JLabel getLblTeam() {
        if (lblTeam == null) {
            lblTeam = new JLabel("Team:");
            lblTeam.setLabelFor(getCbTeams());
            lblTeam.setForeground(new Color(0, 64, 128));
            lblTeam.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblTeam.setBounds(59, 56, 78, 20);
        }
        return lblTeam;
    }

    private JLabel getLblPlayer() {
        if (lblPlayer == null) {
            lblPlayer = new JLabel("Player:");
            lblPlayer.setLabelFor(getCbPlayers());
            lblPlayer.setForeground(new Color(0, 64, 128));
            lblPlayer.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblPlayer.setBounds(60, 107, 78, 20);
        }
        return lblPlayer;
    }

    private JLabel getLblTraining() {
        if (lblTraining == null) {
            lblTraining = new JLabel("Training:");
            lblTraining.setLabelFor(getCbTrainings());
            lblTraining.setForeground(new Color(0, 64, 128));
            lblTraining.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblTraining.setBounds(59, 161, 78, 20);
        }
        return lblTraining;
    }

    private JLabel getLblDescription() {
        if (lblDescription == null) {
            lblDescription = new JLabel("Injury description:");
            lblDescription.setForeground(new Color(0, 64, 128));
            lblDescription.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblDescription.setBounds(60, 263, 171, 20);
        }
        return lblDescription;
    }

    private JLabel getLblMatches() {
        if (lblMatches == null) {
            lblMatches = new JLabel("Matches:");
            lblMatches.setLabelFor(getCbMatches());
            lblMatches.setForeground(new Color(0, 64, 128));
            lblMatches.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblMatches.setBounds(60, 213, 78, 20);
        }
        return lblMatches;
    }

    private JComboBox getCbTeams() {
        if (cbTeams == null) {
            cbTeams = new JComboBox();
            cbTeams.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (cbTeams.getSelectedItem() != null) {
                        loadPlayers();
                        loadTrainings();
                        loadMatches();
                        
                        cbTrainings.setSelectedIndex(0);
                        cbMatches.setSelectedIndex(0);
                        cbTrainings.setEnabled(true);
                        cbMatches.setEnabled(true);
                    }
                }
            });
            cbTeams.setBounds(204, 50, 228, 38);
        }
        return cbTeams;
    }

    private JComboBox getCbPlayers() {
        if (cbPlayers == null) {
            cbPlayers = new JComboBox();
            cbPlayers.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadTrainings();
                    loadMatches();
                    
                    cbTrainings.setSelectedIndex(0);
                    cbMatches.setSelectedIndex(0);
                    cbTrainings.setEnabled(true);
                    cbMatches.setEnabled(true);
                }
            });
            cbPlayers.setBounds(204, 102, 228, 38);
        }
        return cbPlayers;
    }

    private JComboBox getCbTrainings() {
        if (cbTrainings == null) {
            cbTrainings = new JComboBox();
            cbTrainings.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (cbTrainings.getSelectedIndex() > 0) {
                        cbMatches.setSelectedIndex(0);
                        cbMatches.setEnabled(false);
                    } else {
                        cbMatches.setEnabled(true);
                    }
                }
            });
            cbTrainings.setBounds(204, 154, 287, 38);
        }
        return cbTrainings;
    }

    private JComboBox getCbMatches() {
        if (cbMatches == null) {
            cbMatches = new JComboBox();
            cbMatches.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (cbMatches.getSelectedIndex() > 0) {
                        cbTrainings.setSelectedIndex(0);
                        cbTrainings.setEnabled(false);
                    } else {
                        cbTrainings.setEnabled(true);
                    }
                }
            });
            cbMatches.setBounds(204, 206, 287, 38);
        }
        return cbMatches;
    }

    private JButton getBtnRegister() {
        if (btnRegister == null) {
            btnRegister = new JButton("Register");
            btnRegister.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!checkMatchOrTrainingSelected() || !checkDescriptionProvided()) {
                        return;
                    }
                    
                    if(registerInjury()) {
                        JOptionPane.showMessageDialog(RegisterInjuryWindow.this, "Injury registered successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);      

                    }
                }

            });
            btnRegister.setBounds(402, 417, 89, 25);
        }
        return btnRegister;
    }

    private JButton getBtnReturn() {
        if (btnReturn == null) {
            btnReturn = new JButton("Return");
            btnReturn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            btnReturn.setMnemonic('R');
            btnReturn.setBounds(303, 417, 89, 25);
        }
        return btnReturn;
    }

    private JScrollPane getScpDescription() {
        if (scpDescription == null) {
            scpDescription = new JScrollPane();
            scpDescription.setBounds(246, 263, 245, 130);
            scpDescription.setViewportView(getTxtaDescription_1());
        }
        return scpDescription;
    }

    private JTextArea getTxtaDescription_1() {
        if (txtrNoDescriptionProvided == null) {
            txtrNoDescriptionProvided = new JTextArea();
            txtrNoDescriptionProvided.setFont(new Font("Arial", Font.PLAIN, 12));
            txtrNoDescriptionProvided.setWrapStyleWord(true);
            txtrNoDescriptionProvided.setLineWrap(true);
        }
        return txtrNoDescriptionProvided;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialize() {
        try {
            facilityController.loadFacilities();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading facilities: " + e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }

        loadTeams();
        loadPlayers();
        loadMatches();
        loadTrainings();
    }

    public void loadTeams() {
        try {
            cbTeams.setModel(new DefaultComboBoxModel<>(teamController.getTeams().toArray(new TeamItem[0])));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading teams: " + e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPlayers() {
        try {
            int teamId = ((TeamItem) cbTeams.getSelectedItem()).getId();
            players = teamController.getPlayersByTeam(teamId);

            List<String> playerNames = new ArrayList<>();
            for (Employee player : players) {
                playerNames.add(player.getFirstName() + " " + player.getLastName());
            }
            cbPlayers.setModel(new DefaultComboBoxModel<>(playerNames.toArray(new String[0])));
            cbPlayers.setEnabled(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading players: " + e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTrainings() {
        try {
            trainings = trainingController
                .getTrainingSessionsByPlayer(players.get(cbPlayers.getSelectedIndex()).getId());

            trainings.sort(
                Comparator.comparing(TrainingSession::getDate).thenComparing(TrainingSession::getStartTime).reversed());

            List<String> trainingDescriptions = new ArrayList<>();
            trainingDescriptions.add("No training session selected");
            for (TrainingSession session : trainings) {
                String facility = getFacilityName((int) session.getFacility());
                trainingDescriptions.add(facility + " (" + session.getDate() + " / " + session.getStartTime() + ")");
            }

            cbTrainings.setModel(new DefaultComboBoxModel<>(trainingDescriptions.toArray(new String[0])));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading matches and trainings: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMatches() {
        try {
            int playerId = players.get(cbPlayers.getSelectedIndex()).getId();
            matches = matchController.getMatchesByPlayer(playerId);

            matches.sort(Comparator.comparing(Match::getMatchDate).thenComparing(Match::getStartTime).reversed());

            List<String> matchDescriptions = new ArrayList<>();
            matchDescriptions.add("No match selected");
            for (Match match : matches) {
                String facility = getFacilityName(match.getFacilityId());
                matchDescriptions.add(facility + " (" + match.getMatchDate() + " / " + match.getStartTime() + ")");
            }

            cbMatches.setModel(new DefaultComboBoxModel<>(matchDescriptions.toArray(new String[0])));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading matches: " + e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getFacilityName(int facility) {
        List<Facility> facilities = facilityController.getFacilities();
        for (Facility f : facilities) {
            if (f.getId() == facility) {
                return f.getName();
            }
        }
        return "No facility assigned";
    }

    private boolean checkMatchOrTrainingSelected() {
        if (cbTrainings.getSelectedIndex() == 0 && cbMatches.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select either a training session or a match.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean checkDescriptionProvided() {
        String description = txtrNoDescriptionProvided.getText().trim();
        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide a description of the injury.", "Description Required",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean registerInjury() {
        try {
            Injury injury = createInjuryFromInput();
            injuryController.registerInjury(injury);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error registering injury: " + e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private Injury createInjuryFromInput() {
        int playerId = players.get(cbPlayers.getSelectedIndex()).getId();
        String description = txtrNoDescriptionProvided.getText().trim();
        int trainingId = -1;
        int matchId = -1;
        LocalDate date;
        LocalTime time;
        int facilityId = -1;
        
        if(cbTrainings.getSelectedIndex() > 0) {
            TrainingSession selectedTraining = trainings.get(cbTrainings.getSelectedIndex() - 1);
            trainingId = (int) selectedTraining.getId();
            date = selectedTraining.getDate();
            time = selectedTraining.getStartTime();
            
        } else {
            Match selectedMatch = matches.get(cbMatches.getSelectedIndex() - 1);
            matchId = selectedMatch.getId();
            date = LocalDate.parse(selectedMatch.getMatchDate());
            time = selectedMatch.getStartTime();
        } 

        return new Injury(0, playerId, matchId, trainingId, description, date, false, doctorId, time);
    }

}
