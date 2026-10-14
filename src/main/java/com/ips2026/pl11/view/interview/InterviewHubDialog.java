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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.ips2026.pl11.controller.slots.InterviewSlotsController;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.slots.CreateInterviewSlotDialog;

public class InterviewHubDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JFrame parentFrame;
    private final InterviewSlotsController controller;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnCards;
    private JPanel pnFooter;
    private JButton btnClose;

    /**
     * Main constructor: only requires parent frame and the controller.
     * Coaches and players are loaded dynamically inside CreateInterviewSlotDialog.
     */
    public InterviewHubDialog(JFrame parent, InterviewSlotsController controller) {
        super(parent, "Interview Management - Selection Hub", true);
        this.parentFrame = parent;
        this.controller = (controller != null) ? controller : new InterviewSlotsController();

        // Corporate styling and club window icons
        Branding.applyTo(this);

        initComponents();
        setSize(580, 410);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * Overloaded constructor for backwards compatibility.
     */
    public InterviewHubDialog(JFrame parent, InterviewSlotsController controller, int coachId, List<Employee> players) {
        this(parent, controller);
    }

    private void initComponents() {
        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(Branding.BACKGROUND);
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);

        pnBody = new JPanel(new BorderLayout(0, 16));
        pnBody.setOpaque(false);
        pnBody.setBorder(new EmptyBorder(24, 24, 18, 24));
        pnBody.add(getPnCards(), BorderLayout.CENTER);
        pnBody.add(getPnFooter(), BorderLayout.SOUTH);

        contentPane.add(pnBody, BorderLayout.CENTER);
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(
                    "Interview Management Hub",
                    "Choose whether to define availability slots or book interviews for media outlets"
            );
        }
        return pnHeader;
    }

    private JPanel getPnCards() {
        if (pnCards == null) {
            pnCards = new JPanel(new GridLayout(1, 2, 16, 0));
            pnCards.setOpaque(false);

            JPanel cardCreate = createHubOptionCard(
                    "Create Slots",
                    "Coach / Staff",
                    "Define interview availability time slots for professional team players.",
                    Branding.NAVY,
                    () -> openCreateSlotsDialog()
            );

            JPanel cardBook = createHubOptionCard(
                    "Book Interview",
                    "Communications Dir.",
                    "Select an available slot, enter media details, and finalize interview booking.",
                    new Color(0x1F, 0x4E, 0x8C),
                    () -> openBookInterviewDialog()
            );

            pnCards.add(cardCreate);
            pnCards.add(cardBook);
        }
        return pnCards;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnFooter.setOpaque(false);
            pnFooter.add(getBtnClose());
        }
        return pnFooter;
    }

    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton("Close");
            btnClose.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            btnClose.setFocusPainted(false);
            btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnClose.setBackground(Color.WHITE);
            btnClose.setForeground(Branding.NAVY);
            btnClose.setPreferredSize(new Dimension(90, 32));
            btnClose.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(6, 14, 6, 14)
            ));

            btnClose.addActionListener(e -> dispose());
        }
        return btnClose;
    }

    private JPanel createHubOptionCard(String actionTitle, String roleBadgeText, String descriptionText, Color accentColor, Runnable onAction) {
        JPanel card = new JPanel(new BorderLayout(8, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Branding.BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 16, 18, 16)
        ));

        JPanel cardTop = new JPanel(new BorderLayout(4, 4));
        cardTop.setOpaque(false);

        JLabel lblBadge = new JLabel(roleBadgeText.toUpperCase());
        lblBadge.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 10));
        lblBadge.setForeground(Branding.NAVY_DARKER);

        JLabel lblAction = new JLabel(actionTitle);
        lblAction.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 16));
        lblAction.setForeground(Branding.NAVY);

        cardTop.add(lblBadge, BorderLayout.NORTH);
        cardTop.add(lblAction, BorderLayout.SOUTH);
        card.add(cardTop, BorderLayout.NORTH);

        JLabel lblDesc = new JLabel("<html><body style='width: 170px;'>" + descriptionText + "</body></html>");
        lblDesc.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        lblDesc.setForeground(Branding.TEXT_MUTED);
        lblDesc.setVerticalAlignment(SwingConstants.TOP);
        card.add(lblDesc, BorderLayout.CENTER);

        JButton btnSelect = new JButton("Open " + actionTitle);
        btnSelect.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setBackground(accentColor);
        btnSelect.setFocusPainted(false);
        btnSelect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSelect.setPreferredSize(new Dimension(190, 36));
        btnSelect.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(accentColor, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnSelect.addActionListener(e -> onAction.run());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnSelect);
        card.add(btnWrapper, BorderLayout.SOUTH);

        return card;
    }

    private void openCreateSlotsDialog() {
        this.setVisible(false);
        CreateInterviewSlotDialog dialog = new CreateInterviewSlotDialog(parentFrame, controller);
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