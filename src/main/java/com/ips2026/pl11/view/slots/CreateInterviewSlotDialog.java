package com.ips2026.pl11.view.slots;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
    private JSplitPane splitPane;

    // Left side: Form
    private JPanel pnFormContainer;
    private JPanel pnFormFields;
    private JComboBox<EmployeeItem> cbPlayers;
    private JTextField txtDate;
    private JTextField txtStartTime;
    private JTextField txtEndTime;
    private JButton btnSave;

    // Right side: Table
    private JPanel pnTableContainer;
    private JScrollPane spTable;
    private JTable tblSlots;
    private DefaultTableModel tableModel;

    // Bottom actions
    private JPanel pnFooter;
    private JButton btnClose;

    public CreateInterviewSlotDialog(JFrame parent, InterviewSlotsController controller, int coachId, List<Employee> players) {
        super(parent, "Manage Interview Slots", true);
        this.controller = controller;
        this.coachId = coachId;
        this.teamPlayers = players;

        setSize(980, 560);
        setLocationRelativeTo(parent);
        setResizable(true);

        contentPane = new JPanel(new BorderLayout(0, 12));
        contentPane.setBackground(new Color(248, 250, 252));
        contentPane.setBorder(new EmptyBorder(16, 20, 16, 20));
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getSplitPane(), BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);

        loadSlotsTable();
    }

    private JPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new JPanel(new BorderLayout(0, 4));
            pnHeader.setOpaque(false);
            pnHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

            JLabel lblTitle = new JLabel("Interview Availability Management");
            lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
            lblTitle.setForeground(new Color(30, 41, 59));

            JLabel lblSubtitle = new JLabel("Create new availability slots for players and review previously scheduled slots");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSubtitle.setForeground(new Color(100, 116, 139));
            lblSubtitle.setBorder(new EmptyBorder(0, 0, 8, 0));

            pnHeader.add(lblTitle, BorderLayout.NORTH);
            pnHeader.add(lblSubtitle, BorderLayout.SOUTH);
        }
        return pnHeader;
    }

    private JSplitPane getSplitPane() {
        if (splitPane == null) {
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getPnFormContainer(), getPnTableContainer());
            splitPane.setResizeWeight(0.38);
            splitPane.setDividerSize(8);
            splitPane.setOpaque(false);
            splitPane.setBorder(null);
        }
        return splitPane;
    }

    // --- FORM (LEFT PANEL) ---
    private JPanel getPnFormContainer() {
        if (pnFormContainer == null) {
            pnFormContainer = new JPanel(new BorderLayout(0, 12));
            pnFormContainer.setOpaque(false);
            pnFormContainer.setBorder(new EmptyBorder(0, 0, 0, 12));

            JLabel lblFormTitle = new JLabel("New Slot Details");
            lblFormTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
            lblFormTitle.setForeground(new Color(51, 65, 85));

            pnFormContainer.add(lblFormTitle, BorderLayout.NORTH);
            pnFormContainer.add(getPnFormFields(), BorderLayout.CENTER);

            JPanel pnButtonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnButtonWrapper.setOpaque(false);
            pnButtonWrapper.add(getBtnSave());
            pnFormContainer.add(pnButtonWrapper, BorderLayout.SOUTH);
        }
        return pnFormContainer;
    }

    private JPanel getPnFormFields() {
        if (pnFormFields == null) {
            pnFormFields = new JPanel(new GridLayout(4, 2, 8, 12));
            pnFormFields.setOpaque(false);

            pnFormFields.add(crearLabel("Player:"));
            pnFormFields.add(getCbPlayers());

            pnFormFields.add(crearLabel("Date (YYYY-MM-DD):"));
            pnFormFields.add(getTxtDate());

            pnFormFields.add(crearLabel("Start Time (HH:MM):"));
            pnFormFields.add(getTxtStartTime());

            pnFormFields.add(crearLabel("End Time (HH:MM):"));
            pnFormFields.add(getTxtEndTime());
        }
        return pnFormFields;
    }

    private JComboBox<EmployeeItem> getCbPlayers() {
        if (cbPlayers == null) {
            cbPlayers = new JComboBox<>();
            cbPlayers.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cbPlayers.setBackground(Color.WHITE);
            cbPlayers.setForeground(Color.BLACK);
            cbPlayers.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));

            if (teamPlayers != null) {
                for (Employee emp : teamPlayers) {
                    cbPlayers.addItem(new EmployeeItem(emp.getId(), emp.getFirstName() + " " + emp.getLastName()));
                }
            }

            cbPlayers.addActionListener(e -> loadSlotsTable());
        }
        return cbPlayers;
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
            btnSave.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

            btnSave.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnSave.setBackground(new Color(32, 101, 160));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnSave.setBackground(new Color(24, 76, 120));
                }
            });

            btnSave.addActionListener(e -> saveSlot());
        }
        return btnSave;
    }

    // --- TABLE (RIGHT PANEL) ---
    private JPanel getPnTableContainer() {
        if (pnTableContainer == null) {
            pnTableContainer = new JPanel(new BorderLayout(0, 10));
            pnTableContainer.setOpaque(false);
            pnTableContainer.setBorder(new EmptyBorder(0, 12, 0, 0));

            JLabel lblTableTitle = new JLabel("Existing Interview Slots");
            lblTableTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
            lblTableTitle.setForeground(new Color(51, 65, 85));

            pnTableContainer.add(lblTableTitle, BorderLayout.NORTH);
            pnTableContainer.add(getSpTable(), BorderLayout.CENTER);
        }
        return pnTableContainer;
    }

    private JScrollPane getSpTable() {
        if (spTable == null) {
            spTable = new JScrollPane(getTblSlots());
            spTable.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        }
        return spTable;
    }

    private JTable getTblSlots() {
        if (tblSlots == null) {
            String[] columns = {"ID", "Player", "Date", "Start", "End", "Status"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblSlots = new JTable(tableModel);
            tblSlots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblSlots.setRowHeight(25);
            tblSlots.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tblSlots.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            tblSlots.getTableHeader().setBackground(new Color(241, 245, 249));

            tblSlots.getColumnModel().getColumn(0).setPreferredWidth(40);
            tblSlots.getColumnModel().getColumn(1).setPreferredWidth(140);
            tblSlots.getColumnModel().getColumn(2).setPreferredWidth(90);
            tblSlots.getColumnModel().getColumn(3).setPreferredWidth(60);
            tblSlots.getColumnModel().getColumn(4).setPreferredWidth(60);
            tblSlots.getColumnModel().getColumn(5).setPreferredWidth(90);
        }
        return tblSlots;
    }

    // --- FOOTER ---
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
            btnClose.setBackground(Color.WHITE);
            btnClose.setForeground(new Color(51, 65, 85));
            btnClose.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1, true),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));
            btnClose.addActionListener(e -> dispose());
        }
        return btnClose;
    }

    // --- HELPERS & ACTIONS ---
    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        label.setForeground(new Color(51, 65, 85));
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JTextField crearTextField(String defaultText) {
        JTextField tf = new JTextField(defaultText);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }

    private void loadSlotsTable() {
        tableModel.setRowCount(0);
        EmployeeItem selected = (EmployeeItem) getCbPlayers().getSelectedItem();
        int playerId = (selected != null) ? selected.id : 0;

        try {
            List<InterviewSlotRecord> slots = controller.getSlotsByPlayer(playerId);
            for (InterviewSlotRecord s : slots) {
                tableModel.addRow(new Object[]{
                        s.getId(),
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
        EmployeeItem selected = (EmployeeItem) getCbPlayers().getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a player first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(getTxtDate().getText().trim());
            LocalTime start = LocalTime.parse(getTxtStartTime().getText().trim());
            LocalTime end = LocalTime.parse(getTxtEndTime().getText().trim());

            controller.createInterviewSlot(selected.id, coachId, date, start, end);

            JOptionPane.showMessageDialog(this, "Interview slot registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            loadSlotsTable();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date/time format. Please use YYYY-MM-DD and HH:MM.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Constraint Violation", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class EmployeeItem {
        final int id;
        final String displayName;

        EmployeeItem(int id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }


}
