package com.ips2026.pl11.view.campaign;

import com.ips2026.pl11.controller.campaign.CampaignController;
import com.ips2026.pl11.data.campaign.CampaignDAO;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MenuCardButton;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Shareholder campaigns: the manager chooses one of the campaign options.
 * New campaign options are added as cards in "Options".
 *
 * <p>It is a modal dialog: the main menu is blocked while it is open. It
 * follows the lazy WindowBuilder style of the main menu.</p>
 */
public class CampaignsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String COMING_SOON = "Coming soon";
    private static final String PHASES_DESCRIPTION = "Move the open campaign to its next phase";

    private final transient CampaignController controller = new CampaignController(new CampaignDAO());

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnOptions;
    private MenuCardButton btnCreate;
    private MenuCardButton btnPhases;
    private MenuCardButton btnOption3;
    private JPanel pnButtons;
    private JButton btnClose;

    /**
     * @param owner window that opens it (the main menu); it stays blocked
     *              until this window is closed
     */
    public CampaignsDialog(Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);

        setTitle("Shareholder Campaigns");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(420, 460));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 480, 540, owner); // small: it only has three options

        refreshPhasesOption();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Shareholder Campaigns", "Create and manage share capital campaigns");
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
            pnBody.add(getPnOptions(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnOptions() {
        if (pnOptions == null) {
            pnOptions = new JPanel();
            pnOptions.setBorder(BorderFactory.createCompoundBorder(titled("Options"), new EmptyBorder(6, 6, 6, 6)));
            pnOptions.setLayout(new GridLayout(3, 1, 0, 12));
            pnOptions.add(getBtnCreate());
            pnOptions.add(getBtnPhases());
            pnOptions.add(getBtnOption3());
        }
        return pnOptions;
    }

    private MenuCardButton getBtnCreate() {
        if (btnCreate == null) {
            btnCreate = new MenuCardButton("Create Campaign", "Start a new share campaign");
            btnCreate.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnCreate.setMnemonic('R');
            btnCreate.addActionListener(event -> openCreateCampaign());
        }
        return btnCreate;
    }

    /** US 35778: only enabled while there is an open campaign (see {@link #refreshPhasesOption()}). */
    private MenuCardButton getBtnPhases() {
        if (btnPhases == null) {
            btnPhases = new MenuCardButton("Campaign Phases", PHASES_DESCRIPTION);
            btnPhases.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnPhases.setMnemonic('P');
            btnPhases.addActionListener(event -> openCampaignPhases());
        }
        return btnPhases;
    }

    private MenuCardButton getBtnOption3() {
        if (btnOption3 == null) {
            btnOption3 = new MenuCardButton("Option 3", COMING_SOON);
            btnOption3.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption3.setEnabled(false);
        }
        return btnOption3;
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

    /** Opens the form, unless there is already a campaign that is not finished. */
    private void openCreateCampaign() {
        Optional<Campaign> open;
        try {
            open = controller.findOpenCampaign();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The campaigns could not be loaded:" + System.lineSeparator() + exception.getMessage(),
                    "Create Campaign", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (open.isPresent()) {
            JOptionPane.showMessageDialog(this,
                    "Campaign '" + open.get().getName() + "' is still open (" + open.get().getStatus().getLabel()
                            + ")." + System.lineSeparator() + "Finish it before creating a new one.",
                    "Create Campaign", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new CreateCampaignDialog(controller, this).setVisible(true);
        refreshPhasesOption(); // the new campaign is open: its phases can be changed now
    }

    /** US 35778: "Campaign Phases" can only be opened while there is an open campaign. */
    private void refreshPhasesOption() {
        boolean open;
        try {
            open = controller.findOpenCampaign().isPresent();
        } catch (SQLException exception) {
            open = false;
        }
        getBtnPhases().setEnabled(open);
        getBtnPhases().setText("Campaign Phases", open ? PHASES_DESCRIPTION : "There is no open campaign");
    }

    /** US 35778: opens the phases of the open campaign. */
    private void openCampaignPhases() {
        Optional<Campaign> open;
        try {
            open = controller.findOpenCampaign();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The campaigns could not be loaded:" + System.lineSeparator() + exception.getMessage(),
                    "Campaign Phases", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (open.isEmpty()) {
            refreshPhasesOption();
            return;
        }
        new CampaignPhasesDialog(controller, open.get(), this).setVisible(true);
        refreshPhasesOption(); // after finishing the campaign there is no open campaign
    }
}
