package com.ips2026.pl11.view.slots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.controller.slots.InterviewSlotsController;
import com.ips2026.pl11.model.employee.Employee;



public class CreateInterviewSlotDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final InterviewSlotsController controller;
    private final int coachId;
    private final List<Employee> teamPlayers;

    private JPanel contentPane;
    private JPanel pnHeader;
    private JPanel pnForm;
    private JPanel pnFooter;

    private JComboBox<EmployeeItem> cbPlayers;
    private JTextField txtDate;
    private JTextField txtStartTime;
    private JTextField txtEndTime;
    private JButton btnSave;
    private JButton btnCancel;

    public CreateInterviewSlotDialog(JFrame parent, InterviewSlotsController controller, int coachId, List<Employee> players) {
        super(parent, "Create Interview Slot", true);
        this.controller = controller;
        this.coachId = coachId;
        this.teamPlayers = players;

        setSize(460, 430);
        setLocationRelativeTo(parent);
        setResizable(false);

        contentPane = new JPanel(new BorderLayout(0, 15));
        contentPane.setBackground(new Color(248, 250, 252));
        contentPane.setBorder(new EmptyBorder(20, 25, 20, 25));
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnForm(), BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
    }

    private JPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new JPanel(new BorderLayout(0, 4));
            pnHeader.setOpaque(false);
            pnHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));

            JLabel lblTitle = new JLabel("Create Interview Slot");
            lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
            lblTitle.setForeground(new Color(30, 41, 59));

            JLabel lblSubtitle = new JLabel("Define player availability avoiding match & training conflicts");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSubtitle.setForeground(new Color(100, 116, 139));
            lblSubtitle.setBorder(new EmptyBorder(0, 0, 8, 0));

            pnHeader.add(lblTitle, BorderLayout.NORTH);
            pnHeader.add(lblSubtitle, BorderLayout.SOUTH);
        }
        return pnHeader;
    }

    private JPanel getPnForm() {
        if (pnForm == null) {
            pnForm = new JPanel(new GridLayout(4, 2, 10, 14));
            pnForm.setOpaque(false);
            pnForm.setBorder(new EmptyBorder(8, 4, 8, 4));

            pnForm.add(crearLabel("Player:"));
            pnForm.add(getCbPlayers());

            pnForm.add(crearLabel("Date (YYYY-MM-DD):"));
            pnForm.add(getTxtDate());

            pnForm.add(crearLabel("Start Time (HH:MM):"));
            pnForm.add(getTxtStartTime());

            pnForm.add(crearLabel("End Time (HH:MM):"));
            pnForm.add(getTxtEndTime());
        }
        return pnForm;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel(new GridLayout(1, 2, 12, 0));
            pnFooter.setOpaque(false);
            pnFooter.add(getBtnCancel());
            pnFooter.add(getBtnSave());
        }
        return pnFooter;
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

    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton("Cancel");
            btnCancel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            btnCancel.setFocusPainted(false);
            btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCancel.setBackground(Color.WHITE);
            btnCancel.setForeground(new Color(51, 65, 85));
            btnCancel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1, true),
                    BorderFactory.createEmptyBorder(8, 14, 8, 14)
            ));
            btnCancel.addActionListener(e -> dispose());
        }
        return btnCancel;
    }

    private JButton getBtnSave() {
        if (btnSave == null) {
            btnSave = new JButton("Create Slot");
            btnSave.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            btnSave.setFocusPainted(false);
            btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSave.setBackground(new Color(24, 76, 120));
            btnSave.setForeground(Color.WHITE);
            btnSave.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

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
                BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
        return tf;
    }

    private void saveSlot() {
        EmployeeItem selected = (EmployeeItem) getCbPlayers().getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a player.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(getTxtDate().getText().trim());
            LocalTime start = LocalTime.parse(getTxtStartTime().getText().trim());
            LocalTime end = LocalTime.parse(getTxtEndTime().getText().trim());

            controller.createInterviewSlot(selected.id, coachId, date, start, end);

            JOptionPane.showMessageDialog(this, "Interview slot registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date or time format. Please use YYYY-MM-DD and HH:MM.", "Format Error", JOptionPane.ERROR_MESSAGE);
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
