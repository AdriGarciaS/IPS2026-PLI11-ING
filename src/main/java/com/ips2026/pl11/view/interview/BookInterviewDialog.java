package com.ips2026.pl11.view.interview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.interview.BookInterviewController;
import com.ips2026.pl11.model.interview.AvailableSlotRecord;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class BookInterviewDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final BookInterviewController controller;
    private final List<AvailableSlotRecord> currentSlots = new ArrayList<>();
    private AvailableSlotRecord selectedSlot = null;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;

    private JTable tblSlots;
    private DefaultTableModel slotsModel;
    private JTextField txtPlayer;
    private JTextField txtDateTime;
    private JTextField txtMediaName;
    private JTextField txtMediaContact;
    private JButton btnConfirmBooking;
    private JButton btnClose;

    public BookInterviewDialog(JFrame parent) {
        super(parent, "Book Interview Slot - Director of Communications", true);
        this.controller = new BookInterviewController();

        // Aplicar Branding
        Branding.applyTo(this);

        initComponents();
        loadAvailableSlots();
        setSize(880, 560);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(Branding.BACKGROUND);
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);

        // Panel de contenido interno
        pnBody = new JPanel(new BorderLayout(16, 12));
        pnBody.setOpaque(false);
        pnBody.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Centro: Tabla y formulario
        JPanel pnCenter = new JPanel(new BorderLayout(16, 0));
        pnCenter.setOpaque(false);

        // Tabla de slots disponibles
        String[] columns = {"ID", "Player", "Date", "Start", "End"};
        slotsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblSlots = new JTable(slotsModel);
        tblSlots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSlots.setRowHeight(25);
        tblSlots.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        tblSlots.getTableHeader().setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        tblSlots.getTableHeader().setBackground(new Color(241, 245, 249));

        tblSlots.getColumnModel().getColumn(0).setMinWidth(0);
        tblSlots.getColumnModel().getColumn(0).setMaxWidth(0);
        tblSlots.getColumnModel().getColumn(0).setPreferredWidth(0);

        tblSlots.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSlotSelection();
            }
        });

        JScrollPane spTable = new JScrollPane(tblSlots);
        spTable.setPreferredSize(new Dimension(470, 300));
        spTable.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Branding.BORDER, 1, true),
                "Available Slots",
                0, 0,
                new Font(Branding.FONT_FAMILY, Font.BOLD, 12),
                Branding.NAVY
        ));
        pnCenter.add(spTable, BorderLayout.CENTER);

        // Formulario de asignación a medios
        JPanel pnForm = new JPanel(new GridBagLayout());
        pnForm.setOpaque(false);
        pnForm.setPreferredSize(new Dimension(340, 300));
        pnForm.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Branding.BORDER, 1, true),
                "Interview & Media Details",
                0, 0,
                new Font(Branding.FONT_FAMILY, Font.BOLD, 12),
                Branding.NAVY
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPlayer = crearTextField(false);
        txtDateTime = crearTextField(false);
        txtMediaName = crearTextField(true);
        txtMediaContact = crearTextField(true);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnForm.add(crearLabel("Selected Player:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnForm.add(txtPlayer, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnForm.add(crearLabel("Slot Time:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnForm.add(txtDateTime, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnForm.add(crearLabel("Media Name *:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnForm.add(txtMediaName, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        pnForm.add(crearLabel("Media Contact:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnForm.add(txtMediaContact, gbc);

        btnConfirmBooking = new JButton("Confirm Booking");
        btnConfirmBooking.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        btnConfirmBooking.setFocusPainted(false);
        btnConfirmBooking.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirmBooking.setBackground(Branding.NAVY);
        btnConfirmBooking.setForeground(Color.WHITE);
        btnConfirmBooking.setEnabled(false);
        btnConfirmBooking.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Branding.NAVY, 1, true),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        btnConfirmBooking.addActionListener(e -> bookInterview());

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.insets = new Insets(16, 6, 6, 6);
        pnForm.add(btnConfirmBooking, gbc);

        pnCenter.add(pnForm, BorderLayout.EAST);
        pnBody.add(pnCenter, BorderLayout.CENTER);

        // Pie inferior
        JPanel pnFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnFooter.setOpaque(false);

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
        btnClose.addActionListener(e -> dispose());

        pnFooter.add(btnClose);
        pnBody.add(pnFooter, BorderLayout.SOUTH);

        contentPane.add(pnBody, BorderLayout.CENTER);
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(
                    "Book Player Interview",
                    "Select an available slot, enter media information, and confirm assignment"
            );
        }
        return pnHeader;
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        label.setForeground(Branding.NAVY);
        return label;
    }

    private JTextField crearTextField(boolean editable) {
        JTextField tf = new JTextField();
        tf.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        tf.setEditable(editable);
        tf.setBackground(editable ? Color.WHITE : new Color(241, 245, 249));
        tf.setForeground(editable ? Color.BLACK : Branding.TEXT_MUTED);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Branding.BORDER, 1, true),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
        return tf;
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

            txtMediaName.setText("");
            txtMediaContact.setText("");
            txtPlayer.setText("");
            txtDateTime.setText("");
            btnConfirmBooking.setEnabled(false);
            selectedSlot = null;

            loadAvailableSlots();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Warning", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to book interview slot: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}