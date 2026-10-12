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
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.slots.InterviewSlotsController;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.slots.InterviewSlotRecord;


public class CreateInterviewSlotDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final InterviewSlotsController controller;
    private final int coachId;
    private final List<Employee> teamPlayers;

    private JPanel contentPane;
    private JPanel pnHeader;
    private JPanel pnCenter;

    // Left Panel: Players Table
    private JPanel pnPlayersContainer;
    private JScrollPane spPlayers;
    private JTable tblPlayers;
    private DefaultTableModel playersModel;

    // Center Panel: Slot Form
    private JPanel pnFormContainer;
    private JTextField txtSelectedPlayer;
    private JTextField txtDate;
    private JTextField txtStartTime;
    private JTextField txtEndTime;
    private JButton btnSave;

    // Right Panel: Slots Table
    private JPanel pnSlotsContainer;
    private JScrollPane spSlots;
    private JTable tblSlots;
    private DefaultTableModel slotsModel;

    // Footer
    private JPanel pnFooter;
    private JButton btnClose;

    private Employee selectedPlayer;

    public CreateInterviewSlotDialog(JFrame parent, InterviewSlotsController controller, int coachId, List<Employee> players) {
        super(parent, "Manage Interview Availability", true);
        this.controller = controller;
        this.coachId = coachId;
        this.teamPlayers = players;

        setSize(1150, 580);
        setLocationRelativeTo(parent);
        setResizable(true);

        contentPane = new JPanel(new BorderLayout(0, 12));
        contentPane.setBackground(new Color(248, 250, 252));
        contentPane.setBorder(new EmptyBorder(16, 20, 16, 20));
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnCenter(), BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);

        loadPlayersTable();
    }

    private JPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new JPanel(new BorderLayout(0, 4));
            pnHeader.setOpaque(false);
            pnHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

            JLabel lblTitle = new JLabel("Interview Availability Management");
            lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
            lblTitle.setForeground(new Color(30, 41, 59));

            JLabel lblSubtitle = new JLabel("Select a player from the left, define time slot details, and review scheduled slots");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSubtitle.setForeground(new Color(100, 116, 139));
            lblSubtitle.setBorder(new EmptyBorder(0, 0, 8, 0));

            pnHeader.add(lblTitle, BorderLayout.NORTH);
            pnHeader.add(lblSubtitle, BorderLayout.SOUTH);
        }
        return pnHeader;
    }

    private JPanel getPnCenter() {
        if (pnCenter == null) {
            pnCenter = new JPanel(new BorderLayout(14, 0));
            pnCenter.setOpaque(false);

            pnCenter.add(getPnPlayersContainer(), BorderLayout.WEST);
            pnCenter.add(getPnFormContainer(), BorderLayout.CENTER);
            pnCenter.add(getPnSlotsContainer(), BorderLayout.EAST);
        }
        return pnCenter;
    }
    
    private JPanel getPnPlayersContainer() {
        if (pnPlayersContainer == null) {
            pnPlayersContainer = new JPanel(new BorderLayout(0, 8));
            pnPlayersContainer.setPreferredSize(new Dimension(320, 0));
            pnPlayersContainer.setOpaque(false);

            JLabel lbl = new JLabel("Team Players");
            lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            lbl.setForeground(new Color(51, 65, 85));

            pnPlayersContainer.add(lbl, BorderLayout.NORTH);
            pnPlayersContainer.add(getSpPlayers(), BorderLayout.CENTER);
        }
        return pnPlayersContainer;
    }

    private JScrollPane getSpPlayers() {
        if (spPlayers == null) {
            spPlayers = new JScrollPane(getTblPlayers());
            spPlayers.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        }
        return spPlayers;
    }

    private JTable getTblPlayers() {
        if (tblPlayers == null) {
            String[] cols = {"ID", "Name", "Position"};
            playersModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblPlayers = new JTable(playersModel);
            tblPlayers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblPlayers.setRowHeight(25);
            tblPlayers.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tblPlayers.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            tblPlayers.getTableHeader().setBackground(new Color(241, 245, 249));

            tblPlayers.getColumnModel().getColumn(0).setPreferredWidth(35);
            tblPlayers.getColumnModel().getColumn(1).setPreferredWidth(180);
            tblPlayers.getColumnModel().getColumn(2).setPreferredWidth(85);

            tblPlayers.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = tblPlayers.getSelectedRow();
                    if (row != -1 && row < teamPlayers.size()) {
                        selectedPlayer = teamPlayers.get(row);
                        getTxtSelectedPlayer().setText(selectedPlayer.getFirstName() + " " + selectedPlayer.getLastName());
                        getBtnSave().setEnabled(true);
                        loadSlotsTable(selectedPlayer.getId());
                    }
                }
            });
        }
        return tblPlayers;
    }

    private JPanel getPnFormContainer() {
        if (pnFormContainer == null) {
            pnFormContainer = new JPanel(new BorderLayout(0, 10));
            pnFormContainer.setOpaque(false);
            pnFormContainer.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(14, 14, 14, 14)
            ));

            JLabel lblFormTitle = new JLabel("Create Availability Slot");
            lblFormTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            lblFormTitle.setForeground(new Color(51, 65, 85));

            JPanel pnInputs = new JPanel(new GridLayout(4, 2, 8, 12));
            pnInputs.setOpaque(false);

            pnInputs.add(crearLabel("Selected:"));
            pnInputs.add(getTxtSelectedPlayer());

            pnInputs.add(crearLabel("Date (YYYY-MM-DD):"));
            pnInputs.add(getTxtDate());

            pnInputs.add(crearLabel("Start (HH:MM):"));
            pnInputs.add(getTxtStartTime());

            pnInputs.add(crearLabel("End (HH:MM):"));
            pnInputs.add(getTxtEndTime());

            pnFormContainer.add(lblFormTitle, BorderLayout.NORTH);
            pnFormContainer.add(pnInputs, BorderLayout.CENTER);

            JPanel pnButtonWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            pnButtonWrap.setOpaque(false);
            pnButtonWrap.add(getBtnSave());
            pnFormContainer.add(pnButtonWrap, BorderLayout.SOUTH);
        }
        return pnFormContainer;
    }

    private JTextField getTxtSelectedPlayer() {
        if (txtSelectedPlayer == null) {
            txtSelectedPlayer = crearTextField("Select a player from left");
            txtSelectedPlayer.setEditable(false);
            txtSelectedPlayer.setBackground(new Color(241, 245, 249));
        }
        return txtSelectedPlayer;
    }

    private JTextField getTxtDate() {
        if (txtDate == null) {
            txtDate = crearTextField(LocalDate.now().toString());
        }
        return txtDate;
    }

    private JTextField getTxtStartTime() {
        if (txtStartTime == null) {
            txtStartTime = crearTextField("10:00");
        }
        return txtStartTime;
    }

    private JTextField getTxtEndTime() {
        if (txtEndTime == null) {
            txtEndTime = crearTextField("10:30");
        }
        return txtEndTime;
    }

    private JButton getBtnSave() {
        if (btnSave == null) {
            btnSave = new JButton("Create Slot");
            btnSave.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            btnSave.setFocusPainted(false);
            btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSave.setBackground(new Color(24, 76, 120));
            btnSave.setForeground(Color.WHITE);
            btnSave.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            btnSave.setEnabled(false);

            btnSave.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (btnSave.isEnabled()) btnSave.setBackground(new Color(32, 101, 160));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (btnSave.isEnabled()) btnSave.setBackground(new Color(24, 76, 120));
                }
            });

            btnSave.addActionListener(e -> saveSlot());
        }
        return btnSave;
    }

    private JPanel getPnSlotsContainer() {
        if (pnSlotsContainer == null) {
            pnSlotsContainer = new JPanel(new BorderLayout(0, 8));
            pnSlotsContainer.setPreferredSize(new Dimension(440, 0));
            pnSlotsContainer.setOpaque(false);

            JLabel lblTableTitle = new JLabel("Existing Interview Slots");
            lblTableTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            lblTableTitle.setForeground(new Color(51, 65, 85));

            pnSlotsContainer.add(lblTableTitle, BorderLayout.NORTH);
            pnSlotsContainer.add(getSpSlots(), BorderLayout.CENTER);
        }
        return pnSlotsContainer;
    }

    private JScrollPane getSpSlots() {
        if (spSlots == null) {
            spSlots = new JScrollPane(getTblSlots());
            spSlots.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
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
            tblSlots.setRowHeight(25);
            tblSlots.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tblSlots.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            tblSlots.getTableHeader().setBackground(new Color(241, 245, 249));

            tblSlots.getColumnModel().getColumn(0).setPreferredWidth(140);
            tblSlots.getColumnModel().getColumn(1).setPreferredWidth(90);  
            tblSlots.getColumnModel().getColumn(2).setPreferredWidth(60);  
            tblSlots.getColumnModel().getColumn(3).setPreferredWidth(60); 
            tblSlots.getColumnModel().getColumn(4).setPreferredWidth(85);  
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
            btnClose.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            btnClose.setFocusPainted(false);
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.setBackground(Color.RED);
            btnClose.setForeground(new Color(51, 65, 85));
            btnClose.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1, true),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));
            btnClose.addActionListener(e -> dispose());
        }
        return btnClose;
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        label.setForeground(new Color(51, 65, 85));
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JTextField crearTextField(String defaultText) {
        JTextField tf = new JTextField(defaultText);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
        return tf;
    }

    private void loadPlayersTable() {
        playersModel.setRowCount(0);
        if (teamPlayers != null) {
            for (Employee p : teamPlayers) {
                playersModel.addRow(new Object[]{p.getId(), p.getFirstName() + " " + p.getLastName(), p.getPosition()});
            }
            if (!teamPlayers.isEmpty()) {
                getTblPlayers().setRowSelectionInterval(0, 0);
            }
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
            JOptionPane.showMessageDialog(this,
                    "Error loading interview slots: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSlot() {
        if (selectedPlayer == null) {
            JOptionPane.showMessageDialog(this, "Please select a player from the table on the left.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(getTxtDate().getText().trim());
            LocalTime start = LocalTime.parse(getTxtStartTime().getText().trim());
            LocalTime end = LocalTime.parse(getTxtEndTime().getText().trim());

            controller.createInterviewSlot(selectedPlayer.getId(), coachId, date, start, end);

            JOptionPane.showMessageDialog(this, "Interview slot registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSlotsTable(selectedPlayer.getId());

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date/time format. Please use YYYY-MM-DD and HH:MM.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Constraint Violation", JOptionPane.ERROR_MESSAGE);
        }
    }
}