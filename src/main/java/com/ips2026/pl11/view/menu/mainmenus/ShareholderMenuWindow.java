package com.ips2026.pl11.view.menu.mainmenus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.controller.shareholder.ShareholderPortalController;
import com.ips2026.pl11.data.shareholder.ShareholderDAO;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.news.NewsFeedWindow;
import com.ips2026.pl11.view.shareholder.ShareholderPortalDialog;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class ShareholderMenuWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnButtons;
    private JPanel pnFooter;
    private JLabel lblFooter;
    
    private MenuCardButton btnOption13;
    private MenuCardButton btnOption7;


    public ShareholderMenuWindow() {
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
                    "Shareholder main menu — choose an option to start", true);
        }
        return pnHeader;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setBorder(new EmptyBorder(24, 24, 16, 24));
            pnButtons.setLayout(new GridLayout(2, 1, 18, 18));
            pnButtons.add(getBtnOption13());
            pnButtons.add(getBtnOption7());

        }
        return pnButtons;
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
    
    private MenuCardButton getBtnOption7() {
        if (btnOption7 == null) {
        	btnOption7 = new MenuCardButton("Shareholders Portal", "Shares wallet and other options for shareholders");
        	btnOption7.setFont(new Font("Tahoma", Font.PLAIN, 16));
        	btnOption7.addActionListener(event -> openShareholderPortal());
        }
        return btnOption7;
    }

    private void openShareholderPortal() {
        ShareholderPortalController controller = new ShareholderPortalController(new ShareholderDAO());
        try {
            controller.load();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The shareholders could not be loaded:" + System.lineSeparator() + exception.getMessage(),
                    "Shareholders Portal", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new ShareholderPortalDialog(controller, this).setVisible(true);
    }
}