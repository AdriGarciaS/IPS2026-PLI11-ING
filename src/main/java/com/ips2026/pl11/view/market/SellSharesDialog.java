package com.ips2026.pl11.view.market;

import com.ips2026.pl11.controller.market.SellSharesController;
import com.ips2026.pl11.model.market.OwnedShare;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Sell Shares: the shareholder chooses shares of their wallet (filtered by
 * campaign, one by one or by a range of IDs) and puts them up for sale as
 * one listing, with a credit card to receive the money, a price per share
 * and, optionally, a pack price.
 *
 * <p>It is a modal dialog. It follows the lazy WindowBuilder style of the
 * main menu.</p>
 */
public class SellSharesDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String TITLE = "Sell Shares";
    private static final String ALL_CAMPAIGNS = "All campaigns";

    private final transient SellSharesController controller;
    /** The shares chosen, also the ones of campaigns not shown now. */
    private final Set<Integer> selected = new TreeSet<>();

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnCenter;
    private JPanel pnShares;
    private JPanel pnShareFilters;
    private JComboBox<String> cbCampaign;
    private JTextField txtRangeFrom;
    private JTextField txtRangeTo;
    private JButton btnSelectRange;
    private JScrollPane scrShares;
    private JPanel pnShareList;
    private JPanel pnSelection;
    private JLabel lblSelected;
    private JButton btnClearSelection;
    private JPanel pnDetails;
    private JTextField txtCard;
    private JTextField txtPrice;
    private JTextField txtPackPrice;
    private JLabel lblSum;
    private JLabel lblPack;
    private JLabel lblProblem;
    private JPanel pnButtons;
    private JButton btnClear;
    private JButton btnClose;
    private JButton btnPutUp;

    /**
     * @param controller controller with the wallet of the seller already loaded
     * @param owner      window that opens it (the shareholders portal); it stays blocked until this window is closed
     */
    public SellSharesDialog(SellSharesController controller, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setTitle(TITLE);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1100, 720, owner);

        showShares();
        updateSale();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(TITLE + " — " + controller.getSeller().getFullName(),
                    "Choose shares from your wallet and set the price");
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
            pnBody.add(getPnCenter(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    /** Shares on the left, sale details on the right. */
    private JPanel getPnCenter() {
        if (pnCenter == null) {
            pnCenter = new JPanel();
            pnCenter.setOpaque(false);
            pnCenter.setLayout(new GridLayout(1, 2, 14, 0));
            pnCenter.add(getPnShares());
            JPanel pnRight = new JPanel(new BorderLayout(0, 0));
            pnRight.setOpaque(false);
            pnRight.add(getPnDetails(), BorderLayout.NORTH);
            pnCenter.add(pnRight);
        }
        return pnCenter;
    }

    private JPanel getPnShares() {
        if (pnShares == null) {
            pnShares = new JPanel();
            pnShares.setBorder(BorderFactory.createCompoundBorder(titled("1. Shares to sell"), new EmptyBorder(6, 6, 6, 6)));
            pnShares.setLayout(new BorderLayout(0, 8));
            pnShares.add(getPnShareFilters(), BorderLayout.NORTH);
            pnShares.add(getScrShares(), BorderLayout.CENTER);
            pnShares.add(getPnSelection(), BorderLayout.SOUTH);
        }
        return pnShares;
    }

    /** Campaign and range of IDs. */
    private JPanel getPnShareFilters() {
        if (pnShareFilters == null) {
            pnShareFilters = new JPanel();
            pnShareFilters.setOpaque(false);
            pnShareFilters.setLayout(new GridBagLayout());

            JLabel lblCampaign = new JLabel("Campaign:");
            lblCampaign.setDisplayedMnemonic('G');
            lblCampaign.setLabelFor(getCbCampaign());
            GridBagConstraints constraints = constraints(0, 0);
            pnShareFilters.add(lblCampaign, constraints);
            constraints = constraints(1, 0);
            constraints.gridwidth = 4;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            pnShareFilters.add(getCbCampaign(), constraints);

            JLabel lblFrom = new JLabel("Range from");
            lblFrom.setDisplayedMnemonic('F');
            lblFrom.setLabelFor(getTxtRangeFrom());
            pnShareFilters.add(lblFrom, constraints(0, 1));
            constraints = constraints(1, 1);
            constraints.weightx = 0.5;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            pnShareFilters.add(getTxtRangeFrom(), constraints);
            pnShareFilters.add(new JLabel("to"), constraints(2, 1));
            constraints = constraints(3, 1);
            constraints.weightx = 0.5;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            pnShareFilters.add(getTxtRangeTo(), constraints);
            pnShareFilters.add(getBtnSelectRange(), constraints(4, 1));
        }
        return pnShareFilters;
    }

    private static GridBagConstraints constraints(int x, int y) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.insets = new Insets(4, 0, 4, 8);
        constraints.anchor = GridBagConstraints.WEST;
        return constraints;
    }

    private JComboBox<String> getCbCampaign() {
        if (cbCampaign == null) {
            cbCampaign = new JComboBox<>();
            cbCampaign.addItem(ALL_CAMPAIGNS);
            controller.getCampaigns().forEach(cbCampaign::addItem);
            cbCampaign.addActionListener(event -> showShares());
        }
        return cbCampaign;
    }

    private JTextField getTxtRangeFrom() {
        if (txtRangeFrom == null) {
            txtRangeFrom = new JTextField(6);
            txtRangeFrom.setToolTipText("First share ID of the range (included)");
        }
        return txtRangeFrom;
    }

    private JTextField getTxtRangeTo() {
        if (txtRangeTo == null) {
            txtRangeTo = new JTextField(6);
            txtRangeTo.setToolTipText("Last share ID of the range (included)");
        }
        return txtRangeTo;
    }

    private JButton getBtnSelectRange() {
        if (btnSelectRange == null) {
            btnSelectRange = new JButton("Select range");
            btnSelectRange.setMnemonic('R');
            btnSelectRange.setToolTipText("Checks the shares of the campaign with an ID in the range");
            btnSelectRange.addActionListener(event -> selectRange());
        }
        return btnSelectRange;
    }

    private JScrollPane getScrShares() {
        if (scrShares == null) {
            scrShares = new JScrollPane();
            scrShares.setViewportView(getPnShareList());
            scrShares.getVerticalScrollBar().setUnitIncrement(16);
        }
        return scrShares;
    }

    /** One check box per share of the campaign chosen (filled in {@link #showShares()}). */
    private JPanel getPnShareList() {
        if (pnShareList == null) {
            pnShareList = new JPanel();
            pnShareList.setBorder(new EmptyBorder(4, 8, 4, 8));
            pnShareList.setLayout(new GridLayout(0, 1, 0, 2));
        }
        return pnShareList;
    }

    private JPanel getPnSelection() {
        if (pnSelection == null) {
            pnSelection = new JPanel();
            pnSelection.setOpaque(false);
            pnSelection.setLayout(new BorderLayout(12, 0));
            pnSelection.add(getLblSelected(), BorderLayout.WEST);
            pnSelection.add(getBtnClearSelection(), BorderLayout.EAST);
        }
        return pnSelection;
    }

    private JLabel getLblSelected() {
        if (lblSelected == null) {
            lblSelected = new JLabel();
            lblSelected.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        }
        return lblSelected;
    }

    private JButton getBtnClearSelection() {
        if (btnClearSelection == null) {
            btnClearSelection = new JButton("Clear selection");
            btnClearSelection.setMnemonic('S');
            btnClearSelection.addActionListener(event -> {
                selected.clear();
                showShares();
                updateSale();
            });
        }
        return btnClearSelection;
    }

    private JPanel getPnDetails() {
        if (pnDetails == null) {
            pnDetails = new JPanel();
            pnDetails.setBorder(BorderFactory.createCompoundBorder(titled("2. Sale details"), new EmptyBorder(8, 12, 8, 12)));
            pnDetails.setLayout(new GridBagLayout());

            addField(0, "Credit card number:", 'N', getTxtCard(), null);
            addField(1, "Price per share:", 'P', getTxtPrice(), "€");
            addField(2, "Pack price (optional):", 'K', getTxtPackPrice(), "€");

            GridBagConstraints constraints = constraints(0, 3);
            constraints.gridwidth = 3;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(12, 0, 8, 0);
            pnDetails.add(new JSeparator(), constraints);

            addSummary(4, "Sum of individual prices:", getLblSum());
            addSummary(5, "Pack price:", getLblPack());

            constraints = constraints(0, 6);
            constraints.gridwidth = 3;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(12, 0, 0, 0);
            pnDetails.add(getLblProblem(), constraints);
        }
        return pnDetails;
    }

    /** One row of the sale details: label, field and an optional unit. */
    private void addField(int row, String text, char mnemonic, JTextField field, String unit) {
        JLabel label = new JLabel(text);
        label.setDisplayedMnemonic(mnemonic);
        label.setLabelFor(field);
        GridBagConstraints constraints = constraints(0, row);
        constraints.insets = new Insets(8, 0, 8, 16);
        pnDetails.add(label, constraints);

        constraints = constraints(1, row);
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(8, 0, 8, 8);
        pnDetails.add(field, constraints);

        if (unit != null) {
            pnDetails.add(mutedLabel(unit), constraints(2, row));
        }
    }

    /** One row of the summary: muted label on the left, bold value on the right. */
    private void addSummary(int row, String text, JLabel value) {
        pnDetails.add(mutedLabel(text), constraints(0, row));
        GridBagConstraints constraints = constraints(1, row);
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(4, 0, 4, 0);
        pnDetails.add(value, constraints);
    }

    private JTextField getTxtCard() {
        if (txtCard == null) {
            txtCard = detailField("Card where you receive the money: 13 to 19 digits (it is checked, not stored)");
        }
        return txtCard;
    }

    private JTextField getTxtPrice() {
        if (txtPrice == null) {
            txtPrice = detailField("Price of one share bought individually, for example 12,50");
        }
        return txtPrice;
    }

    private JTextField getTxtPackPrice() {
        if (txtPackPrice == null) {
            txtPackPrice = detailField("Price of all the shares together (empty = no pack); it does not need to match the sum");
        }
        return txtPackPrice;
    }

    /** A text field that updates the summary and the button while typing. */
    private JTextField detailField(String tooltip) {
        JTextField field = new JTextField(16);
        field.setToolTipText(tooltip);
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateSale();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateSale();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateSale();
            }
        });
        return field;
    }

    private JLabel getLblSum() {
        if (lblSum == null) {
            lblSum = boldValue();
        }
        return lblSum;
    }

    private JLabel getLblPack() {
        if (lblPack == null) {
            lblPack = boldValue();
        }
        return lblPack;
    }

    /** What is still missing to put the shares up for sale. */
    private JLabel getLblProblem() {
        if (lblProblem == null) {
            lblProblem = mutedLabel(" ");
            lblProblem.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 12));
        }
        return lblProblem;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnClear());
            pnButtons.add(getBtnClose());
            pnButtons.add(getBtnPutUp());
        }
        return pnButtons;
    }

    private JButton getBtnClear() {
        if (btnClear == null) {
            btnClear = new JButton("Clear");
            btnClear.setMnemonic('L');
            btnClear.setToolTipText("Clears the selection and the sale details");
            btnClear.addActionListener(event -> clearAll());
        }
        return btnClear;
    }

    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton("Close");
            btnClose.setMnemonic('C');
            btnClose.addActionListener(event -> dispose());
        }
        return btnClose;
    }

    private JButton getBtnPutUp() {
        if (btnPutUp == null) {
            btnPutUp = new JButton("Put up for sale");
            btnPutUp.setFont(btnPutUp.getFont().deriveFont(Font.BOLD));
            btnPutUp.setMnemonic('U');
            btnPutUp.addActionListener(event -> putUpForSale());
        }
        return btnPutUp;
    }

    private static JLabel boldValue() {
        JLabel label = new JLabel();
        label.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
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

    private String getCampaign() {
        Object campaign = getCbCampaign().getSelectedItem();
        return ALL_CAMPAIGNS.equals(campaign) ? null : (String) campaign;
    }

    /** One check box per share of the campaign; the shares already on sale are disabled. */
    private void showShares() {
        getPnShareList().removeAll();
        for (OwnedShare share : controller.getShares(getCampaign())) {
            int shareId = share.getShareId();
            JCheckBox box = new JCheckBox("ID " + shareId + (share.isOnSale() ? "  (already on sale)" : ""));
            box.setSelected(selected.contains(shareId));
            box.setEnabled(!share.isOnSale());
            box.addActionListener(event -> {
                if (box.isSelected()) {
                    selected.add(shareId);
                } else {
                    selected.remove(shareId);
                }
                updateSale();
            });
            getPnShareList().add(box);
        }
        getPnShareList().revalidate();
        getPnShareList().repaint();
    }

    private void selectRange() {
        List<Integer> ids;
        try {
            ids = controller.getIdsInRange(getCampaign(), getTxtRangeFrom().getText(), getTxtRangeTo().getText());
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ids.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no shares that can be sold in that range.",
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        selected.addAll(ids);
        showShares();
        updateSale();
    }

    /** Number of shares chosen, summary of prices, and whether they can be put up for sale. */
    private void updateSale() {
        getLblSelected().setText("Selected: " + selected.size() + (selected.size() == 1 ? " share" : " shares"));

        BigDecimal sum = controller.getSumOfPrices(selected.size(), getTxtPrice().getText());
        getLblSum().setText(sum == null ? "—" : MoneyFormat.format(sum));
        BigDecimal pack = controller.getSumOfPrices(1, getTxtPackPrice().getText());
        getLblPack().setText(getTxtPackPrice().getText().isBlank() ? "not offered"
                : pack == null ? "—" : MoneyFormat.format(pack));

        List<String> problems = controller.findProblems(selected, getTxtCard().getText(), getTxtPrice().getText(),
                getTxtPackPrice().getText());
        getBtnPutUp().setEnabled(problems.isEmpty());
        getLblProblem().setText(problems.isEmpty() ? "Ready to put up for sale." : problems.get(0));
        getLblProblem().setForeground(problems.isEmpty() ? Branding.SUCCESS : Branding.TEXT_MUTED);
    }

    private void clearAll() {
        selected.clear();
        getCbCampaign().setSelectedItem(ALL_CAMPAIGNS);
        getTxtRangeFrom().setText("");
        getTxtRangeTo().setText("");
        getTxtCard().setText("");
        getTxtPrice().setText("");
        getTxtPackPrice().setText("");
        showShares();
        updateSale();
    }

    private void putUpForSale() {
        int listingId;
        try {
            listingId = controller.putUpForSale(selected, getTxtCard().getText(), getTxtPrice().getText(),
                    getTxtPackPrice().getText());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "The sale could not be saved:" + System.lineSeparator()
                    + exception.getMessage(), TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        String pack = getTxtPackPrice().getText().isBlank() ? "no pack"
                : "pack " + getLblPack().getText();
        JOptionPane.showMessageDialog(this, "Sale #" + listingId + " put up for sale: " + selected.size()
                + (selected.size() == 1 ? " share" : " shares") + " at " + pricePerShareText() + " each (" + pack + ")."
                + System.lineSeparator() + "Other shareholders can now buy them in the share market.",
                TITLE, JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private String pricePerShareText() {
        BigDecimal price = controller.getSumOfPrices(1, getTxtPrice().getText());
        return price == null ? "" : MoneyFormat.format(price);
    }
}
