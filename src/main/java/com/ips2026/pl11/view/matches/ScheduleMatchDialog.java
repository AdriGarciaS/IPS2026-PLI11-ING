package com.ips2026.pl11.view.matches;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import com.ips2026.pl11.controller.matches.ScheduleMatchController;
import com.ips2026.pl11.data.matches.MatchPriceDAO;
import com.ips2026.pl11.data.seasonpass.SeasonPassDAO;
import com.ips2026.pl11.data.team.TeamDAO;
import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

/**
 * Dialog for scheduling home matches (US 35775).
 * Features fixture details with facility selection, pricing configuration per stand/section,
 * JCalendar integration, JSpinner time picker, and rounded titled section borders.
 */
public class ScheduleMatchDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final ScheduleMatchController controller;
    private final SeasonPassDAO seasonPassDAO;
    private final MatchDAO matchDAO;
    private final MatchPriceDAO matchPriceDAO;

    // Temporal price storage for dynamic UI switching
    private final Map<String, Double> tempPriceMap = new HashMap<>();
    private String lastSelectedKey = "A-A";

    // Section 1 UI Controls
    private JComboBox<TeamDAO.TeamItem> cbLocalTeam;
    private JTextField txtAwayTeam;
    private JComboBox<String> cbFacility;
    private JDateChooser dateChooserMatchDate;
    private JSpinner spinnerStartTime;
    private JRadioButton rbSpecialYes;
    private JRadioButton rbSpecialNo;
    
    // Activities Table
    private JTable tblActivities;
    private DefaultTableModel modelActivities;

    // Section 2 UI Controls
    private JComboBox<String> cbGrandstand;
    private JComboBox<String> cbSection;
    private JTextField txtBasePrice;

    // Section 3 UI Controls
    private JPanel pnSeatGrid;
    private final JButton[][] seatButtons = new JButton[10][15];

    public ScheduleMatchDialog(JFrame parent) {
        super(parent, "Schedule Home Match — US 35775", true);
        this.controller = new ScheduleMatchController();
        this.seasonPassDAO = new SeasonPassDAO();
        this.matchDAO = new MatchDAO();
        this.matchPriceDAO = new MatchPriceDAO();

        Branding.applyTo(this);
        Branding.setInitialSize(this, 1050, 720, parent);
        getContentPane().setLayout(new BorderLayout(0, 0));

        HeaderPanel header = new HeaderPanel("Schedule Home Match",
                "Set up fixture details, pricing and auto-cancel facility conflicts", true);
        getContentPane().add(header, BorderLayout.NORTH);

        JPanel pnMain = new JPanel(new GridLayout(1, 2, 15, 15));
        pnMain.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnMain.setOpaque(false);

        JPanel pnLeft = new JPanel(new BorderLayout(0, 10));
        pnLeft.setOpaque(false);
        pnLeft.add(createFixtureDetailsPanel(), BorderLayout.CENTER);

        JPanel pnRight = new JPanel(new BorderLayout(0, 10));
        pnRight.setOpaque(false);
        pnRight.add(createSeatingPricesPanel(), BorderLayout.NORTH);
        pnRight.add(createSeatPreviewPanel(), BorderLayout.CENTER);

        pnMain.add(pnLeft);
        pnMain.add(pnRight);

        getContentPane().add(pnMain, BorderLayout.CENTER);

        // Footer Actions
        JPanel pnFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnFooter.setOpaque(false);

        JButton btnCancel = new JButton("CANCEL");
        JButton btnSave = new JButton("SAVE & SCHEDULE");
        btnSave.setBackground(Branding.NAVY);
        btnSave.setForeground(Branding.WHITE);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSaveAndSchedule());

        pnFooter.add(btnCancel);
        pnFooter.add(btnSave);
        getContentPane().add(pnFooter, BorderLayout.SOUTH);

        loadTeams();
        updateActivitiesTable();
        updateSeatPreview();
    }

    private JPanel createFixtureDetailsPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setOpaque(false);
        container.setBorder(new RoundedTitledBorder("SECTION 1: FIXTURE DETAILS", 16));

        JPanel pnlForm = new JPanel(new GridLayout(7, 2, 8, 6));
        pnlForm.setOpaque(false);

        pnlForm.add(new JLabel("LOCAL TEAM:"));
        cbLocalTeam = new JComboBox<>();
        pnlForm.add(cbLocalTeam);

        pnlForm.add(new JLabel("AWAY TEAM:"));
        txtAwayTeam = new JTextField();
        pnlForm.add(txtAwayTeam);

        pnlForm.add(new JLabel("SELECT FACILITY:"));
        cbFacility = new JComboBox<>(new String[]{"Main Pitch", "Training Pitch 1", "Training Pitch 2"});
        cbFacility.addActionListener(e -> updateActivitiesTable());
        pnlForm.add(cbFacility);

        pnlForm.add(new JLabel("MATCH DATE:"));
        dateChooserMatchDate = new JDateChooser();
        dateChooserMatchDate.setDateFormatString("dd/MM/yyyy");
        dateChooserMatchDate.setDate(new Date());
        dateChooserMatchDate.addPropertyChangeListener("date", evt -> updateActivitiesTable());
        pnlForm.add(dateChooserMatchDate);

        pnlForm.add(new JLabel("START TIME:"));
        SpinnerDateModel timeModel = new SpinnerDateModel();
        spinnerStartTime = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinnerStartTime, "HH:mm");
        spinnerStartTime.setEditor(timeEditor);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 0);
        spinnerStartTime.setValue(cal.getTime());
        pnlForm.add(spinnerStartTime);

        pnlForm.add(new JLabel("DURATION:"));
        JLabel lblDuration = new JLabel("2 HOURS");
        lblDuration.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        pnlForm.add(lblDuration);

        pnlForm.add(new JLabel("SPECIAL MATCH?:"));
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlRadio.setOpaque(false);
        rbSpecialYes = new JRadioButton("YES");
        rbSpecialNo = new JRadioButton("NO", true);
        rbSpecialYes.setOpaque(false);
        rbSpecialNo.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        group.add(rbSpecialYes);
        group.add(rbSpecialNo);
        ActionListener radioListener = e -> updateSeatPreview();
        rbSpecialYes.addActionListener(radioListener);
        rbSpecialNo.addActionListener(radioListener);
        pnlRadio.add(rbSpecialYes);
        pnlRadio.add(rbSpecialNo);
        pnlForm.add(pnlRadio);

        container.add(pnlForm, BorderLayout.NORTH);

        // ACTIVITIES FOR THAT DAY Table
        JPanel pnlTable = new JPanel(new BorderLayout(0, 5));
        pnlTable.setOpaque(false);
        JLabel lblTableTitle = new JLabel("ACTIVITIES FOR THAT DAY:");
        lblTableTitle.setFont(Branding.DEFAULT_FONT.deriveFont(Font.ITALIC, 11f));
        pnlTable.add(lblTableTitle, BorderLayout.NORTH);

        modelActivities = new DefaultTableModel(new String[]{"Activity", "Start", "End"}, 0);
        tblActivities = new JTable(modelActivities);
        tblActivities.setRowHeight(20);
        JScrollPane scrollTable = new JScrollPane(tblActivities);
        scrollTable.setPreferredSize(new Dimension(300, 120));
        pnlTable.add(scrollTable, BorderLayout.CENTER);

        container.add(pnlTable, BorderLayout.CENTER);

        return container;
    }

    private JPanel createSeatingPricesPanel() {
        JPanel pn = new JPanel(new GridLayout(3, 2, 8, 8));
        pn.setOpaque(false);
        pn.setBorder(new RoundedTitledBorder("SECTION 2: SEATING & PRICES", 16));

        pn.add(new JLabel("SELECT GRANDSTAND:"));
        cbGrandstand = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        pn.add(cbGrandstand);

        pn.add(new JLabel("SELECT SECTION:"));
        cbSection = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F"});
        pn.add(cbSection);

        pn.add(new JLabel("BASE PRICE (€):"));
        txtBasePrice = new JTextField("30.00");
        pn.add(txtBasePrice);

        // Event listener to dynamically save current price before switching combo values
        ActionListener refreshGrid = e -> handleStandOrSectionChange();
        cbGrandstand.addActionListener(refreshGrid);
        cbSection.addActionListener(refreshGrid);

        return pn;
    }

    private JPanel createSeatPreviewPanel() {
        JPanel pn = new JPanel(new BorderLayout(5, 5));
        pn.setOpaque(false);
        pn.setBorder(new RoundedTitledBorder("SECTION 3: STADIUM MATCH PREVIEW", 16));

        pnSeatGrid = new JPanel(new GridLayout(10, 15, 2, 2));
        pnSeatGrid.setOpaque(false);

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 15; c++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(18, 18));
                btn.setEnabled(false);
                btn.setContentAreaFilled(true);
                btn.setOpaque(true);
                btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                seatButtons[r][c] = btn;
                pnSeatGrid.add(btn);
            }
        }

        JLabel lblLegend = new JLabel("In case it is not a special match, some seats appear occupied (O)", SwingConstants.CENTER);
        lblLegend.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 10));

        pn.add(pnSeatGrid, BorderLayout.CENTER);
        pn.add(lblLegend, BorderLayout.SOUTH);
        return pn;
    }

    private void handleStandOrSectionChange() {
        if (cbGrandstand == null || cbSection == null || txtBasePrice == null) return;

        try {
            String currentPriceText = txtBasePrice.getText().trim();
            if (!currentPriceText.isEmpty()) {
                double price = Double.parseDouble(currentPriceText);
                tempPriceMap.put(lastSelectedKey, price);
            }
        } catch (NumberFormatException ignored) {
        }

        String newGrandstand = (String) cbGrandstand.getSelectedItem();
        String newSection = (String) cbSection.getSelectedItem();
        if (newGrandstand == null || newSection == null) return;

        String newKey = newGrandstand + "-" + newSection;
        lastSelectedKey = newKey;

        double loadedPrice = tempPriceMap.getOrDefault(newKey, 30.00);
        txtBasePrice.setText(String.format(Locale.US, "%.2f", loadedPrice));

        updateSeatPreview();
    }

    private void loadTeams() {
        try {
            TeamDAO teamDAO = new TeamDAO();
            List<TeamDAO.TeamItem> teams = teamDAO.findAll();
            for (TeamDAO.TeamItem team : teams) {
                cbLocalTeam.addItem(team);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading teams: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSelectedFacilityId() {
        int index = cbFacility.getSelectedIndex();
        return index + 1;
    }

    private void updateActivitiesTable() {
        if (dateChooserMatchDate == null || dateChooserMatchDate.getDate() == null || cbFacility == null) return;
        
        modelActivities.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(dateChooserMatchDate.getDate());
        int facilityId = getSelectedFacilityId();

        try {
            List<Object[]> activities = matchDAO.getActivitiesForDay(facilityId, dateStr);
            for (Object[] act : activities) {
                modelActivities.addRow(act);
            }
        } catch (SQLException ex) {
        }
    }

    private void updateSeatPreview() {
        boolean isSpecial = rbSpecialYes.isSelected();
        String grandstand = (String) cbGrandstand.getSelectedItem();
        String section = (String) cbSection.getSelectedItem();

        boolean[][] occupiedGrid = new boolean[10][15];

        if (!isSpecial && grandstand != null && section != null) {
            try {
                occupiedGrid = seasonPassDAO.getOccupiedSeasonPassSeats(grandstand, section);
            } catch (SQLException ex) {
                // Fallback
            }
        }

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 15; c++) {
                if (occupiedGrid[r][c]) {
                    seatButtons[r][c].setBackground(new Color(239, 68, 68)); // Red
                    seatButtons[r][c].setForeground(Color.WHITE);
                    seatButtons[r][c].setText("O");
                } else {
                    seatButtons[r][c].setBackground(new Color(34, 197, 94)); // Green
                    seatButtons[r][c].setForeground(Color.WHITE);
                    seatButtons[r][c].setText("");
                }
            }
        }
        pnSeatGrid.revalidate();
        pnSeatGrid.repaint();
    }

    private void handleSaveAndSchedule() {
        try {
            TeamDAO.TeamItem selectedTeam = (TeamDAO.TeamItem) cbLocalTeam.getSelectedItem();
            if (selectedTeam == null) throw new IllegalArgumentException("Please select a local team.");

            String rival = txtAwayTeam.getText().trim();
            if (rival.isEmpty()) throw new IllegalArgumentException("Please enter the away opponent name.");

            Date selectedDate = dateChooserMatchDate.getDate();
            if (selectedDate == null) throw new IllegalArgumentException("Please select a valid match date.");
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdfDate.format(selectedDate);

            Date selectedTime = (Date) spinnerStartTime.getValue();
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
            String startTime = sdfTime.format(selectedTime);

            boolean isSpecial = rbSpecialYes.isSelected();
            String activeGrandstand = (String) cbGrandstand.getSelectedItem();
            String activeSection = (String) cbSection.getSelectedItem();
            double activePrice = Double.parseDouble(txtBasePrice.getText().trim());

            int facilityId = getSelectedFacilityId();

            handleStandOrSectionChange();

            int matchId = controller.scheduleMatch(selectedTeam.getId(), rival, date, startTime, isSpecial, activeGrandstand, activeSection, activePrice, facilityId);

            System.out.println("\n========== MATCH CREATED [ID: " + matchId + "] - SEAT PRICING MATRIX ==========");
            System.out.println("Match: " + selectedTeam.getName() + " vs " + rival + " | Date: " + date + " " + startTime);
            System.out.println("------------------------------------------------------------------");

            String[] grandstands = {"A", "B", "C", "D"};
            String[] sections = {"A", "B", "C", "D", "E", "F"};

            for (String gs : grandstands) {
                for (String sec : sections) {
                    String key = gs + "-" + sec;
                    
                    double priceToSave = tempPriceMap.getOrDefault(key, 30.00);

                    matchPriceDAO.saveMatchPrice(matchId, gs, sec, priceToSave);

                    System.out.printf(Locale.US, "Grandstand: %s | Section: %s | Price per seat: €%.2f%n", gs, sec, priceToSave);
                }
            }
            System.out.println("==================================================================\n");

            JOptionPane.showMessageDialog(this,
                    "Home match scheduled successfully!\nFacility conflicts cleared automatically.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid base price.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Scheduling Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class RoundedTitledBorder extends AbstractBorder {
        private static final long serialVersionUID = 1L;
        private final String title;
        private final int cornerRadius;

        public RoundedTitledBorder(String title, int cornerRadius) {
            this.title = title;
            this.cornerRadius = cornerRadius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Font titleFont = Branding.DEFAULT_FONT.deriveFont(Font.BOLD, 12f);
            g2.setFont(titleFont);

            int titleHeight = g2.getFontMetrics().getAscent();
            int titleWidth = g2.getFontMetrics().stringWidth(title);

            int lineY = y + titleHeight / 2 + 2;
            int rectHeight = height - (lineY - y) - 2;

            g2.setColor(new Color(203, 213, 225));
            g2.drawRoundRect(x + 1, lineY, width - 3, rectHeight - 1, cornerRadius, cornerRadius);

            g2.setColor(c.getBackground());
            g2.fillRect(x + 12, y, titleWidth + 6, titleHeight + 2);

            g2.setColor(Branding.NAVY);
            g2.drawString(title, x + 15, y + titleHeight);

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(20, 12, 12, 12);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(20, 12, 12, 12);
            return insets;
        }
    }
}