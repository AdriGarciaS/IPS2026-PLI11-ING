package com.ips2026.pl11.view.menu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import com.ips2026.pl11.controller.PasswordEncrypting;
import com.ips2026.pl11.controller.campaign.ExternalSharePurchaseController;
import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.controller.shareholder.ShareholderPortalController;
import com.ips2026.pl11.data.campaign.CampaignDAO;
import com.ips2026.pl11.data.campaign.CampaignPurchaseDAO;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.shareholder.ShareholderDAO;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.shareholder.Shareholder;
import com.ips2026.pl11.view.campaign.ExternalBuySharesDialog;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;
import com.ips2026.pl11.view.menu.mainmenus.CoachMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.DirectorOfCommsMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.DoctorMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.FacilityManagerMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.GardenerMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.GeneralManagerMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.PlayerMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.PressOfficerMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.ShareholderMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.SocialNetworksMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.StoreEmployeeMenuWindow;
import com.ips2026.pl11.view.menu.mainmenus.TicketSellerMenuWindow;

/**
 * First window of the application, split in two zones:
 * <ul>
 *   <li>left: employees and shareholders log in with their username and
 *       password, and the menu of their role is opened;</li>
 *   <li>right (US 35828): external users who want to become shareholders
 *       see the active campaign and can buy shares of it in phase 3.</li>
 * </ul>
 */
