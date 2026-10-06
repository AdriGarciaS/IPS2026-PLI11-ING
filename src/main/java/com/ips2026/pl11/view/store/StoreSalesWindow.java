package com.ips2026.pl11.view.store;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ips2026.pl11.controller.store.StoreSalesController;
import com.ips2026.pl11.model.store.CartLine;
import com.ips2026.pl11.model.store.Merchandise;
import com.ips2026.pl11.model.store.SaleReceipt;

import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

/**
 * Store sales window: the catalog of merchandising products on the left and
 * the cart on the right, with the total price updated dynamically.
 *
 * <p>It is a modal dialog: the main menu is blocked while it is open.</p>
 *
 * <p>Follows the same "lazy" WindowBuilder style as the main menu ({@code VentanaPrincipal}):
 * one private attribute per component, created by its getter.</p>
 */
public class StoreSalesWindow extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String ALL_TYPES = "All types";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient StoreSalesController controller;
    private final CatalogTableModel catalogModel;
    private final CartTableModel cartModel;

    /** True while the cart spinner is changed by code, so it is not taken as a user change. */
    private boolean updatingCartSpinner;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnLists;
    private JPanel pnCatalog;
    private JPanel pnFilters;
    private JLabel lblSearch;
    private JTextField txtSearch;
    private JLabel lblType;
    private JComboBox<String> cbType;
    private JScrollPane scrCatalog;
    private JTable tblCatalog;
    private JPanel pnCatalogActions;
    private JLabel lblCatalogUnits;
    private JSpinner spCatalogUnits;
    private JButton btnAdd;
    private JPanel pnCart;
    private JScrollPane scrCart;
    private JTable tblCart;
    private JPanel pnCartBottom;
    private JPanel pnCartActions;
    private JLabel lblCartUnits;
    private JSpinner spCartUnits;
    private JButton btnRemove;
    private JPanel pnTotal;
    private JLabel lblTotalText;
    private JLabel lblTotal;
    private JPanel pnButtons;
    private JButton btnClose;
    private JButton btnPurchase;

    /**
     * @param controller controller with the catalog already loaded
     * @param owner      window that opens it (the main menu); it stays
     *                   blocked until this window is closed
     */
    public StoreSalesWindow(StoreSalesController controller, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.catalogModel = new CatalogTableModel(controller.getCart());
        this.cartModel = new CartTableModel(controller.getCart());

        setTitle("Store Sales");
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                close();
            }
        });
        setMinimumSize(new Dimension(850, 500));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);

        refreshCatalog();
        updateCartActions();
        updateTotal();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Merchandising Store",
                    "Select products from the catalog and add them to the cart.");
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
            pnBody.add(getPnLists(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    /** Both lists side by side, sharing the width equally when resizing. */
    private JPanel getPnLists() {
        if (pnLists == null) {
            pnLists = new JPanel();
            pnLists.setOpaque(false);
            pnLists.setLayout(new GridLayout(1, 2, 12, 0));
            pnLists.add(getPnCatalog());
            pnLists.add(getPnCart());
        }
        return pnLists;
    }

    private JPanel getPnCatalog() {
        if (pnCatalog == null) {
            pnCatalog = new JPanel();
            pnCatalog.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Catalog"),
                    new EmptyBorder(6, 8, 8, 8)));
            pnCatalog.setLayout(new BorderLayout(0, 8));
            pnCatalog.add(getPnFilters(), BorderLayout.NORTH);
            pnCatalog.add(getScrCatalog(), BorderLayout.CENTER);
            pnCatalog.add(getPnCatalogActions(), BorderLayout.SOUTH);
        }
        return pnCatalog;
    }

    private JPanel getPnFilters() {
        if (pnFilters == null) {
            pnFilters = new JPanel();
            pnFilters.setLayout(new GridBagLayout());

            GridBagConstraints gbcLblSearch = new GridBagConstraints();
            gbcLblSearch.anchor = GridBagConstraints.WEST;
            gbcLblSearch.insets = new Insets(0, 0, 6, 8);
            gbcLblSearch.gridx = 0;
            gbcLblSearch.gridy = 0;
            pnFilters.add(getLblSearch(), gbcLblSearch);

            GridBagConstraints gbcTxtSearch = new GridBagConstraints();
            gbcTxtSearch.fill = GridBagConstraints.HORIZONTAL;
            gbcTxtSearch.weightx = 1.0;
            gbcTxtSearch.insets = new Insets(0, 0, 6, 0);
            gbcTxtSearch.gridx = 1;
            gbcTxtSearch.gridy = 0;
            pnFilters.add(getTxtSearch(), gbcTxtSearch);

            GridBagConstraints gbcLblType = new GridBagConstraints();
            gbcLblType.anchor = GridBagConstraints.WEST;
            gbcLblType.insets = new Insets(0, 0, 0, 8);
            gbcLblType.gridx = 0;
            gbcLblType.gridy = 1;
            pnFilters.add(getLblType(), gbcLblType);

            GridBagConstraints gbcCbType = new GridBagConstraints();
            gbcCbType.fill = GridBagConstraints.HORIZONTAL;
            gbcCbType.weightx = 1.0;
            gbcCbType.gridx = 1;
            gbcCbType.gridy = 1;
            pnFilters.add(getCbType(), gbcCbType);
        }
        return pnFilters;
    }

    private JLabel getLblSearch() {
        if (lblSearch == null) {
            lblSearch = new JLabel("Search by name:");
            lblSearch.setDisplayedMnemonic('S');
            lblSearch.setLabelFor(getTxtSearch());
        }
        return lblSearch;
    }

    private JTextField getTxtSearch() {
        if (txtSearch == null) {
            txtSearch = new JTextField();
            txtSearch.setToolTipText("Shows the products whose name contains this text (not case sensitive)");
            txtSearch.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    refreshCatalog();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    refreshCatalog();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    refreshCatalog();
                }
            });
        }
        return txtSearch;
    }

    private JLabel getLblType() {
        if (lblType == null) {
            lblType = new JLabel("Type:");
            lblType.setDisplayedMnemonic('T');
            lblType.setLabelFor(getCbType());
        }
        return lblType;
    }

    private JComboBox<String> getCbType() {
        if (cbType == null) {
            cbType = new JComboBox<>();
            cbType.addItem(ALL_TYPES);
            for (String type : controller.getTypes()) {
                cbType.addItem(type);
            }
            cbType.addActionListener(event -> refreshCatalog());
        }
        return cbType;
    }

    private JScrollPane getScrCatalog() {
        if (scrCatalog == null) {
            scrCatalog = new JScrollPane();
            scrCatalog.setViewportView(getTblCatalog());
        }
        return scrCatalog;
    }

    private JTable getTblCatalog() {
        if (tblCatalog == null) {
            tblCatalog = new JTable(catalogModel);
            configureTable(tblCatalog);
            tblCatalog.getColumnModel().getColumn(CatalogTableModel.COLUMN_PRICE)
                    .setCellRenderer(MoneyFormat.tableRenderer());
            tblCatalog.getColumnModel().getColumn(0).setPreferredWidth(200);
            tblCatalog.getColumnModel().getColumn(1).setPreferredWidth(110);
            tblCatalog.getColumnModel().getColumn(2).setPreferredWidth(80);
            tblCatalog.getColumnModel().getColumn(3).setPreferredWidth(70);
            tblCatalog.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    updateCatalogActions();
                }
            });
        }
        return tblCatalog;
    }

    private JPanel getPnCatalogActions() {
        if (pnCatalogActions == null) {
            pnCatalogActions = new JPanel();
            pnCatalogActions.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnCatalogActions.add(getLblCatalogUnits());
            pnCatalogActions.add(getSpCatalogUnits());
            pnCatalogActions.add(getBtnAdd());
        }
        return pnCatalogActions;
    }

    private JLabel getLblCatalogUnits() {
        if (lblCatalogUnits == null) {
            lblCatalogUnits = new JLabel("Units:");
            lblCatalogUnits.setLabelFor(getSpCatalogUnits());
        }
        return lblCatalogUnits;
    }

    private JSpinner getSpCatalogUnits() {
        if (spCatalogUnits == null) {
            spCatalogUnits = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
            spCatalogUnits.setPreferredSize(new Dimension(70, spCatalogUnits.getPreferredSize().height));
            spCatalogUnits.setToolTipText("Number of units to add to the cart");
        }
        return spCatalogUnits;
    }

    private JButton getBtnAdd() {
        if (btnAdd == null) {
            btnAdd = new JButton("Add to cart →");
            btnAdd.setMnemonic('A');
            btnAdd.addActionListener(event -> addToCart());
        }
        return btnAdd;
    }

    private JPanel getPnCart() {
        if (pnCart == null) {
            pnCart = new JPanel();
            pnCart.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Cart"),
                    new EmptyBorder(6, 8, 8, 8)));
            pnCart.setLayout(new BorderLayout(0, 8));
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
            configureTable(tblCart);
            tblCart.getColumnModel().getColumn(CartTableModel.COLUMN_PRICE)
                    .setCellRenderer(MoneyFormat.tableRenderer());
            tblCart.getColumnModel().getColumn(CartTableModel.COLUMN_SUBTOTAL)
                    .setCellRenderer(MoneyFormat.tableRenderer());
            tblCart.getColumnModel().getColumn(0).setPreferredWidth(200);
            tblCart.getColumnModel().getColumn(1).setPreferredWidth(80);
            tblCart.getColumnModel().getColumn(2).setPreferredWidth(50);
            tblCart.getColumnModel().getColumn(3).setPreferredWidth(90);
            tblCart.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    updateCartActions();
                }
            });
        }
        return tblCart;
    }

    private JPanel getPnCartBottom() {
        if (pnCartBottom == null) {
            pnCartBottom = new JPanel();
            pnCartBottom.setLayout(new BorderLayout(0, 8));
            pnCartBottom.add(getPnCartActions(), BorderLayout.NORTH);
            pnCartBottom.add(getPnTotal(), BorderLayout.SOUTH);
        }
        return pnCartBottom;
    }

    private JPanel getPnCartActions() {
        if (pnCartActions == null) {
            pnCartActions = new JPanel();
            pnCartActions.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnCartActions.add(getLblCartUnits());
            pnCartActions.add(getSpCartUnits());
            pnCartActions.add(getBtnRemove());
        }
        return pnCartActions;
    }

    private JLabel getLblCartUnits() {
        if (lblCartUnits == null) {
            lblCartUnits = new JLabel("Units:");
            lblCartUnits.setLabelFor(getSpCartUnits());
        }
        return lblCartUnits;
    }

    private JSpinner getSpCartUnits() {
        if (spCartUnits == null) {
            spCartUnits = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
            spCartUnits.setPreferredSize(new Dimension(70, spCartUnits.getPreferredSize().height));
            spCartUnits.setToolTipText("Changes the units of the selected cart line");
            spCartUnits.addChangeListener(event -> {
                if (!updatingCartSpinner) {
                    changeCartUnits();
                }
            });
        }
        return spCartUnits;
    }

    private JButton getBtnRemove() {
        if (btnRemove == null) {
            btnRemove = new JButton("Remove");
            btnRemove.setMnemonic('R');
            btnRemove.setToolTipText("Removes the whole selected line from the cart");
            btnRemove.addActionListener(event -> removeFromCart());
        }
        return btnRemove;
    }

    private JPanel getPnTotal() {
        if (pnTotal == null) {
            pnTotal = new JPanel();
            pnTotal.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(1, 0, 0, 0, Branding.BORDER),
                    new EmptyBorder(8, 0, 0, 0)));
            pnTotal.setLayout(new BorderLayout(10, 0));
            pnTotal.add(getLblTotalText(), BorderLayout.WEST);
            pnTotal.add(getLblTotal(), BorderLayout.EAST);
        }
        return pnTotal;
    }

    private JLabel getLblTotalText() {
        if (lblTotalText == null) {
            lblTotalText = new JLabel("TOTAL:");
            lblTotalText.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 16));
        }
        return lblTotalText;
    }

    private JLabel getLblTotal() {
        if (lblTotal == null) {
            lblTotal = new JLabel();
            lblTotal.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 18));
            lblTotal.setForeground(Branding.NAVY);
            lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return lblTotal;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnClose());
            pnButtons.add(getBtnPurchase());
        }
        return pnButtons;
    }

    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton("Close");
            btnClose.setMnemonic('C');
            btnClose.addActionListener(event -> close());
        }
        return btnClose;
    }

    private JButton getBtnPurchase() {
        if (btnPurchase == null) {
            btnPurchase = new JButton("Purchase");
            btnPurchase.setMnemonic('P');
            btnPurchase.setFont(btnPurchase.getFont().deriveFont(Font.BOLD));
            btnPurchase.addActionListener(event -> purchase());
        }
        return btnPurchase;
    }

    /** Common settings of both tables: read-only, one row at a time, sortable. */
    private static void configureTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    /** Applies the name search and the type filter to the catalog. */
    private void refreshCatalog() {
        String type = getCbType().getSelectedIndex() <= 0 ? null : (String) getCbType().getSelectedItem();
        catalogModel.setProducts(controller.filterCatalog(getTxtSearch().getText(), type));
        updateCatalogActions();
    }

    private Merchandise getSelectedProduct() {
        int viewRow = getTblCatalog().getSelectedRow();
        return viewRow < 0 ? null : catalogModel.getProductAt(getTblCatalog().convertRowIndexToModel(viewRow));
    }

    private int getSelectedCartModelRow() {
        int viewRow = getTblCart().getSelectedRow();
        return viewRow < 0 ? -1 : getTblCart().convertRowIndexToModel(viewRow);
    }

    /** The units spinner of the catalog goes from 1 to the units that can still be added. */
    private void updateCatalogActions() {
        Merchandise product = getSelectedProduct();
        int remaining = product == null ? 0 : controller.getCart().getRemainingUnits(product);
        boolean canAdd = remaining > 0;
        if (canAdd) {
            int current = (Integer) getSpCatalogUnits().getValue();
            getSpCatalogUnits().setModel(new SpinnerNumberModel(Math.min(current, remaining), 1, remaining, 1));
        } else {
            getSpCatalogUnits().setModel(new SpinnerNumberModel(1, 1, 1, 1));
        }
        getSpCatalogUnits().setEnabled(canAdd);
        getBtnAdd().setEnabled(canAdd);
    }

    /** The units spinner of the cart shows the units of the selected line (1 to the available units). */
    private void updateCartActions() {
        int modelRow = getSelectedCartModelRow();
        updatingCartSpinner = true;
        if (modelRow >= 0) {
            CartLine line = cartModel.getLineAt(modelRow);
            getSpCartUnits().setModel(new SpinnerNumberModel(
                    line.getUnits(), 1, line.getMerchandise().getAvailableUnits(), 1));
        } else {
            getSpCartUnits().setModel(new SpinnerNumberModel(1, 1, 1, 1));
        }
        updatingCartSpinner = false;
        getSpCartUnits().setEnabled(modelRow >= 0);
        getBtnRemove().setEnabled(modelRow >= 0);
    }

    private void updateTotal() {
        getLblTotal().setText(MoneyFormat.format(controller.getCart().getTotal()));
    }

    /** Called after any change of the cart: refreshes the stock shown in the catalog and the total. */
    private void cartChanged() {
        catalogModel.refreshAvailable();
        updateCatalogActions();
        updateTotal();
    }

    private void addToCart() {
        Merchandise product = getSelectedProduct();
        if (product == null) {
            return;
        }
        try {
            controller.addToCart(product, (Integer) getSpCatalogUnits().getValue());
        } catch (IllegalArgumentException exception) {
            showWarning(exception.getMessage());
            return;
        }
        cartModel.reload();
        selectCartLine(product.getId());
        cartChanged();
    }

    private void selectCartLine(int merchandiseId) {
        for (int row = 0; row < cartModel.getRowCount(); row++) {
            if (cartModel.getLineAt(row).getMerchandise().getId() == merchandiseId) {
                int viewRow = getTblCart().convertRowIndexToView(row);
                getTblCart().setRowSelectionInterval(viewRow, viewRow);
                return;
            }
        }
    }

    private void changeCartUnits() {
        int modelRow = getSelectedCartModelRow();
        if (modelRow < 0) {
            return;
        }
        CartLine line = cartModel.getLineAt(modelRow);
        try {
            controller.changeUnits(line.getMerchandise().getId(), (Integer) getSpCartUnits().getValue());
        } catch (IllegalArgumentException exception) {
            showWarning(exception.getMessage());
            return;
        }
        cartModel.reloadRow(modelRow);
        cartChanged();
    }

    private void removeFromCart() {
        int modelRow = getSelectedCartModelRow();
        if (modelRow < 0) {
            return;
        }
        controller.removeFromCart(cartModel.getLineAt(modelRow).getMerchandise().getId());
        cartModel.reload();
        updateCartActions();
        cartChanged();
    }

    private void purchase() {
        if (controller.getCart().isEmpty()) {
            showWarning("The cart is empty. Add at least one product before purchasing.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Do you want to purchase the cart for a total of "
                        + MoneyFormat.format(controller.getCart().getTotal()) + "?",
                "Confirm purchase", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        SaleReceipt receipt;
        try {
            receipt = controller.purchase();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The purchase could not be completed:\n" + exception.getMessage(),
                    "Purchase error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        cartModel.reload();
        updateCartActions();
        refreshCatalog();
        updateTotal();

        JOptionPane.showMessageDialog(this, buildSummary(receipt),
                "Purchase completed", JOptionPane.INFORMATION_MESSAGE);
    }

    /** HTML summary of the purchase: one row per line and the final price. */
    private static String buildSummary(SaleReceipt receipt) {
        StringBuilder html = new StringBuilder("<html><body style='width: 320px'>");
        html.append("<b>Sale nº ").append(receipt.getSaleNumber()).append("</b> &mdash; ")
                .append(receipt.getDate().format(DATE_FORMAT))
                .append("<table width='100%' cellpadding='2' style='margin-top: 8px'>");
        for (CartLine line : receipt.getLines()) {
            html.append("<tr><td>").append(line.getUnits()).append(" x ")
                    .append(escapeHtml(line.getMerchandise().getName()))
                    .append("</td><td align='right'>").append(MoneyFormat.format(line.getSubtotal()))
                    .append("</td></tr>");
        }
        html.append("</table><hr><b style='font-size: 13px'>FINAL PRICE: ")
                .append(MoneyFormat.format(receipt.getFinalPrice()))
                .append("</b></body></html>");
        return html.toString();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Store Sales", JOptionPane.WARNING_MESSAGE);
    }

    /** Closes the window, asking first if there are products in the cart. */
    private void close() {
        if (!controller.getCart().isEmpty()) {
            int option = JOptionPane.showConfirmDialog(this,
                    "The cart is not empty. Close the window and discard it?",
                    "Close Store Sales", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
    }
}
