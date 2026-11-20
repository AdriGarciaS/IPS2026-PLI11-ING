package com.ips2026.pl11.view.menu.mainmenus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.controller.slots.InterviewSlotsController;
import com.ips2026.pl11.data.slots.InterviewSlotDAO;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.news.NewsFeedWindow;
import com.ips2026.pl11.view.slots.CreateInterviewSlotDialog;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class CoachMenuWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnButtons;
    private MenuCardButton btnOption1;
    private MenuCardButton btnOption13;

    private JPanel pnFooter;
    private JLabel lblFooter;
    
    InterviewSlotsController controller = new InterviewSlotsController(new InterviewSlotDAO());

    public CoachMenuWindow() {
        setTitle("Football Club Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        Branding.setInitialSize(this, 640, 480, null);
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Football Club Management",
                    "Coach main menu — choose an option to start", true);
        }
        return pnHeader;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setBorder(new EmptyBorder(24, 24, 16, 24));
            pnButtons.setLayout(new GridLayout(2, 1, 18, 18));
            pnButtons.add(getBtnOption1());
            pnButtons.add(getBtnOption13());
        }
        return pnButtons;
    }

    private MenuCardButton getBtnOption1() {
        if (btnOption1 == null) {
            btnOption1 = new MenuCardButton("Interview Management", "Manage availability slots and book press interviews");
            btnOption1.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption1.addActionListener(e -> openCreateSlotsDialog());
        }
        return btnOption1;
    }
    
    private MenuCardButton getBtnOption13() {
        if (btnOption13 == null) {
            btnOption13 = new MenuCardButton("View News Articles", "Browse published articles, view details, and attached images");
            btnOption13.setMnemonic('N');
            btnOption13.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption13.addActionListener(event -> openViewNewsDialog());
        }
        return btnOption13;
    }
    
    private void openViewNewsDialog() {
        try {
            NewsFeedWindow feedWindow = new NewsFeedWindow(this);
            feedWindow.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error opening News Feed: " + ex.getMessage(),
                    "News Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openCreateSlotsDialog() {
        this.setVisible(false);
        CreateInterviewSlotDialog dialog = new CreateInterviewSlotDialog(this, controller);
        dialog.setVisible(true);
        this.setVisible(true);
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel();
            pnFooter.setOpaque(false);
            pnFooter.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(1, 0, 0, 0, Branding.BORDER),
                    new EmptyBorder(8, 0, 10, 0)));
            pnFooter.setLayout(new BorderLayout(0, 0));
            pnFooter.add(getLblFooter(), BorderLayout.CENTER);
        }
        return pnFooter;
    }

    private JLabel getLblFooter() {
        if (lblFooter == null) {
            lblFooter = new JLabel("IPS 2026 · Team PL11");
            lblFooter.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 11));
            lblFooter.setForeground(Branding.TEXT_MUTED);
            lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return lblFooter;
    }
    
    
}