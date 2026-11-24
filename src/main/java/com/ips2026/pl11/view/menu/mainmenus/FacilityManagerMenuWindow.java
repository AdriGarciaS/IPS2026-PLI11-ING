package com.ips2026.pl11.view.menu.mainmenus;

import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.ips2026.pl11.controller.reservation.FacilityReservationController;
import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.view.campaign.CampaignsDialog;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.gardening.manager.GardeningScheduleWindow;
import com.ips2026.pl11.view.matches.ScheduleMatchDialog;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.menu.RoleMenuWindow;
import com.ips2026.pl11.view.news.NewsFeedWindow;
import com.ips2026.pl11.view.reservation.FacilityReservationWindow;
import com.ips2026.pl11.view.schedule.DayScheduleWindow;
import com.ips2026.pl11.view.schedule.NSEWorkScheduleWindow;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class FacilityManagerMenuWindow extends RoleMenuWindow {

    private static final long serialVersionUID = 1L;
    private MenuCardButton btnOption1;
    private MenuCardButton btnOption2;
    private MenuCardButton btnOption9;
    private MenuCardButton btnOption10;
    private MenuCardButton btnOption11;
    private MenuCardButton btnOption13;
    private MenuCardButton btnOption4;

    private GardeningScheduleWindow scheduleGardeningSession = new GardeningScheduleWindow(this);
    private DayScheduleWindow dayScheduleWindow = new DayScheduleWindow(this);
    private NSEWorkScheduleWindow nseWorkScheduleWindow = new NSEWorkScheduleWindow(this);

    public FacilityManagerMenuWindow() {
        super("Manager main menu — choose an option to start");
        setCards(new GridLayout(3, 2, 18, 18), getBtnOption2(), getBtnOption1(), getBtnOption11(), getBtnOption10(),
            getBtnOption9(), getBtnOption13(), getBtnOption4());
        Branding.setInitialSize(this, 640, 580, null);
    }

    private MenuCardButton getBtnOption2() {
        if (btnOption2 == null) {
            btnOption2 = new MenuCardButton("Add Specific-day Schedules", "For non sporting employees");
            btnOption2.addActionListener(e -> openScheduleManagement());
            btnOption2.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption2.setEnabled(true);
        }
        return btnOption2;
    }

    private MenuCardButton getBtnOption1() {
        if (btnOption1 == null) {
            btnOption1 = new MenuCardButton("Consult Schedule ", "View your work schedule for any given week");
            btnOption1.addActionListener(e -> nseWorkScheduleWindow.setVisible(true));
            btnOption1.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption1.setEnabled(true);
        }
        return btnOption1;
    }

    private MenuCardButton getBtnOption13() {
        if (btnOption13 == null) {
            btnOption13 = new MenuCardButton("View News Articles",
                "Browse published articles, view details, and attached images");
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
            JOptionPane.showMessageDialog(this, "Error opening News Feed: " + ex.getMessage(), "News Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object openScheduleManagement() {
        try {
            dayScheduleWindow.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error opening Schedule Management dialog: " + ex.getMessage(),
                "Schedule Management Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private MenuCardButton getBtnOption9() {
        if (btnOption9 == null) {
            btnOption9 = new MenuCardButton("Shareholder Campaigns", "Create and manage share capital campaigns");
            btnOption9.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption9.setMnemonic('H');
            btnOption9.addActionListener(event -> new CampaignsDialog(this).setVisible(true));
        }
        return btnOption9;
    }

    private MenuCardButton getBtnOption10() {
        if (btnOption10 == null) {
            btnOption10 = new MenuCardButton("Schedule Home Match",
                "Schedule local matches, dynamic pricing and auto-cancel conflicts");
            btnOption10.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption10.setMnemonic('M');
            btnOption10.addActionListener(event -> openScheduleMatch());
        }
        return btnOption10;
    }

    private MenuCardButton getBtnOption11() {
        if (btnOption11 == null) {
            btnOption11 = new MenuCardButton("Schedule Gardening Sessions", "For the club's facilities");
            btnOption11.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption11.setMnemonic('G');
            btnOption11.addActionListener(event -> openGardeningSession());
        }
        return btnOption11;
    }

    private Object openGardeningSession() {
        try {
            scheduleGardeningSession.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error opening Gardening Session dialog: " + ex.getMessage(),
                "Gardening Session Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public void openScheduleMatch() {
        try {
            ScheduleMatchDialog dialog = new ScheduleMatchDialog(this);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error opening Schedule Match dialog: " + ex.getMessage(),
                "Schedule Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private MenuCardButton getBtnOption4() {
        if (btnOption4 == null) {
            btnOption4 = new MenuCardButton("Facility Reservations", "Book club facilities for external people");
            btnOption4.setMnemonic('F');
            btnOption4.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption4.addActionListener(event -> openFacilityReservations());
        }
        return btnOption4;
    }

    private void openFacilityReservations() {
        FacilityReservationController controller = new FacilityReservationController(new FacilityDAO(),
            new TeamUseDAO(), new ReservationDAO());
        try {
            controller.loadFacilities();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "The facilities could not be loaded:\n" + exception.getMessage(),
                "Facility Reservations", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new FacilityReservationWindow(controller, this).setVisible(true);
    }

}