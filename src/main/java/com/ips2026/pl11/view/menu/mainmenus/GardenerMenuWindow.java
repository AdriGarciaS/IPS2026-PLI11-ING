package com.ips2026.pl11.view.menu.mainmenus;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JOptionPane;

import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.gardening.gardener.ViewGardeningSessions;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.menu.RoleMenuWindow;
import com.ips2026.pl11.view.news.NewsFeedWindow;
import com.ips2026.pl11.view.schedule.NSEWorkScheduleWindow;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class GardenerMenuWindow extends RoleMenuWindow {

    private static final long serialVersionUID = 1L;
    private MenuCardButton btnOption1;
    private MenuCardButton btnOption2;
    private MenuCardButton btnOption13;
    private NSEWorkScheduleWindow nseWorkScheduleWindow = new NSEWorkScheduleWindow(this);
    private ViewGardeningSessions viewGardeningSessions = new ViewGardeningSessions(this);
    

    public GardenerMenuWindow() {
        super("Gardener main menu — choose an option to start");
        setCards(new GridLayout(0, 1, 0, 0), getBtnOption2(), getBtnOption1(), getBtnOption13());
        Branding.setInitialSize(this, 640, 480, null);
    }

    private MenuCardButton getBtnOption1() {
        if (btnOption1 == null) {
            btnOption1 = new MenuCardButton("Assigned Sessions", "View your assigned gardening sessions");
            btnOption1.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption1.setMnemonic('G');
            btnOption1.addActionListener(event -> openGardeningSession());
        }
        return btnOption1;
    }
    
    private MenuCardButton getBtnOption2() {
        if (btnOption2 == null) {
            btnOption2 = new MenuCardButton("Consult Schedule ", "View your work schedule for any given week");
            btnOption2.addActionListener(e -> nseWorkScheduleWindow.setVisible(true));
            btnOption2.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption2.setEnabled(true);
        }
        return btnOption2;
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

    private Object openGardeningSession() {
        try {
            viewGardeningSessions.loadGardeners();
            viewGardeningSessions.refreshCalendar();
            viewGardeningSessions.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error opening Gardening Session dialog: " + ex.getMessage(),
                    "Gardening Session Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    
  
    
    
}