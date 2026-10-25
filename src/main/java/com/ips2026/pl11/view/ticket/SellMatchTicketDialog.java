package com.ips2026.pl11.view.ticket;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.ips2026.pl11.controller.ticket.MatchTicketController;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class SellMatchTicketDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final MatchTicketController controller;
    private final int matchId;

    private JComboBox<String> cbGrandstand;
    private JComboBox<String> cbSection;
    private JSpinner spQuantity;
    private JLabel lblTotalPrice;

    public SellMatchTicketDialog(JFrame parent, int matchId, String matchTitle) {
        super(parent, "Sell Match Tickets", true);
        this.matchId = matchId;
        this.controller = new MatchTicketController();

        Branding.applyTo(this);
        setSize(520, 420);
        setLocationRelativeTo(parent);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Branding.BACKGROUND);
        setContentPane(contentPane);

        HeaderPanel header = new HeaderPanel("Ticket Sales: " + matchTitle, 
                "Select grandstand, section and number of contiguous seats (Max 15 per row)");
        contentPane.add(header, BorderLayout.NORTH);

        JPanel pnForm = new JPanel(new GridLayout(5, 2, 10, 14));
        pnForm.setOpaque(false);
        pnForm.setBorder(new EmptyBorder(20, 30, 20, 30));

        cbGrandstand = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        cbSection = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F"});
        spQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
        
        lblTotalPrice = new JLabel("30.00 €");
        lblTotalPrice.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
        lblTotalPrice.setForeground(Branding.NAVY);

        spQuantity.addChangeListener(e -> {
            int qty = (int) spQuantity.getValue();
            lblTotalPrice.setText(String.format("%.2f €", qty * 30.0));
        });

        pnForm.add(createLabel("Grandstand:"));
        pnForm.add(cbGrandstand);
        pnForm.add(createLabel("Section:"));
        pnForm.add(cbSection);
        pnForm.add(createLabel("Tickets (Contiguous):"));
        pnForm.add(spQuantity);
        pnForm.add(createLabel("Total Price (30€/ea):"));
        pnForm.add(lblTotalPrice);

        contentPane.add(pnForm, BorderLayout.CENTER);

        JPanel pnFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnFooter.setOpaque(false);

        JButton btnSell = new JButton("Sell Tickets");
        btnSell.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        btnSell.setBackground(Branding.NAVY);
        btnSell.setForeground(Color.WHITE);
        btnSell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnSell.addActionListener(e -> processSale());

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());

        pnFooter.add(btnSell);
        pnFooter.add(btnClose);
        contentPane.add(pnFooter, BorderLayout.SOUTH);
    }

    private void processSale() {
        String grandstand = (String) cbGrandstand.getSelectedItem();
        String section = (String) cbSection.getSelectedItem();
        int qty = (int) spQuantity.getValue();

        try {
            List<int[]> seats = controller.sellTickets(matchId, grandstand, section, qty);
            
            StringBuilder sb = new StringBuilder("Tickets issued successfully!\n\n");
            sb.append("Grandstand: ").append(grandstand).append(" | Section: ").append(section).append("\n");
            sb.append("Assigned Seats:\n");
            for (int[] seat : seats) {
                sb.append(" • Row ").append(seat[0]).append(", Seat ").append(seat[1]).append("\n");
            }
            sb.append("\nTotal Charged: ").append(qty * 30).append(" €");

            JOptionPane.showMessageDialog(this, sb.toString(), "Sale Confirmed", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Availability Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        lbl.setForeground(Branding.NAVY);
        return lbl;
    }
}