public class LoginWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private final transient EmployeeController employeeController = new EmployeeController(new EmployeeDAO());
    private final transient ShareholderPortalController shareholderController =
            new ShareholderPortalController(new ShareholderDAO());

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnLogin;
    private JTextField txtUsername;
    private JPasswordField pswPassword;
    private JButton btnLogin;
    private JPanel pnExternal;
    private JLabel lblExternalText;
    private JLabel lblCampaign;
    private JLabel lblCampaignPhase;
    private JButton btnBuyShares;
    private JPanel pnFooter;
    private JLabel lblFooter;

    public LoginWindow() {
        setTitle("Football Club Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 420));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        Branding.setInitialSize(this, 900, 460, null);
        getRootPane().setDefaultButton(getBtnLogin());

        showActiveCampaign();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Football Club Management", "Log in, or become a shareholder of the club");
        }
        return pnHeader;
    }

    /** The two zones, with a vertical line between them. */
    private JPanel getPnBody() {
        if (pnBody == null) {
            pnBody = new JPanel(new GridBagLayout());
            pnBody.setOpaque(false);
            pnBody.setBorder(new EmptyBorder(14, 14, 14, 14));

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1;
            constraints.weighty = 1;
            pnBody.add(getPnLogin(), constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.insets = new Insets(0, 14, 0, 14);
            pnBody.add(new JSeparator(SwingConstants.VERTICAL), constraints);

            constraints.gridx = 2;
            constraints.weightx = 1;
            constraints.insets = new Insets(0, 0, 0, 0);
            pnBody.add(getPnExternal(), constraints);
        }
        return pnBody;
    }

    private JPanel getPnLogin() {
        if (pnLogin == null) {
            pnLogin = new JPanel(new GridBagLayout());
            pnLogin.setBorder(BorderFactory.createCompoundBorder(titled("Log in"), new EmptyBorder(10, 14, 10, 14)));

            JLabel lblUsername = new JLabel("Username:");
            lblUsername.setDisplayedMnemonic('U');
            lblUsername.setLabelFor(getTxtUsername());
            pnLogin.add(lblUsername, constraints(0, 0, 0, new Insets(6, 0, 6, 12)));
            pnLogin.add(getTxtUsername(), field(1, 0));

            JLabel lblPassword = new JLabel("Password:");
            lblPassword.setDisplayedMnemonic('P');
            lblPassword.setLabelFor(getPswPassword());
            pnLogin.add(lblPassword, constraints(0, 1, 0, new Insets(6, 0, 6, 12)));
            pnLogin.add(getPswPassword(), field(1, 1));

            GridBagConstraints button = constraints(1, 2, 1, new Insets(14, 0, 0, 0));
            button.anchor = GridBagConstraints.NORTHEAST;
            button.fill = GridBagConstraints.NONE;
            button.weighty = 1;
            pnLogin.add(getBtnLogin(), button);
        }
        return pnLogin;
    }

    private JTextField getTxtUsername() {
        if (txtUsername == null) {
            txtUsername = new JTextField(16);
        }
        return txtUsername;
    }

    private JPasswordField getPswPassword() {
        if (pswPassword == null) {
            pswPassword = new JPasswordField(16);
        }
        return pswPassword;
    }

    private JButton getBtnLogin() {
        if (btnLogin == null) {
            btnLogin = new JButton("Log In");
            btnLogin.setMnemonic('L');
            btnLogin.setFont(btnLogin.getFont().deriveFont(Font.BOLD));
            btnLogin.addActionListener(event -> logIn());
        }
        return btnLogin;
    }

    /** US 35828: zone for external users who want to buy shares of the active campaign. */
    private JPanel getPnExternal() {
        if (pnExternal == null) {
            pnExternal = new JPanel(new GridBagLayout());
            pnExternal.setBorder(BorderFactory.createCompoundBorder(titled("New shareholders"),
                    new EmptyBorder(10, 14, 10, 14)));
            pnExternal.add(getLblExternalText(), constraints(0, 0, 1, new Insets(0, 0, 14, 0)));
            pnExternal.add(getLblCampaign(), constraints(0, 1, 1, new Insets(0, 0, 2, 0)));
            pnExternal.add(getLblCampaignPhase(), constraints(0, 2, 1, new Insets(0, 0, 14, 0)));

            GridBagConstraints button = constraints(0, 3, 1, new Insets(0, 0, 0, 0));
            button.anchor = GridBagConstraints.NORTH;
            button.fill = GridBagConstraints.NONE;
            button.weighty = 1;
            pnExternal.add(getBtnBuyShares(), button);
        }
        return pnExternal;
    }

    private JLabel getLblExternalText() {
        if (lblExternalText == null) {
            lblExternalText = new JLabel("<html>Not a shareholder yet? Buy shares of the active campaign "
                    + "and become one.</html>");
        }
        return lblExternalText;
    }

    private JLabel getLblCampaign() {
        if (lblCampaign == null) {
            lblCampaign = new JLabel();
            lblCampaign.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
            lblCampaign.setForeground(Branding.NAVY);
        }
        return lblCampaign;
    }

    private JLabel getLblCampaignPhase() {
        if (lblCampaignPhase == null) {
            lblCampaignPhase = new JLabel();
            lblCampaignPhase.setForeground(Branding.TEXT_MUTED);
        }
        return lblCampaignPhase;
    }

    private JButton getBtnBuyShares() {
        if (btnBuyShares == null) {
            btnBuyShares = new JButton("Buy shares...");
            btnBuyShares.setMnemonic('B');
            btnBuyShares.addActionListener(event -> openExternalPurchase());
        }
        return btnBuyShares;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel();
            pnFooter.setOpaque(false);
            pnFooter.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 0, 0, Branding.BORDER),
                    new EmptyBorder(8, 0, 10, 0)));
            pnFooter.setLayout(new BorderLayout(0, 0));
            pnFooter.add(getLblFooter(), BorderLayout.CENTER);
        }
        return pnFooter;
    }

    private JLabel getLblFooter() {
        if (lblFooter == null) {
            lblFooter = new JLabel("IPS 2026 · Team PL11");
            lblFooter.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 11));
            lblFooter.setForeground(Branding.TEXT_MUTED);
            lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return lblFooter;
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
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = insets;
        return constraints;
    }

    private static GridBagConstraints field(int column, int row) {
        return constraints(column, row, 1, new Insets(6, 0, 6, 0));
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    private void logIn() {
        if (getTxtUsername().getText().isEmpty() || getPswPassword().getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String password = new String(getPswPassword().getPassword());
            Optional<Employee> oe = employeeController.getEmployeeByUsername(getTxtUsername().getText());
            shareholderController.load();
            Optional<Shareholder> os = shareholderController.findShareholderByUsername(getTxtUsername().getText());

            if (oe.isEmpty() && os.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Invalid username.", "Authentication Error",
                        JOptionPane.ERROR_MESSAGE);
                initialize();
                return;
            }

            if (oe.isPresent()) {
                if (!PasswordEncrypting.checkPassword(password, oe.get().getPassword())) {
                    JOptionPane.showMessageDialog(this, "Invalid password.", "Authentication Error",
                            JOptionPane.ERROR_MESSAGE);
                    getPswPassword().setText("");
                    return;
                }
                JOptionPane.showMessageDialog(this, "Welcome, " + oe.get().getFirstName() + "!", "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                openPositionMenu(oe.get().getPosition(), oe.get().getId());
                dispose();
            } else {
                if (!PasswordEncrypting.checkPassword(password, os.get().getPassword())) {
                    JOptionPane.showMessageDialog(this, "Invalid password.", "Authentication Error",
                            JOptionPane.ERROR_MESSAGE);
                    getPswPassword().setText("");
                    return;
                }
                JOptionPane.showMessageDialog(this, "Welcome, " + os.get().getFirstName() + "!", "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                new ShareholderMenuWindow(os.get()).setVisible(true);
                dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            initialize();
        }
    }

    private void initialize() {
        getTxtUsername().requestFocus();
        getTxtUsername().setText("");
        getPswPassword().setText("");
    }

    private void openPositionMenu(String position, int employeeId) {
        switch (position.toLowerCase()) {
        case "player":
            new PlayerMenuWindow().setVisible(true);
            break;
        case "coach":
            new CoachMenuWindow().setVisible(true);
            break;
        case "general manager":
            new GeneralManagerMenuWindow().setVisible(true);
            break;
        case "ticket seller":
            new TicketSellerMenuWindow().setVisible(true);
            break;
        case "store employee":
            new StoreEmployeeMenuWindow().setVisible(true);
            break;
        case "gardener":
            new GardenerMenuWindow().setVisible(true);
            break;
        case "director of communications":
            new DirectorOfCommsMenuWindow().setVisible(true);
            break;
        case "facility manager":
            new FacilityManagerMenuWindow().setVisible(true);
            break;
        case "social networks":
            new SocialNetworksMenuWindow().setVisible(true);
            break;
        case "doctor":
            new DoctorMenuWindow(employeeId).setVisible(true);
            break;
        case "press officer":
            new PressOfficerMenuWindow().setVisible(true);
            break;
        default:
            JOptionPane.showMessageDialog(this, "Unknown position: " + position, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    private ExternalSharePurchaseController newExternalController() {
        return new ExternalSharePurchaseController(new CampaignDAO(), new CampaignPurchaseDAO(), new ShareholderDAO());
    }

    /** US 35828: shows the active campaign and its phase in the zone of the external users. */
    private void showActiveCampaign() {
        ExternalSharePurchaseController controller = newExternalController();
        try {
            if (!controller.load()) {
                getLblCampaign().setText("There is no active campaign");
                getLblCampaignPhase().setText("Come back when a new campaign is opened.");
                getBtnBuyShares().setEnabled(false);
                return;
            }
        } catch (SQLException exception) {
            getLblCampaign().setText("The active campaign could not be loaded");
            getLblCampaignPhase().setText(exception.getMessage());
            getBtnBuyShares().setEnabled(false);
            return;
        }
        Campaign campaign = controller.getCampaign();
        getLblCampaign().setText(campaign.getName());
        getLblCampaignPhase().setText(campaign.getStatus().getLabel() + " — "
                + MoneyFormat.format(campaign.getNominalPrice()) + " per share"
                + (campaign.getStatus().allowsExternalPurchases() ? "" : " (you can buy in phase 3)"));
        getBtnBuyShares().setEnabled(true);
    }

    /** US 35828: opens the purchase for external users and shows the campaign again when it is closed. */
    private void openExternalPurchase() {
        ExternalSharePurchaseController controller = newExternalController();
        try {
            if (!controller.load()) {
                JOptionPane.showMessageDialog(this, "There is no active campaign.", "Buy Campaign Shares",
                        JOptionPane.INFORMATION_MESSAGE);
                showActiveCampaign();
                return;
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "The active campaign could not be loaded:\n" + exception.getMessage(),
                    "Buy Campaign Shares", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new ExternalBuySharesDialog(controller, this).setVisible(true);
        showActiveCampaign();
    }
}
