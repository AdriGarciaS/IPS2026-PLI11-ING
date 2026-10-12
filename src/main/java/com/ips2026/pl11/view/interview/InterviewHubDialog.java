package com.ips2026.pl11.view.interview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.ips2026.pl11.controller.slots.InterviewSlotsController;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.view.slots.CreateInterviewSlotDialog;


public class InterviewHubDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JFrame parentFrame;
    private final InterviewSlotsController controller;
    private final int coachId;
    private final List<Employee> players;

    public InterviewHubDialog(JFrame parent, InterviewSlotsController controller, int coachId, List<Employee> players) {
        super(parent, "Interview Management - Selection Hub", true);
        this.parentFrame = parent;
        this.controller = controller;
        this.coachId = coachId;
        this.players = players;

        initComponents();
        setSize(560, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        getContentPane().setLayout(new BorderLayout());

        // Header panel
        JPanel pnHeader = new JPanel(new BorderLayout(5, 5));
        pnHeader.setBackground(new Color(248, 250, 252));
        pnHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)
        ));

        JLabel lblTitle = new JLabel("Interview Management Hub");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSubtitle = new JLabel("Choose whether to define player availability slots or book interviews for media outlets.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        pnHeader.add(lblTitle, BorderLayout.NORTH);
        pnHeader.add(lblSubtitle, BorderLayout.SOUTH);
        getContentPane().add(pnHeader, BorderLayout.NORTH);

        // Center action cards
        JPanel pnCards = new JPanel(new GridLayout(1, 2, 16, 0));
        pnCards.setBackground(Color.WHITE);
        pnCards.setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));

        JPanel cardCreate = createHubOptionCard(
                "Create Slots",
                "Coach / Staff",
                "Define interview availability time slots for professional team players.",
                new Color(37, 99, 235),
                () -> openCreateSlotsDialog()
        );

        JPanel cardBook = createHubOptionCard(
                "Book Interview",
                "Communications Dir.",
                "Select an available slot, enter media details, and finalize interview booking.",
                new Color(13, 148, 136),
                () -> openBookInterviewDialog()
        );

        pnCards.add(cardCreate);
        pnCards.add(cardBook);
        getContentPane().add(pnCards, BorderLayout.CENTER);

        // Footer panel
        JPanel pnFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        pnFooter.setBackground(new Color(248, 250, 252));
        pnFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.setFocusPainted(false);
        btnClose.setPreferredSize(new Dimension(85, 30));
        btnClose.addActionListener(e -> dispose());

        pnFooter.add(btnClose);
        getContentPane().add(pnFooter, BorderLayout.SOUTH);
    }

    private JPanel createHubOptionCard(String actionTitle, String roleBadgeText, String descriptionText, Color accentColor, Runnable onAction) {
        JPanel card = new JPanel(new BorderLayout(8, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(16, 14, 16, 14)
        ));

        JPanel cardTop = new JPanel(new BorderLayout(4, 4));
        cardTop.setOpaque(false);

        JLabel lblBadge = new JLabel(roleBadgeText.toUpperCase());
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadge.setForeground(accentColor);

        JLabel lblAction = new JLabel(actionTitle);
        lblAction.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAction.setForeground(new Color(30, 41, 59));

        cardTop.add(lblBadge, BorderLayout.NORTH);
        cardTop.add(lblAction, BorderLayout.SOUTH);
        card.add(cardTop, BorderLayout.NORTH);

        JLabel lblDesc = new JLabel("<html><body style='width: 170px;'>" + descriptionText + "</body></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(100, 116, 139));
        lblDesc.setVerticalAlignment(SwingConstants.TOP);
        card.add(lblDesc, BorderLayout.CENTER);

        JButton btnSelect = new JButton("Open " + actionTitle);
        btnSelect.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setBackground(accentColor);
        btnSelect.setFocusPainted(false);
        btnSelect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSelect.setPreferredSize(new Dimension(180, 36));
        btnSelect.addActionListener(e -> onAction.run());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnSelect);
        card.add(btnWrapper, BorderLayout.SOUTH);

        return card;
    }

    private void openCreateSlotsDialog() {
    	this.setVisible(false);
        CreateInterviewSlotDialog dialog = new CreateInterviewSlotDialog(parentFrame, controller, coachId, players);
        dialog.setVisible(true);
        this.setVisible(true);
    }

    private void openBookInterviewDialog() {
    	this.setVisible(false);
        BookInterviewDialog dialog = new BookInterviewDialog(parentFrame);
        dialog.setVisible(true);
        this.setVisible(true);
    }
}