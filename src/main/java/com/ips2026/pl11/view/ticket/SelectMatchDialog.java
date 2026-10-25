package com.ips2026.pl11.view.ticket;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.ips2026.pl11.model.ticket.HomeMatch;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class SelectMatchDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JComboBox<HomeMatch> cbMatches;
    private HomeMatch selectedMatch;

    public SelectMatchDialog(JFrame parent, List<HomeMatch> matches) {
    	super(parent, "Select Home Match", true);
        Branding.applyTo(this);
        setSize(520, 280);
        setLocationRelativeTo(parent);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Branding.BACKGROUND);
        setContentPane(contentPane);

        HeaderPanel header = new HeaderPanel("Ticket Sales - Select Match", 
                "Choose a home match to manage section and seat reservations");
        contentPane.add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        centerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        centerPanel.setOpaque(false);

        JLabel lblSelect = new JLabel("Select Home Match:");
        lblSelect.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        lblSelect.setForeground(Branding.NAVY);

        cbMatches = new JComboBox<>(matches.toArray(new HomeMatch[0]));
        cbMatches.setPreferredSize(new Dimension(0, 32));
        cbMatches.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 13));

        centerPanel.add(lblSelect);
        centerPanel.add(cbMatches);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setOpaque(false);

        JButton btnContinue = new JButton("Continue to Seats");
        btnContinue.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        btnContinue.setBackground(Branding.NAVY);
        btnContinue.setForeground(Color.WHITE);

        btnContinue.addActionListener(e -> {
            selectedMatch = (HomeMatch) cbMatches.getSelectedItem();
            if (selectedMatch != null) {
                dispose();
            }
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnContinue);
        footer.add(btnCancel);
        contentPane.add(footer, BorderLayout.SOUTH);
    }

    public HomeMatch getSelectedMatch() {
        return selectedMatch;
    }
}