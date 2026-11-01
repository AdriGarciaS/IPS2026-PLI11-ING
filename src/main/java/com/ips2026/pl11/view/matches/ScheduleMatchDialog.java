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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JDateChooser;

import com.ips2026.pl11.controller.matches.ScheduleMatchController;
import com.ips2026.pl11.data.seasonpass.SeasonPassDAO;
import com.ips2026.pl11.data.team.TeamDAO;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

/**
 * Dialog for scheduling home matches (US 35775).
 * Features fixture details with JCalendar date selection and JSpinner time picker, 
 * seating section pricing, a dynamic stadium preview grid, and automatic conflict cancellation.
 */
public class ScheduleMatchDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final ScheduleMatchController controller;
    private final SeasonPassDAO seasonPassDAO;

    // Section 1: Fixture Details UI Controls
    private JComboBox<TeamDAO.TeamItem> cbLocalTeam;
    private JTextField txtAwayTeam;
    private JDateChooser dateChooserMatchDate;
    private JSpinner spinnerStartTime; // Cambiado de JTextField a JSpinner
    private JCheckBox chkSpecialMatch;

    // Section 2: Seating & Prices UI Controls
    private JComboBox<String> cbGrandstand;
    private JComboBox<String> cbSection;
    private JTextField txtBasePrice;

    // Section 3: Stadium Preview Controls
    private JPanel pnSeatGrid;
    private final JButton[][] seatButtons = new JButton[10][15];

    /**
     * Constructs the match scheduling dialog attached to the parent window.
     *
     * @param parent the parent frame anchoring this dialog
     */
    public ScheduleMatchDialog(JFrame parent) {
        super(parent, "Schedule Home Match — US 35775", true);
        this.controller = new ScheduleMatchController();
        this.seasonPassDAO = new SeasonPassDAO();

        Branding.applyTo(this);
        Branding.setInitialSize(this, 1000, 680, parent);
        getContentPane().setLayout(new BorderLayout(0, 0));

        // Header
        HeaderPanel header = new HeaderPanel("Schedule Home Match",
                "Set up fixture details, pricing and auto-cancel facility conflicts", true);
        getContentPane().add(header, BorderLayout.NORTH);

        // Main split container (Left: Form Panels, Right: Seat Map Preview)
        JPanel pnMain = new JPanel(new GridLayout(1, 2, 15, 15));
        pnMain.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnMain.setOpaque(false);

        JPanel pnLeft = new JPanel(new GridLayout(2, 1, 10, 10));
        pnLeft.setOpaque(false);
        pnLeft.add(createFixtureDetailsPanel());
        pnLeft.add(createSeatingPricesPanel());

        pnMain.add(pnLeft);
        pnMain.add(createSeatPreviewPanel());

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
        updateSeatPreview();
    }

    /**
     * Creates Section 1: Fixture details panel featuring JDateChooser for date and JSpinner for start time.
     */
    private JPanel createFixtureDetailsPanel() {
        JPanel pn = new JPanel(new GridLayout(6, 2, 8, 8));
        pn.setOpaque(false);
        pn.setBorder(new RoundedTitledBorder("SECTION 1: FIXTURE DETAILS", 16));

        pn.add(new JLabel("LOCAL TEAM:"));
        cbLocalTeam = new JComboBox<>();
        pn.add(cbLocalTeam);

        pn.add(new JLabel("AWAY TEAM:"));
        txtAwayTeam = new JTextField();
        pn.add(txtAwayTeam);

        pn.add(new JLabel("MATCH DATE:"));
        dateChooserMatchDate = new JDateChooser();
        dateChooserMatchDate.setDateFormatString("yyyy-MM-dd");
        dateChooserMatchDate.setDate(new Date());
        pn.add(dateChooserMatchDate);

        pn.add(new JLabel("START TIME (HH:MM):"));
        
        // JSpinner configurado con SpinnerDateModel para seleccion de hora
        SpinnerDateModel timeModel = new SpinnerDateModel();
        spinnerStartTime = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinnerStartTime, "HH:mm");
        spinnerStartTime.setEditor(timeEditor);
        
        // Hora por defecto: 18:00
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 0);
        spinnerStartTime.setValue(cal.getTime());
        
        pn.add(spinnerStartTime);

        pn.add(new JLabel("DURATION:"));
        JLabel lblDuration = new JLabel("2 HOURS (Fixed Window)");
        lblDuration.setFont(new Font(Branding.FONT_FAMILY, Font.ITALIC, 12));
        pn.add(lblDuration);

        pn.add(new JLabel("SPECIAL MATCH?:"));
        chkSpecialMatch = new JCheckBox("Mark as Special Match");
        chkSpecialMatch.setOpaque(false);
        chkSpecialMatch.addActionListener(e -> updateSeatPreview());
        pn.add(chkSpecialMatch);

        return pn;
    }

    /**
     * Creates Section 2: Seating & prices selection panel with a custom rounded border.
     */
    private JPanel createSeatingPricesPanel() {
        JPanel pn = new JPanel(new GridLayout(4, 2, 8, 8));
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

        ActionListener refreshGrid = e -> updateSeatPreview();
        cbGrandstand.addActionListener(refreshGrid);
        cbSection.addActionListener(refreshGrid);

        return pn;
    }

    /**
     * Creates Section 3: Stadium match seat preview grid with a custom rounded border.
     */
    private JPanel createSeatPreviewPanel() {
        JPanel pn = new JPanel(new BorderLayout(5, 5));
        pn.setOpaque(false);
        pn.setBorder(new RoundedTitledBorder("SECTION 3: STADIUM MATCH PREVIEW", 16));

        pnSeatGrid = new JPanel(new GridLayout(10, 15, 2, 2));
        pnSeatGrid.setOpaque(false);

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 15; c++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(20, 20));
                btn.setEnabled(false);
                btn.setContentAreaFilled(true);
                btn.setOpaque(true);
                btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                seatButtons[r][c] = btn;
                pnSeatGrid.add(btn);
            }
        }

        JLabel lblLegend = new JLabel("Green: Available / Red (O): Occupied by Season Pass", SwingConstants.CENTER);
        lblLegend.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 11));

        pn.add(pnSeatGrid, BorderLayout.CENTER);
        pn.add(lblLegend, BorderLayout.SOUTH);
        return pn;
    }

    /**
     * Loads available local teams into the dropdown selection box.
     */
    private void loadTeams() {
        try {
            TeamDAO teamDAO = new TeamDAO();
            List<TeamDAO.TeamItem> teams = teamDAO.findAll();
            for (TeamDAO.TeamItem team : teams) {
                cbLocalTeam.addItem(team);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading local teams: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates seat grid representation dynamically according to selected section and special match flag.
     */
    private void updateSeatPreview() {
        boolean isSpecial = chkSpecialMatch.isSelected();
        String grandstand = (String) cbGrandstand.getSelectedItem();
        String section = (String) cbSection.getSelectedItem();

        boolean[][] occupiedGrid = new boolean[10][15];

        if (!isSpecial && grandstand != null && section != null) {
            try {
                occupiedGrid = seasonPassDAO.getOccupiedSeasonPassSeats(grandstand, section);
            } catch (SQLException ex) {
                // Fallback to empty grid on SQL exception
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

    /**
     * Validates inputs, formats date from JDateChooser and time from JSpinner, schedules the home match, and auto-cancels facility conflicts.
     */
    private void handleSaveAndSchedule() {
        try {
            TeamDAO.TeamItem selectedTeam = (TeamDAO.TeamItem) cbLocalTeam.getSelectedItem();
            if (selectedTeam == null) throw new IllegalArgumentException("Please select a local team.");

            String rival = txtAwayTeam.getText().trim();
            if (rival.isEmpty()) throw new IllegalArgumentException("Please enter the away opponent name.");

            // Formateo seguro de fecha desde JDateChooser
            Date selectedDate = dateChooserMatchDate.getDate();
            if (selectedDate == null) throw new IllegalArgumentException("Please select a valid match date.");
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdfDate.format(selectedDate);

            // Formateo seguro de hora desde JSpinner
            Date selectedTime = (Date) spinnerStartTime.getValue();
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
            String startTime = sdfTime.format(selectedTime);

            boolean isSpecial = chkSpecialMatch.isSelected();
            String grandstand = (String) cbGrandstand.getSelectedItem();
            String section = (String) cbSection.getSelectedItem();
            double price = Double.parseDouble(txtBasePrice.getText().trim());

            controller.scheduleMatch(selectedTeam.getId(), rival, date, startTime, isSpecial, grandstand, section, price);

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

    /**
     * Custom Swing border class drawing smoothly antialiased rounded rectangles with top titles.
     */
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