package com.ips2026.pl11.view.shareholder;

import com.ips2026.pl11.model.shareholder.ShareHolding;
import com.ips2026.pl11.model.shareholder.SharesWallet;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Shares wallet of a shareholder: at the top, a box with the total portfolio
 * value (green if it went up since the purchases, red if it went down) and
 * the details; below, every share holding with its current value.
 *
 * <p>It is a modal dialog owned by the shareholders portal. It follows the
 * lazy WindowBuilder style of the main menu.</p>
 */
public class SharesWalletDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Locale LOCALE_ES = Locale.of("es", "ES");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient SharesWallet wallet;
    private final HoldingsTableModel holdingsModel = new HoldingsTableModel();

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnSummary;
    private JPanel pnTotal;
    private JLabel lblTotalTitle;
    private JLabel lblTotal;
    private JLabel lblTotalVariation;
    private JPanel pnDetails;
    private JPanel pnHoldings;
    private JScrollPane scrHoldings;
    private JTable tblHoldings;
    private JLabel lblNoShares;
    private JPanel pnButtons;
    private JButton btnClose;

    /**
     * @param wallet wallet to show
     * @param owner  window that opens it (the shareholders portal)
     */
    public SharesWalletDialog(SharesWallet wallet, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.wallet = wallet;
        holdingsModel.setHoldings(wallet.getHoldings(), wallet.getCurrentPrice().getPrice());

        setTitle("Shares Wallet");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(950, 600));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Shares Wallet — " + wallet.getShareholder().getFullName(),
                    "Current market prices and details of your shares");
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
            pnBody.add(getPnSummary(), BorderLayout.NORTH);
            pnBody.add(getPnHoldings(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    /** The box at the top, in two columns: the big total on the left, the details on the right. */
    private JPanel getPnSummary() {
        if (pnSummary == null) {
            pnSummary = new JPanel();
            pnSummary.setBackground(Color.WHITE);
            pnSummary.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.BORDER, 1, true),
                    new EmptyBorder(18, 22, 18, 22)));
            pnSummary.setLayout(new GridLayout(1, 2, 24, 0));
            pnSummary.add(getPnTotal());
            pnSummary.add(getPnDetails());
        }
        return pnSummary;
    }

    private JPanel getPnTotal() {
        if (pnTotal == null) {
            pnTotal = new JPanel();
            pnTotal.setOpaque(false);
            pnTotal.setLayout(new BoxLayout(pnTotal, BoxLayout.Y_AXIS));
            pnTotal.add(getLblTotalTitle());
            pnTotal.add(getLblTotal());
            pnTotal.add(getLblTotalVariation());
        }
        return pnTotal;
    }

    private JLabel getLblTotalTitle() {
        if (lblTotalTitle == null) {
            lblTotalTitle = new JLabel("TOTAL PORTFOLIO VALUE");
            lblTotalTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            lblTotalTitle.setForeground(Branding.TEXT_MUTED);
        }
        return lblTotalTitle;
    }

    private JLabel getLblTotal() {
        if (lblTotal == null) {
            lblTotal = new JLabel(MoneyFormat.format(wallet.getTotalValue()));
            lblTotal.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 40));
            lblTotal.setForeground(trendColor(wallet.isUp()));
            lblTotal.setBorder(new EmptyBorder(4, 0, 4, 0));
        }
        return lblTotal;
    }

    private JLabel getLblTotalVariation() {
        if (lblTotalVariation == null) {
            lblTotalVariation = new JLabel();
            lblTotalVariation.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 15));
            if (wallet.isEmpty()) {
                lblTotalVariation.setText("You have no shares yet.");
                lblTotalVariation.setForeground(Branding.TEXT_MUTED);
            } else {
                lblTotalVariation.setText(signedMoney(wallet.getTotalVariation()) + "  ("
                        + signedPercent(wallet.getTotalVariationPercent()) + ")  since purchase");
                lblTotalVariation.setForeground(trendColor(wallet.isUp()));
            }
        }
        return lblTotalVariation;
    }

    /** Details of the wallet as "label: value" rows. */
    private JPanel getPnDetails() {
        if (pnDetails == null) {
            pnDetails = new JPanel();
            pnDetails.setOpaque(false);
            pnDetails.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 1, 0, 0, Branding.BORDER),
                    new EmptyBorder(0, 24, 0, 0)));
            pnDetails.setLayout(new GridBagLayout());

            BigDecimal dailyChange = wallet.getDailyChangePercent();
            addDetail(0, "Shareholder:", wallet.getShareholder().getFullName(), null);
            addDetail(1, "National ID:", wallet.getShareholder().getNationalId(), null);
            addDetail(2, "Shares owned:", wallet.getTotalShares() + " shares from " + wallet.getCampaignCount()
                    + (wallet.getCampaignCount() == 1 ? " campaign" : " campaigns"), null);
            addDetail(3, "Invested (purchase price):", MoneyFormat.format(wallet.getTotalCost()), null);
            addDetail(4, "Market price (" + wallet.getCurrentPrice().getDate().format(DATE_FORMAT) + "):",
                    MoneyFormat.format(wallet.getCurrentPrice().getPrice()) + " per share", null);
            addDetail(5, "Change from the previous day:",
                    dailyChange == null ? "-" : signedPercent(dailyChange),
                    dailyChange == null ? null : trendColor(dailyChange.signum() >= 0));
        }
        return pnDetails;
    }

    private void addDetail(int row, String label, String value, Color valueColor) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setForeground(Branding.TEXT_MUTED);
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(0, 0, 6, 12);
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        pnDetails.add(lblLabel, gbcLabel);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        lblValue.setForeground(valueColor == null ? Branding.NAVY : valueColor);
        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.anchor = GridBagConstraints.WEST;
        gbcValue.weightx = 1.0;
        gbcValue.insets = new Insets(0, 0, 6, 0);
        gbcValue.gridx = 1;
        gbcValue.gridy = row;
        pnDetails.add(lblValue, gbcValue);
    }

    private JPanel getPnHoldings() {
        if (pnHoldings == null) {
            pnHoldings = new JPanel();
            TitledBorder border = BorderFactory.createTitledBorder("Share holdings");
            border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            pnHoldings.setBorder(border);
            pnHoldings.setLayout(new BorderLayout(0, 6));
            pnHoldings.add(getScrHoldings(), BorderLayout.CENTER);
            if (wallet.isEmpty()) {
                pnHoldings.add(getLblNoShares(), BorderLayout.NORTH);
            }
        }
        return pnHoldings;
    }

    private JLabel getLblNoShares() {
        if (lblNoShares == null) {
            lblNoShares = new JLabel("You do not own any shares of the club yet.");
            lblNoShares.setForeground(Branding.TEXT_MUTED);
        }
        return lblNoShares;
    }

    private JScrollPane getScrHoldings() {
        if (scrHoldings == null) {
            scrHoldings = new JScrollPane();
            scrHoldings.setViewportView(getTblHoldings());
        }
        return scrHoldings;
    }

    private JTable getTblHoldings() {
        if (tblHoldings == null) {
            tblHoldings = new JTable(holdingsModel);
            tblHoldings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblHoldings.setAutoCreateRowSorter(true);
            tblHoldings.setFillsViewportHeight(true);
            tblHoldings.setRowHeight(24);
            tblHoldings.getTableHeader().setReorderingAllowed(false);

            tblHoldings.getColumnModel().getColumn(HoldingsTableModel.COLUMN_PURCHASE_DATE).setCellRenderer(dateRenderer());
            tblHoldings.getColumnModel().getColumn(HoldingsTableModel.COLUMN_PURCHASE_PRICE).setCellRenderer(MoneyFormat.tableRenderer());
            tblHoldings.getColumnModel().getColumn(HoldingsTableModel.COLUMN_CURRENT_PRICE).setCellRenderer(MoneyFormat.tableRenderer());
            tblHoldings.getColumnModel().getColumn(HoldingsTableModel.COLUMN_CURRENT_VALUE).setCellRenderer(MoneyFormat.tableRenderer());
            tblHoldings.getColumnModel().getColumn(HoldingsTableModel.COLUMN_VARIATION).setCellRenderer(variationRenderer(false));
            tblHoldings.getColumnModel().getColumn(HoldingsTableModel.COLUMN_VARIATION_PERCENT).setCellRenderer(variationRenderer(true));

            tblHoldings.getColumnModel().getColumn(HoldingsTableModel.COLUMN_SHARE_IDS).setCellRenderer(shareIdsRenderer());
            ((DefaultRowSorter<?, ?>) tblHoldings.getRowSorter()).setSortable(HoldingsTableModel.COLUMN_SHARE_IDS, false);
            tblHoldings.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    int viewRow = tblHoldings.rowAtPoint(event.getPoint());
                    int viewColumn = tblHoldings.columnAtPoint(event.getPoint());
                    if (viewRow >= 0 && tblHoldings.convertColumnIndexToModel(viewColumn) == HoldingsTableModel.COLUMN_SHARE_IDS) {
                        showShareIds(viewRow);
                    }
                }
            });

            int[] widths = { 190, 75, 105, 105, 105, 120, 120, 100, 120 };
            for (int column = 0; column < widths.length; column++) {
                tblHoldings.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
            }
        }
        return tblHoldings;
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

    /** Opens the vertical list of the share IDs of the holding in that row. */
    private void showShareIds(int viewRow) {
        ShareHolding holding = holdingsModel.getHoldingAt(getTblHoldings().convertRowIndexToModel(viewRow));
        new ShareIdsDialog(holding, this).setVisible(true);
    }

    // ------------------------------------------------------------------
    // Formats and renderers
    // ------------------------------------------------------------------

    /** Green if the value went up (or stayed the same), red if it went down. */
    private static Color trendColor(boolean up) {
        return up ? Branding.SUCCESS : Branding.ERROR;
    }

    /** For example "+1.240,00 €" or "-310,50 €". */
    static String signedMoney(BigDecimal amount) {
        return (amount.signum() < 0 ? "-" : "+") + MoneyFormat.format(amount.abs());
    }

    /** For example "+10,00 %" or "-2,35 %". */
    static String signedPercent(BigDecimal percent) {
        return String.format(LOCALE_ES, "%+.2f %%", percent);
    }

    /** The last column looks like a button: "View 500 IDs". */
    private static TableCellRenderer shareIdsRenderer() {
        JButton button = new JButton();
        button.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        button.setToolTipText("Click to see the list of the IDs of these shares");
        return (table, value, isSelected, hasFocus, row, column) -> {
            ShareHolding holding = (ShareHolding) value;
            button.setText("View " + holding.getQuantity() + " IDs");
            return button;
        };
    }

    private static DefaultTableCellRenderer dateRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setText(value instanceof LocalDate date ? date.format(DATE_FORMAT) : "");
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }

    /** Variation in euros or in percentage, with its sign, in green or red. */
    private static DefaultTableCellRenderer variationRenderer(boolean percent) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof BigDecimal amount) {
                    setText(percent ? signedPercent(amount) : signedMoney(amount));
                    if (!isSelected) {
                        component.setForeground(trendColor(amount.signum() >= 0));
                    }
                }
                setFont(getFont().deriveFont(Font.BOLD));
                return component;
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        return renderer;
    }
}
