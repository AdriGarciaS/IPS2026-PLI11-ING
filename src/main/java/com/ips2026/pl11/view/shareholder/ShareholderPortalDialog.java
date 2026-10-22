package com.ips2026.pl11.view.shareholder;

import com.ips2026.pl11.controller.shareholder.ShareholderPortalController;
import com.ips2026.pl11.model.shareholder.Shareholder;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MenuCardButton;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Shareholders portal: the shareholder identifies with the national ID (or
 * is searched by name) and then chooses one of the shareholder options.
 * New shareholder options are added as cards in "2. Options".
 *
 * <p>It is a modal dialog: the main menu is blocked while it is open. It
 * follows the lazy WindowBuilder style of the main menu.</p>
 */
public class ShareholderPortalDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String COMING_SOON = "Coming soon";

    private final transient ShareholderPortalController controller;
    private final ShareholdersTableModel resultsModel = new ShareholdersTableModel();

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnIdentify;
    private JPanel pnSearch;
    private JLabel lblSearch;
    private JTextField txtSearch;
    private JScrollPane scrResults;
    private JTable tblResults;
    private JLabel lblSelected;
    private JPanel pnSouth;
    private JPanel pnOptions;
    private MenuCardButton btnWallet;
    private MenuCardButton btnOption2;
    private MenuCardButton btnOption3;
    private JPanel pnButtons;
    private JButton btnClose;

    /**
     * @param controller controller with the shareholders already loaded
     * @param owner      window that opens it (the main menu); it stays
     *                   blocked until this window is closed
     */
    public ShareholderPortalDialog(ShareholderPortalController controller, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setTitle("Shareholders Portal");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(800, 560));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);

        showSelected();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Shareholders Portal",
                    "Identify yourself with your national ID, or search your name, and choose an option");
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
            pnBody.add(getPnIdentify(), BorderLayout.CENTER);
            pnBody.add(getPnSouth(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnIdentify() {
        if (pnIdentify == null) {
            pnIdentify = new JPanel();
            pnIdentify.setBorder(titled("1. Shareholder"));
            pnIdentify.setLayout(new BorderLayout(0, 8));
            pnIdentify.add(getPnSearch(), BorderLayout.NORTH);
            pnIdentify.add(getScrResults(), BorderLayout.CENTER);
            pnIdentify.add(getLblSelected(), BorderLayout.SOUTH);
        }
        return pnIdentify;
    }

    private JPanel getPnSearch() {
        if (pnSearch == null) {
            pnSearch = new JPanel();
            pnSearch.setOpaque(false);
            pnSearch.setLayout(new BorderLayout(8, 0));
            pnSearch.add(getLblSearch(), BorderLayout.WEST);
            pnSearch.add(getTxtSearch(), BorderLayout.CENTER);
        }
        return pnSearch;
    }

    private JLabel getLblSearch() {
        if (lblSearch == null) {
            lblSearch = new JLabel("National ID or name:");
            lblSearch.setDisplayedMnemonic('I');
            lblSearch.setLabelFor(getTxtSearch());
        }
        return lblSearch;
    }

    private JTextField getTxtSearch() {
        if (txtSearch == null) {
            txtSearch = new JTextField();
            txtSearch.setToolTipText("Type your national ID, or part of your name (not case or accent sensitive)");
            txtSearch.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    searchChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    searchChanged();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    searchChanged();
                }
            });
        }
        return txtSearch;
    }

    private JScrollPane getScrResults() {
        if (scrResults == null) {
            scrResults = new JScrollPane();
            scrResults.setViewportView(getTblResults());
            scrResults.setPreferredSize(new Dimension(500, 150));
        }
        return scrResults;
    }

    private JTable getTblResults() {
        if (tblResults == null) {
            tblResults = new JTable(resultsModel);
            tblResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblResults.setAutoCreateRowSorter(true);
            tblResults.setFillsViewportHeight(true);
            tblResults.setRowHeight(24);
            tblResults.getTableHeader().setReorderingAllowed(false);
            tblResults.getSelectionModel().addListSelectionListener(event -> {
                int viewRow = tblResults.getSelectedRow();
                if (!event.getValueIsAdjusting() && viewRow >= 0) {
                    controller.select(resultsModel.getShareholderAt(tblResults.convertRowIndexToModel(viewRow)));
                    showSelected();
                }
            });
        }
        return tblResults;
    }

    private JLabel getLblSelected() {
        if (lblSelected == null) {
            lblSelected = new JLabel();
            lblSelected.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
        }
        return lblSelected;
    }

    /** Options and, below them, the window buttons. */
    private JPanel getPnSouth() {
        if (pnSouth == null) {
            pnSouth = new JPanel();
            pnSouth.setOpaque(false);
            pnSouth.setLayout(new BorderLayout(0, 12));
            pnSouth.add(getPnOptions(), BorderLayout.CENTER);
            pnSouth.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnSouth;
    }

    private JPanel getPnOptions() {
        if (pnOptions == null) {
            pnOptions = new JPanel();
            pnOptions.setBorder(BorderFactory.createCompoundBorder(titled("2. Options"), new EmptyBorder(6, 6, 6, 6)));
            pnOptions.setLayout(new GridLayout(1, 3, 14, 0));
            pnOptions.setPreferredSize(new Dimension(500, 170));
            pnOptions.add(getBtnWallet());
            pnOptions.add(getBtnOption2());
            pnOptions.add(getBtnOption3());
        }
        return pnOptions;
    }

    /** The shares wallet: it will be connected in the next step of the user story. */
    private MenuCardButton getBtnWallet() {
        if (btnWallet == null) {
            btnWallet = new MenuCardButton("My Shares Wallet", COMING_SOON);
            btnWallet.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnWallet.setEnabled(false);
        }
        return btnWallet;
    }

    private MenuCardButton getBtnOption2() {
        if (btnOption2 == null) {
            btnOption2 = new MenuCardButton("Option 2", COMING_SOON);
            btnOption2.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption2.setEnabled(false);
        }
        return btnOption2;
    }

    private MenuCardButton getBtnOption3() {
        if (btnOption3 == null) {
            btnOption3 = new MenuCardButton("Option 3", COMING_SOON);
            btnOption3.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption3.setEnabled(false);
        }
        return btnOption3;
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

    /** Shows the shareholders that match; an exact national ID is chosen directly. */
    private void searchChanged() {
        String text = getTxtSearch().getText();
        List<Shareholder> found = controller.search(text);
        resultsModel.setShareholders(found);

        Optional<Shareholder> exact = controller.findByNationalId(text);
        if (exact.isPresent()) {
            int viewRow = getTblResults().convertRowIndexToView(found.indexOf(exact.get()));
            getTblResults().setRowSelectionInterval(viewRow, viewRow);
        } else if (found.isEmpty()) {
            controller.select(null);
            showSelected();
        }
    }

    private void showSelected() {
        Shareholder selected = controller.getSelected();
        if (selected == null) {
            getLblSelected().setForeground(Branding.TEXT_MUTED);
            getLblSelected().setText("No shareholder chosen: type your national ID or search your name.");
        } else {
            getLblSelected().setForeground(Branding.NAVY);
            getLblSelected().setText("Shareholder: " + selected);
        }
    }
}
