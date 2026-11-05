package com.ips2026.pl11.view.ticket;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

import com.ips2026.pl11.controller.email.EmailController;
import com.ips2026.pl11.controller.ticket.MatchTicketController;
import com.ips2026.pl11.controller.ticket.SpecialMatchTicketController;
import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.model.ticket.HomeMatch;
import com.ips2026.pl11.view.common.Branding;

import jakarta.mail.MessagingException;

/**
 * Dialog for purchasing match tickets, supporting standard seat sales as well as
 * US 35777 Special Match season pass member seat verification and payment check-in.
 */
public class SellMatchTicketDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private  EmailController emailController = new EmailController("smtp.gmail.com", 587, "ipswichmgmt@gmail.com", "lndqblaxqraeetjw");
    private String matchDisplayName;

    private final MatchTicketController controller;
    private final SpecialMatchTicketController specialController;
    private final int matchId;
    private HomeMatch currentMatch;

    // Standard Ticket Purchase UI Controls
    private JComboBox<String> cbGrandstand;
    private JComboBox<String> cbSection;
    private JSpinner spinnerQuantity;
    private StandBoardPanel boardPanel;

    // US 35777 Special Match Check-in Controls
    private JPanel pnlSpecialMemberCheckIn;
    private JTextField txtMemberDni;
    private JButton btnVerifyMember;
    private JButton btnPayMemberSeat;
    private String[] claimedMemberSeat = null;

    /**
     * Constructs the ticket sales dialog attached to a parent window.
     *
     * @param parent the anchoring window for modal display
     * @param matchId the unique database identifier of the selected match
     * @param matchDisplayName the text description of the match for the title bar
     */
    public SellMatchTicketDialog(Window parent, int matchId, String matchDisplayName) {
        super(parent, "Ticket Sales - " + matchDisplayName, ModalityType.APPLICATION_MODAL);
        this.matchId = matchId;
        this.controller = new MatchTicketController();
        this.specialController = new SpecialMatchTicketController();
        
        this.matchDisplayName = matchDisplayName;

        try {
            MatchDAO matchDAO = new MatchDAO();
            this.currentMatch = matchDAO.getMatchById(matchId);
        } catch (Exception ex) {
            this.currentMatch = null;
        }

        Branding.applyTo(this);
        initUI();
        refreshBoard();
    }

    /**
     * Initializes components, layouts, card panels, and bottom action controls.
     */
    private void initUI() {
        getContentPane().setLayout(new BorderLayout(15, 15));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left Control Panel Container
        JPanel leftFormPanel = new JPanel();
        leftFormPanel.setOpaque(false);
        leftFormPanel.setLayout(new BoxLayout(leftFormPanel, BoxLayout.Y_AXIS));

        // General Ticket Details Card Panel
        JPanel pnlDetails = new JPanel();
        pnlDetails.setOpaque(false);
        pnlDetails.setLayout(new BoxLayout(pnlDetails, BoxLayout.Y_AXIS));
        pnlDetails.setBorder(new RoundedTitledBorder("Ticket Details", 16));

        // Grandstand Selector
        JPanel pnlGrandstand = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlGrandstand.setOpaque(false);
        pnlGrandstand.add(new JLabel("Grandstand:"));
        cbGrandstand = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        cbGrandstand.addActionListener(e -> refreshBoard());
        pnlGrandstand.add(cbGrandstand);
        pnlDetails.add(pnlGrandstand);

        // Section Selector
        JPanel pnlSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSection.setOpaque(false);
        pnlSection.add(new JLabel("Section:       "));
        cbSection = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F"});
        cbSection.addActionListener(e -> refreshBoard());
        pnlSection.add(cbSection);
        pnlDetails.add(pnlSection);

        // Quantity Selector
        JPanel pnlQuantity = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlQuantity.setOpaque(false);
        pnlQuantity.add(new JLabel("Quantity:      "));
        spinnerQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
        pnlQuantity.add(spinnerQuantity);
        pnlDetails.add(pnlQuantity);

        leftFormPanel.add(pnlDetails);

        // US 35777: Season Pass Member Check-in Panel (Rendered dynamically for Special Matches only)
        if (currentMatch != null && currentMatch.isSpecialMatch()) {
            pnlSpecialMemberCheckIn = createSpecialMatchMemberPanel();
            leftFormPanel.add(pnlSpecialMemberCheckIn);
        }

        getContentPane().add(leftFormPanel, BorderLayout.WEST);

        // Center Panel: Visual Stadium Board
        boardPanel = new StandBoardPanel();
        getContentPane().add(boardPanel, BorderLayout.CENTER);

        // Footer Actions Bar
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomActionPanel.setOpaque(false);

        JButton btnBuy = new JButton("Buy General Tickets");
        btnBuy.setBackground(Branding.NAVY);
        btnBuy.setForeground(Branding.WHITE);
        btnBuy.addActionListener(e -> processGeneralPurchase());
        bottomActionPanel.add(btnBuy);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        bottomActionPanel.add(btnClose);

        getContentPane().add(bottomActionPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(880, 560));
        pack();
        setLocationRelativeTo(getParent());
    }

    /**
     * Builds the interactive check-in module for US 35777 (Season Pass Member Verification).
     *
     * @return the constructed member verification panel
     */
    private JPanel createSpecialMatchMemberPanel() {
        JPanel pn = new JPanel();
        pn.setOpaque(false);
        pn.setLayout(new BoxLayout(pn, BoxLayout.Y_AXIS));
        pn.setBorder(new RoundedTitledBorder("US 35777: Season Pass Member Check-in", 16));

        JPanel pnlDni = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlDni.setOpaque(false);
        pnlDni.add(new JLabel("Member DNI:"));
        txtMemberDni = new JTextField(10);
        pnlDni.add(txtMemberDni);
        pn.add(pnlDni);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBtns.setOpaque(false);

        btnVerifyMember = new JButton("Verify Seat");
        btnPayMemberSeat = new JButton("Pay & Lock Seat (€30)");
        btnPayMemberSeat.setEnabled(false);

        btnVerifyMember.addActionListener(e -> handleVerifyMemberSeat());
        btnPayMemberSeat.addActionListener(e -> handlePayMemberSeat());

        pnlBtns.add(btnVerifyMember);
        pnlBtns.add(btnPayMemberSeat);
        pn.add(pnlBtns);

        return pn;
    }

    /**
     * Handles verification of the season pass member's DNI and locates their usual seat location.
     */
    private void handleVerifyMemberSeat() {
        String dni = txtMemberDni.getText().trim();
        if (dni.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your Season Pass Member DNI.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            claimedMemberSeat = specialController.verifyAndClaimMemberSeat(matchId, dni);
            cbGrandstand.setSelectedItem(claimedMemberSeat[0]);
            cbSection.setSelectedItem(claimedMemberSeat[1]);

            JOptionPane.showMessageDialog(this,
                    "Regular seat identified!\n" +
                    "Grandstand: " + claimedMemberSeat[0] + ", Section: " + claimedMemberSeat[1] +
                    "\nRow: " + claimedMemberSeat[2] + ", Seat: " + claimedMemberSeat[3] +
                    "\n\nClick 'Pay & Lock Seat' before the cutoff date to confirm your reservation.",
                    "Member Verified", JOptionPane.INFORMATION_MESSAGE);

            btnPayMemberSeat.setEnabled(true);
            refreshBoard();

        } catch (Exception ex) {
            btnPayMemberSeat.setEnabled(false);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Special Match Verification Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Processes payment for the verified season pass member seat and locks it exclusively for the match.
     */
    private void handlePayMemberSeat() {
        if (claimedMemberSeat == null) {
            return;
        }

        try {
            String grandstand = claimedMemberSeat[0];
            String section = claimedMemberSeat[1];
            int row = Integer.parseInt(claimedMemberSeat[2]);
            int seat = Integer.parseInt(claimedMemberSeat[3]);

            boolean success = specialController.confirmMemberSpecialTicket(matchId, grandstand, section, row, seat, 30.0);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Payment successful! Your regular seat (Row " + row + ", Seat " + seat + ") is secured for this special match.",
                        "Ticket Secured", JOptionPane.INFORMATION_MESSAGE);

                btnPayMemberSeat.setEnabled(false);
                txtMemberDni.setText("");
                claimedMemberSeat = null;
                refreshBoard();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Payment failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Fetches current seat availability for the active grandstand/section selection and updates the board grid.
     */
    private void refreshBoard() {
        String grandstand = (String) cbGrandstand.getSelectedItem();
        String section = (String) cbSection.getSelectedItem();

        if (grandstand == null || section == null) {
            return;
        }

        try {
            boolean[][] occupiedMatrix = controller.getOccupiedSeats(matchId, grandstand, section);
            boardPanel.updateBoard(occupiedMatrix);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading seat map: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Processes general public purchase for contiguous seats in the selected stand and section.
     */
    private void processGeneralPurchase() {
        String grandstand = (String) cbGrandstand.getSelectedItem();
        String section = (String) cbSection.getSelectedItem();
        int quantity = (Integer) spinnerQuantity.getValue();

        try {
            var assignedSeats = controller.sellTickets(matchId, grandstand, section, quantity, 30.0);

            StringBuilder seatsMsg = new StringBuilder("Tickets booked successfully in Grandstand ")
                    .append(grandstand).append(", Section ").append(section).append(":\n");

            List<String> seatLines = new ArrayList<>();
            for (int[] seat : assignedSeats) {
                int row = seat[0] < 10 ? seat[0] + 1 : seat[0];
                int col = seat[1] < 15 ? seat[1] + 1 : seat[1];
                seatLines.add("Row " + row + ", Seat " + col);
                seatsMsg.append("• Row ").append(row).append(", Seat ").append(col).append("\n");
            }
            
            String email = askForEmail();
            if(email == null) {
                return;
            }
            
            try {
                BigDecimal totalPrice = BigDecimal.valueOf(quantity * 30.0);
                emailController.sendTicketReceipt(email, matchDisplayName, grandstand, section, seatLines, totalPrice);
                JOptionPane.showMessageDialog(this, seatsMsg.toString()+ "\nReceipt sent to " + email, "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (MessagingException | IllegalStateException exception) {
                JOptionPane.showMessageDialog(this,
                    "The receipt could not be sent:\n" + exception.getMessage(),
                    "Email error", JOptionPane.ERROR_MESSAGE);
            }

            refreshBoard();

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No Contiguous Seats", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String askForEmail() {
        while (true) {
            JTextField txtEmail = new JTextField(25);

            int result = JOptionPane.showConfirmDialog(this,
                new Object[] { "Enter the customer's email address:", txtEmail }, "Send purchase receipt",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            String email = txtEmail.getText().trim();

            if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                return email;
            }

            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid email",
                JOptionPane.ERROR_MESSAGE);
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