package com.ips2026.pl11.view.market;

import com.ips2026.pl11.model.market.ShareListing;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Choice of individual shares of a listing of the share market: one check
 * box per available share. The shares already in the cart appear checked
 * and disabled. After "Add to cart", {@link #getChosenShareIds()} gives the
 * shares chosen (empty if the window was cancelled).
 *
 * <p>It is a modal dialog. It follows the lazy WindowBuilder style of the
 * main menu.</p>
 */
public class ChooseSharesDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient ShareListing listing;
    private final transient Set<Integer> idsInCart;
    private final List<JCheckBox> shareBoxes = new ArrayList<>();
    private transient List<Integer> chosenShareIds = List.of();

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnShares;
    private JScrollPane scrShares;
    private JPanel pnShareList;
    private JPanel pnSelection;
    private JCheckBox chkSelectAll;
    private JLabel lblSelected;
    private JPanel pnButtons;
    private JButton btnCancel;
    private JButton btnAdd;

    /**
     * @param listing   the listing whose shares are chosen
     * @param idsInCart IDs of the shares of this listing that are already in the cart
     * @param owner     window that opens it; it stays blocked until this window is closed
     */
    public ChooseSharesDialog(ShareListing listing, Set<Integer> idsInCart, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.listing = listing;
        this.idsInCart = idsInCart;

        setTitle("Choose Shares");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(440, 460));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        getRootPane().setDefaultButton(getBtnAdd());
        Branding.setInitialSize(this, 520, 620, owner); // small: only the shares of one listing

        updateSelection();
    }

    /** The shares chosen to add to the cart (not the ones that were already in it); empty if cancelled. */
    public List<Integer> getChosenShareIds() {
        return chosenShareIds;
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Choose Shares", listing.getSellerName() + " — " + listing.getAvailableShares()
                    + " shares at " + MoneyFormat.format(listing.getPricePerShare()) + " each");
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
            pnBody.add(getPnShares(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnShares() {
        if (pnShares == null) {
            pnShares = new JPanel();
            pnShares.setBorder(BorderFactory.createCompoundBorder(titled("Available shares"), new EmptyBorder(6, 6, 6, 6)));
            pnShares.setLayout(new BorderLayout(0, 8));
            pnShares.add(getScrShares(), BorderLayout.CENTER);
            pnShares.add(getPnSelection(), BorderLayout.SOUTH);
        }
        return pnShares;
    }

    private JScrollPane getScrShares() {
        if (scrShares == null) {
            scrShares = new JScrollPane();
            scrShares.setViewportView(getPnShareList());
            scrShares.getVerticalScrollBar().setUnitIncrement(16);
        }
        return scrShares;
    }

    /** One check box per available share; the ones in the cart are checked and disabled. */
    private JPanel getPnShareList() {
        if (pnShareList == null) {
            pnShareList = new JPanel();
            pnShareList.setBorder(new EmptyBorder(4, 8, 4, 8));
            pnShareList.setLayout(new GridLayout(0, 1, 0, 2));
            for (int shareId : listing.getAvailableShareIds()) {
                JCheckBox box = new JCheckBox("ID " + shareId);
                box.putClientProperty("shareId", shareId);
                if (idsInCart.contains(shareId)) {
                    box.setSelected(true);
                    box.setEnabled(false);
                    box.setToolTipText("Already in the cart");
                }
                box.addActionListener(event -> updateSelection());
                shareBoxes.add(box);
                pnShareList.add(box);
            }
        }
        return pnShareList;
    }

    private JPanel getPnSelection() {
        if (pnSelection == null) {
            pnSelection = new JPanel();
            pnSelection.setOpaque(false);
            pnSelection.setLayout(new BorderLayout(12, 0));
            pnSelection.add(getChkSelectAll(), BorderLayout.WEST);
            pnSelection.add(getLblSelected(), BorderLayout.EAST);
        }
        return pnSelection;
    }

    private JCheckBox getChkSelectAll() {
        if (chkSelectAll == null) {
            chkSelectAll = new JCheckBox("Select all");
            chkSelectAll.setMnemonic('S');
            chkSelectAll.addActionListener(event -> {
                for (JCheckBox box : shareBoxes) {
                    if (box.isEnabled()) {
                        box.setSelected(chkSelectAll.isSelected());
                    }
                }
                updateSelection();
            });
        }
        return chkSelectAll;
    }

    private JLabel getLblSelected() {
        if (lblSelected == null) {
            lblSelected = new JLabel();
            lblSelected.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        }
        return lblSelected;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnCancel());
            pnButtons.add(getBtnAdd());
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

    private JButton getBtnAdd() {
        if (btnAdd == null) {
            btnAdd = new JButton("Add to cart");
            btnAdd.setFont(btnAdd.getFont().deriveFont(Font.BOLD));
            btnAdd.setMnemonic('A');
            btnAdd.addActionListener(event -> {
                chosenShareIds = newlySelected();
                dispose();
            });
        }
        return btnAdd;
    }

    private static TitledBorder titled(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        return border;
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    /** The shares checked now that were not already in the cart. */
    private List<Integer> newlySelected() {
        List<Integer> ids = new ArrayList<>();
        for (JCheckBox box : shareBoxes) {
            if (box.isEnabled() && box.isSelected()) {
                ids.add((Integer) box.getClientProperty("shareId"));
            }
        }
        return ids;
    }

    /** Number of shares chosen and their subtotal; "Add to cart" needs at least one. */
    private void updateSelection() {
        int selected = newlySelected().size();
        BigDecimal subtotal = listing.getPricePerShare().multiply(BigDecimal.valueOf(selected));
        getLblSelected().setText("Selected: " + selected + "  ·  Subtotal: " + MoneyFormat.format(subtotal));
        getBtnAdd().setEnabled(selected > 0);
        boolean allSelected = shareBoxes.stream().filter(JCheckBox::isEnabled).allMatch(JCheckBox::isSelected);
        getChkSelectAll().setSelected(allSelected && shareBoxes.stream().anyMatch(JCheckBox::isEnabled));
    }
}
