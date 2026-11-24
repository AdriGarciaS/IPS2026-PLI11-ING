package com.ips2026.pl11.view.campaign;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ips2026.pl11.controller.campaign.ExternalSharePurchaseController;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignPurchaseLimit;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

/**
 * US 35828: an external user views the active campaign and chooses how many
 * shares to buy (only in phase 3, any number of the remaining shares). With
 * "Next..." they register as a shareholder and pay
 * ({@link RegisterShareholderDialog}).
 *
 * <p>It is opened from the login window. It is a modal dialog and follows
 * the lazy WindowBuilder style.</p>
 */
public class ExternalBuySharesDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient ExternalSharePurchaseController controller;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnCenter;
    private JPanel pnShares;
    private JLabel lblPhase;
    private JLabel lblRemaining;
    private JLabel lblMax;
    private JTextField txtQuantity;
    private JLabel lblRule;
    private JPanel pnSummary;
    private JLabel lblSummaryCampaign;
    private JLabel lblSummaryShares;
    private JLabel lblSummaryPrice;
    private JLabel lblTotal;
    private JPanel pnSouth;
    private JLabel lblStatus;
    private JPanel pnButtons;
    private JButton btnClose;
    private JButton btnNext;

    /**
     * @param controller controller already loaded with the active campaign
     * @param owner      window that opens it (the login window); it stays
     *                   blocked until this window is closed
     */
    public ExternalBuySharesDialog(ExternalSharePurchaseController controller, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setTitle("Buy Campaign Shares");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(820, 480));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);
        getRootPane().setDefaultButton(getBtnNext());

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

    /** "1. Shares" and "2. Summary" side by side. */
    private JPanel getPnCenter() {
        if (pnCenter == null) {
            pnCenter = new JPanel(new GridBagLayout());
            pnCenter.setOpaque(false);
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.insets = new Insets(0, 0, 0, 12);
            pnCenter.add(getPnShares(), constraints);

            constraints.gridx = 1;
            constraints.insets = new Insets(0, 0, 0, 0);
            pnCenter.add(getPnSummary(), constraints);
        }
        return pnCenter;
    }

    private JPanel getPnShares() {
        if (pnShares == null) {
            pnShares = new JPanel(new GridBagLayout());
            pnShares.setBorder(BorderFactory.createCompoundBorder(titled("1. Shares"), new EmptyBorder(6, 10, 6, 10)));
            addPair(pnShares, 0, "Current phase:", getLblPhase());
            addPair(pnShares, 1, "Remaining shares:", getLblRemaining());
            addPair(pnShares, 2, "Max. you can buy:", getLblMax());

            JLabel lblQuantity = new JLabel("Quantity:");
            lblQuantity.setDisplayedMnemonic('Q');
            lblQuantity.setLabelFor(getTxtQuantity());
            pnShares.add(lblQuantity, constraints(0, 3, 0, new Insets(10, 0, 4, 16)));
            pnShares.add(getTxtQuantity(), constraints(1, 3, 1, new Insets(10, 0, 4, 0)));

            GridBagConstraints rule = constraints(0, 4, 1, new Insets(10, 0, 4, 0));
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

    private JLabel getLblPhase() {
        if (lblPhase == null) {
            lblPhase = new JLabel();
        }
        return lblPhase;
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
            txtQuantity.getDocument().addDocumentListener(new DocumentListener() {
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
            });
        }
        return txtQuantity;
    }

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
            pnButtons.add(getBtnClose());
            pnButtons.add(getBtnNext());
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

    private JButton getBtnNext() {
        if (btnNext == null) {
            btnNext = new JButton("Next...");
            btnNext.setMnemonic('N');
            btnNext.setFont(btnNext.getFont().deriveFont(Font.BOLD));
            btnNext.addActionListener(event -> openRegistration());
        }
        return btnNext;
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

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    private void showCampaign() {
        Campaign campaign = controller.getCampaign();
        CampaignPurchaseLimit limit = controller.getLimit();
        String price = MoneyFormat.format(campaign.getNominalPrice());

        getPnHeader().setSubtitle(campaign.getName() + " — " + campaign.getStatus().getLabel() + " — "
                + price + " per share");
        getLblPhase().setText(campaign.getStatus().getLabel());
        getLblRemaining().setText(String.valueOf(limit.getRemainingShares()));
        getLblMax().setText(String.valueOf(limit.getMax()));
        getLblRule().setText(campaign.getStatus().allowsExternalPurchases()
                ? "Phase 3: you can buy any number of the remaining shares."
                : "External users can buy shares only in phase 3.");
        getLblSummaryCampaign().setText(campaign.getName());
        getLblSummaryPrice().setText(price);

        getTxtQuantity().setEnabled(limit.getMax() > 0);
        updateSummary();
    }

    /** Updates the summary and the total, the status message and the "Next..." button. */
    private void updateSummary() {
        Optional<String> problem = controller.findQuantityProblem(getTxtQuantity().getText());
        int quantity = problem.isEmpty() ? Integer.parseInt(getTxtQuantity().getText().strip()) : 0;
        getLblSummaryShares().setText(String.valueOf(quantity));
        getLblTotal().setText(MoneyFormat.format(controller.totalFor(quantity)));

        if (problem.isPresent()) {
            boolean hint = getTxtQuantity().getText().isBlank() && controller.getLimit().getMax() > 0;
            getLblStatus().setForeground(hint ? Branding.TEXT_MUTED : Branding.ERROR);
            getLblStatus().setText(problem.get());
        } else {
            getLblStatus().setForeground(Branding.SUCCESS);
            getLblStatus().setText("Press 'Next...' to register as a shareholder and pay.");
        }
        getBtnNext().setEnabled(problem.isEmpty());
    }

    /** Second step: registration and payment. If the purchase is done, this window is closed too. */
    private void openRegistration() {
        RegisterShareholderDialog registration =
                new RegisterShareholderDialog(controller, getTxtQuantity().getText().strip(), this);
        registration.setVisible(true);
        if (registration.isPurchaseCompleted()) {
            dispose();
        } else if (controller.getCampaign() == null) {
            dispose(); // the campaign was finished meanwhile
        } else {
            showCampaign();
        }
    }
}
