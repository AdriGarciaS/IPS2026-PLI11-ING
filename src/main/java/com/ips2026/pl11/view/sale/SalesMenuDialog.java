package com.ips2026.pl11.view.sale;

import com.ips2026.pl11.controller.history.SalesHistoryController;
import com.ips2026.pl11.controller.store.StoreSalesController;
import com.ips2026.pl11.controller.ticket.MatchTicketController;
import com.ips2026.pl11.data.history.IncomeDAO;
import com.ips2026.pl11.data.store.MerchandiseDAO;
import com.ips2026.pl11.data.store.MerchandiseSaleDAO;
import com.ips2026.pl11.model.history.SalesHistory;
import com.ips2026.pl11.model.ticket.HomeMatch;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.history.SalesHistoryDialog;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.store.StoreSalesWindow;
import com.ips2026.pl11.view.ticket.SelectMatchDialog;
import com.ips2026.pl11.view.ticket.SellMatchTicketDialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Sales menu: shop sales, match ticket sales and the sales history.
 *
 * <p>It is a modal dialog: the main menu is blocked while it is open. It
 * follows the lazy WindowBuilder style of the main menu.</p>
 */
public class SalesMenuDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnOptions;
    private MenuCardButton btnShopSale;
    private MenuCardButton btnTicketSale;
    private MenuCardButton btnSalesHistory;
    private JPanel pnButtons;
    private JButton btnClose;

    /**
     * @param owner window that opens it (the main menu); it stays blocked
     *              until this window is closed
     */
    public SalesMenuDialog(Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);

        setTitle("Sales");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(420, 460));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 480, 540, owner); // small: it only has three options
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Sales", "Shop and ticket sales, and the sales history");
        }
        return pnHeader;
    }

    /** Everything below the header, with the window margins. */
    private JPanel getPnBody() {
        if (pnBody == null) {
            pnBody = new JPanel();
            pnBody.setOpaque(false);
            pnBody.setBorder(new EmptyBorder(14, 14, 14, 14));
            pnBody.setLayout(new BorderLayout(0, 12));
            pnBody.add(getPnOptions(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnOptions() {
        if (pnOptions == null) {
            pnOptions = new JPanel();
            pnOptions.setBorder(BorderFactory.createCompoundBorder(titled("Options"), new EmptyBorder(6, 6, 6, 6)));
            pnOptions.setLayout(new GridLayout(3, 1, 0, 12));
            pnOptions.add(getBtnShopSale());
            pnOptions.add(getBtnTicketSale());
            pnOptions.add(getBtnSalesHistory());
        }
        return pnOptions;
    }

    private MenuCardButton getBtnShopSale() {
        if (btnShopSale == null) {
            btnShopSale = new MenuCardButton("Shop Sale", "Sell merchandising products from the catalog");
            btnShopSale.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnShopSale.setMnemonic('S');
            btnShopSale.addActionListener(event -> openShopSale());
        }
        return btnShopSale;
    }

    private MenuCardButton getBtnTicketSale() {
        if (btnTicketSale == null) {
            btnTicketSale = new MenuCardButton("Ticket Sale", "Sell match tickets choosing section and seat");
            btnTicketSale.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnTicketSale.setMnemonic('T');
            btnTicketSale.addActionListener(event -> openTicketSale());
        }
        return btnTicketSale;
    }

    private MenuCardButton getBtnSalesHistory() {
        if (btnSalesHistory == null) {
            btnSalesHistory = new MenuCardButton("Sales History", "Income of the club between two dates");
            btnSalesHistory.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnSalesHistory.setMnemonic('H');
            btnSalesHistory.addActionListener(event -> openSalesHistory());
        }
        return btnSalesHistory;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnClose());
        }
        return pnButtons;
    }

    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton("Close");
            btnClose.setMnemonic('C');
            btnClose.addActionListener(event -> dispose());
        }
        return btnClose;
    }

    private static TitledBorder titled(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        return border;
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

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
        new StoreSalesWindow(controller, this).setVisible(true);
    }

    private void openTicketSale() {
        try {
            List<HomeMatch> homeMatches = new MatchTicketController().getHomeMatches();
            if (homeMatches.isEmpty()) {
                JOptionPane.showMessageDialog(this, "There are no home matches available for ticket sales.",
                        "Ticket Sale", JOptionPane.WARNING_MESSAGE);
                return;
            }
            SelectMatchDialog selector = new SelectMatchDialog(this, homeMatches);
            selector.setVisible(true);

            HomeMatch selected = selector.getSelectedMatch();
            if (selected != null) {
                new SellMatchTicketDialog(this, selected.getId(), selected.getDisplayName()).setVisible(true);
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this,
                    "The home matches could not be loaded:" + System.lineSeparator() + exception.getMessage(),
                    "Ticket Sale", JOptionPane.ERROR_MESSAGE);
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
}
