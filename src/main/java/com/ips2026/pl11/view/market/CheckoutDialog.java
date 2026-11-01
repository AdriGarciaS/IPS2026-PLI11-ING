package com.ips2026.pl11.view.market;

import com.ips2026.pl11.controller.market.ShareMarketController;
import com.ips2026.pl11.model.market.MarketPurchase;
import com.ips2026.pl11.model.market.ShareCart;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Checkout of the share market: summary of the cart and payment with the
 * shareholder ID and a credit card. After a purchase, {@link #getPurchase()}
 * gives what was bought (null if nothing was bought).
 *
 * <p>It is a modal dialog. It follows the lazy WindowBuilder style of the
 * main menu.</p>
 */
public class CheckoutDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String TITLE = "Checkout";

    private final transient ShareMarketController controller;
    private transient MarketPurchase purchase;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnCenter;
    private JPanel pnSummary;
    private JLabel lblItems;
    private JLabel lblTotal;
    private JPanel pnPayment;
    private JLabel lblNationalId;
    private JTextField txtNationalId;
    private JLabel lblCard;
    private JTextField txtCard;
    private JPanel pnButtons;
    private JButton btnCancel;
    private JButton btnConfirm;

    /**
     * @param controller share market controller with the cart to buy
     * @param owner      window that opens it; it stays blocked until this window is closed
     */
    public CheckoutDialog(ShareMarketController controller, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setTitle(TITLE);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(480, 420));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        getRootPane().setDefaultButton(getBtnConfirm());
        Branding.setInitialSize(this, 560, 480, owner); // small: only a summary and two fields
    }

    /** What was bought, or null if the window was closed without buying. */
    public MarketPurchase getPurchase() {
        return purchase;
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(TITLE, "Confirm your purchase");
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
            pnBody.add(getPnCenter(), BorderLayout.NORTH);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnCenter() {
        if (pnCenter == null) {
            pnCenter = new JPanel();
            pnCenter.setOpaque(false);
            pnCenter.setLayout(new BorderLayout(0, 12));
            pnCenter.add(getPnSummary(), BorderLayout.NORTH);
            pnCenter.add(getPnPayment(), BorderLayout.CENTER);
        }
        return pnCenter;
    }

    private JPanel getPnSummary() {
        if (pnSummary == null) {
            pnSummary = new JPanel();
            pnSummary.setBorder(BorderFactory.createCompoundBorder(titled("Order summary"), new EmptyBorder(8, 12, 8, 12)));
            pnSummary.setLayout(new GridBagLayout());
            addRow(pnSummary, 0, mutedLabel("Items in cart:"), getLblItems());
            JLabel lblTotalTitle = mutedLabel("TOTAL TO PAY");
            lblTotalTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            addRow(pnSummary, 1, lblTotalTitle, getLblTotal());
        }
        return pnSummary;
    }

    private JLabel getLblItems() {
        if (lblItems == null) {
            ShareCart cart = controller.getCart();
            lblItems = new JLabel(cart.getItemCount() + (cart.getItemCount() == 1 ? " item" : " items") + " · "
                    + cart.getShareCount() + (cart.getShareCount() == 1 ? " share" : " shares"));
            lblItems.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
        }
        return lblItems;
    }

    private JLabel getLblTotal() {
        if (lblTotal == null) {
            lblTotal = new JLabel(MoneyFormat.format(controller.getCart().getTotal()));
            lblTotal.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 28));
            lblTotal.setForeground(Branding.NAVY);
        }
        return lblTotal;
    }

    private JPanel getPnPayment() {
        if (pnPayment == null) {
            pnPayment = new JPanel();
            pnPayment.setBorder(BorderFactory.createCompoundBorder(titled("Payment"), new EmptyBorder(8, 12, 8, 12)));
            pnPayment.setLayout(new GridBagLayout());
            addRow(pnPayment, 0, getLblNationalId(), getTxtNationalId());
            addRow(pnPayment, 1, getLblCard(), getTxtCard());
        }
        return pnPayment;
    }

    private JLabel getLblNationalId() {
        if (lblNationalId == null) {
            lblNationalId = new JLabel("Shareholder ID:");
            lblNationalId.setDisplayedMnemonic('I');
            lblNationalId.setLabelFor(getTxtNationalId());
        }
        return lblNationalId;
    }

    private JTextField getTxtNationalId() {
        if (txtNationalId == null) {
            txtNationalId = new JTextField(20);
            txtNationalId.setToolTipText("Your national ID (the shareholder chosen in the portal)");
        }
        return txtNationalId;
    }

    private JLabel getLblCard() {
        if (lblCard == null) {
            lblCard = new JLabel("Credit card number:");
            lblCard.setDisplayedMnemonic('N');
            lblCard.setLabelFor(getTxtCard());
        }
        return lblCard;
    }

    private JTextField getTxtCard() {
        if (txtCard == null) {
            txtCard = new JTextField(20);
            txtCard.setToolTipText("13 to 19 digits; spaces and dashes are allowed");
        }
        return txtCard;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnCancel());
            pnButtons.add(getBtnConfirm());
        }
        return pnButtons;
    }

    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton("Cancel");
            btnCancel.setMnemonic('C');
            btnCancel.addActionListener(event -> dispose());
        }
        return btnCancel;
    }

    private JButton getBtnConfirm() {
        if (btnConfirm == null) {
            btnConfirm = new JButton("Confirm purchase");
            btnConfirm.setFont(btnConfirm.getFont().deriveFont(Font.BOLD));
            btnConfirm.setMnemonic('P');
            btnConfirm.addActionListener(event -> confirm());
        }
        return btnConfirm;
    }

    /** One row of a form: label and value. */
    private static void addRow(JPanel panel, int row, JLabel label, JComponent value) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.insets = new Insets(8, 0, 8, 16);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        panel.add(label, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(8, 0, 8, 0);
        panel.add(value, constraints);
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
    // Behaviour
    // ------------------------------------------------------------------

    private void confirm() {
        try {
            purchase = controller.checkout(getTxtNationalId().getText(), getTxtCard().getText());
        } catch (IllegalArgumentException exception) {
            // Wrong ID or card: the window stays open to correct them
            JOptionPane.showMessageDialog(this, exception.getMessage(), TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IllegalStateException exception) {
            // A share was bought by someone else: nothing was bought
            JOptionPane.showMessageDialog(this, exception.getMessage(), TITLE, JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "The purchase could not be completed:" + System.lineSeparator()
                    + exception.getMessage(), TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Purchase completed: " + purchase.getShareIds().size()
                + (purchase.getShareIds().size() == 1 ? " share" : " shares") + " for "
                + MoneyFormat.format(purchase.getTotal()) + "." + System.lineSeparator()
                + "They are now in your wallet. Share IDs: " + idRanges(purchase.getShareIds()),
                TITLE, JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    /** For example "1001-1020, 1301, 1305-1306". */
    private static String idRanges(List<Integer> ids) {
        List<Integer> sorted = ids.stream().sorted().toList();
        StringBuilder text = new StringBuilder();
        int start = sorted.get(0);
        int previous = start;
        for (int i = 1; i <= sorted.size(); i++) {
            Integer current = i < sorted.size() ? sorted.get(i) : null;
            if (current == null || current != previous + 1) {
                text.append(text.length() == 0 ? "" : ", ").append(start == previous ? start : start + "-" + previous);
                if (current != null) {
                    start = current;
                }
            }
            if (current != null) {
                previous = current;
            }
        }
        return text.toString();
    }
}
