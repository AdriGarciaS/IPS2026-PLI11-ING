package com.ips2026.pl11.view.doctor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.controller.injury.InjuryController;
import com.ips2026.pl11.controller.matches.ScheduleMatchController;
import com.ips2026.pl11.controller.reservation.FacilityReservationController;
import com.ips2026.pl11.controller.team.TeamCreationController;
import com.ips2026.pl11.controller.training.TrainingSessionController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.data.team.SportsEmployeeDAO;
import com.ips2026.pl11.data.team.TeamDAO;
import com.ips2026.pl11.data.training.TrainingSessionDAO;
import com.ips2026.pl11.model.injury.Injury;
import com.ips2026.pl11.model.matches.Match;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.model.team.SportsEmployee;
import com.ips2026.pl11.model.training.TrainingSession;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class UpdateInjuryWindow extends JDialog {

    private static final long serialVersionUID = 1L;

    private List<Injury> injuries = new ArrayList<>();
    private int doctorId;

    private HeaderPanel pnHeader;
    private JPanel pnButtons;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JPanel contentPane;
    private JLabel lblTeam;
    private JLabel lblPlayer;
    private JLabel lblDescription;
    private JButton btnUpdate;
    private JButton btnReturn;
    private JScrollPane scpDescription;
    private JTextArea txtaDescription;
    private JLabel lblFacility;
    private JLabel lblTime;
    private JLabel lblDate;
    private JTextField txtPlayer;
    private JTextField txtTeam;
    private JTextField txtFacility;
    private JTextField txtDate;
    private JTextField txtTime;
    private JSeparator separator;
    private JScrollPane spnNotes;
    private JTable tableInjuries;
    private JLabel lblInjuries;
    private JLabel lblNotes;
    private JScrollPane scpNotes;
    private JTextArea txtaNotes;
    private JCheckBox chckbxNewCheckBox;

    private InjuryController injuryController = new InjuryController();
    private EmployeeController employeeController = new EmployeeController(new EmployeeDAO());
    private TeamCreationController teamCreationController = new TeamCreationController(new SportsEmployeeDAO(),
        new TeamDAO());
    private TrainingSessionController trainingSessionController = new TrainingSessionController(
        new TrainingSessionDAO());
    private FacilityReservationController facilityController = new FacilityReservationController(new FacilityDAO(),
        new TeamUseDAO(), new ReservationDAO());
    private ScheduleMatchController matchController = new ScheduleMatchController();

    private DefaultTableModel injuriesModel = new DefaultTableModel(new Object[][] {},
        new String[] { "ID", "Date", "Player" }) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private DefaultTableModel evolutionModel = new DefaultTableModel(new Object[][] {},
        new String[] { "Date", "Notes" }) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JScrollPane spnHistory;
    private JTable tableNotes;
    private JLabel lblInjuryEvolution;

    /**
     * Create the dialog.
     */
    public UpdateInjuryWindow(JFrame parent, int doctorId) {
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
        Branding.setInitialSize(this, 900, 675, null);
        rootPane.setDefaultButton(getBtnUpdate());

        loadFacilitiesIntoController();

    }

    private void loadFacilitiesIntoController() {
        try {
            facilityController.loadFacilities();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading facilities: " + e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }

    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setBorder(new EmptyBorder(24, 24, 16, 24));
            pnButtons.setLayout(null);
            pnButtons.add(getLblTeam());
            pnButtons.add(getLblPlayer());
            pnButtons.add(getLblDescription());
            pnButtons.add(getBtnUpdate());
            pnButtons.add(getBtnReturn());
            pnButtons.add(getScpDescription());
            pnButtons.add(getLblFacility());
            pnButtons.add(getLblTime());
            pnButtons.add(getLblDate());
            pnButtons.add(getTxtPlayer());
            pnButtons.add(getTxtTeam());
            pnButtons.add(getTxtFacility());
            pnButtons.add(getTxtDate());
            pnButtons.add(getTxtTime());
            pnButtons.add(getSeparator());
            pnButtons.add(getSpnNotes());
            pnButtons.add(getLblInjuries());
            pnButtons.add(getLblNotes());
            pnButtons.add(getScpNotes());
            pnButtons.add(getChckbxNewCheckBox());
            pnButtons.add(getSpnHistory());
            pnButtons.add(getLblInjuryEvolution());

        }
        return pnButtons;
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Update a Player Injury",
                "Check and update the state of an injury after revision", false);
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
            lblTeam.setForeground(new Color(0, 64, 128));
            lblTeam.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblTeam.setBounds(500, 90, 78, 20);
        }
        return lblTeam;
    }

    private JLabel getLblPlayer() {
        if (lblPlayer == null) {
            lblPlayer = new JLabel("Player:");
            lblPlayer.setForeground(new Color(0, 64, 128));
            lblPlayer.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblPlayer.setBounds(500, 27, 78, 20);
        }
        return lblPlayer;
    }

    private JLabel getLblDescription() {
        if (lblDescription == null) {
            lblDescription = new JLabel("Injury description:");
            lblDescription.setForeground(new Color(0, 64, 128));
            lblDescription.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblDescription.setBounds(500, 229, 171, 20);
        }
        return lblDescription;
    }

    private JButton getBtnUpdate() {
        if (btnUpdate == null) {
            btnUpdate = new JButton("Update");
            btnUpdate.setEnabled(false);
            btnUpdate.setFont(new Font("Tahoma", Font.PLAIN, 12));
            btnUpdate.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (txtaNotes.getText().isBlank()) {
                        JOptionPane.showMessageDialog(UpdateInjuryWindow.this,
                            "Please enter evolution notes before updating the injury.", "Input Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Injury selectedInjury = null;
                    for (Injury injury : injuries) {
                        if (injury.getId() == (int) tableInjuries.getValueAt(tableInjuries.getSelectedRow(), 0)) {
                            selectedInjury = injury;
                            break;
                        }
                    }

                    if (selectedInjury != null) {
                        boolean isHealed = chckbxNewCheckBox.isSelected();
                        List<String[]> notes = new ArrayList<>(selectedInjury.getEvolutionNotes());
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
                        notes.add(new String[] { LocalDate.now().format(formatter).toString(), txtaNotes.getText() });

                        Injury newInjury = new Injury(0, selectedInjury.getPlayerId(), selectedInjury.getMatchId(),
                            selectedInjury.getTrainingId(), selectedInjury.getDescription(),
                            selectedInjury.getInjuryDate(), isHealed, selectedInjury.getDoctorId(),
                            selectedInjury.getInjuryTime(), notes);

                        try {
                            injuryController.registerInjury(newInjury);
                            JOptionPane.showMessageDialog(UpdateInjuryWindow.this, "Injury updated successfully.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                            ;
                            loadInjuries();
                            initializeFields();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(UpdateInjuryWindow.this,
                                "Error updating injury: " + ex.getMessage(), "Database Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(UpdateInjuryWindow.this, "No injury selected.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }

            });
            btnUpdate.setBounds(778, 497, 89, 25);
        }
        return btnUpdate;
    }

    private JButton getBtnReturn() {
        if (btnReturn == null) {
            btnReturn = new JButton("Return");
            btnReturn.setFont(new Font("Tahoma", Font.PLAIN, 12));
            btnReturn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            btnReturn.setMnemonic('R');
            btnReturn.setBounds(679, 497, 89, 25);
        }
        return btnReturn;
    }

    private JScrollPane getScpDescription() {
        if (scpDescription == null) {
            scpDescription = new JScrollPane();
            scpDescription.setBounds(500, 257, 319, 80);
            scpDescription.setViewportView(getTxtaDescription_1());
        }
        return scpDescription;
    }

    private JTextArea getTxtaDescription_1() {
        if (txtaDescription == null) {
            txtaDescription = new JTextArea();
            txtaDescription.setEditable(false);
            txtaDescription.setFont(new Font("Arial", Font.PLAIN, 12));
            txtaDescription.setWrapStyleWord(true);
            txtaDescription.setLineWrap(true);
        }
        return txtaDescription;
    }

    private JLabel getLblFacility() {
        if (lblFacility == null) {
            lblFacility = new JLabel("Facility:");
            lblFacility.setForeground(new Color(0, 64, 128));
            lblFacility.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblFacility.setBounds(696, 89, 78, 20);
        }
        return lblFacility;
    }

    private JLabel getLblTime() {
        if (lblTime == null) {
            lblTime = new JLabel("Time:");
            lblTime.setForeground(new Color(0, 64, 128));
            lblTime.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblTime.setBounds(695, 151, 78, 20);
        }
        return lblTime;
    }

    private JLabel getLblDate() {
        if (lblDate == null) {
            lblDate = new JLabel("Date:");
            lblDate.setForeground(new Color(0, 64, 128));
            lblDate.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblDate.setBounds(501, 151, 78, 20);
        }
        return lblDate;
    }

    private JTextField getTxtPlayer() {
        if (txtPlayer == null) {
            txtPlayer = new JTextField();
            txtPlayer.setEditable(false);
            txtPlayer.setBounds(502, 52, 171, 32);
            txtPlayer.setColumns(10);
        }
        return txtPlayer;
    }

    private JTextField getTxtTeam() {
        if (txtTeam == null) {
            txtTeam = new JTextField();
            txtTeam.setEditable(false);
            txtTeam.setColumns(10);
            txtTeam.setBounds(501, 112, 171, 32);
        }
        return txtTeam;
    }

    private JTextField getTxtFacility() {
        if (txtFacility == null) {
            txtFacility = new JTextField();
            txtFacility.setEditable(false);
            txtFacility.setColumns(10);
            txtFacility.setBounds(696, 112, 171, 32);
        }
        return txtFacility;
    }

    private JTextField getTxtDate() {
        if (txtDate == null) {
            txtDate = new JTextField();
            txtDate.setEditable(false);
            txtDate.setColumns(10);
            txtDate.setBounds(500, 176, 171, 32);
        }
        return txtDate;
    }

    private JTextField getTxtTime() {
        if (txtTime == null) {
            txtTime = new JTextField();
            txtTime.setEditable(false);
            txtTime.setColumns(10);
            txtTime.setBounds(696, 176, 123, 32);
        }
        return txtTime;
    }

    private JSeparator getSeparator() {
        if (separator == null) {
            separator = new JSeparator();
            separator.setOrientation(SwingConstants.VERTICAL);
            separator.setBounds(460, 21, 27, 293);
        }
        return separator;
    }

    private JScrollPane getSpnNotes() {
        if (spnNotes == null) {
            spnNotes = new JScrollPane();
            spnNotes.setBounds(21, 52, 397, 156);
            spnNotes.setViewportView(getTableInjuries());
        }
        return spnNotes;
    }

    private JTable getTableInjuries() {
        if (tableInjuries == null) {
            tableInjuries = new JTable();
            tableInjuries.setModel(injuriesModel);
            TableColumn column = tableInjuries.getColumnModel().getColumn(0);
            column.setMaxWidth(50);
            tableInjuries.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && tableInjuries.getSelectedRow() != -1) {
                    btnUpdate.setEnabled(true);

                    int selectedRow = tableInjuries.getSelectedRow();
                    Injury selectedInjury = injuries.get(selectedRow);
                    SportsEmployee player;

                    try {
                        player = teamCreationController.getAllPlayers().stream()
                            .filter(p -> p.getId() == selectedInjury.getPlayerId()).findFirst().orElse(null);
                    } catch (SQLException e1) {
                        JOptionPane.showMessageDialog(this, "Error finding player: " + e1.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String facilityName = findFacilityName(selectedInjury);

                    txtPlayer.setText(player.getFirstName() + " " + player.getLastName());
                    txtDate.setText(selectedInjury.getInjuryDate().toString());
                    txtaDescription.setText(selectedInjury.getDescription());
                    chckbxNewCheckBox.setSelected(selectedInjury.isHealed());

                    txtTeam.setText(player.getCurrentTeam());
                    txtFacility.setText(facilityName);
                    txtTime.setText(selectedInjury.getInjuryTime().toString());

                    loadEvolutionNotes(selectedInjury);

                }
            });
        }
        return tableInjuries;
    }

    private JScrollPane getSpnHistory() {
        if (spnHistory == null) {
            spnHistory = new JScrollPane();
            spnHistory.setBounds(21, 368, 849, 121);
            spnHistory.setViewportView(getTableNotes());
        }
        return spnHistory;
    }

    private JTable getTableNotes() {
        if (tableNotes == null) {
            tableNotes = new JTable();
            tableNotes.setModel(evolutionModel);
            TableColumn column = tableNotes.getColumnModel().getColumn(0);
            column.setMaxWidth(60);
        }
        return tableNotes;
    }

    private JLabel getLblInjuryEvolution() {
        if (lblInjuryEvolution == null) {
            lblInjuryEvolution = new JLabel("Injury evolution:");
            lblInjuryEvolution.setForeground(new Color(0, 64, 128));
            lblInjuryEvolution.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblInjuryEvolution.setBounds(21, 346, 171, 20);
        }
        return lblInjuryEvolution;
    }

    private JLabel getLblInjuries() {
        if (lblInjuries == null) {
            lblInjuries = new JLabel("Injuries:");
            lblInjuries.setForeground(new Color(0, 64, 128));
            lblInjuries.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblInjuries.setBounds(21, 27, 78, 20);
        }
        return lblInjuries;
    }

    private JLabel getLblNotes() {
        if (lblNotes == null) {
            lblNotes = new JLabel("Healing process notes:");
            lblNotes.setForeground(new Color(0, 64, 128));
            lblNotes.setFont(new Font("Tahoma", Font.BOLD, 16));
            lblNotes.setBounds(21, 229, 233, 20);
        }
        return lblNotes;
    }

    private JScrollPane getScpNotes() {
        if (scpNotes == null) {
            scpNotes = new JScrollPane();
            scpNotes.setBounds(21, 257, 397, 80);
            scpNotes.setViewportView(getTxtaDescription_1_1());
        }
        return scpNotes;
    }

    private JTextArea getTxtaDescription_1_1() {
        if (txtaNotes == null) {
            txtaNotes = new JTextArea();
            txtaNotes.setWrapStyleWord(true);
            txtaNotes.setLineWrap(true);
            txtaNotes.setFont(new Font("Arial", Font.PLAIN, 12));
        }
        return txtaNotes;
    }

    private JCheckBox getChckbxNewCheckBox() {
        if (chckbxNewCheckBox == null) {
            chckbxNewCheckBox = new JCheckBox("  Recovered?");
            chckbxNewCheckBox.setForeground(new Color(0, 64, 128));
            chckbxNewCheckBox.setFont(new Font("Tahoma", Font.BOLD, 16));
            chckbxNewCheckBox.setBounds(294, 227, 130, 25);
        }
        return chckbxNewCheckBox;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void loadInjuries() {
        try {
            injuriesModel.setRowCount(0);
            injuryController.loadInjuries();
            injuries = injuryController.findInjuriesByDoctor(doctorId);
            if (!injuries.isEmpty()) {
                for (Injury injury : injuries) {
                    String playerName = getPlayerName(injury);
                    injuriesModel.addRow(new Object[] { injury.getId(), injury.getInjuryDate(), playerName });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading injuries: " + e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }

    }

    private void loadEvolutionNotes(Injury injury) {
        evolutionModel.setRowCount(0);
        List<String[]> notes = injury.getEvolutionNotes();

        for (String[] note : notes) {
            if (note.length == 2) {
                evolutionModel.addRow(new Object[] { note[0], note[1] });
            }
        }
    }

    private void initializeFields() {
        txtPlayer.setText("");
        txtDate.setText("");
        txtaDescription.setText("");
        chckbxNewCheckBox.setSelected(false);
        txtTeam.setText("");
        txtFacility.setText("");
        txtTime.setText("");
        txtaNotes.setText("");
        evolutionModel.setRowCount(0);
        btnUpdate.setEnabled(false);
    }

    private String getPlayerName(Injury injury) throws SQLException {
        return employeeController.getEmployees().stream().filter(employee -> employee.getId() == injury.getPlayerId())
            .map(employee -> employee.getFirstName() + " " + employee.getLastName()).findFirst()
            .orElse("Unknown Player");
    }

    private String findFacilityName(Injury selectedInjury) {
        int matchId = selectedInjury.getMatchId();
        int trainingId = selectedInjury.getTrainingId();
        try {
            if (matchId != -1) {
                Optional<Match> om = matchController.findMatchById(matchId);
                if (om.isPresent()) {

                    Optional<Facility> of = facilityController.findFacilityById(om.get().getFacilityId());
                    if (of.isPresent()) {
                        return of.get().getName();
                    }
                }

            } else if (trainingId != -1) {
                Optional<TrainingSession> training = trainingSessionController.findTrainingById(trainingId);
                if (training.isPresent()) {
                    Optional<Facility> of = facilityController.findFacilityById((int) training.get().getFacility());
                    if (of.isPresent()) {
                        return of.get().getName();
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error finding facility: " + e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return "Unknown Facility";
    }

}
