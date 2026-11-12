package com.ips2026.pl11.view.seasonpass;

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
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

import com.ips2026.pl11.controller.email.EmailController;
import com.ips2026.pl11.controller.seasonpass.SeasonPassController;
import com.ips2026.pl11.model.seasonpass.SeasonDateUtils;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

import jakarta.mail.MessagingException;

public class PurchaseSeasonPassDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private EmailController emailController = new EmailController("smtp.gmail.com", 587, "ipswichmgmt@gmail.com",
        "lndqblaxqraeetjw");

    private final SeasonPassController controller;
    private HeaderPanel pnHeader;

    // Form Components
    private JTextField txtDni;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JComboBox<String> cbAgeCategory;
    private JComboBox<String> cbTeam;
    private JTextField txtValidPeriod;
    private JComboBox<String> cbGrandstand;
    private JComboBox<String> cbSection;
    private JLabel lblSelectedRow;
    private JLabel lblSelectedSeat;
    private JLabel lblFinalPrice;

    // Right Panel Components (Grid)
    private JPanel gridPanel;
    private final JButton[][] seatButtons = new JButton[10][15];
    private boolean[][] occupiedMatrix = new boolean[10][15];

    // State
    private int selectedRow = -1;
    private int selectedSeat = -1;
    private final double basePrice = 100.00;

    public PurchaseSeasonPassDialog(JFrame parent) {
        super(parent, "Season Pass Holder Site", true);
        this.controller = new SeasonPassController();

        Branding.applyTo(this);
        // Ampliamos el ancho de la ventana para dar espacio holgado a la matriz
        Branding.setInitialSize(this, 1400, 820, parent);
        getContentPane().setLayout(new BorderLayout(0, 0));

        initComponents();
        loadOccupiedSeats();
        updatePriceAndSummary();
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("SEASON PASS HOLDER SITE", "Purchase annual season pass for specific club teams",
                true);
        }
        return pnHeader;
    }

    private void initComponents() {
        // 1. Corporate Club Header
        getContentPane().add(getPnHeader(), BorderLayout.NORTH);

        // 2. Main Content Layout (2 columns: Left Form, Right Grid Preview)
        JPanel pnMain = new JPanel(new GridLayout(1, 2, 20, 20));
        pnMain.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnMain.setOpaque(false);

        JPanel pnLeft = new JPanel(new BorderLayout(0, 10));
        pnLeft.setOpaque(false);
        pnLeft.add(createLeftFormPanel(), BorderLayout.CENTER);

        JPanel pnRight = new JPanel(new BorderLayout(0, 10));
        pnRight.setOpaque(false);
        pnRight.add(createRightGridPanel(), BorderLayout.CENTER);

        pnMain.add(pnLeft);
        pnMain.add(pnRight);
        getContentPane().add(pnMain, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel pnFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnFooter.setOpaque(false);

        JButton btnCancel = new JButton("CANCEL");
        JButton btnConfirm = new JButton("CONFIRM & PURCHASE");
        btnConfirm.setBackground(Branding.NAVY);
        btnConfirm.setForeground(Branding.WHITE);

        btnCancel.addActionListener(e -> dispose());
        btnConfirm.addActionListener(e -> handlePurchase());

        pnFooter.add(btnCancel);
        pnFooter.add(btnConfirm);
        getContentPane().add(pnFooter, BorderLayout.SOUTH);
    }

    private JPanel createLeftFormPanel() {
        JPanel container = new JPanel(new GridLayout(4, 1, 0, 12));
        container.setOpaque(false);

        // Panel 1: Pass Holder Details
        JPanel pnlHolder = new JPanel(new GridLayout(3, 2, 10, 8));
        pnlHolder.setOpaque(false);
        pnlHolder.setBorder(new RoundedTitledBorder("PASS HOLDER DETAILS", 16));

        pnlHolder.add(new JLabel("DNI / NIF:"));
        txtDni = new JTextField();
        pnlHolder.add(txtDni);

        pnlHolder.add(new JLabel("FULL NAME:"));
        txtFullName = new JTextField();
        pnlHolder.add(txtFullName);

        pnlHolder.add(new JLabel("EMAIL:"));
        txtEmail = new JTextField();
        pnlHolder.add(txtEmail);
        container.add(pnlHolder);

        // Panel 2: Category & Team Selection
        JPanel pnlCategoryTeam = new JPanel(new GridLayout(3, 2, 10, 8));
        pnlCategoryTeam.setOpaque(false);
        pnlCategoryTeam.setBorder(new RoundedTitledBorder("CATEGORY & TEAM SELECTION", 16));

        pnlCategoryTeam.add(new JLabel("AGE CATEGORY:"));
        cbAgeCategory = new JComboBox<>(new String[] { "Adults", "Seniors", "Under 26", "Children" });
        cbAgeCategory.addActionListener(e -> updatePriceAndSummary());
        pnlCategoryTeam.add(cbAgeCategory);

        pnlCategoryTeam.add(new JLabel("SELECTED TEAM:"));
        cbTeam = new JComboBox<>(new String[] { "First Team", "Women's Team", "Youth Team" });
        cbTeam.addActionListener(e -> loadOccupiedSeats());
        pnlCategoryTeam.add(cbTeam);

        pnlCategoryTeam.add(new JLabel("VALID PERIOD:"));
        String[] bounds = SeasonDateUtils.getCurrentSeasonBounds();
        txtValidPeriod = new JTextField(bounds[0] + " - " + bounds[1]);
        txtValidPeriod.setEditable(false);
        pnlCategoryTeam.add(txtValidPeriod);
        container.add(pnlCategoryTeam);

        // Panel 3: Seat Location
        JPanel pnlLocation = new JPanel(new GridLayout(3, 2, 10, 8));
        pnlLocation.setOpaque(false);
        pnlLocation.setBorder(new RoundedTitledBorder("SEAT LOCATION", 16));

        pnlLocation.add(new JLabel("GRANDSTAND:"));
        cbGrandstand = new JComboBox<>(new String[] { "A", "B", "C", "D" });
        cbGrandstand.addActionListener(e -> loadOccupiedSeats());
        pnlLocation.add(cbGrandstand);

        pnlLocation.add(new JLabel("SECTION:"));
        cbSection = new JComboBox<>(new String[] { "A", "B", "C", "D", "E", "F" });
        cbSection.addActionListener(e -> loadOccupiedSeats());
        pnlLocation.add(cbSection);

        pnlLocation.add(new JLabel("ASSIGNED SEAT:"));
        JPanel seatDetailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        seatDetailPanel.setOpaque(false);
        lblSelectedRow = new JLabel("-");
        lblSelectedSeat = new JLabel("-");
        seatDetailPanel.add(new JLabel("Row:"));
        seatDetailPanel.add(lblSelectedRow);
        seatDetailPanel.add(new JLabel("Seat:"));
        seatDetailPanel.add(lblSelectedSeat);
        pnlLocation.add(seatDetailPanel);
        container.add(pnlLocation);

        // Panel 4: Summary
        JPanel pnlSummary = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        pnlSummary.setOpaque(false);
        pnlSummary.setBorder(new RoundedTitledBorder("SUMMARY", 16));

        JLabel lblPriceLabel = new JLabel("FINAL PRICE:");
        lblPriceLabel.setFont(Branding.DEFAULT_FONT.deriveFont(Font.BOLD, 13f));
        pnlSummary.add(lblPriceLabel);

        lblFinalPrice = new JLabel("100.00 €");
        lblFinalPrice.setFont(Branding.DEFAULT_FONT.deriveFont(Font.BOLD, 16f));
        lblFinalPrice.setForeground(new Color(22, 163, 74));
        pnlSummary.add(lblFinalPrice);
        container.add(pnlSummary);

        return container;
    }

    private JPanel createRightGridPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new RoundedTitledBorder("STADIUM SEAT SELECTION PREVIEW", 16));

        // Espaciado ajustado entre botones de la matriz
        gridPanel = new JPanel(new GridLayout(10, 15, 3, 3));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (int rowIndex = 0; rowIndex < 10; rowIndex++) {
            for (int colIndex = 0; colIndex < 15; colIndex++) {
                JButton btn = new JButton();
                btn.setFont(Branding.DEFAULT_FONT.deriveFont(Font.BOLD, 11f));
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setFocusable(false);
                // Tamaño optimizado de 38x38 para que encaje perfectamente sin cortes en los bordes
                btn.setPreferredSize(new Dimension(38, 38));

                final int r = rowIndex;
                final int c = colIndex;

                btn.addActionListener(e -> selectSeat(r, c));
                seatButtons[rowIndex][colIndex] = btn;
                gridPanel.add(btn);
            }
        }

        // Panel contenedor con FlowLayout para centrar la matriz sin recortes
        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gridWrapper.setOpaque(false);
        gridWrapper.add(gridPanel);

        JLabel lblLegend = new JLabel("Click an available green seat to select it (turns blue).", SwingConstants.CENTER);
        lblLegend.setFont(Branding.DEFAULT_FONT.deriveFont(Font.ITALIC, 12f));
        lblLegend.setBorder(new EmptyBorder(5, 0, 10, 0));

        rightPanel.add(gridWrapper, BorderLayout.CENTER);
        rightPanel.add(lblLegend, BorderLayout.SOUTH);

        return rightPanel;
    }

    private void loadOccupiedSeats() {
        String teamName = (String) cbTeam.getSelectedItem();
        String grandstand = (String) cbGrandstand.getSelectedItem();
        String section = (String) cbSection.getSelectedItem();

        selectedRow = -1;
        selectedSeat = -1;
        if (lblSelectedRow != null && lblSelectedSeat != null) {
            lblSelectedRow.setText("-");
            lblSelectedSeat.setText("-");
        }

        try {
            occupiedMatrix = controller.getOccupiedSeasonPassSeats(teamName, grandstand, section);
            refreshGridUI();
        } catch (Exception e) {
            occupiedMatrix = new boolean[10][15];
            refreshGridUI();
        }
    }

    private void refreshGridUI() {
        for (int rowIndex = 0; rowIndex < 10; rowIndex++) {
            for (int colIndex = 0; colIndex < 15; colIndex++) {
                JButton btn = seatButtons[rowIndex][colIndex];

                if (occupiedMatrix[rowIndex][colIndex]) {
                    btn.setText("O");
                    btn.setBackground(new Color(220, 53, 69)); // Red
                    btn.setForeground(Color.WHITE);
                    btn.setOpaque(true);
                    btn.setBorderPainted(false);
                    btn.setEnabled(false);
                } else if (rowIndex == selectedRow && colIndex == selectedSeat) {
                    btn.setText("X");
                    btn.setBackground(new Color(13, 110, 253)); // Blue
                    btn.setForeground(Color.WHITE);
                    btn.setOpaque(true);
                    btn.setBorderPainted(true);
                    btn.setEnabled(true);
                } else {
                    btn.setText("");
                    btn.setBackground(new Color(40, 167, 69)); // Green
                    btn.setForeground(Color.WHITE);
                    btn.setOpaque(true);
                    btn.setBorderPainted(true);
                    btn.setEnabled(true);
                }
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void selectSeat(int rowIndex, int colIndex) {
        if (occupiedMatrix[rowIndex][colIndex]) {
            return;
        }

        selectedRow = rowIndex;
        selectedSeat = colIndex;

        lblSelectedRow.setText(String.valueOf(rowIndex + 1));
        lblSelectedSeat.setText(String.valueOf(colIndex + 1));

        refreshGridUI();
    }

    private void updatePriceAndSummary() {
        String ageCategory = (String) cbAgeCategory.getSelectedItem();
        double finalPrice = controller.calculatePrice(basePrice, ageCategory);
        lblFinalPrice.setText(String.format("%.2f €", finalPrice));
    }

    private void handlePurchase() {
        if (selectedRow == -1 || selectedSeat == -1) {
            JOptionPane.showMessageDialog(this, "Please select an available seat on the grid.", "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String email = txtEmail.getText().trim();
            String dni = txtDni.getText().trim();
            String fullName = txtFullName.getText().trim();
            String team = (String) cbTeam.getSelectedItem();
            String grandstand = (String) cbGrandstand.getSelectedItem();
            String section = (String) cbSection.getSelectedItem();
            String ageCategory = (String) cbAgeCategory.getSelectedItem();

            boolean success = controller.purchaseSeasonPass(dni, fullName, email, team, grandstand, section,
                selectedRow + 1, selectedSeat + 1, ageCategory, basePrice);

            if (success) {
                emailController.sendSeasonPassReceipt(email, dni, fullName, team, grandstand, section, selectedRow + 1,
                    selectedSeat + 1, ageCategory, basePrice);

                JOptionPane.showMessageDialog(this, "Season pass successfully purchased!\nReceipt sent to " + email, "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "The receipt could not be sent:\n" + e.getMessage(), "Email error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Borde redondeado con título incorporado para mantener consistencia visual con ScheduleMatchDialog.
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