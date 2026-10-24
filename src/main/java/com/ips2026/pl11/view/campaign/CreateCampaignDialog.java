package com.ips2026.pl11.view.campaign;

import com.ips2026.pl11.controller.campaign.CampaignController;
import com.ips2026.pl11.model.campaign.Campaign;
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
 * Form to create a new shareholder campaign: name, nominal price per share
 * and total number of shares offered. The campaign is always created
 * inactive.
 *
 * <p>It is a modal dialog. It follows the lazy WindowBuilder style of the
 * main menu.</p>
 */
public class CreateCampaignDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String TITLE = "Create Campaign";

    private final transient CampaignController controller;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnForm;
    private JLabel lblName;
    private JTextField txtName;
    private JLabel lblPrice;
    private JTextField txtPrice;
    private JLabel lblPriceUnit;
    private JLabel lblShares;
    private JTextField txtShares;
    private JPanel pnButtons;
    private JButton btnClear;
    private JButton btnClose;
    private JButton btnCreate;

    /**
     * @param controller campaigns controller
     * @param owner      window that opens it; it stays blocked until this
     *                   window is closed
     */
    public CreateCampaignDialog(CampaignController controller, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setTitle(TITLE);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(480, 400));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        getRootPane().setDefaultButton(getBtnCreate());
        Branding.setInitialSize(this, 560, 340, owner); // small: it only has three fields
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(TITLE, "New share capital campaign; it is created inactive");
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
            pnBody.add(getPnForm(), BorderLayout.NORTH);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnForm() {
        if (pnForm == null) {
            pnForm = new JPanel();
            pnForm.setBorder(BorderFactory.createCompoundBorder(titled("New campaign"), new EmptyBorder(8, 8, 8, 8)));
            pnForm.setLayout(new GridBagLayout());
            addRow(0, getLblName(), getTxtName(), null);
            addRow(1, getLblPrice(), getTxtPrice(), getLblPriceUnit());
            addRow(2, getLblShares(), getTxtShares(), null);
        }
        return pnForm;
    }

    /** One row of the form: label, field and, optionally, a text after the field. */
    private void addRow(int row, JLabel label, JTextField field, JComponent after) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.insets = new Insets(6, 4, 6, 12);
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        pnForm.add(label, constraints);

        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        if (after == null) {
            constraints.gridwidth = 2;
        }
        pnForm.add(field, constraints);

        if (after != null) {
            constraints.gridx = 2;
            constraints.fill = GridBagConstraints.NONE;
            constraints.weightx = 0;
            pnForm.add(after, constraints);
        }
    }

    private JLabel getLblName() {
        if (lblName == null) {
            lblName = new JLabel("Campaign name:");
            lblName.setDisplayedMnemonic('N');
            lblName.setLabelFor(getTxtName());
        }
        return lblName;
    }

    private JTextField getTxtName() {
        if (txtName == null) {
            txtName = new JTextField(30);
            txtName.setToolTipText("Unique name of the campaign, for example \"Capital increase 2027\"");
        }
        return txtName;
    }

    private JLabel getLblPrice() {
        if (lblPrice == null) {
            lblPrice = new JLabel("Nominal price per share:");
            lblPrice.setDisplayedMnemonic('P');
            lblPrice.setLabelFor(getTxtPrice());
        }
        return lblPrice;
    }

    private JTextField getTxtPrice() {
        if (txtPrice == null) {
            txtPrice = new JTextField(10);
            txtPrice.setToolTipText("Price of one share, greater than 0 and with at most 2 decimals (for example 10,50)");
        }
        return txtPrice;
    }

    private JLabel getLblPriceUnit() {
        if (lblPriceUnit == null) {
            lblPriceUnit = new JLabel("€");
            lblPriceUnit.setForeground(Branding.TEXT_MUTED);
        }
        return lblPriceUnit;
    }

    private JLabel getLblShares() {
        if (lblShares == null) {
            lblShares = new JLabel("Total shares offered:");
            lblShares.setDisplayedMnemonic('S');
            lblShares.setLabelFor(getTxtShares());
        }
        return lblShares;
    }

    private JTextField getTxtShares() {
        if (txtShares == null) {
            txtShares = new JTextField(10);
            txtShares.setToolTipText("Whole number of shares for sale, at least 1");
        }
        return txtShares;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnClear());
            pnButtons.add(getBtnClose());
            pnButtons.add(getBtnCreate());
        }
        return pnButtons;
    }

    private JButton getBtnClear() {
        if (btnClear == null) {
            btnClear = new JButton("Clear");
            btnClear.setMnemonic('L');
            btnClear.addActionListener(event -> clearForm());
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

    private JButton getBtnCreate() {
        if (btnCreate == null) {
            btnCreate = new JButton("Create");
            btnCreate.setFont(btnCreate.getFont().deriveFont(Font.BOLD));
            btnCreate.setMnemonic('R');
            btnCreate.addActionListener(event -> createCampaign());
        }
        return btnCreate;
    }

    private static TitledBorder titled(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        return border;
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    private void createCampaign() {
        Campaign campaign;
        try {
            campaign = controller.create(getTxtName().getText(), getTxtPrice().getText(), getTxtShares().getText());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), TITLE, JOptionPane.WARNING_MESSAGE);
            return;
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The campaign could not be saved:" + System.lineSeparator() + exception.getMessage(),
                    TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Campaign '" + campaign.getName() + "' created: " + campaign.getTotalShares() + " shares at "
                        + MoneyFormat.format(campaign.getNominalPrice()) + " each ("
                        + campaign.getStatus().getLabel() + ").",
                TITLE, JOptionPane.INFORMATION_MESSAGE);
        // The new campaign is now the open one, so no other can be created: the window closes
        dispose();
    }

    private void clearForm() {
        getTxtName().setText("");
        getTxtPrice().setText("");
        getTxtShares().setText("");
        getTxtName().requestFocusInWindow();
    }
}
