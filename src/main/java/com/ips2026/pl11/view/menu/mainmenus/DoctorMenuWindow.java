package com.ips2026.pl11.view.menu.mainmenus;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JOptionPane;

import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.doctor.RegisterInjuryWindow;
import com.ips2026.pl11.view.doctor.UpdateInjuryWindow;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.menu.RoleMenuWindow;
import com.ips2026.pl11.view.news.NewsFeedWindow;
import com.ips2026.pl11.view.schedule.NSEWorkScheduleWindow;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class DoctorMenuWindow extends RoleMenuWindow {

    private static final long serialVersionUID = 1L;
    private int doctorId;
    private MenuCardButton btnOption1;
    private MenuCardButton btnOption2;
    private MenuCardButton btnOption3;
    private MenuCardButton btnOption13;

    private NSEWorkScheduleWindow nseWorkScheduleWindow = new NSEWorkScheduleWindow(this);
    private RegisterInjuryWindow registerInjuryWindow = new RegisterInjuryWindow(this, doctorId);
    private UpdateInjuryWindow updateInjuryWindow = new UpdateInjuryWindow(this, doctorId);
    
    public DoctorMenuWindow(int doctorId) {
        super("Doctor main menu — choose an option to start");
        this.doctorId = doctorId;
        setCards(new GridLayout(2, 2, 18, 18), getBtnOption1(), getBtnOption2(), getBtnOption3(), getBtnOption13());
        Branding.setInitialSize(this, 640, 480, null);
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
            btnOption13 = new MenuCardButton("View News Articles", "Browse published articles, view details, and attached images");
            btnOption13.setMnemonic('N');
            btnOption13.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption13.addActionListener(event -> openViewNewsDialog());
        }
        return btnOption13;
    }
    
    private MenuCardButton getBtnOption2() {
        if (btnOption2 == null) {
            btnOption2 = new MenuCardButton("Register Injury", "For a given player");
            btnOption2.addActionListener(e -> {
                registerInjuryWindow.loadTeams(); 
                registerInjuryWindow.setVisible(true);
                
            });
            btnOption2.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption2.setEnabled(true);
        }
        return btnOption2;
    }
    
    private MenuCardButton getBtnOption3() {
        if (btnOption3 == null) {
            btnOption3 = new MenuCardButton("Update Injury", "For a given player");
            btnOption3.addActionListener(e -> {
                updateInjuryWindow.loadInjuries(); 
                updateInjuryWindow.setVisible(true);
                
            });
            btnOption3.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption3.setEnabled(true);
        }
        return btnOption3;
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
  

    
    
    
    
}