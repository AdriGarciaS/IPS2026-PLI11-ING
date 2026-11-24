package com.ips2026.pl11.view.menu.mainmenus;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JOptionPane;

import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.model.ticket.HomeMatch;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.menu.RoleMenuWindow;
import com.ips2026.pl11.view.news.NewsFeedWindow;
import com.ips2026.pl11.view.schedule.NSEWorkScheduleWindow;
import com.ips2026.pl11.view.seasonpass.PurchaseSeasonPassDialog;
import com.ips2026.pl11.view.ticket.SelectMatchDialog;
import com.ips2026.pl11.view.ticket.SellMatchTicketDialog;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class TicketSellerMenuWindow extends RoleMenuWindow {

    private static final long serialVersionUID = 1L;
    private int employeeId;
    private MenuCardButton btnOption2;
    private MenuCardButton btnOption3;
    private MenuCardButton btnOption13;
    private NSEWorkScheduleWindow nseWorkScheduleWindow = new NSEWorkScheduleWindow(this);
    private MenuCardButton btnOption8;

    public TicketSellerMenuWindow(int employeeId) {
        super("Ticket seller main menu — choose an option to start");
        this.employeeId = employeeId;
        
        setMinimumSize(new Dimension(640, 580));
        setCards(new GridLayout(2, 2, 18, 18), getBtnOption3(), getBtnOption8(), getBtnOption2(), getBtnOption13());
        Branding.setInitialSize(this, 640, 480, null);
    }

    private MenuCardButton getBtnOption3() {
        if (btnOption3 == null) {
            btnOption3 = new MenuCardButton("Ticket Sales", "For upcoming matches");
            btnOption3.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption3.setMnemonic('S');
            btnOption3.addActionListener(event -> openTicketSale());
        }
        return btnOption3;
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
    
    private void openTicketSale() {
        try {
            List<HomeMatch> homeMatches = new MatchDAO().getHomeMatches();
            if (homeMatches.isEmpty()) {
                JOptionPane.showMessageDialog(this, "There are no home matches available for ticket sales.",
                        "Ticket Sale", JOptionPane.WARNING_MESSAGE);
                return;
            }
            SelectMatchDialog selector = new SelectMatchDialog(this, homeMatches);
            selector.setVisible(true);

            HomeMatch selected = selector.getSelectedMatch();
            if (selected != null) {
                new SellMatchTicketDialog(this, selected.getId(), selected.getDisplayName(), employeeId).setVisible(true);
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    "The home matches could not be loaded:" + System.lineSeparator() + exception.getMessage(),
                    "Ticket Sale", JOptionPane.ERROR_MESSAGE);
        }
    }

    
   
    private MenuCardButton getBtnOption8() {
        if (btnOption8 == null) {
        	btnOption8 = new MenuCardButton("Season Pass Holder Site", "Purchase annual season passes for specific teams");
        	btnOption8.setMnemonic('P');
        	btnOption8.setFont(new Font("Tahoma", Font.PLAIN, 16));
        	btnOption8.addActionListener(event -> openSeasonPass());
        }
        return btnOption8;
    }
    
    private void openSeasonPass() {
        try {
            PurchaseSeasonPassDialog dialog = new PurchaseSeasonPassDialog(this);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error opening Season Pass dialog: " + ex.getMessage(),
                    "Season Pass Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}