package com.ips2026.pl11.view.campaign;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ips2026.pl11.controller.campaign.BuyCampaignSharesController;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignPurchase;
import com.ips2026.pl11.model.campaign.CampaignPurchaseLimit;
import com.ips2026.pl11.model.campaign.CampaignStatus;
import com.ips2026.pl11.model.reservation.CardValidator;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

/**
 * US 35947: the shareholder who has logged in buys shares of the active
 * campaign (phase 1, 2 or 3). They type how many shares they want, see the
 * summary with the total price, and pay with a credit card. The shares go to
 * their wallet.
 *
 * <p>It is opened from the shareholder main menu only when there is a
 * campaign in phase 1, 2 or 3. It is a modal dialog and follows the lazy
 * WindowBuilder style.</p>
 */
public class BuyCampaignSharesDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient BuyCampaignSharesController controller;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnCenter;
    private JPanel pnShares;
    private JLabel lblShareholder;
    private JLabel lblOwned;
    private JLabel lblRemaining;
    private JLabel lblMax;
    private JTextField txtQuantity;
    private JLabel lblRule;
    private JPanel pnSummary;
    private JLabel lblSummaryCampaign;
    private JLabel lblSummaryShares;
    private JLabel lblSummaryPrice;
    private JLabel lblTotal;
    private JPanel pnPayment;
    private JTextField txtCard;
    private JPanel pnSouth;
    private JLabel lblStatus;
    private JPanel pnButtons;
    private JButton btnClear;
    private JButton btnClose;
    private JButton btnBuy;

    /**
     * @param controller controller already loaded with a campaign in phase 1, 2 or 3
     * @param owner      window that opens it (the shareholder main menu); it
     *                   stays blocked until this window is closed
     */
    public BuyCampaignSharesDialog(BuyCampaignSharesController controller, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setTitle("Buy Campaign Shares");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(820, 560));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);
        getRootPane().setDefaultButton(getBtnBuy());

        showCampaign();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Buy Campaign Shares", "");
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
            pnBody.add(getPnSouth(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    /** "1. Shares" and "2. Summary" side by side, and "3. Payment" below. */
    private JPanel getPnCenter() {
        if (pnCenter == null) {
            pnCenter = new JPanel(new GridBagLayout());
            pnCenter.setOpaque(false);
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.insets = new Insets(0, 0, 12, 12);
            pnCenter.add(getPnShares(), constraints);

            constraints.gridx = 1;
            constraints.insets = new Insets(0, 0, 12, 0);
            pnCenter.add(getPnSummary(), constraints);

            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.gridwidth = 2;
            constraints.weighty = 0;
            constraints.insets = new Insets(0, 0, 0, 0);
            pnCenter.add(getPnPayment(), constraints);
        }
        return pnCenter;
    }

    private JPanel getPnShares() {
        if (pnShares == null) {
            pnShares = new JPanel(new GridBagLayout());
            pnShares.setBorder(BorderFactory.createCompoundBorder(titled("1. Shares"), new EmptyBorder(6, 10, 6, 10)));
            addPair(pnShares, 0, "Shareholder:", getLblShareholder());
            addPair(pnShares, 1, "Shares you own:", getLblOwned());
            addPair(pnShares, 2, "Remaining shares:", getLblRemaining());
            addPair(pnShares, 3, "Max. you can buy:", getLblMax());

            JLabel lblQuantity = new JLabel("Quantity:");
            lblQuantity.setDisplayedMnemonic('Q');
            lblQuantity.setLabelFor(getTxtQuantity());
            pnShares.add(lblQuantity, constraints(0, 4, 0, new Insets(10, 0, 4, 16)));
            GridBagConstraints field = constraints(1, 4, 1, new Insets(10, 0, 4, 0));
            field.fill = GridBagConstraints.NONE;
            pnShares.add(getTxtQuantity(), field);

            GridBagConstraints rule = constraints(0, 5, 1, new Insets(10, 0, 4, 0));
            rule.gridwidth = 2;
            rule.weighty = 1;
            rule.anchor = GridBagConstraints.NORTHWEST;
            pnShares.add(getLblRule(), rule);
        }
        return pnShares;
    }

    private JPanel getPnSummary() {
        if (pnSummary == null) {
            pnSummary = new JPanel(new GridBagLayout());
            pnSummary.setBorder(BorderFactory.createCompoundBorder(titled("2. Summary"), new EmptyBorder(6, 10, 6, 10)));
            addPair(pnSummary, 0, "Campaign:", getLblSummaryCampaign());
            addPair(pnSummary, 1, "Shares:", getLblSummaryShares());
            addPair(pnSummary, 2, "Price per share:", getLblSummaryPrice());

            GridBagConstraints line = constraints(0, 3, 1, new Insets(10, 0, 10, 0));
            line.gridwidth = 2;
            line.fill = GridBagConstraints.HORIZONTAL;
            pnSummary.add(new JSeparator(), line);

            JLabel lblTotalCaption = new JLabel("TOTAL");
            lblTotalCaption.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 15));
            lblTotalCaption.setForeground(Branding.TEXT_MUTED);
            GridBagConstraints caption = constraints(0, 4, 0, new Insets(4, 0, 4, 16));
            caption.weighty = 1;
            caption.anchor = GridBagConstraints.NORTHWEST;
            pnSummary.add(lblTotalCaption, caption);
            GridBagConstraints total = constraints(1, 4, 1, new Insets(0, 0, 4, 0));
            total.weighty = 1;
            total.anchor = GridBagConstraints.NORTHEAST;
            pnSummary.add(getLblTotal(), total);
        }
        return pnSummary;
    }

    private JPanel getPnPayment() {
        if (pnPayment == null) {
            pnPayment = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            pnPayment.setBorder(BorderFactory.createCompoundBorder(titled("3. Payment"), new EmptyBorder(4, 0, 4, 10)));
            JLabel lblCard = new JLabel("Credit card number:");
            lblCard.setDisplayedMnemonic('N');
            lblCard.setLabelFor(getTxtCard());
            pnPayment.add(lblCard);
            pnPayment.add(getTxtCard());
        }
        return pnPayment;
    }

    private JLabel getLblShareholder() {
        if (lblShareholder == null) {
            lblShareholder = new JLabel();
        }
        return lblShareholder;
    }

    private JLabel getLblOwned() {
        if (lblOwned == null) {
            lblOwned = new JLabel();
        }
        return lblOwned;
    }

    private JLabel getLblRemaining() {
        if (lblRemaining == null) {
            lblRemaining = new JLabel();
        }
        return lblRemaining;
    }

    private JLabel getLblMax() {
        if (lblMax == null) {
            lblMax = new JLabel();
            lblMax.setForeground(Branding.NAVY);
        }
        return lblMax;
    }

    private JTextField getTxtQuantity() {
        if (txtQuantity == null) {
            txtQuantity = new JTextField(8);
            txtQuantity.setHorizontalAlignment(SwingConstants.RIGHT);
            txtQuantity.getDocument().addDocumentListener(onChange());
        }
        return txtQuantity;
    }

    /** Explanation of the limit of the current phase. */
    private JLabel getLblRule() {
        if (lblRule == null) {
            lblRule = new JLabel();
            lblRule.setForeground(Branding.TEXT_MUTED);
        }
        return lblRule;
    }

    private JLabel getLblSummaryCampaign() {
        if (lblSummaryCampaign == null) {
            lblSummaryCampaign = new JLabel();
        }
        return lblSummaryCampaign;
    }

    private JLabel getLblSummaryShares() {
        if (lblSummaryShares == null) {
            lblSummaryShares = new JLabel();
        }
        return lblSummaryShares;
    }

    private JLabel getLblSummaryPrice() {
        if (lblSummaryPrice == null) {
            lblSummaryPrice = new JLabel();
        }
        return lblSummaryPrice;
    }

    private JLabel getLblTotal() {
        if (lblTotal == null) {
            lblTotal = new JLabel();
            lblTotal.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 30));
            lblTotal.setForeground(Branding.NAVY);
        }
        return lblTotal;
    }

    private JTextField getTxtCard() {
        if (txtCard == null) {
            txtCard = new JTextField(24);
            txtCard.getDocument().addDocumentListener(onChange());
        }
        return txtCard;
    }

    /** Status message above the buttons, and the buttons. */
    private JPanel getPnSouth() {
        if (pnSouth == null) {
            pnSouth = new JPanel(new BorderLayout(0, 8));
            pnSouth.setOpaque(false);
            pnSouth.add(getLblStatus(), BorderLayout.NORTH);
            pnSouth.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnSouth;
    }

    private JLabel getLblStatus() {
        if (lblStatus == null) {
            lblStatus = new JLabel(" ");
        }
        return lblStatus;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnClear());
            pnButtons.add(getBtnClose());
            pnButtons.add(getBtnBuy());
        }
        return pnButtons;
    }

    private JButton getBtnClear() {
        if (btnClear == null) {
            btnClear = new JButton("Clear");
            btnClear.setMnemonic('L');
            btnClear.addActionListener(event -> clear());
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

    private JButton getBtnBuy() {
        if (btnBuy == null) {
            btnBuy = new JButton("Buy shares");
            btnBuy.setMnemonic('B');
            btnBuy.setFont(btnBuy.getFont().deriveFont(Font.BOLD));
            btnBuy.addActionListener(event -> buy());
        }
        return btnBuy;
    }

    private static TitledBorder titled(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        return border;
    }

    private static GridBagConstraints constraints(int column, int row, double weightx, Insets insets) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.weightx = weightx;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;
        return constraints;
    }

    /** A muted caption and its value in bold, in one row. */
    private static void addPair(JPanel panel, int row, String caption, JLabel value) {
        JLabel label = new JLabel(caption);
        label.setForeground(Branding.TEXT_MUTED);
        label.setLabelFor(value);
        panel.add(label, constraints(0, row, 0, new Insets(4, 0, 4, 16)));
        value.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        panel.add(value, constraints(1, row, 1, new Insets(4, 0, 4, 0)));
    }

    private DocumentListener onChange() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateSummary();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateSummary();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateSummary();
            }
        };
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    /** Shows the campaign and the limit of the shareholder (also after a purchase). */
    private void showCampaign() {
        Campaign campaign = controller.getCampaign();
        CampaignPurchaseLimit limit = controller.getLimit();
        String price = MoneyFormat.format(campaign.getNominalPrice());

        getPnHeader().setSubtitle(campaign.getName() + " — " + campaign.getStatus().getLabel() + " — "
                + price + " per share");
        getLblShareholder().setText(controller.getShareholder().getFullName());
        getLblOwned().setText(String.valueOf(limit.getOwnedShares()));
        getLblRemaining().setText(String.valueOf(limit.getRemainingShares()));
        getLblMax().setText(String.valueOf(limit.getMax()));
        getLblRule().setText(campaign.getStatus() == CampaignStatus.PHASE_1
                ? "Phase 1: you can buy up to the " + limit.getOwnedOutsideCampaign()
                        + " shares you own of other campaigns (bought in this one: " + limit.getBoughtInCampaign() + ")."
                : campaign.getStatus().getLabel() + ": you can buy any number of the remaining shares.");
        getLblSummaryCampaign().setText(campaign.getName());
        getLblSummaryPrice().setText(price);

        boolean canBuy = limit.getMax() > 0;
        getTxtQuantity().setEnabled(canBuy);
        getTxtCard().setEnabled(canBuy);
        updateSummary();
        if (canBuy) {
            getTxtQuantity().requestFocusInWindow();
        }
    }

    /** Updates the summary and the total, the status message and the "Buy shares" button. */
    private void updateSummary() {
        Optional<String> quantityProblem = controller.findQuantityProblem(getTxtQuantity().getText());
        int quantity = quantityProblem.isEmpty() ? Integer.parseInt(getTxtQuantity().getText().strip()) : 0;
        getLblSummaryShares().setText(String.valueOf(quantity));
        getLblTotal().setText(MoneyFormat.format(controller.totalFor(quantity)));

        Optional<String> problem = quantityProblem;
        boolean fieldEmpty = getTxtQuantity().getText().isBlank();
        if (problem.isEmpty()) {
            problem = CardValidator.findProblem(getTxtCard().getText());
            fieldEmpty = getTxtCard().getText().isBlank();
        }
        if (problem.isPresent()) {
            // A field not typed yet is a hint, not an error (except when nothing can be bought).
            boolean hint = fieldEmpty && controller.getLimit().getMax() > 0;
            getLblStatus().setForeground(hint ? Branding.TEXT_MUTED : Branding.ERROR);
            getLblStatus().setText(problem.get());
        } else {
            getLblStatus().setForeground(Branding.SUCCESS);
            getLblStatus().setText("Ready: press 'Buy shares' to pay " + MoneyFormat.format(controller.totalFor(quantity))
                    + " with the card " + CardValidator.mask(getTxtCard().getText()) + ".");
        }
        getBtnBuy().setEnabled(problem.isEmpty());
    }

    private void clear() {
        getTxtQuantity().setText("");
        getTxtCard().setText("");
        getTxtQuantity().requestFocusInWindow();
    }

    private void buy() {
        CampaignPurchase purchase;
        try {
            purchase = controller.buy(getTxtQuantity().getText(), getTxtCard().getText());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Buy Campaign Shares",
                    JOptionPane.WARNING_MESSAGE);
            afterPurchaseAttempt();
            return;
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "The shares could not be bought:\n" + exception.getMessage(),
                    "Buy Campaign Shares", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new CampaignPurchaseCompletedDialog(purchase, this).setVisible(true);
        clear();
        afterPurchaseAttempt();
    }

    /** Shows the new limit, or closes the window if the campaign can no longer sell shares. */
    private void afterPurchaseAttempt() {
        if (controller.getCampaign() == null) {
            JOptionPane.showMessageDialog(this, "The campaign is no longer open for purchases.",
                    "Buy Campaign Shares", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }
        showCampaign();
    }
}
