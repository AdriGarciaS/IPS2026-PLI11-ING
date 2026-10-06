package com.ips2026.pl11.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.WorkScheduleController;
import com.ips2026.pl11.datos.WorkScheduleDAO;
import com.ips2026.pl11.model.WorkSchedule;

public class GMWorkScheduleWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    private static final String[] DAYS = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
        "Sunday" };

    private WorkScheduleController controller = new WorkScheduleController(new WorkScheduleDAO());

    private VentanaPrincipal mw;
    private JPanel pnHeader;
    private JLabel lblLogo;
    private JLabel lblWeeklyWorkSchedule;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JPanel pnButtons;
    private JTextField txtWorkerId;
    private JComboBox cbDay;
    private JSpinner spStart;
    private JSpinner spEnd;
    private JLabel lblWorkerId;
    private JLabel lblDay;
    private JLabel lblStart;
    private JLabel lblEnd;
    private JButton btnReturn;
    private JButton btnAdd;
    private JScrollPane spnTable;
    private JTable table;

    /**
     * Create the frame.
     */
    public GMWorkScheduleWindow(VentanaPrincipal mw) {
        this.mw = mw;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 603, 547);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.add(getPnHeader_1(), BorderLayout.NORTH);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        contentPane.add(getLblFooter(), BorderLayout.SOUTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
        rootPane.setDefaultButton(btnAdd);
        setLocationRelativeTo(mw);

    }

    private JPanel getPnHeader_1() {
        if (pnHeader == null) {
            pnHeader = new JPanel();
            pnHeader.setLayout(new BorderLayout(15, 0));
            pnHeader.add(getLblLogo(), BorderLayout.WEST);
            pnHeader.add(getLblWeeklyWorkSchedule(), BorderLayout.CENTER);
        }
        return pnHeader;
    }

    private JLabel getLblLogo() {
        if (lblLogo == null) {
            lblLogo = new JLabel("LOGO");
            lblLogo.setPreferredSize(new Dimension(80, 80));
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            lblLogo.setBorder(new LineBorder(Color.GRAY));
        }
        return lblLogo;
    }

    private JLabel getLblWeeklyWorkSchedule() {
        if (lblWeeklyWorkSchedule == null) {
            lblWeeklyWorkSchedule = new JLabel("Weekly Work Schedule Manager");
            lblWeeklyWorkSchedule.setHorizontalAlignment(SwingConstants.CENTER);
            lblWeeklyWorkSchedule.setFont(new Font("Tahoma", Font.BOLD, 26));
        }
        return lblWeeklyWorkSchedule;
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
            pnButtons.add(getTxtWorkerId());
            pnButtons.add(getCbDay());
            pnButtons.add(getSpStart());
            pnButtons.add(getSpEnd());
            pnButtons.add(getLblWorkerId());
            pnButtons.add(getLblDay());
            pnButtons.add(getLblStart());
            pnButtons.add(getLblEnd());
            pnButtons.add(getBtnReturn());
            pnButtons.add(getBtnAdd());
            pnButtons.add(getSpnTable());
        }
        return pnButtons;
    }

    private JTextField getTxtWorkerId() {
        if (txtWorkerId == null) {
            txtWorkerId = new JTextField();
            txtWorkerId.setBounds(206, 22, 248, 32);
            txtWorkerId.setColumns(10);
        }
        return txtWorkerId;
    }

    private JComboBox getCbDay() {
        if (cbDay == null) {
            cbDay = new JComboBox(DAYS);
            cbDay.setBounds(206, 65, 248, 32);
        }
        return cbDay;
    }

    private JSpinner getSpStart() {
        if (spStart == null) {
            spStart = new JSpinner(new SpinnerDateModel());
            spStart.setEditor(new JSpinner.DateEditor(spStart, "HH:mm"));
            spStart.setBounds(206, 108, 75, 32);
        }
        return spStart;
    }

    private JSpinner getSpEnd() {
        if (spEnd == null) {
            spEnd = new JSpinner(new SpinnerDateModel());
            spEnd.setEditor(new JSpinner.DateEditor(spEnd, "HH:mm"));
            spEnd.setBounds(206, 151, 75, 32);
        }
        return spEnd;
    }

    private JLabel getLblWorkerId() {
        if (lblWorkerId == null) {
            lblWorkerId = new JLabel("Worker Id:");
            lblWorkerId.setDisplayedMnemonic('W');
            lblWorkerId.setLabelFor(getTxtWorkerId());
            lblWorkerId.setBounds(133, 27, 63, 23);
        }
        return lblWorkerId;
    }

    private JLabel getLblDay() {
        if (lblDay == null) {
            lblDay = new JLabel("Day of the week:");
            lblDay.setToolTipText("D");
            lblDay.setLabelFor(getCbDay());
            lblDay.setDisplayedMnemonic('W');
            lblDay.setBounds(104, 74, 101, 23);
        }
        return lblDay;
    }

    private JLabel getLblStart() {
        if (lblStart == null) {
            lblStart = new JLabel("Start time:");
            lblStart.setLabelFor(lblStart);
            lblStart.setDisplayedMnemonic('S');
            lblStart.setBounds(133, 117, 63, 23);
        }
        return lblStart;
    }

    private JLabel getLblEnd() {
        if (lblEnd == null) {
            lblEnd = new JLabel("End time:");
            lblEnd.setDisplayedMnemonic('W');
            lblEnd.setBounds(133, 160, 63, 23);
        }
        return lblEnd;
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
            btnReturn.setFont(new Font("Tahoma", Font.BOLD, 14));
            btnReturn.setBounds(31, 240, 101, 32);
        }
        return btnReturn;
    }

    private JButton getBtnAdd() {
        if (btnAdd == null) {
            btnAdd = new JButton("Add shift");
            btnAdd.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addShift();
                }
            });
            btnAdd.setFont(new Font("Tahoma", Font.BOLD, 14));
            btnAdd.setBackground(UIManager.getColor("Button.background"));
            btnAdd.setBounds(455, 240, 101, 32);
        }
        return btnAdd;
    }

    private void addShift() {
        try {
            long employeeId = Long.parseLong(txtWorkerId.getText().trim());
            int weekDay = cbDay.getSelectedIndex() + 1;
            
            LocalTime start = ((Date) spStart.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalTime();
            LocalTime end = ((Date) spEnd.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalTime();
            
            controller.addShift(new WorkSchedule(0L, employeeId, weekDay, start, end));

            refreshTable();
            JOptionPane.showMessageDialog(this, "Shift added");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Employee id must be a number");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }
    
    private void refreshTable() {
        try {
            long employeeId = Long.parseLong(txtWorkerId.getText().trim());
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            for (WorkSchedule s : controller.getWorkSchedulesByEmployee(employeeId)) {
                model.addRow(new Object[] {
                        DAYS[s.getWeekDay() - 1], s.getStartTime(), s.getEndTime()});
            }
        } catch (NumberFormatException e) {
            showError("Employee id must be a number");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Error", JOptionPane.ERROR_MESSAGE);
    }
    private JScrollPane getSpnTable() {
        if (spnTable == null) {
        	spnTable = new JScrollPane();
        	spnTable.setBounds(137, 194, 317, 198);
        	spnTable.setViewportView(getTable());
        }
        return spnTable;
    }
    private JTable getTable() {
        if (table == null) {
        	table = new JTable();
        	table.setModel(new DefaultTableModel(
        	    new Object[][] {
        	    },
        	    new String[] {
        	        "Start", "End", "Date"
        	    }
        	));
        }
        return table;
    }
}
