package com.ips2026.pl11.view.seasonpass;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

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

import com.ips2026.pl11.controller.seasonpass.SeasonPassController;
import com.ips2026.pl11.model.seasonpass.SeasonDateUtils;
import com.ips2026.pl11.view.common.HeaderPanel;

public class PurchaseSeasonPassDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final SeasonPassController controller;
    private HeaderPanel pnHeader;

    // Left Panel Components
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

        setSize(1150, 720); // Expanded size for optimal 10x15 cell visibility
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        loadOccupiedSeats();
        updatePriceAndSummary();
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("SEASON PASS HOLDER SITE", 
                    "Purchase annual season pass for specific club teams", false);
        }
        return pnHeader;
    }

    private void initComponents() {
        // 1. Corporate Club Header
        add(getPnHeader(), BorderLayout.NORTH);

        // 2. Center Panel with 2 columns separated by a line border
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContent.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel leftPanel = createLeftFormPanel();
        JPanel rightPanel = createRightGridPanel();

        // Division line on left panel right edge
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(0, 0, 0, 15)
        ));

        mainContent.add(leftPanel);
        mainContent.add(rightPanel);
        add(mainContent, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnCancel = new JButton("CANCEL");
        JButton btnConfirm = new JButton("CONFIRM & PURCHASE");

        btnCancel.addActionListener(e -> dispose());
        btnConfirm.addActionListener(e -> handlePurchase());

        bottomBar.add(btnCancel);
        bottomBar.add(btnConfirm);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createLeftFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        int row = 0;

        // --- PASS HOLDER DETAILS ---
        panel.add(createSectionHeader("PASS HOLDER DETAILS"), getGbc(0, row++, 2));

        panel.add(new JLabel("DNI / NIF:"), getGbc(0, row, 1));
        txtDni = new JTextField(12);
        panel.add(txtDni, getGbc(1, row++, 1));

        panel.add(new JLabel("FULL NAME:"), getGbc(0, row, 1));
        txtFullName = new JTextField(12);
        panel.add(txtFullName, getGbc(1, row++, 1));

        panel.add(new JLabel("EMAIL:"), getGbc(0, row, 1));
        txtEmail = new JTextField(12);
        panel.add(txtEmail, getGbc(1, row++, 1));

        // --- CATEGORY ---
        panel.add(createSectionHeader("CATEGORY"), getGbc(0, row++, 2));

        panel.add(new JLabel("AGE CATEGORY:"), getGbc(0, row, 1));
        cbAgeCategory = new JComboBox<>(new String[]{"Adults", "Seniors", "Under 26", "Children"});
        cbAgeCategory.addActionListener(e -> updatePriceAndSummary());
        panel.add(cbAgeCategory, getGbc(1, row++, 1));

        // --- TEAM SELECTION ---
        panel.add(createSectionHeader("TEAM SELECTION"), getGbc(0, row++, 2));

        panel.add(new JLabel("SELECTED TEAM:"), getGbc(0, row, 1));
        cbTeam = new JComboBox<>(new String[]{"First Team", "Women's Team", "Youth Team"});
        cbTeam.addActionListener(e -> loadOccupiedSeats());
        panel.add(cbTeam, getGbc(1, row++, 1));

        panel.add(new JLabel("VALID PERIOD:"), getGbc(0, row, 1));
        String[] bounds = SeasonDateUtils.getCurrentSeasonBounds();
        txtValidPeriod = new JTextField(bounds[0] + " - " + bounds[1]);
        txtValidPeriod.setEditable(false);
        panel.add(txtValidPeriod, getGbc(1, row++, 1));

        // --- SEAT LOCATION ---
        panel.add(createSectionHeader("SEAT LOCATION"), getGbc(0, row++, 2));

        panel.add(new JLabel("GRANDSTAND:"), getGbc(0, row, 1));
        cbGrandstand = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        cbGrandstand.addActionListener(e -> loadOccupiedSeats());
        panel.add(cbGrandstand, getGbc(1, row++, 1));

        panel.add(new JLabel("SECTION:"), getGbc(0, row, 1));
        cbSection = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F"});
        cbSection.addActionListener(e -> loadOccupiedSeats());
        panel.add(cbSection, getGbc(1, row++, 1));

        JPanel seatDetailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        lblSelectedRow = new JLabel("-");
        lblSelectedSeat = new JLabel("-");
        seatDetailPanel.add(new JLabel("ROW:"));
        seatDetailPanel.add(lblSelectedRow);
        seatDetailPanel.add(new JLabel("SEAT:"));
        seatDetailPanel.add(lblSelectedSeat);
        panel.add(seatDetailPanel, getGbc(0, row++, 2));

        // --- SUMMARY ---
        panel.add(createSectionHeader("SUMMARY"), getGbc(0, row++, 2));

        panel.add(new JLabel("FINAL PRICE:"), getGbc(0, row, 1));
        lblFinalPrice = new JLabel("100.00 €");
        lblFinalPrice.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblFinalPrice.setForeground(new Color(0, 128, 0));
        panel.add(lblFinalPrice, getGbc(1, row++, 1));

        return panel;
    }

    private JPanel createRightGridPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        
        JLabel lblGridTitle = new JLabel("SEAT SELECTION", SwingConstants.CENTER);
        lblGridTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        rightPanel.add(lblGridTitle, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(10, 15, 4, 4));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int rowIndex = 0; rowIndex < 10; rowIndex++) {
            for (int colIndex = 0; colIndex < 15; colIndex++) {
                JButton btn = new JButton();
                btn.setFont(new Font("SansSerif", Font.BOLD, 12));
                btn.setMargin(new Insets(0, 0, 0, 0)); // Fix padding to prevent '...'
                btn.setFocusable(false);
                btn.setPreferredSize(new Dimension(36, 36));

                final int r = rowIndex;
                final int c = colIndex;

                btn.addActionListener(e -> selectSeat(r, c));
                seatButtons[rowIndex][colIndex] = btn;
                gridPanel.add(btn);
            }
        }

        rightPanel.add(gridPanel, BorderLayout.CENTER);

        // Legend
        JLabel lblLegend = new JLabel("Click an available green seat to select it (turns blue).", SwingConstants.CENTER);
        lblLegend.setFont(new Font("SansSerif", Font.ITALIC, 11));
        rightPanel.add(lblLegend, BorderLayout.SOUTH);

        return rightPanel;
    }

    private JLabel createSectionHeader(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(new Color(25, 75, 140));
        return label;
    }

    private GridBagConstraints getGbc(int x, int y, int width) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void loadOccupiedSeats() {
        String teamName = (String) cbTeam.getSelectedItem();
        String grandstand = (String) cbGrandstand.getSelectedItem();
        String section = (String) cbSection.getSelectedItem();

        // Reset user selection when changing team, grandstand, or section
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
                    // Occupied / Season Pass Taken -> Solid Red
                    btn.setText("O");
                    btn.setBackground(new Color(220, 53, 69)); // Crimson Red
                    btn.setForeground(Color.WHITE);
                    btn.setOpaque(true);
                    btn.setBorderPainted(false); // Removes OS disable styling overlay
                    btn.setEnabled(false);
                } else if (rowIndex == selectedRow && colIndex == selectedSeat) {
                    // Currently Selected -> Solid Blue
                    btn.setText("X");
                    btn.setBackground(new Color(13, 110, 253)); // Electric Blue
                    btn.setForeground(Color.WHITE);
                    btn.setOpaque(true);
                    btn.setBorderPainted(true);
                    btn.setEnabled(true);
                } else {
                    // Available -> Solid Green
                    btn.setText("A");
                    btn.setBackground(new Color(40, 167, 69)); // Success Green
                    btn.setForeground(Color.WHITE);
                    btn.setOpaque(true);
                    btn.setBorderPainted(true);
                    btn.setEnabled(true);
                }
            }
        }
    }

    private void selectSeat(int rowIndex, int colIndex) {
        if (occupiedMatrix[rowIndex][colIndex]) return;

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
            JOptionPane.showMessageDialog(this, "Please select an available seat on the grid.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean success = controller.purchaseSeasonPass(
                    txtDni.getText().trim(),
                    txtFullName.getText().trim(),
                    txtEmail.getText().trim(),
                    (String) cbTeam.getSelectedItem(),
                    (String) cbGrandstand.getSelectedItem(),
                    (String) cbSection.getSelectedItem(),
                    selectedRow + 1,
                    selectedSeat + 1,
                    (String) cbAgeCategory.getSelectedItem(),
                    basePrice
            );

            if (success) {
                JOptionPane.showMessageDialog(this, "Season pass successfully purchased!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}