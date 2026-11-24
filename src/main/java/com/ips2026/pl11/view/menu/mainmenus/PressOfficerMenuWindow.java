package com.ips2026.pl11.view.menu.mainmenus;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JOptionPane;

import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.menu.RoleMenuWindow;
import com.ips2026.pl11.view.news.NewsMenuDialog;
import com.ips2026.pl11.view.schedule.NSEWorkScheduleWindow;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class PressOfficerMenuWindow extends RoleMenuWindow {

    private static final long serialVersionUID = 1L;
    private MenuCardButton btnOption1;
    private MenuCardButton btnOption13;
    
    private NSEWorkScheduleWindow nseWorkScheduleWindow = new NSEWorkScheduleWindow(this);

    public PressOfficerMenuWindow() {
        super("Press officer main menu — choose an option to start");
        setCards(new GridLayout(2, 1, 18, 18), getBtnOption1(), getBtnOption13());
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
            btnOption13 = new MenuCardButton("Publish News", "Draft and publish news articles with headline, text and images");
            btnOption13.setMnemonic('N');
            btnOption13.setFont(new Font("Tahoma", Font.PLAIN, 16));
            btnOption13.addActionListener(event -> openNewsMenu());
        }
        return btnOption13;
    }
    
    private void openNewsMenu() {
        try {
            NewsMenuDialog dialog = new NewsMenuDialog(this);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,"Error opening News Menu: " + ex.getMessage(),"News Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    
    
    
}