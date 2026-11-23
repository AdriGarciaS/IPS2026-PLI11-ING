package com.ips2026.pl11.view.campaign;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.ips2026.pl11.model.campaign.CampaignPurchase;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

/**
 * US 35947: details of the campaign shares just bought (shareholder,
 * campaign, shares with their IDs, and total paid). Modal dialog in the lazy
 * WindowBuilder style.
 */
public class CampaignPurchaseCompletedDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient CampaignPurchase purchase;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnDetails;
    private JPanel pnButtons;
    private JButton btnOk;

    /**
     * @param purchase the purchase done
     * @param owner    window that opens it ("Buy Campaign Shares")
     */
    public CampaignPurchaseCompletedDialog(CampaignPurchase purchase, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.purchase = purchase;

        setTitle("Purchase Completed");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(520, 340));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 640, 400, owner); // smaller than usual: it only shows a summary
        getRootPane().setDefaultButton(getBtnOk());
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Purchase Completed", "The shares are now in your wallet");
        }
        return pnHeader;
    }

    private JPanel getPnBody() {
        if (pnBody == null) {
            pnBody = new JPanel();
            pnBody.setOpaque(false);
            pnBody.setBorder(new EmptyBorder(14, 14, 14, 14));
            pnBody.setLayout(new BorderLayout(0, 12));
            pnBody.add(getPnDetails(), BorderLayout.NORTH);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnDetails() {
        if (pnDetails == null) {
            pnDetails = new JPanel(new GridBagLayout());
            TitledBorder border = BorderFactory.createTitledBorder("Details");
            border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            pnDetails.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(6, 10, 6, 10)));
            addPair(0, "Shareholder:", purchase.getShareholderName());
            addPair(1, "Campaign:", purchase.getCampaignName());
            addPair(2, "Shares bought:", sharesText());
            addPair(3, "Total paid:", MoneyFormat.format(purchase.getTotal()));
        }
        return pnDetails;
    }

    /** For example "20 (IDs 3101 - 3120)", or "1 (ID 3101)". */
    private String sharesText() {
        if (purchase.getQuantity() == 1) {
            return "1 (ID " + purchase.getFirstShareId() + ")";
        }
        return purchase.getQuantity() + " (IDs " + purchase.getFirstShareId() + " - " + purchase.getLastShareId() + ")";
    }

    private void addPair(int row, String caption, String value) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 0, 4, 24);
        JLabel label = new JLabel(caption);
        label.setForeground(Branding.TEXT_MUTED);
        pnDetails.add(label, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        pnDetails.add(valueLabel, constraints);
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnOk());
        }
        return pnButtons;
    }

    private JButton getBtnOk() {
        if (btnOk == null) {
            btnOk = new JButton("OK");
            btnOk.setMnemonic('O');
            btnOk.setFont(btnOk.getFont().deriveFont(Font.BOLD));
            btnOk.addActionListener(event -> dispose());
        }
        return btnOk;
    }
}
