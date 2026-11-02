package com.ips2026.pl11.view.training;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalTime;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.ips2026.pl11.model.reservation.Facility;
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
    private JLabel lblGardeners;
    private JButton btnReturn;
    private JButton btnSchedule;
    private JScrollPane spnTimeslots;
    private JTable tableTimeslots;
    private JDateChooser date;

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

    private JLabel lblTimeslots;
    private JScrollPane spGardeners;
    private JTable tableGardeners;
    private JLabel lblDate;
    private JComboBox cbGardeners;
    private JButton btnAddGardener;
    private JComboBox cbFacilities;
    private JLabel lblFacility;
    private JComboBox<String> cbSessionType;
    private JLabel lblType;
    private JLabel lblAddedGardeners;
    private JDateChooser dateSession;
    private JSeparator separator;

    private JButton btnRemoveGardener;
    
    private JTextField txtSelectedTimeslot;
    private JLabel lblSelectedTimeslot;
    private JTextArea txtaDescrption;
    private JLabel lblDescription;
    private JScrollPane scrollPane;

    /**
     * Create the frame.
     */
    public TrainingSessionWindow(MainWindow mainWindow) {
        setResizable(false);
        setTitle("Football Club Management");
        this.mw = mainWindow;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 629, 638);
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
            pnHeader = new HeaderPanel("Specific Work Schedule Management",
                "Modify specific days from non sporting employees", false);
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
            pnButtons.add(getLblGardeners());
            pnButtons.add(getBtnReturn());
            pnButtons.add(getBtnSchedule());
            pnButtons.add(getSpnTimeslots());
            pnButtons.add(getLblTimeslots());
            pnButtons.add(getSpGardeners());
            pnButtons.add(getLblDate());
            pnButtons.add(getCbGardeners());
            pnButtons.add(getBtnAddGardener());
            pnButtons.add(getCbFacilities());
            pnButtons.add(getLblFacility());
            pnButtons.add(getCbSessionType());
            pnButtons.add(getLblType());
            pnButtons.add(getLblAddedGardeners());
            pnButtons.add(getDate());
            pnButtons.add(getSeparator());
            pnButtons.add(getBtnRemoveGardener());
            pnButtons.add(getTxtSelectedTimeslot());
            pnButtons.add(getLblSelectedTimeslot());
            pnButtons.add(getLblDescription());
            pnButtons.add(getScrollPane());
        }
        return pnButtons;
    }

    private JLabel getLblGardeners() {
        if (lblGardeners == null) {
            lblGardeners = new JLabel("Gardeners:");
            lblGardeners.setLabelFor(getSpGardeners());
            lblGardeners.setBounds(21, 114, 176, 23);
        }
        return lblGardeners;
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
            btnReturn.setBounds(408, 463, 85, 27);
        }
        return btnReturn;
    }

    private JButton getBtnSchedule() {
        if (btnSchedule == null) {
            btnSchedule = new JButton("Schedule");
            btnSchedule.setEnabled(false);

            btnSchedule.setFont(new Font("Tahoma", Font.BOLD, 11));
            btnSchedule.setBackground(UIManager.getColor("Button.background"));
            btnSchedule.setBounds(503, 463, 85, 27);
        }
        return btnSchedule;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JScrollPane getSpnTimeslots() {
        if (spnTimeslots == null) {
            spnTimeslots = new JScrollPane();
            spnTimeslots.setBounds(407, 98, 181, 236);
            spnTimeslots.setViewportView(getTableTimeslots());
        }
        return spnTimeslots;
    }

    private JTable getTableTimeslots() {
        if (tableTimeslots == null) {
            tableTimeslots = new JTable();
            tableTimeslots.setModel(timeslotsModel);
            tableTimeslots.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    btnSchedule.setEnabled(true);
                    int row = tableTimeslots.getSelectedRow();

                    if (row != -1) {
                        String start = ((LocalTime) tableTimeslots.getValueAt(row, 0)).toString();
                        String end = ((LocalTime) tableTimeslots.getValueAt(row, 1)).toString();
                        getTxtSelectedTimeslot().setText(String.valueOf(start + " - " + end));
                    }
                }
            });

            tableTimeslots.getTableHeader().setResizingAllowed(false);
            tableTimeslots.getTableHeader().setEnabled(false);

        }
        return tableTimeslots;
    }

    private JTable getTableGardeners() {
        if (tableGardeners == null) {
            tableGardeners = new JTable();
            tableGardeners.setModel(gardenersModel);

            TableColumn first = tableGardeners.getColumnModel().getColumn(0);
            first.setMaxWidth(50);
            first.setPreferredWidth(50);

            tableGardeners.getTableHeader().setResizingAllowed(false);
            tableGardeners.getTableHeader().setEnabled(false);

        }
        return tableGardeners;
    }

    private JLabel getLblTimeslots() {
        if (lblTimeslots == null) {
            lblTimeslots = new JLabel("Available timeslots:");
            lblTimeslots.setDisplayedMnemonic('A');
            lblTimeslots.setLabelFor(getSpnTimeslots());
            lblTimeslots.setBounds(407, 84, 220, 14);
        }
        return lblTimeslots;
    }

    private JScrollPane getSpGardeners() {
        if (spGardeners == null) {
            spGardeners = new JScrollPane();
            spGardeners.setBounds(21, 179, 331, 188);
            spGardeners.setViewportView(getTableGardeners());
        }
        return spGardeners;
    }

    private JLabel getLblDate() {
        if (lblDate == null) {
            lblDate = new JLabel("Date:");
            lblDate.setBounds(407, 21, 48, 14);
        }
        return lblDate;
    }

    private JComboBox<String> getCbGardeners() {
        if (cbGardeners == null) {
            cbGardeners = new JComboBox();
            cbGardeners.setBounds(21, 135, 181, 27);
        }
        return cbGardeners;
    }

    private JButton getBtnRemoveGardener() {
        if (btnRemoveGardener == null) {
            btnRemoveGardener = new JButton("Remove");
           
            btnRemoveGardener.setMnemonic('R');
            btnRemoveGardener.setFont(new Font("Tahoma", Font.PLAIN, 11));
            btnRemoveGardener.setBackground(UIManager.getColor("Button.background"));
            btnRemoveGardener.setBounds(282, 135, 70, 27);
        }
        return btnRemoveGardener;
    }

    private JButton getBtnAddGardener() {
        if (btnAddGardener == null) {
            btnAddGardener = new JButton("Add");
            btnAddGardener.setMnemonic('A');
            btnAddGardener.setFont(new Font("Tahoma", Font.PLAIN, 11));
            btnAddGardener.setBackground(UIManager.getColor("Button.background"));
            btnAddGardener.setBounds(208, 135, 70, 27);
        }
        return btnAddGardener;
    }

    private JComboBox<Facility> getCbFacilities() {
        if (cbFacilities == null) {
            cbFacilities = new JComboBox();
            cbFacilities.addActionListener(e -> {
                if (dateSession != null) {
                    dateSession.getJCalendar().getDayChooser().repaint();
                }
            });
            cbFacilities.setBounds(21, 39, 181, 27);
        }
        return cbFacilities;
    }

    private JLabel getLblFacility() {
        if (lblFacility == null) {
            lblFacility = new JLabel("Facility:");
            lblFacility.setBounds(21, 21, 85, 23);
        }
        return lblFacility;
    }

    private JComboBox getCbSessionType() {
        if (cbSessionType == null) {
            cbSessionType = new JComboBox();
            cbSessionType.setBounds(21, 87, 181, 27);
            cbSessionType.setModel(new DefaultComboBoxModel<>(new String[] { "Interior", "Exterior" }));
        }
        return cbSessionType;
    }

    private JLabel getLblType() {
        if (lblType == null) {
            lblType = new JLabel("Session type:");
            lblType.setBounds(21, 65, 130, 23);
        }
        return lblType;
    }

    private JLabel getLblAddedGardeners() {
        if (lblAddedGardeners == null) {
            lblAddedGardeners = new JLabel("Added gardeners:");
            lblAddedGardeners.setDisplayedMnemonic('W');
            lblAddedGardeners.setBounds(21, 160, 176, 23);
        }
        return lblAddedGardeners;
    }

    private JDateChooser getDate() {
        if (dateSession == null) {
            dateSession = new JDateChooser();
            dateSession.addPropertyChangeListener("date", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {

                    getTxtSelectedTimeslot().setText("");
                }
            });

            dateSession.setFont(new Font("Tahoma", Font.PLAIN, 13));
            dateSession.setDateFormatString("yyyy-MM-dd");
            dateSession.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Branding.BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            dateSession.setBackground(Color.WHITE);
            dateSession.setBounds(407, 38, 181, 38);
        }
        return dateSession;
    }

    private JSeparator getSeparator() {
        if (separator == null) {
            separator = new JSeparator();
            separator.setOrientation(SwingConstants.VERTICAL);
            separator.setForeground(new Color(126, 126, 126));
            separator.setBounds(373, 21, 8, 346);
        }
        return separator;
    }

    private JTextField getTxtSelectedTimeslot() {
        if (txtSelectedTimeslot == null) {
            txtSelectedTimeslot = new JTextField();
            txtSelectedTimeslot.setHorizontalAlignment(SwingConstants.CENTER);
            txtSelectedTimeslot.setEditable(false);
            txtSelectedTimeslot.setBounds(483, 340, 94, 27);
            txtSelectedTimeslot.setColumns(10);
        }
        return txtSelectedTimeslot;
    }

    private JLabel getLblSelectedTimeslot() {
        if (lblSelectedTimeslot == null) {
            lblSelectedTimeslot = new JLabel("Selected:");
            lblSelectedTimeslot.setLabelFor(getTxtSelectedTimeslot());
            lblSelectedTimeslot.setBounds(418, 341, 85, 26);
        }
        return lblSelectedTimeslot;
    }

    private JTextArea getTxtaDescrption() {
        if (txtaDescrption == null) {
            txtaDescrption = new JTextArea();
            txtaDescrption.setWrapStyleWord(true);
            txtaDescrption.setLineWrap(true);
            txtaDescrption.setFont(new Font("Tahoma", Font.PLAIN, 13));
        }
        return txtaDescrption;
    }

    private JLabel getLblDescription() {
        if (lblDescription == null) {
            lblDescription = new JLabel("Task details: (Optional)");
            lblDescription.setDisplayedMnemonic('T');
            lblDescription.setLabelFor(getTxtaDescrption());
            lblDescription.setBounds(21, 369, 257, 26);
        }
        return lblDescription;
    }
    private JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane();
            scrollPane.setBounds(21, 398, 561, 64);
            scrollPane.setViewportView(getTxtaDescrption());
        }
        return scrollPane;
    }

    // ---- Auxiliary methods ----

}
