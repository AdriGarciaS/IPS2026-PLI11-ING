package com.ips2026.pl11.view.campaign;

import com.ips2026.pl11.controller.campaign.CampaignController;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignStatus;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * US 35778: the manager moves the open campaign forward through its phases
 * (inactive, phase 1, phase 2, phase 3, finished). Phases never go back and
 * every change is recorded.
 *
 * <p>It is opened from "Shareholder Campaigns" only when there is an open
 * campaign. It is a modal dialog and follows the lazy WindowBuilder style.</p>
 */
public class CampaignPhasesDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient CampaignController controller;
    private transient Campaign campaign;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnCenter;
    private JPanel pnCampaign;
    private JLabel lblName;
    private JLabel lblPrice;
    private JLabel lblTotal;
    private JLabel lblSold;
    private JLabel lblRemaining;
    private JLabel lblPhase;
    private JPanel pnPhase;
    private PhaseStepper phaseStepper;
    private JLabel lblNow;
    private JLabel lblNext;
    private JPanel pnMove;
    private JButton btnMove;
    private JPanel pnButtons;
    private JButton btnClose;

    /**
     * @param controller controller of the campaigns
     * @param campaign   the open campaign (not finished)
     * @param owner      window that opens it ("Shareholder Campaigns"); it stays
     *                   blocked until this window is closed
     */
    public CampaignPhasesDialog(CampaignController controller, Campaign campaign, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.campaign = campaign;

        setTitle("Campaign Phases");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 560));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 900, 620, owner); // smaller than usual: it only shows one campaign

        showCampaign();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Campaign Phases", "Move the open campaign forward; phases never go back");
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
            pnCenter.setLayout(new BoxLayout(pnCenter, BoxLayout.Y_AXIS));
            pnCenter.add(getPnCampaign());
            pnCenter.add(Box.createVerticalStrut(12));
            pnCenter.add(getPnPhase());
            pnCenter.add(Box.createVerticalStrut(18));
            pnCenter.add(getPnMove());
        }
        return pnCenter;
    }

    /** Data of the open campaign, in two columns like the prototype. */
    private JPanel getPnCampaign() {
        if (pnCampaign == null) {
            pnCampaign = new JPanel(new GridBagLayout());
            pnCampaign.setBorder(BorderFactory.createCompoundBorder(titled("Open campaign"), new EmptyBorder(6, 10, 6, 10)));
            addPair(pnCampaign, 0, 0, "Name:", getLblName());
            addPair(pnCampaign, 0, 1, "Nominal price:", getLblPrice());
            addPair(pnCampaign, 0, 2, "Total shares:", getLblTotal());
            addPair(pnCampaign, 2, 0, "Shares sold:", getLblSold());
            addPair(pnCampaign, 2, 1, "Remaining shares:", getLblRemaining());
            addPair(pnCampaign, 2, 2, "Current phase:", getLblPhase());
        }
        return pnCampaign;
    }

    private static void addPair(JPanel panel, int column, int row, String caption, JLabel value) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 0, 4, 16);
        JLabel label = new JLabel(caption);
        label.setForeground(Branding.TEXT_MUTED);
        label.setLabelFor(value);
        panel.add(label, constraints);

        constraints.gridx = column + 1;
        constraints.weightx = 1;
        constraints.insets = new Insets(4, 0, 4, 24);
        value.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        panel.add(value, constraints);
    }

    private JLabel getLblName() {
        if (lblName == null) {
            lblName = new JLabel();
        }
        return lblName;
    }

    private JLabel getLblPrice() {
        if (lblPrice == null) {
            lblPrice = new JLabel();
        }
        return lblPrice;
    }

    private JLabel getLblTotal() {
        if (lblTotal == null) {
            lblTotal = new JLabel();
        }
        return lblTotal;
    }

    private JLabel getLblSold() {
        if (lblSold == null) {
            lblSold = new JLabel();
        }
        return lblSold;
    }

    private JLabel getLblRemaining() {
        if (lblRemaining == null) {
            lblRemaining = new JLabel();
        }
        return lblRemaining;
    }

    private JLabel getLblPhase() {
        if (lblPhase == null) {
            lblPhase = new JLabel();
            lblPhase.setForeground(Branding.NAVY);
        }
        return lblPhase;
    }

    /** The line of phases and what the current and the next phase allow. */
    private JPanel getPnPhase() {
        if (pnPhase == null) {
            pnPhase = new JPanel(new GridBagLayout());
            pnPhase.setBorder(BorderFactory.createCompoundBorder(titled("Phase"), new EmptyBorder(6, 10, 6, 10)));

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(4, 0, 10, 0);
            pnPhase.add(getPhaseStepper(), constraints);

            addRule(pnPhase, 1, "Now:", getLblNow());
            addRule(pnPhase, 2, "Next:", getLblNext());
        }
        return pnPhase;
    }

    private static void addRule(JPanel panel, int row, String caption, JLabel value) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 0, 4, 12);
        JLabel label = new JLabel(caption);
        label.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        panel.add(label, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(value, constraints);
    }

    private PhaseStepper getPhaseStepper() {
        if (phaseStepper == null) {
            phaseStepper = new PhaseStepper();
        }
        return phaseStepper;
    }

    private JLabel getLblNow() {
        if (lblNow == null) {
            lblNow = new JLabel();
        }
        return lblNow;
    }

    private JLabel getLblNext() {
        if (lblNext == null) {
            lblNext = new JLabel();
            lblNext.setForeground(Branding.TEXT_MUTED);
        }
        return lblNext;
    }

    /** The main action, centered and large as in the prototype. */
    private JPanel getPnMove() {
        if (pnMove == null) {
            pnMove = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            pnMove.setOpaque(false);
            pnMove.add(getBtnMove());
        }
        return pnMove;
    }

    private JButton getBtnMove() {
        if (btnMove == null) {
            btnMove = new JButton();
            btnMove.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 18));
            btnMove.setPreferredSize(new Dimension(360, 56));
            btnMove.addActionListener(event -> moveToNextPhase());
        }
        return btnMove;
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

    private static TitledBorder titled(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        return border;
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    /** Fills the window with the campaign as it is now. */
    private void showCampaign() {
        CampaignStatus status = campaign.getStatus();
        CampaignStatus next = status.next();

        getLblName().setText(campaign.getName());
        getLblPrice().setText(MoneyFormat.format(campaign.getNominalPrice()) + " per share");
        getLblTotal().setText(String.valueOf(campaign.getTotalShares()));
        try {
            int sold = controller.countSoldShares(campaign);
            getLblSold().setText(String.valueOf(sold));
            getLblRemaining().setText(String.valueOf(Math.max(0, campaign.getTotalShares() - sold)));
        } catch (SQLException exception) {
            getLblSold().setText("?");
            getLblRemaining().setText("?");
            JOptionPane.showMessageDialog(this, "The shares sold could not be loaded:" + System.lineSeparator()
                    + exception.getMessage(), "Campaign Phases", JOptionPane.ERROR_MESSAGE);
        }
        getLblPhase().setText(status.getLabel().toUpperCase());

        getPhaseStepper().setCurrent(status);
        getLblNow().setText(status.getRule());
        if (next == null) {
            getLblNext().setText("The campaign is finished: it cannot be reactivated.");
            getBtnMove().setText("Campaign finished");
            getBtnMove().setMnemonic(0);
            getBtnMove().setEnabled(false);
        } else {
            getLblNext().setText(next.getLabel() + " — " + next.getRule());
            boolean finish = next == CampaignStatus.FINISHED;
            getBtnMove().setText(finish ? "Finish campaign" : "Move to " + next.getLabel() + "  →");
            getBtnMove().setMnemonic(finish ? 'F' : 'M');
            getBtnMove().setEnabled(true);
        }
    }

    /** Asks for confirmation and moves the campaign to the next phase. */
    private void moveToNextPhase() {
        CampaignStatus next = campaign.getStatus().next();
        if (next == null) {
            return;
        }
        String question = next == CampaignStatus.FINISHED
                ? "Finish campaign '" + campaign.getName() + "'?" + System.lineSeparator()
                        + "No more shares can be bought and it cannot be reactivated."
                : "Move campaign '" + campaign.getName() + "' from " + campaign.getStatus().getLabel() + " to "
                        + next.getLabel() + "?" + System.lineSeparator()
                        + next.getLabel() + ": " + next.getRule() + "." + System.lineSeparator()
                        + "Phases never go back.";
        int answer = JOptionPane.showConfirmDialog(this, question, "Campaign Phases",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            campaign = controller.moveToNextPhase(campaign);
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Campaign Phases", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "The phase could not be changed:" + System.lineSeparator()
                    + exception.getMessage(), "Campaign Phases", JOptionPane.ERROR_MESSAGE);
            return;
        }
        showCampaign();
    }
}
