package com.ips2026.pl11.view.menu.mainmenus;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.ips2026.pl11.controller.store.StoreSalesController;
import com.ips2026.pl11.data.store.MerchandiseDAO;
import com.ips2026.pl11.data.store.MerchandiseSaleDAO;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.menu.RoleMenuWindow;
import com.ips2026.pl11.view.news.NewsFeedWindow;
import com.ips2026.pl11.view.schedule.NSEWorkScheduleWindow;
import com.ips2026.pl11.view.store.StoreSalesWindow;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class StoreEmployeeMenuWindow extends RoleMenuWindow {

    private static final long serialVersionUID = 1L;
    private int employeeId;
    private MenuCardButton btnOption2;
    private MenuCardButton btnOption3;
    private MenuCardButton btnOption13;
    private NSEWorkScheduleWindow nseWorkScheduleWindow = new NSEWorkScheduleWindow(this);

    public StoreEmployeeMenuWindow(int employeeId) {
        super("Store employee main menu — choose an option to start");
        this.employeeId = employeeId;
        
        setMinimumSize(new Dimension(640, 580));
        setCards(new GridLayout(3, 1, 18, 18), getBtnOption3(), getBtnOption2(), getBtnOption13());
        Branding.setInitialSize(this, 640, 480, null);
    }

    private MenuCardButton getBtnOption3() {
        if (btnOption3 == null) {
            btnOption3 = new MenuCardButton("Merchandising Store", "Manage merchandise sales, view inventory, and generate sales reports");
            btnOption3.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption3.setMnemonic('S');
            btnOption3.addActionListener(event -> openShopSale());
        }
        return btnOption3;
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
    
    private MenuCardButton getBtnOption2() {
        if (btnOption2 == null) {
            btnOption2 = new MenuCardButton("Consult Schedule ", "View your work schedule for any given week");
            btnOption2.addActionListener(e -> nseWorkScheduleWindow.setVisible(true));
            btnOption2.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption2.setEnabled(true);
        }
        return btnOption2;
    }
    
    private void openShopSale() {
        StoreSalesController controller = new StoreSalesController(new MerchandiseDAO(), new MerchandiseSaleDAO());
        try {
            controller.loadCatalog();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The catalog could not be loaded:" + System.lineSeparator() + exception.getMessage(),
                    "Shop Sale", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new StoreSalesWindow(controller, this, employeeId).setVisible(true);
    }

    
   
}