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

import com.ips2026.pl11.controller.campaign.BuyCampaignSharesController;
import com.ips2026.pl11.controller.market.SellSharesController;
import com.ips2026.pl11.controller.market.ShareMarketController;
import com.ips2026.pl11.controller.shareholder.SharesWalletController;
import com.ips2026.pl11.data.campaign.CampaignDAO;
import com.ips2026.pl11.data.campaign.CampaignPurchaseDAO;
import com.ips2026.pl11.data.market.ListingDAO;
import com.ips2026.pl11.data.market.MarketDAO;
import com.ips2026.pl11.data.shareholder.MarketPriceDAO;
import com.ips2026.pl11.data.shareholder.ShareHoldingDAO;
import com.ips2026.pl11.model.shareholder.Shareholder;
import com.ips2026.pl11.model.shareholder.SharesWallet;
import com.ips2026.pl11.view.campaign.BuyCampaignSharesDialog;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.market.SellSharesDialog;
import com.ips2026.pl11.view.market.ShareMarketDialog;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.news.NewsFeedWindow;
import com.ips2026.pl11.view.shareholder.SharesWalletDialog;

/**
 * Main menu of a shareholder who has logged in. Every shareholder option
 * works with the shareholder of the session (there is no need to search
 * them, as the old Shareholders Portal did).
 */
public class ShareholderMenuWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private final transient Shareholder shareholder;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnButtons;
    private JPanel pnFooter;
    private JLabel lblFooter;

    private MenuCardButton btnNews;
    private MenuCardButton btnWallet;
    private MenuCardButton btnSellShares;
    private MenuCardButton btnShareMarket;
    private MenuCardButton btnBuyCampaignShares;

    /** @param shareholder the shareholder who has logged in */
    public ShareholderMenuWindow(Shareholder shareholder) {
        this.shareholder = shareholder;
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
        Branding.setInitialSize(this, 1024, 720, null);
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Football Club Management",
                    shareholder.getFullName() + " — shareholder main menu", true);
        }
        return pnHeader;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setBorder(new EmptyBorder(24, 24, 16, 24));
            pnButtons.setLayout(new GridLayout(0, 2, 18, 18));
            pnButtons.add(getBtnNews());
            pnButtons.add(getBtnWallet());
            pnButtons.add(getBtnSellShares());
            pnButtons.add(getBtnShareMarket());
            pnButtons.add(getBtnBuyCampaignShares());
        }
        return pnButtons;
    }

    private MenuCardButton getBtnNews() {
        if (btnNews == null) {
            btnNews = new MenuCardButton("View News Articles", "Browse published articles, view details, and attached images");
            btnNews.setMnemonic('N');
            btnNews.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnNews.addActionListener(event -> openViewNewsDialog());
        }
        return btnNews;
    }

    private MenuCardButton getBtnWallet() {
        if (btnWallet == null) {
            btnWallet = new MenuCardButton("My Shares Wallet", "Portfolio value and details of my shares");
            btnWallet.setMnemonic('W');
            btnWallet.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnWallet.addActionListener(event -> openWallet());
        }
        return btnWallet;
    }

    private MenuCardButton getBtnSellShares() {
        if (btnSellShares == null) {
            btnSellShares = new MenuCardButton("Sell Shares", "Put shares from my wallet up for sale");
            btnSellShares.setMnemonic('S');
            btnSellShares.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnSellShares.addActionListener(event -> openSellShares());
        }
        return btnSellShares;
    }

    private MenuCardButton getBtnShareMarket() {
        if (btnShareMarket == null) {
            btnShareMarket = new MenuCardButton("Share Market", "Buy shares sold by other shareholders");
            btnShareMarket.setMnemonic('M');
            btnShareMarket.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnShareMarket.addActionListener(event -> openMarket());
        }
        return btnShareMarket;
    }

    private MenuCardButton getBtnBuyCampaignShares() {
        if (btnBuyCampaignShares == null) {
            btnBuyCampaignShares = new MenuCardButton("Buy Campaign Shares", "Buy shares in the active campaign");
            btnBuyCampaignShares.setMnemonic('B');
            btnBuyCampaignShares.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnBuyCampaignShares.addActionListener(event -> openBuyCampaignShares());
        }
        return btnBuyCampaignShares;
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

    private void openViewNewsDialog() {
        try {
            new NewsFeedWindow(this).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error opening News Feed: " + ex.getMessage(),
                    "News Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openWallet() {
        SharesWalletController walletController = new SharesWalletController(new ShareHoldingDAO(), new MarketPriceDAO());
        SharesWallet wallet;
        try {
            wallet = walletController.loadWallet(shareholder);
        } catch (SQLException | IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, "The shares wallet could not be loaded:\n" + exception.getMessage(),
                    "Shares Wallet", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new SharesWalletDialog(wallet, this).setVisible(true);
    }

    /** Sell Shares: shares of the shareholder's wallet put up for sale. */
    private void openSellShares() {
        SellSharesController sellController = new SellSharesController(new ListingDAO(), shareholder);
        try {
            sellController.load();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "Your shares could not be loaded:" + System.lineSeparator()
                    + exception.getMessage(), "Sell Shares", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (sellController.getShares(null).isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no shares to sell.", "Sell Shares", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new SellSharesDialog(sellController, this).setVisible(true);
    }

    /** The share market (the shareholder's own sales are not listed). */
    private void openMarket() {
        ShareMarketController marketController = new ShareMarketController(new MarketDAO(), shareholder);
        try {
            marketController.load();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "The share market could not be loaded:\n" + exception.getMessage(),
                    "Share Market", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new ShareMarketDialog(marketController, this).setVisible(true);
    }

    /** US 35947: buy shares of the active campaign (only while it is in phase 1, 2 or 3). */
    private void openBuyCampaignShares() {
        BuyCampaignSharesController buyController =
                new BuyCampaignSharesController(new CampaignDAO(), new CampaignPurchaseDAO(), shareholder);
        try {
            if (!buyController.load()) {
                JOptionPane.showMessageDialog(this, "There is no campaign open for purchases right now.",
                        "Buy Campaign Shares", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "The active campaign could not be loaded:\n" + exception.getMessage(),
                    "Buy Campaign Shares", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new BuyCampaignSharesDialog(buyController, this).setVisible(true);
    }
}
