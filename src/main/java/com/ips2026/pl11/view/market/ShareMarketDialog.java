package com.ips2026.pl11.view.market;

import com.ips2026.pl11.controller.market.ShareMarketController;
import com.ips2026.pl11.model.market.CartItem;
import com.ips2026.pl11.model.market.MarketFilter;
import com.ips2026.pl11.model.market.SaleType;
import com.ips2026.pl11.model.market.ShareCart;
import com.ips2026.pl11.model.market.ShareListing;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

/**
 * Share market: the shareholder sees the shares that the other shareholders
 * sell, filters them, and adds whole packs or individual shares to a cart
 * that is paid in the checkout.
 *
 * <p>It is a modal dialog. It follows the lazy WindowBuilder style of the
 * main menu.</p>
 */
public class ShareMarketDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String TITLE = "Share Market";
    private static final DateTimeFormatter LISTED_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient ShareMarketController controller;
    private final ListingsTableModel listingsModel = new ListingsTableModel();
    private final ShareCartTableModel cartModel = new ShareCartTableModel();
    private transient MarketFilter filter = MarketFilter.NONE;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnFilters;
    private JTextField txtMinPrice;
    private JTextField txtMaxPrice;
    private JTextField txtMinShares;
    private JTextField txtMaxShares;
    private JRadioButton rbPacks;
    private JRadioButton rbIndividual;
    private JRadioButton rbBoth;
    private JButton btnApply;
    private JButton btnReset;
    private JPanel pnCenter;
    private JPanel pnSales;
    private JScrollPane scrSales;
    private JTable tblSales;
    private JLabel lblSalesNote;
    private JPanel pnCart;
    private JScrollPane scrCart;
    private JTable tblCart;
    private JPanel pnCartBottom;
    private JLabel lblTotal;
    private JButton btnEmptyCart;
    private JButton btnBuy;
    private JPanel pnButtons;
    private JButton btnClose;

    /**
     * @param controller controller with the listings already loaded
     * @param owner      window that opens it (the shareholders portal); it stays blocked until this window is closed
     */
    public ShareMarketDialog(ShareMarketController controller, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setTitle(TITLE);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 600));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);

        refreshListings();
        refreshCart();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(TITLE, "Buy shares sold by other shareholders, as packs or one by one — "
                    + controller.getBuyer().getFullName());
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
            pnBody.add(getPnFilters(), BorderLayout.NORTH);
            pnBody.add(getPnCenter(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnFilters() {
        if (pnFilters == null) {
            pnFilters = new JPanel();
            pnFilters.setBorder(BorderFactory.createCompoundBorder(titled("Filters"), new EmptyBorder(4, 4, 4, 4)));
            pnFilters.setLayout(new BorderLayout(0, 0));

            JPanel pnFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            pnFields.setOpaque(false);
            JLabel lblPrice = new JLabel("Price:");
            lblPrice.setDisplayedMnemonic('P');
            lblPrice.setLabelFor(getTxtMinPrice());
            lblPrice.setToolTipText("Price of the pack for packs, price per share for individual shares");
            pnFields.add(lblPrice);
            pnFields.add(getTxtMinPrice());
            pnFields.add(new JLabel("–"));
            pnFields.add(getTxtMaxPrice());
            pnFields.add(mutedLabel("€"));
            pnFields.add(Box.createHorizontalStrut(14));
            JLabel lblShares = new JLabel("Shares:");
            lblShares.setDisplayedMnemonic('H');
            lblShares.setLabelFor(getTxtMinShares());
            lblShares.setToolTipText("Number of shares available in the sale");
            pnFields.add(lblShares);
            pnFields.add(getTxtMinShares());
            pnFields.add(new JLabel("–"));
            pnFields.add(getTxtMaxShares());
            pnFields.add(Box.createHorizontalStrut(14));
            pnFields.add(new JLabel("Sale type:"));
            pnFields.add(getRbPacks());
            pnFields.add(getRbIndividual());
            pnFields.add(getRbBoth());
            ButtonGroup saleTypes = new ButtonGroup();
            saleTypes.add(getRbPacks());
            saleTypes.add(getRbIndividual());
            saleTypes.add(getRbBoth());

            JPanel pnFilterButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnFilterButtons.setOpaque(false);
            pnFilterButtons.add(getBtnReset());
            pnFilterButtons.add(getBtnApply());

            pnFilters.add(pnFields, BorderLayout.CENTER);
            pnFilters.add(pnFilterButtons, BorderLayout.EAST);
        }
        return pnFilters;
    }

    private JTextField getTxtMinPrice() {
        if (txtMinPrice == null) {
            txtMinPrice = filterField("Minimum price (empty = no limit)");
        }
        return txtMinPrice;
    }

    private JTextField getTxtMaxPrice() {
        if (txtMaxPrice == null) {
            txtMaxPrice = filterField("Maximum price (empty = no limit)");
        }
        return txtMaxPrice;
    }

    private JTextField getTxtMinShares() {
        if (txtMinShares == null) {
            txtMinShares = filterField("Minimum number of shares (empty = no limit)");
        }
        return txtMinShares;
    }

    private JTextField getTxtMaxShares() {
        if (txtMaxShares == null) {
            txtMaxShares = filterField("Maximum number of shares (empty = no limit)");
        }
        return txtMaxShares;
    }

    private static JTextField filterField(String tooltip) {
        JTextField field = new JTextField(6);
        field.setToolTipText(tooltip);
        return field;
    }

    private JRadioButton getRbPacks() {
        if (rbPacks == null) {
            rbPacks = new JRadioButton("Packs");
            rbPacks.setOpaque(false);
            rbPacks.setToolTipText("Only the sales whose whole pack can still be bought");
        }
        return rbPacks;
    }

    private JRadioButton getRbIndividual() {
        if (rbIndividual == null) {
            rbIndividual = new JRadioButton("Individual shares");
            rbIndividual.setOpaque(false);
        }
        return rbIndividual;
    }

    private JRadioButton getRbBoth() {
        if (rbBoth == null) {
            rbBoth = new JRadioButton("Both");
            rbBoth.setOpaque(false);
            rbBoth.setSelected(true);
        }
        return rbBoth;
    }

    private JButton getBtnApply() {
        if (btnApply == null) {
            btnApply = new JButton("Apply");
            btnApply.setFont(btnApply.getFont().deriveFont(Font.BOLD));
            btnApply.setMnemonic('A');
            btnApply.addActionListener(event -> applyFilter());
        }
        return btnApply;
    }

    private JButton getBtnReset() {
        if (btnReset == null) {
            btnReset = new JButton("Reset");
            btnReset.setMnemonic('R');
            btnReset.addActionListener(event -> resetFilter());
        }
        return btnReset;
    }

    /** Current sales on the left, cart on the right. */
    private JPanel getPnCenter() {
        if (pnCenter == null) {
            pnCenter = new JPanel();
            pnCenter.setOpaque(false);
            pnCenter.setLayout(new BorderLayout(14, 0));
            pnCenter.add(getPnSales(), BorderLayout.CENTER);
            pnCenter.add(getPnCart(), BorderLayout.EAST);
        }
        return pnCenter;
    }

    private JPanel getPnSales() {
        if (pnSales == null) {
            pnSales = new JPanel();
            pnSales.setBorder(BorderFactory.createCompoundBorder(titled("Current sales"), new EmptyBorder(6, 6, 6, 6)));
            pnSales.setLayout(new BorderLayout(0, 6));
            pnSales.add(getScrSales(), BorderLayout.CENTER);
            pnSales.add(getLblSalesNote(), BorderLayout.SOUTH);
        }
        return pnSales;
    }

    private JScrollPane getScrSales() {
        if (scrSales == null) {
            scrSales = new JScrollPane();
            scrSales.setViewportView(getTblSales());
        }
        return scrSales;
    }

    private JTable getTblSales() {
        if (tblSales == null) {
            tblSales = new JTable(listingsModel);
            tblSales.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblSales.setFillsViewportHeight(true);
            tblSales.setRowHeight(28);
            tblSales.getTableHeader().setReorderingAllowed(false);
            TableRowSorter<ListingsTableModel> sorter = new TableRowSorter<>(listingsModel);
            sorter.setSortable(ListingsTableModel.COLUMN_PACK_PRICE, false);
            sorter.setSortable(ListingsTableModel.COLUMN_PACK, false);
            sorter.setSortable(ListingsTableModel.COLUMN_INDIVIDUAL, false);
            tblSales.setRowSorter(sorter);

            tblSales.getColumnModel().getColumn(ListingsTableModel.COLUMN_PRICE).setCellRenderer(MoneyFormat.tableRenderer());
            tblSales.getColumnModel().getColumn(ListingsTableModel.COLUMN_PACK_PRICE).setCellRenderer(packPriceRenderer());
            tblSales.getColumnModel().getColumn(ListingsTableModel.COLUMN_LISTED).setCellRenderer(listedRenderer());
            tblSales.getColumnModel().getColumn(ListingsTableModel.COLUMN_PACK).setCellRenderer(buttonRenderer(this::packButtonText, this::canAddPack));
            tblSales.getColumnModel().getColumn(ListingsTableModel.COLUMN_INDIVIDUAL).setCellRenderer(buttonRenderer(listing -> "Choose shares...", this::canChooseShares));

            int[] widths = { 150, 55, 85, 85, 85, 90, 145 };
            for (int column = 0; column < widths.length; column++) {
                tblSales.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
            }
            tblSales.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    int viewRow = tblSales.rowAtPoint(event.getPoint());
                    int column = tblSales.convertColumnIndexToModel(tblSales.columnAtPoint(event.getPoint()));
                    if (viewRow < 0) {
                        return;
                    }
                    ShareListing listing = listingsModel.getListingAt(tblSales.convertRowIndexToModel(viewRow));
                    if (column == ListingsTableModel.COLUMN_PACK) {
                        addPack(listing);
                    } else if (column == ListingsTableModel.COLUMN_INDIVIDUAL) {
                        chooseShares(listing);
                    }
                }
            });
        }
        return tblSales;
    }

    private JLabel getLblSalesNote() {
        if (lblSalesNote == null) {
            lblSalesNote = mutedLabel("Newest sales first; your own sales are not listed. "
                    + "'n/a': a share was already sold, so the pack price no longer applies.");
            lblSalesNote.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 11));
        }
        return lblSalesNote;
    }

    private JPanel getPnCart() {
        if (pnCart == null) {
            pnCart = new JPanel();
            pnCart.setBorder(BorderFactory.createCompoundBorder(titled("Cart"), new EmptyBorder(6, 6, 6, 6)));
            pnCart.setLayout(new BorderLayout(0, 10));
            pnCart.setPreferredSize(new Dimension(400, 400));
            pnCart.add(getScrCart(), BorderLayout.CENTER);
            pnCart.add(getPnCartBottom(), BorderLayout.SOUTH);
        }
        return pnCart;
    }

    private JScrollPane getScrCart() {
        if (scrCart == null) {
            scrCart = new JScrollPane();
            scrCart.setViewportView(getTblCart());
        }
        return scrCart;
    }

    private JTable getTblCart() {
        if (tblCart == null) {
            tblCart = new JTable(cartModel);
            tblCart.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblCart.setFillsViewportHeight(true);
            tblCart.setRowHeight(28);
            tblCart.getTableHeader().setReorderingAllowed(false);
            tblCart.getColumnModel().getColumn(ShareCartTableModel.COLUMN_PRICE).setCellRenderer(MoneyFormat.tableRenderer());
            tblCart.getColumnModel().getColumn(ShareCartTableModel.COLUMN_REMOVE).setCellRenderer(buttonRenderer(item -> "×", item -> true));
            int[] widths = { 190, 60, 90, 40 };
            for (int column = 0; column < widths.length; column++) {
                tblCart.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
            }
            tblCart.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    int row = tblCart.rowAtPoint(event.getPoint());
                    int column = tblCart.convertColumnIndexToModel(tblCart.columnAtPoint(event.getPoint()));
                    if (row >= 0 && column == ShareCartTableModel.COLUMN_REMOVE) {
                        controller.removeFromCart(cartModel.getItemAt(row));
                        refreshCart();
                    }
                }
            });
        }
        return tblCart;
    }

    /** Total of the cart and its buttons. */
    private JPanel getPnCartBottom() {
        if (pnCartBottom == null) {
            pnCartBottom = new JPanel();
            pnCartBottom.setOpaque(false);
            pnCartBottom.setLayout(new BorderLayout(0, 10));

            JPanel pnTotal = new JPanel(new BorderLayout(12, 0));
            pnTotal.setOpaque(false);
            JLabel lblTotalTitle = mutedLabel("TOTAL");
            lblTotalTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            pnTotal.add(lblTotalTitle, BorderLayout.WEST);
            pnTotal.add(getLblTotal(), BorderLayout.EAST);

            JPanel pnCartButtons = new JPanel(new BorderLayout(8, 0));
            pnCartButtons.setOpaque(false);
            pnCartButtons.add(getBtnEmptyCart(), BorderLayout.WEST);
            pnCartButtons.add(getBtnBuy(), BorderLayout.EAST);

            pnCartBottom.add(new JSeparator(), BorderLayout.NORTH);
            pnCartBottom.add(pnTotal, BorderLayout.CENTER);
            pnCartBottom.add(pnCartButtons, BorderLayout.SOUTH);
        }
        return pnCartBottom;
    }

    private JLabel getLblTotal() {
        if (lblTotal == null) {
            lblTotal = new JLabel();
            lblTotal.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 26));
            lblTotal.setForeground(Branding.NAVY);
            lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return lblTotal;
    }

    private JButton getBtnEmptyCart() {
        if (btnEmptyCart == null) {
            btnEmptyCart = new JButton("Empty cart");
            btnEmptyCart.setMnemonic('E');
            btnEmptyCart.addActionListener(event -> {
                controller.emptyCart();
                refreshCart();
            });
        }
        return btnEmptyCart;
    }

    private JButton getBtnBuy() {
        if (btnBuy == null) {
            btnBuy = new JButton("Buy...");
            btnBuy.setFont(btnBuy.getFont().deriveFont(Font.BOLD));
            btnBuy.setMnemonic('B');
            btnBuy.addActionListener(event -> openCheckout());
        }
        return btnBuy;
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

    private static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Branding.TEXT_MUTED);
        return label;
    }

    private static TitledBorder titled(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        return border;
    }

    // ------------------------------------------------------------------
    // Renderers
    // ------------------------------------------------------------------

    /** A cell drawn as a button with the text and enabled state given by the row value. */
    private static <T> TableCellRenderer buttonRenderer(Function<T, String> text, Function<T, Boolean> enabled) {
        JButton button = new JButton();
        button.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        return (table, value, isSelected, hasFocus, row, column) -> {
            @SuppressWarnings("unchecked")
            T item = (T) value;
            button.setText(text.apply(item));
            button.setEnabled(enabled.apply(item));
            return button;
        };
    }

    /** The pack price as money, or "n/a" / "—" aligned to the right. */
    private static DefaultTableCellRenderer packPriceRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setText(value instanceof BigDecimal price ? MoneyFormat.format(price) : String.valueOf(value));
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        return renderer;
    }

    private static DefaultTableCellRenderer listedRenderer() {
        return new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setText(value instanceof LocalDateTime listed ? listed.format(LISTED_FORMAT) : "");
            }
        };
    }

    private String packButtonText(ShareListing listing) {
        if (controller.getCart().hasPack(listing)) {
            return "In cart";
        }
        return listing.isPackAvailable() ? "Add pack" : "—";
    }

    /** The pack can be added if it is available and nothing of that sale is in the cart. */
    private boolean canAddPack(ShareListing listing) {
        ShareCart cart = controller.getCart();
        return listing.isPackAvailable() && !cart.hasPack(listing) && cart.getSingleShareIds(listing).isEmpty();
    }

    /** Shares can be chosen one by one unless the pack of that sale is in the cart. */
    private boolean canChooseShares(ShareListing listing) {
        return !controller.getCart().hasPack(listing);
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    private void applyFilter() {
        SaleType saleType = getRbPacks().isSelected() ? SaleType.PACKS
                : getRbIndividual().isSelected() ? SaleType.INDIVIDUAL : SaleType.BOTH;
        try {
            filter = controller.createFilter(getTxtMinPrice().getText(), getTxtMaxPrice().getText(),
                    getTxtMinShares().getText(), getTxtMaxShares().getText(), saleType);
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        refreshListings();
    }

    private void resetFilter() {
        getTxtMinPrice().setText("");
        getTxtMaxPrice().setText("");
        getTxtMinShares().setText("");
        getTxtMaxShares().setText("");
        getRbBoth().setSelected(true);
        filter = MarketFilter.NONE;
        refreshListings();
    }

    private void addPack(ShareListing listing) {
        if (!listing.isPackAvailable() || controller.getCart().hasPack(listing)) {
            return; // the button shows "—" or "In cart"
        }
        try {
            controller.addPack(listing);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), TITLE, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        refreshCart();
    }

    private void chooseShares(ShareListing listing) {
        if (!canChooseShares(listing)) {
            JOptionPane.showMessageDialog(this, "The pack of this sale is in the cart: remove it to choose shares.",
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ChooseSharesDialog dialog = new ChooseSharesDialog(listing, controller.getCart().getSingleShareIds(listing), this);
        dialog.setVisible(true);
        List<Integer> chosen = dialog.getChosenShareIds();
        if (!chosen.isEmpty()) {
            controller.addShares(listing, chosen);
            refreshCart();
        }
    }

    private void openCheckout() {
        if (controller.getCart().isEmpty()) {
            return;
        }
        new CheckoutDialog(controller, this).setVisible(true);
        // After a purchase (or if a share was no longer available) the cart and the sales have changed
        refreshListings();
        refreshCart();
    }

    private void refreshListings() {
        listingsModel.setListings(controller.getListings(filter));
    }

    private void refreshCart() {
        ShareCart cart = controller.getCart();
        cartModel.setItems(cart.getItems());
        getLblTotal().setText(MoneyFormat.format(cart.getTotal()));
        getBtnBuy().setEnabled(!cart.isEmpty());
        getBtnEmptyCart().setEnabled(!cart.isEmpty());
        getTblSales().repaint(); // the pack and individual buttons depend on the cart
    }
}
