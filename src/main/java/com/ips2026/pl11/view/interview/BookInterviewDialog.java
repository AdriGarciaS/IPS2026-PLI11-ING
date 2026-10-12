package com.ips2026.pl11.view.interview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
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
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.interview.BookInterviewController;
import com.ips2026.pl11.model.interview.AvailableSlotRecord;

public class BookInterviewDialog extends JDialog{

	private static final long serialVersionUID = 1L;

    private final BookInterviewController controller;
    private final List<AvailableSlotRecord> currentSlots = new ArrayList<>();
    private AvailableSlotRecord selectedSlot = null;

    private JTable tblSlots;
    private DefaultTableModel slotsModel;
    private JTextField txtPlayer;
    private JTextField txtDateTime;
    private JTextField txtMediaName;
    private JTextField txtMediaContact;
    private JButton btnConfirmBooking;

    public BookInterviewDialog(JFrame parent) {
        super(parent, "Book Interview Slot - Director of Communications", true);
        this.controller = new BookInterviewController();

        initComponents();
        loadAvailableSlots();
        setSize(850, 520);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        getContentPane().setLayout(new BorderLayout(10, 10));

        // Header panel
        JPanel pnHeader = new JPanel(new BorderLayout());
        pnHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        pnHeader.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Book Player Interview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel lblSubtitle = new JLabel("Select an available slot, enter media information, and confirm assignment.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(Color.GRAY);

        pnHeader.add(lblTitle, BorderLayout.NORTH);
        pnHeader.add(lblSubtitle, BorderLayout.SOUTH);
        getContentPane().add(pnHeader, BorderLayout.NORTH);

        // Center content panel (Table on the left, Form on the right)
        JPanel pnCenter = new JPanel(new BorderLayout(15, 0));
        pnCenter.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Table setup
        String[] columns = {"ID", "Player", "Date", "Start", "End"};
        slotsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblSlots = new JTable(slotsModel);
        tblSlots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSlots.setRowHeight(24);
        tblSlots.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));

        // Hide technical ID column from display
        tblSlots.getColumnModel().getColumn(0).setMinWidth(0);
        tblSlots.getColumnModel().getColumn(0).setMaxWidth(0);
        tblSlots.getColumnModel().getColumn(0).setPreferredWidth(0);

        tblSlots.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSlotSelection();
            }
        });

        JScrollPane spTable = new JScrollPane(tblSlots);
        spTable.setPreferredSize(new Dimension(460, 300));
        spTable.setBorder(BorderFactory.createTitledBorder("Available Slots"));
        pnCenter.add(spTable, BorderLayout.CENTER);

        // Media details form panel
        JPanel pnForm = new JPanel(new GridBagLayout());
        pnForm.setPreferredSize(new Dimension(320, 300));
        pnForm.setBorder(BorderFactory.createTitledBorder("Interview & Media Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPlayer = new JTextField();
        txtPlayer.setEditable(false);
        txtDateTime = new JTextField();
        txtDateTime.setEditable(false);
        txtMediaName = new JTextField();
        txtMediaContact = new JTextField();

        // Row 0: Selected Player
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnForm.add(new JLabel("Selected Player:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnForm.add(txtPlayer, gbc);

        // Row 1: Time slot
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnForm.add(new JLabel("Slot Time:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnForm.add(txtDateTime, gbc);

        // Row 2: Media name
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnForm.add(new JLabel("Media Name *:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnForm.add(txtMediaName, gbc);

        // Row 3: Media contact / Journalist
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        pnForm.add(new JLabel("Media Contact:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnForm.add(txtMediaContact, gbc);

        // Row 4: Book button
        btnConfirmBooking = new JButton("Confirm Booking");
        btnConfirmBooking.setEnabled(false);
        btnConfirmBooking.addActionListener(e -> bookInterview());
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.insets = new Insets(15, 6, 6, 6);
        pnForm.add(btnConfirmBooking, gbc);

        pnCenter.add(pnForm, BorderLayout.EAST);
        getContentPane().add(pnCenter, BorderLayout.CENTER);

        // Footer panel
        JPanel pnFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        pnFooter.add(btnClose);
        getContentPane().add(pnFooter, BorderLayout.SOUTH);
    }

    private void handleSlotSelection() {
        int selectedRow = tblSlots.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentSlots.size()) {
            selectedSlot = currentSlots.get(selectedRow);
            txtPlayer.setText(selectedSlot.getPlayerName());
            txtDateTime.setText(selectedSlot.getDate() + " (" + selectedSlot.getStartTime() + " - " + selectedSlot.getEndTime() + ")");
            btnConfirmBooking.setEnabled(true);
        } else {
            selectedSlot = null;
            txtPlayer.setText("");
            txtDateTime.setText("");
            btnConfirmBooking.setEnabled(false);
        }
    }

    private void loadAvailableSlots() {
        slotsModel.setRowCount(0);
        currentSlots.clear();
        try {
            List<AvailableSlotRecord> slots = controller.loadAvailableSlots();
            currentSlots.addAll(slots);

            for (AvailableSlotRecord slot : slots) {
                slotsModel.addRow(new Object[]{
                        slot.getId(),
                        slot.getPlayerName(),
                        slot.getDate(),
                        slot.getStartTime(),
                        slot.getEndTime()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading available interview slots: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bookInterview() {
        String mediaName = txtMediaName.getText().trim();
        String mediaContact = txtMediaContact.getText().trim();

        if (selectedSlot == null) {
            JOptionPane.showMessageDialog(this, "Please select an available slot.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (mediaName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide the media outlet name.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            txtMediaName.requestFocus();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Assigning this slot to '" + mediaName + "' will delete all other slots for " +
                selectedSlot.getPlayerName() + " on " + selectedSlot.getDate() + ".\nDo you want to proceed?",
                "Confirm Interview Assignment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controller.bookInterview(selectedSlot, mediaName, mediaContact);

            JOptionPane.showMessageDialog(this,
                    "Interview assigned successfully to " + mediaName + ".\nRemaining slots for that day have been removed.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Reset form fields
            txtMediaName.setText("");
            txtMediaContact.setText("");
            txtPlayer.setText("");
            txtDateTime.setText("");
            btnConfirmBooking.setEnabled(false);
            selectedSlot = null;

            // Refresh table to reflect updated database state
            loadAvailableSlots();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Warning", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to book interview slot: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
