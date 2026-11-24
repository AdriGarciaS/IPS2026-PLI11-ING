package com.ips2026.pl11.view.menu.mainmenus;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JOptionPane;

import com.ips2026.pl11.controller.slots.InterviewSlotsController;
import com.ips2026.pl11.data.slots.InterviewSlotDAO;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.menu.RoleMenuWindow;
import com.ips2026.pl11.view.news.NewsFeedWindow;
import com.ips2026.pl11.view.slots.CreateInterviewSlotDialog;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class CoachMenuWindow extends RoleMenuWindow {

    private static final long serialVersionUID = 1L;
    private MenuCardButton btnOption1;
    private MenuCardButton btnOption13;
    InterviewSlotsController controller = new InterviewSlotsController(new InterviewSlotDAO());

    public CoachMenuWindow() {
        super("Coach main menu — choose an option to start");
        setCards(new GridLayout(2, 1, 18, 18), getBtnOption1(), getBtnOption13());
        Branding.setInitialSize(this, 640, 480, null);
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

    
    
}