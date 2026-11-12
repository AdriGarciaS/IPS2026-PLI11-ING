package com.ips2026.pl11.view.interview;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.controller.slots.InterviewSlotsController;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MenuCardButton;
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
    private MenuCardButton btnCreateSlots;
    private MenuCardButton btnBookInterview;

    public InterviewHubDialog(JFrame parent, InterviewSlotsController controller) {
        super(parent, "Interview Management - Hub", true);
        this.parentFrame = parent;
        this.controller = (controller != null) ? controller : new InterviewSlotsController();

        Branding.applyTo(this);
        initComponents();
        setSize(520, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public InterviewHubDialog(JFrame parent, InterviewSlotsController controller, int coachId, List<Employee> players) {
        this(parent, controller);
    }

    private void initComponents() {
        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(Branding.BACKGROUND);
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);

        pnBody = new JPanel(new BorderLayout(0, 0));
        pnBody.setOpaque(false);
        pnBody.setBorder(new EmptyBorder(20, 20, 16, 20));
        pnBody.add(getPnCards(), BorderLayout.CENTER);

        contentPane.add(pnBody, BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(
                    "Interview Management",
                    "Manage availability slots and book press interviews"
            );
        }
        return pnHeader;
    }

    private JPanel getPnCards() {
        if (pnCards == null) {
            pnCards = new JPanel();
            pnCards.setOpaque(false);
            pnCards.setLayout(new GridLayout(2, 1, 0, 16));
            pnCards.add(getBtnCreateSlots());
            pnCards.add(getBtnBookInterview());
        }
        return pnCards;
    }

    private MenuCardButton getBtnCreateSlots() {
        if (btnCreateSlots == null) {
            btnCreateSlots = new MenuCardButton("Create Slots", "Define interview availability time slots for professional team players");
            btnCreateSlots.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnCreateSlots.setMnemonic('C');
            btnCreateSlots.addActionListener(e -> openCreateSlotsDialog());
        }
        return btnCreateSlots;
    }

    private MenuCardButton getBtnBookInterview() {
        if (btnBookInterview == null) {
            btnBookInterview = new MenuCardButton("Book Interview", "Select an available slot, enter media details, and finalize interview booking");
            btnBookInterview.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnBookInterview.setMnemonic('B');
            btnBookInterview.addActionListener(e -> openBookInterviewDialog());
        }
        return btnBookInterview;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel();
            pnFooter.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(1, 0, 0, 0, Branding.BORDER),
                    new EmptyBorder(8, 0, 10, 20)));
            pnFooter.setLayout(new FlowLayout(FlowLayout.RIGHT));
            pnFooter.setOpaque(false);
            pnFooter.add(getBtnClose());
        }
        return pnFooter;
    }

    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton("Close");
            btnClose.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
            btnClose.setFocusPainted(false);
            btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnClose.addActionListener(e -> dispose());
        }
        return btnClose;
    }

    private void openCreateSlotsDialog() {
        this.setVisible(false);
        CreateInterviewSlotDialog dialog = new CreateInterviewSlotDialog(parentFrame, controller);
        dialog.setVisible(true);
        this.setVisible(true);
    }

    private void openBookInterviewDialog() {
        this.setVisible(false);
        try {
            BookInterviewDialog dialog = new BookInterviewDialog(parentFrame);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error opening Book Interview dialog: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        this.setVisible(true);
    }
}