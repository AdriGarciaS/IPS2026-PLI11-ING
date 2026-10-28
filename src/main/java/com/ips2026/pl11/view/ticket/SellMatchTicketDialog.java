package com.ips2026.pl11.view.ticket;

import com.ips2026.pl11.controller.ticket.MatchTicketController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for purchasing match tickets, featuring a left-side control panel,
 * a central graphical seat board, and a bottom action bar with Buy and Close buttons.
 */
public class SellMatchTicketDialog extends JDialog {

    private final MatchTicketController controller;
    private final int matchId;

    private JComboBox<String> cbGrandstand;
    private JComboBox<String> cbSection;
    private JSpinner spinnerQuantity;
    private StandBoardPanel boardPanel;

    public SellMatchTicketDialog(Window parent, int matchId, String matchDisplayName) {
        super(parent, "Ticket Sales - " + matchDisplayName, ModalityType.APPLICATION_MODAL);
        this.matchId = matchId;
        this.controller = new MatchTicketController();

        initUI();
        refreshBoard();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left Panel: Ticket Details Selection Form
        JPanel leftFormPanel = new JPanel();
        leftFormPanel.setLayout(new BoxLayout(leftFormPanel, BoxLayout.Y_AXIS));
        leftFormPanel.setBorder(BorderFactory.createTitledBorder("Ticket Details"));

        // Grandstand selector
        JPanel pnlGrandstand = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlGrandstand.add(new JLabel("Grandstand:"));
        cbGrandstand = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        cbGrandstand.addActionListener(e -> refreshBoard());
        pnlGrandstand.add(cbGrandstand);
        leftFormPanel.add(pnlGrandstand);

        // Section selector
        JPanel pnlSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSection.add(new JLabel("Section:       "));
        cbSection = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F"});
        cbSection.addActionListener(e -> refreshBoard());
        pnlSection.add(cbSection);
        leftFormPanel.add(pnlSection);

        // Quantity selector
        JPanel pnlQuantity = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlQuantity.add(new JLabel("Quantity:      "));
        spinnerQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
        pnlQuantity.add(spinnerQuantity);
        leftFormPanel.add(pnlQuantity);

        add(leftFormPanel, BorderLayout.WEST);

        // Center Panel: Seating Grid Board
        boardPanel = new StandBoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // Bottom Action Panel: Buy and Close buttons
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton btnBuy = new JButton("Buy Contiguous Tickets");
        btnBuy.addActionListener(e -> processPurchase());
        bottomActionPanel.add(btnBuy);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        bottomActionPanel.add(btnClose);

        add(bottomActionPanel, BorderLayout.SOUTH);

        // Ensure proper window sizing and positioning
        setPreferredSize(new Dimension(850, 500));
        pack();
        setLocationRelativeTo(getParent());
    }

    /**
     * Reads selected grandstand and section, fetching seat status to refresh the central board panel.
     */
    private void refreshBoard() {
        String grandstand = (String) cbGrandstand.getSelectedItem();
        String section = (String) cbSection.getSelectedItem();

        if (grandstand == null || section == null) return;

        try {
            boolean[][] occupiedMatrix = controller.getOccupiedSeats(matchId, grandstand, section);
            boardPanel.updateBoard(occupiedMatrix);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading seat map: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Processes ticket purchase via controller and updates board dynamically.
     */
    private void processPurchase() {
        String grandstand = (String) cbGrandstand.getSelectedItem();
        String section = (String) cbSection.getSelectedItem();
        int quantity = (Integer) spinnerQuantity.getValue();

        try {
            var assignedSeats = controller.sellTickets(matchId, grandstand, section, quantity);

            StringBuilder seatsMsg = new StringBuilder("Tickets booked successfully in Grandstand ")
                    .append(grandstand).append(", Section ").append(section).append(":\n");

            for (int[] seat : assignedSeats) {
                // Array coordinates [row, col] mapped to 1-based display numbers
                seatsMsg.append("• Row ").append(seat[0] + 1).append(", Seat ").append(seat[1] + 1).append("\n");
            }

            JOptionPane.showMessageDialog(this, seatsMsg.toString(), "Success", JOptionPane.INFORMATION_MESSAGE);

            // Dynamically refresh visual board panel
            refreshBoard();

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No Contiguous Seats", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}