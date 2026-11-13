package com.ips2026.pl11.view.menu.mainmenus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.controller.history.SalesHistoryController;
import com.ips2026.pl11.controller.team.TeamCreationController;
import com.ips2026.pl11.data.history.IncomeDAO;
import com.ips2026.pl11.data.team.SportsEmployeeDAO;
import com.ips2026.pl11.data.team.TeamDAO;
import com.ips2026.pl11.model.history.SalesHistory;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.employee.EmployeeMenuDialog;
import com.ips2026.pl11.view.history.SalesHistoryDialog;
import com.ips2026.pl11.view.matches.ScheduleMatchDialog;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.news.NewsFeedWindow;
import com.ips2026.pl11.view.schedule.GMWorkScheduleWindow;
import com.ips2026.pl11.view.schedule.NSEWorkScheduleWindow;
import com.ips2026.pl11.view.team.TeamCreationWindow;
import com.ips2026.pl11.view.training.TrainingSessionWindow;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class GeneralManagerMenuWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnButtons;
    private MenuCardButton btnOption1;
    private MenuCardButton btnOption2;
    private MenuCardButton btnOption3;
    private MenuCardButton btnOption6;
    private MenuCardButton btnOption10;
    private MenuCardButton btnOption12;
    private MenuCardButton btnOption13;    
    private MenuCardButton btnOption4;

    private JPanel pnFooter;
    private JLabel lblFooter;
    private JButton selectedButton;

    private GMWorkScheduleWindow gmWorkScheduleWindow = new GMWorkScheduleWindow(this);
    private NSEWorkScheduleWindow nseWorkScheduleWindow = new NSEWorkScheduleWindow(this);

    public GeneralManagerMenuWindow() {
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
        Branding.setInitialSize(this, 840, 780, null);
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Football Club Management",
                    "General manager main menu — choose an option to start", true);
        }
        return pnHeader;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setBorder(new EmptyBorder(24, 24, 16, 24));
            pnButtons.setLayout(new GridLayout(3, 3, 18, 18));
            pnButtons.add(getBtnOption1());
            pnButtons.add(getBtnOption2());
            pnButtons.add(getBtnOption3());
            pnButtons.add(getBtnOption6());
            pnButtons.add(getBtnOption10());
            pnButtons.add(getBtnOption12());
            pnButtons.add(getBtnOption13());
            pnButtons.add(getBtnOption4());
        }
        return pnButtons;
    }

    private MenuCardButton getBtnOption1() {
        if (btnOption1 == null) {
            btnOption1 = new MenuCardButton("Employees Management", "Register, modify and delete sports and non-sports staff");
            btnOption1.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption1.setMnemonic('E');

            selectedButton = btnOption1;
            btnOption1.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedButton != btnOption1) {
                        btnOption1.setBackground(new Color(241, 245, 249));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedButton != btnOption1) {
                        btnOption1.setBackground(Color.WHITE);
                    }
                }
            });

            btnOption1.addActionListener(e -> new EmployeeMenuDialog(this).setVisible(true));
        }
        return btnOption1;
    }

    private MenuCardButton getBtnOption2() {
        if (btnOption2 == null) {
            btnOption2 = new MenuCardButton("Add Recurring Schedules", "For non-sporting employees");
            btnOption2.addActionListener(e -> {
                
                gmWorkScheduleWindow.refreshShiftsTable();
                gmWorkScheduleWindow.refreshWorkersTable();
                gmWorkScheduleWindow.setVisible(true);
            });
            btnOption2.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption2.setEnabled(true);
        }
        return btnOption2;
    }

    private MenuCardButton getBtnOption3() {
        if (btnOption3 == null) {
            btnOption3 = new MenuCardButton("Sales History", "Income of the club between two dates");
            btnOption3.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption3.setMnemonic('S');
            btnOption3.addActionListener(event -> openSalesHistory());
        }
        return btnOption3;
    }

    private MenuCardButton getBtnOption6() {
        if (btnOption6 == null) {
            btnOption6 = new MenuCardButton("Sports Teams", "Add sports teams to manage them");
            btnOption6.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption6.addActionListener(event -> openTeamCreation());
        }
        return btnOption6;
    }
    
    private MenuCardButton getBtnOption10() {
        if (btnOption10== null) {
            btnOption10 = new MenuCardButton("Schedule Home Match", "Schedule local matches, dynamic pricing and auto-cancel conflicts");
            btnOption10.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption10.setMnemonic('M');
            btnOption10.addActionListener(event -> openScheduleMatch());
        }
        return btnOption10;
    }
    
    private MenuCardButton getBtnOption12() {
        if (btnOption12 == null) {
            btnOption12 = new MenuCardButton("Training Sessions", "Schedule and manage training sessions for the team");
            btnOption12.setMnemonic('T');
            btnOption12.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption12.addActionListener(event -> openTrainingSession());
        }
        return btnOption12;
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
    
    private MenuCardButton getBtnOption4() {
        if (btnOption4 == null) {
            btnOption4 = new MenuCardButton("Consult Schedule ", "View your work schedule for any given week");
            btnOption4.addActionListener(e -> nseWorkScheduleWindow.setVisible(true));
            btnOption4.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption4.setEnabled(true);
        }
        return btnOption4;
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
    
    /** Opens the history with the income of the last 30 days. */
    private void openSalesHistory() {
        SalesHistoryController controller = new SalesHistoryController(new IncomeDAO());
        SalesHistory history;
        try {
            history = controller.searchDefault();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The sales could not be loaded:" + System.lineSeparator() + exception.getMessage(),
                    "Sales History", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new SalesHistoryDialog(controller, history, this).setVisible(true);
    }
      
    private Object openTrainingSession() {
        try {
            TrainingSessionWindow dialog = new TrainingSessionWindow(this);
            dialog.initialize();
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error opening Gardening Session dialog: " + ex.getMessage(),
                    "Gardening Session Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void openTeamCreation() {
        TeamCreationController controller = new TeamCreationController(new SportsEmployeeDAO(), new TeamDAO());
        try {
            controller.load();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The sports employees could not be loaded:" + System.lineSeparator() + exception.getMessage(),
                    "Sports Teams", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new TeamCreationWindow(controller, this).setVisible(true);
    }

    
    public void openScheduleMatch() {
        try {
            ScheduleMatchDialog dialog = new ScheduleMatchDialog(this);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error opening Schedule Match dialog: " + ex.getMessage(),
                    "Schedule Error", JOptionPane.ERROR_MESSAGE);
        }
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