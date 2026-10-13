package com.ips2026.pl11.view.slots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.slots.InterviewSlotsController;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.slots.InterviewSlotRecord;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.toedter.calendar.JDateChooser;

public class CreateInterviewSlotDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final InterviewSlotsController controller;
    private List<Employee> coachesList;
    private List<Employee> currentPlayersList;

    private Employee selectedCoach;
    private Employee selectedPlayer;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnMainContent;

    // Left Container: Coaches and Players Split
    private JPanel pnSelectionContainer;
    private JSplitPane splitCoachesPlayers;

    private JPanel pnCoachesContainer;
    private JScrollPane spCoaches;
    private JTable tblCoaches;
    private DefaultTableModel coachesModel;

    private JPanel pnPlayersContainer;
    private JScrollPane spPlayers;
    private JTable tblPlayers;
    private DefaultTableModel playersModel;

    // Center Panel: Slot Creation Form with JDateChooser & Time Spinners
    private JPanel pnFormContainer;
    private JTextField txtSelectedCoach;
    private JTextField txtSelectedPlayer;
    private JDateChooser dateChooser;
    private JSpinner spStartTime;
    private JSpinner spEndTime;
    private JButton btnSave;

    // Right Panel: Slots Table
    private JPanel pnSlotsContainer;
    private JScrollPane spSlots;
    private JTable tblSlots;
    private DefaultTableModel slotsModel;

    // Footer Panel
    private JPanel pnFooter;
    private JButton btnClose;

    public CreateInterviewSlotDialog(JFrame parent, InterviewSlotsController controller) {
        super(parent, "Manage Interview Availability", true);
        this.controller = controller;

        // Apply corporate window styling and icons
        Branding.applyTo(this);

        setSize(1220, 680);
        setLocationRelativeTo(parent);
        setResizable(true);

        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(Branding.BACKGROUND);
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);

        pnBody = new JPanel(new BorderLayout(0, 12));
        pnBody.setOpaque(false);
        pnBody.setBorder(new EmptyBorder(16, 20, 16, 20));
        pnBody.add(getPnMainContent(), BorderLayout.CENTER);
        pnBody.add(getPnFooter(), BorderLayout.SOUTH);

        contentPane.add(pnBody, BorderLayout.CENTER);

        loadCoachesTable();
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(
                    "Interview Availability Management",
                    "Select a coach, pick an assigned player, define availability date and times, and inspect scheduled slots"
            );
        }
        return pnHeader;
    }

    private JPanel getPnMainContent() {
        if (pnMainContent == null) {
            pnMainContent = new JPanel(new BorderLayout(14, 0));
            pnMainContent.setOpaque(false);

            pnMainContent.add(getPnSelectionContainer(), BorderLayout.WEST);
            pnMainContent.add(getPnFormContainer(), BorderLayout.CENTER);
            pnMainContent.add(getPnSlotsContainer(), BorderLayout.EAST);
        }
        return pnMainContent;
    }

    // --- LEFT CONTAINER: COACHES & PLAYERS ---
    private JPanel getPnSelectionContainer() {
        if (pnSelectionContainer == null) {
            pnSelectionContainer = new JPanel(new BorderLayout());
            pnSelectionContainer.setPreferredSize(new Dimension(360, 0));
            pnSelectionContainer.setOpaque(false);

            splitCoachesPlayers = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getPnCoachesContainer(), getPnPlayersContainer());
            splitCoachesPlayers.setResizeWeight(0.45);
            splitCoachesPlayers.setDividerSize(6);
            splitCoachesPlayers.setOpaque(false);
            splitCoachesPlayers.setBorder(null);

            pnSelectionContainer.add(splitCoachesPlayers, BorderLayout.CENTER);
        }
        return pnSelectionContainer;
    }

    private JPanel getPnCoachesContainer() {
        if (pnCoachesContainer == null) {
            pnCoachesContainer = new JPanel(new BorderLayout(0, 6));
            pnCoachesContainer.setOpaque(false);

            JLabel lbl = new JLabel("1. Select Coach");
            lbl.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            lbl.setForeground(Branding.NAVY);

            pnCoachesContainer.add(lbl, BorderLayout.NORTH);
            pnCoachesContainer.add(getSpCoaches(), BorderLayout.CENTER);
        }
        return pnCoachesContainer;
    }

    private JScrollPane getSpCoaches() {
        if (spCoaches == null) {
            spCoaches = new JScrollPane(getTblCoaches());
            spCoaches.setBorder(new LineBorder(Branding.BORDER, 1, true));
        }
        return spCoaches;
    }

    private JTable getTblCoaches() {
        if (tblCoaches == null) {
            String[] cols = {"ID", "Coach Name"};
            coachesModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblCoaches = new JTable(coachesModel);
            tblCoaches.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblCoaches.setRowHeight(24);
            tblCoaches.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
            tblCoaches.getTableHeader().setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
            tblCoaches.getTableHeader().setBackground(new Color(241, 245, 249));

            tblCoaches.getColumnModel().getColumn(0).setPreferredWidth(45);
            tblCoaches.getColumnModel().getColumn(1).setPreferredWidth(260);

            tblCoaches.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = tblCoaches.getSelectedRow();
                    if (row != -1 && coachesList != null && row < coachesList.size()) {
                        selectedCoach = coachesList.get(row);
                        getTxtSelectedCoach().setText(selectedCoach.getFirstName() + " " + selectedCoach.getLastName());
                        loadPlayersForCoach(selectedCoach.getId());
                    }
                }
            });
        }
        return tblCoaches;
    }

    private JPanel getPnPlayersContainer() {
        if (pnPlayersContainer == null) {
            pnPlayersContainer = new JPanel(new BorderLayout(0, 6));
            pnPlayersContainer.setOpaque(false);

            JLabel lbl = new JLabel("2. Select Assigned Player");
            lbl.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            lbl.setForeground(Branding.NAVY);

            pnPlayersContainer.add(lbl, BorderLayout.NORTH);
            pnPlayersContainer.add(getSpPlayers(), BorderLayout.CENTER);
        }
        return pnPlayersContainer;
    }

    private JScrollPane getSpPlayers() {
        if (spPlayers == null) {
            spPlayers = new JScrollPane(getTblPlayers());
            spPlayers.setBorder(new LineBorder(Branding.BORDER, 1, true));
        }
        return spPlayers;
    }

    private JTable getTblPlayers() {
        if (tblPlayers == null) {
            String[] cols = {"ID", "Player Name", "Position"};
            playersModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblPlayers = new JTable(playersModel);
            tblPlayers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblPlayers.setRowHeight(24);
            tblPlayers.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
            tblPlayers.getTableHeader().setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
            tblPlayers.getTableHeader().setBackground(new Color(241, 245, 249));

            tblPlayers.getColumnModel().getColumn(0).setPreferredWidth(35);
            tblPlayers.getColumnModel().getColumn(1).setPreferredWidth(170);
            tblPlayers.getColumnModel().getColumn(2).setPreferredWidth(95);

            tblPlayers.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = tblPlayers.getSelectedRow();
                    if (row != -1 && currentPlayersList != null && row < currentPlayersList.size()) {
                        selectedPlayer = currentPlayersList.get(row);
                        getTxtSelectedPlayer().setText(selectedPlayer.getFirstName() + " " + selectedPlayer.getLastName());
                        getBtnSave().setEnabled(true);
                        loadSlotsTable(selectedPlayer.getId());
                    } else {
                        selectedPlayer = null;
                        getTxtSelectedPlayer().setText("Select a player above");
                        getBtnSave().setEnabled(false);
                    }
                }
            });
        }
        return tblPlayers;
    }

    // --- CENTER CONTAINER: FORM WITH JDATECHOOSER AND TIME SPINNERS ---
    private JPanel getPnFormContainer() {
        if (pnFormContainer == null) {
            pnFormContainer = new JPanel(new BorderLayout(0, 10));
            pnFormContainer.setOpaque(false);
            pnFormContainer.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.BORDER, 1, true),
                    new EmptyBorder(14, 14, 14, 14)
            ));

            JLabel lblFormTitle = new JLabel("Create Availability Slot");
            lblFormTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
            lblFormTitle.setForeground(Branding.NAVY);

            JPanel pnInputs = new JPanel(new GridLayout(5, 2, 8, 12));
            pnInputs.setOpaque(false);

            pnInputs.add(createLabel("Coach:"));
            pnInputs.add(getTxtSelectedCoach());

            pnInputs.add(createLabel("Player:"));
            pnInputs.add(getTxtSelectedPlayer());

            pnInputs.add(createLabel("Slot Date:"));
            pnInputs.add(getDateChooser());

            pnInputs.add(createLabel("Start Time:"));
            pnInputs.add(getSpStartTime());

            pnInputs.add(createLabel("End Time:"));
            pnInputs.add(getSpEndTime());

            pnFormContainer.add(lblFormTitle, BorderLayout.NORTH);
            pnFormContainer.add(pnInputs, BorderLayout.CENTER);

            JPanel pnButtonWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            pnButtonWrap.setOpaque(false);
            pnButtonWrap.add(getBtnSave());
            pnFormContainer.add(pnButtonWrap, BorderLayout.SOUTH);
        }
        return pnFormContainer;
    }

    private JTextField getTxtSelectedCoach() {
        if (txtSelectedCoach == null) {
            txtSelectedCoach = createTextField("Select a coach first");
            txtSelectedCoach.setEditable(false);
            txtSelectedCoach.setBackground(new Color(241, 245, 249));
            txtSelectedCoach.setForeground(Branding.TEXT_MUTED);
        }
        return txtSelectedCoach;
    }

    private JTextField getTxtSelectedPlayer() {
        if (txtSelectedPlayer == null) {
            txtSelectedPlayer = createTextField("Select a player");
            txtSelectedPlayer.setEditable(false);
            txtSelectedPlayer.setBackground(new Color(241, 245, 249));
            txtSelectedPlayer.setForeground(Branding.TEXT_MUTED);
        }
        return txtSelectedPlayer;
    }

    private JDateChooser getDateChooser() {
        if (dateChooser == null) {
            dateChooser = new JDateChooser();
            dateChooser.setDateFormatString("yyyy-MM-dd");
            dateChooser.setDate(new Date()); // Default to today's date
            dateChooser.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
            dateChooser.setBackground(Color.WHITE);
            dateChooser.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
        }
        return dateChooser;
    }

    private JSpinner getSpStartTime() {
        if (spStartTime == null) {
            spStartTime = createTimeSpinner(10, 0);
        }
        return spStartTime;
    }

    private JSpinner getSpEndTime() {
        if (spEndTime == null) {
            spEndTime = createTimeSpinner(10, 30);
        }
        return spEndTime;
    }

    /**
     * Builds and styles a JSpinner for time selection (HH:mm) that advances minute by minute.
     */
    private JSpinner createTimeSpinner(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        SpinnerDateModel model = new SpinnerDateModel(cal.getTime(), null, null, Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(editor);

        model.setCalendarField(Calendar.MINUTE);

        editor.getTextField().setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        editor.getTextField().setBackground(Color.WHITE);
        editor.getTextField().setForeground(Color.BLACK);

        spinner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Branding.BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));

        return spinner;
    }

    private JButton getBtnSave() {
        if (btnSave == null) {
            btnSave = new JButton("Create Slot");
            btnSave.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            btnSave.setFocusPainted(false);
            btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnSave.setBackground(Branding.NAVY);
            btnSave.setForeground(Color.WHITE);
            btnSave.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.NAVY, 1, true),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)
            ));
            btnSave.setEnabled(false);

            btnSave.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (btnSave.isEnabled()) {
                        btnSave.setBackground(new Color(0x1F, 0x4E, 0x8C));
                        btnSave.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(Branding.GOLD, 1, true),
                                BorderFactory.createEmptyBorder(8, 20, 8, 20)
                        ));
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (btnSave.isEnabled()) {
                        btnSave.setBackground(Branding.NAVY);
                        btnSave.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(Branding.NAVY, 1, true),
                                BorderFactory.createEmptyBorder(8, 20, 8, 20)
                        ));
                    }
                }
            });

            btnSave.addActionListener(e -> saveSlot());
        }
        return btnSave;
    }

    // --- RIGHT CONTAINER: EXISTING SLOTS ---
    private JPanel getPnSlotsContainer() {
        if (pnSlotsContainer == null) {
            pnSlotsContainer = new JPanel(new BorderLayout(0, 8));
            pnSlotsContainer.setPreferredSize(new Dimension(420, 0));
            pnSlotsContainer.setOpaque(false);

            JLabel lblTableTitle = new JLabel("Existing Interview Slots");
            lblTableTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
            lblTableTitle.setForeground(Branding.NAVY);

            pnSlotsContainer.add(lblTableTitle, BorderLayout.NORTH);
            pnSlotsContainer.add(getSpSlots(), BorderLayout.CENTER);
        }
        return pnSlotsContainer;
    }

    private JScrollPane getSpSlots() {
        if (spSlots == null) {
            spSlots = new JScrollPane(getTblSlots());
            spSlots.setBorder(new LineBorder(Branding.BORDER, 1, true));
        }
        return spSlots;
    }

    private JTable getTblSlots() {
        if (tblSlots == null) {
            String[] columns = {"Player", "Date", "Start", "End", "Status"};
            slotsModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblSlots = new JTable(slotsModel);
            tblSlots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblSlots.setRowHeight(24);
            tblSlots.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
            tblSlots.getTableHeader().setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
            tblSlots.getTableHeader().setBackground(new Color(241, 245, 249));

            tblSlots.getColumnModel().getColumn(0).setPreferredWidth(130);
            tblSlots.getColumnModel().getColumn(1).setPreferredWidth(85);  
            tblSlots.getColumnModel().getColumn(2).setPreferredWidth(55);  
            tblSlots.getColumnModel().getColumn(3).setPreferredWidth(55); 
            tblSlots.getColumnModel().getColumn(4).setPreferredWidth(80);  
        }
        return tblSlots;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnFooter.setOpaque(false);
            pnFooter.add(getBtnClose());
        }
        return pnFooter;
    }

    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton("Close");
            btnClose.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            btnClose.setFocusPainted(false);
            btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnClose.setBackground(Color.WHITE);
            btnClose.setForeground(Branding.NAVY);
            btnClose.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));

            btnClose.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnClose.setBackground(new Color(241, 245, 249));
                    btnClose.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.GOLD, 1, true),
                            BorderFactory.createEmptyBorder(8, 16, 8, 16)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnClose.setBackground(Color.WHITE);
                    btnClose.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.BORDER, 1, true),
                            BorderFactory.createEmptyBorder(8, 16, 8, 16)
                    ));
                }
            });

            btnClose.addActionListener(e -> dispose());
        }
        return btnClose;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        label.setForeground(Branding.NAVY);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JTextField createTextField(String defaultText) {
        JTextField tf = new JTextField(defaultText);
        tf.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Branding.BORDER, 1, true),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
        return tf;
    }

    // --- DATA LOADING LOGIC ---
    private void loadCoachesTable() {
        coachesModel.setRowCount(0);
        try {
            coachesList = controller.getAllCoaches();
            if (coachesList != null) {
                for (Employee c : coachesList) {
                    coachesModel.addRow(new Object[]{c.getId(), c.getFirstName() + " " + c.getLastName()});
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading coaches: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPlayersForCoach(int coachId) {
        playersModel.setRowCount(0);
        selectedPlayer = null;
        getTxtSelectedPlayer().setText("Select a player");
        getBtnSave().setEnabled(false);

        try {
            currentPlayersList = controller.getPlayersByCoach(coachId);
            if (currentPlayersList != null) {
                for (Employee p : currentPlayersList) {
                    playersModel.addRow(new Object[]{p.getId(), p.getFirstName() + " " + p.getLastName(), p.getPosition()});
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading players for coach: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSlotsTable(int playerId) {
        slotsModel.setRowCount(0);
        try {
            List<InterviewSlotRecord> slots = controller.getAllSlots();
            for (InterviewSlotRecord s : slots) {
                slotsModel.addRow(new Object[]{
                        s.getPlayerName(), 
                        s.getDate(),
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getStatus()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading interview slots: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSlot() {
        if (selectedCoach == null) {
            JOptionPane.showMessageDialog(this, "Please select a coach first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedPlayer == null) {
            JOptionPane.showMessageDialog(this, "Please select an assigned player.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date pickedDate = getDateChooser().getDate();
        if (pickedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid date from the calendar.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Convert Date to LocalDate
            LocalDate date = pickedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Extract times from spinners
            Date startDate = (Date) getSpStartTime().getValue();
            Date endDate = (Date) getSpEndTime().getValue();

            LocalTime start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
            LocalTime end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);

            if (!end.isAfter(start)) {
                JOptionPane.showMessageDialog(this, "End time must be strictly after start time.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            controller.createInterviewSlot(selectedPlayer.getId(), selectedCoach.getId(), date, start, end);

            JOptionPane.showMessageDialog(this, "Interview slot registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSlotsTable(selectedPlayer.getId());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Constraint Violation", JOptionPane.ERROR_MESSAGE);
        }
    }
}