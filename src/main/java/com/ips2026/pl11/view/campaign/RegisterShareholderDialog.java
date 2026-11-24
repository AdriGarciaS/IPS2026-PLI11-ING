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
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import com.ips2026.pl11.controller.campaign.ExternalSharePurchaseController;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignPurchase;
import com.ips2026.pl11.model.campaign.ShareholderRegistration;
import com.ips2026.pl11.model.reservation.CardValidator;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

/**
 * US 35828: second step of the purchase of an external user. They type their
 * personal data (first name, last name, email, national ID, username and
 * password) and their credit card; with "Register &amp; buy" they become a
 * shareholder and the shares go to their wallet.
 *
 * <p>Modal dialog opened from {@link ExternalBuySharesDialog}, in the lazy
 * WindowBuilder style.</p>
 */
public class RegisterShareholderDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient ExternalSharePurchaseController controller;
    private final String quantityText;
    private boolean purchaseCompleted;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnCenter;
    private JPanel pnPersonal;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtNationalId;
    private JTextField txtUsername;
    private JPasswordField pswPassword;
    private JPanel pnPayment;
    private JTextField txtCard;
    private JPanel pnPurchase;
    private JLabel lblPurchase;
    private JLabel lblTotal;
    private JPanel pnSouth;
    private JLabel lblStatus;
    private JPanel pnButtons;
    private JButton btnCancel;
    private JButton btnRegister;

    /**
     * @param controller   controller with the active campaign loaded
     * @param quantityText number of shares chosen in the previous window (already valid)
     * @param owner        the previous window ("Buy Campaign Shares")
     */
    public RegisterShareholderDialog(ExternalSharePurchaseController controller, String quantityText, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.quantityText = quantityText;

        setTitle("Register as Shareholder");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 620));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);
        getRootPane().setDefaultButton(getBtnRegister());

        showPurchase();
        updateStatus();
    }

    /** Whether the user has been registered and the shares bought (the window can then be closed). */
    public boolean isPurchaseCompleted() {
        return purchaseCompleted;
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Register as Shareholder", "Phase 3 — become a shareholder of the club");
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

    private JPanel getPnCenter() {
        if (pnCenter == null) {
            pnCenter = new JPanel();
            pnCenter.setOpaque(false);
            pnCenter.setLayout(new BoxLayout(pnCenter, BoxLayout.Y_AXIS));
            pnCenter.add(getPnPersonal());
            pnCenter.add(Box.createVerticalStrut(12));
            pnCenter.add(getPnPayment());
            pnCenter.add(Box.createVerticalStrut(12));
            pnCenter.add(getPnPurchase());
        }
        return pnCenter;
    }

    private JPanel getPnPersonal() {
        if (pnPersonal == null) {
            pnPersonal = new JPanel(new GridBagLayout());
            pnPersonal.setBorder(BorderFactory.createCompoundBorder(titled("Personal data"),
                    new EmptyBorder(6, 10, 6, 10)));
            addField(pnPersonal, 0, "First name:", 'F', getTxtFirstName());
            addField(pnPersonal, 1, "Last name:", 'L', getTxtLastName());
            addField(pnPersonal, 2, "Email:", 'E', getTxtEmail());
            addField(pnPersonal, 3, "National ID:", 'I', getTxtNationalId());
            addField(pnPersonal, 4, "Username:", 'U', getTxtUsername());
            addField(pnPersonal, 5, "Password:", 'P', getPswPassword());
        }
        return pnPersonal;
    }

    private JPanel getPnPayment() {
        if (pnPayment == null) {
            pnPayment = new JPanel(new GridBagLayout());
            pnPayment.setBorder(BorderFactory.createCompoundBorder(titled("Payment"), new EmptyBorder(6, 10, 6, 10)));
            addField(pnPayment, 0, "Credit card number:", 'N', getTxtCard());
        }
        return pnPayment;
    }

    private JPanel getPnPurchase() {
        if (pnPurchase == null) {
            pnPurchase = new JPanel(new BorderLayout(12, 0));
            pnPurchase.setBorder(BorderFactory.createCompoundBorder(titled("Purchase"), new EmptyBorder(6, 10, 6, 10)));
            pnPurchase.add(getLblPurchase(), BorderLayout.CENTER);
            pnPurchase.add(getLblTotal(), BorderLayout.EAST);
        }
        return pnPurchase;
    }

    private JTextField getTxtFirstName() {
        if (txtFirstName == null) {
            txtFirstName = newField();
        }
        return txtFirstName;
    }

    private JTextField getTxtLastName() {
        if (txtLastName == null) {
            txtLastName = newField();
        }
        return txtLastName;
    }

    private JTextField getTxtEmail() {
        if (txtEmail == null) {
            txtEmail = newField();
        }
        return txtEmail;
    }

    private JTextField getTxtNationalId() {
        if (txtNationalId == null) {
            txtNationalId = newField();
        }
        return txtNationalId;
    }

    private JTextField getTxtUsername() {
        if (txtUsername == null) {
            txtUsername = newField();
        }
        return txtUsername;
    }

    private JPasswordField getPswPassword() {
        if (pswPassword == null) {
            pswPassword = new JPasswordField(28);
            pswPassword.getDocument().addDocumentListener(onChange());
        }
        return pswPassword;
    }

    private JTextField getTxtCard() {
        if (txtCard == null) {
            txtCard = newField();
        }
        return txtCard;
    }

    private JLabel getLblPurchase() {
        if (lblPurchase == null) {
            lblPurchase = new JLabel();
        }
        return lblPurchase;
    }

    private JLabel getLblTotal() {
        if (lblTotal == null) {
            lblTotal = new JLabel();
            lblTotal.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 24));
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
            pnButtons.add(getBtnCancel());
            pnButtons.add(getBtnRegister());
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

    private JButton getBtnRegister() {
        if (btnRegister == null) {
            btnRegister = new JButton("Register & buy");
            btnRegister.setMnemonic('R');
            btnRegister.setFont(btnRegister.getFont().deriveFont(Font.BOLD));
            btnRegister.addActionListener(event -> registerAndBuy());
        }
        return btnRegister;
    }

    private JTextField newField() {
        JTextField field = new JTextField(28);
        field.getDocument().addDocumentListener(onChange());
        return field;
    }

    private static TitledBorder titled(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        return border;
    }

    /** A label with its mnemonic and its field, in one row. */
    private static void addField(JPanel panel, int row, String caption, char mnemonic, JTextComponent field) {
        JLabel label = new JLabel(caption);
        label.setDisplayedMnemonic(mnemonic);
        label.setLabelFor(field);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 0, 5, 16);
        panel.add(label, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.insets = new Insets(5, 0, 5, 0);
        panel.add(field, constraints);
    }

    private DocumentListener onChange() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateStatus();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateStatus();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateStatus();
            }
        };
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    private ShareholderRegistration readRegistration() {
        return new ShareholderRegistration(getTxtFirstName().getText(), getTxtLastName().getText(),
                getTxtEmail().getText(), getTxtNationalId().getText(), getTxtUsername().getText(),
                new String(getPswPassword().getPassword()));
    }

    /** "N shares x price" and the total. */
    private void showPurchase() {
        Campaign campaign = controller.getCampaign();
        int quantity = Integer.parseInt(quantityText);
        getLblPurchase().setText(quantity + (quantity == 1 ? " share" : " shares") + " x "
                + MoneyFormat.format(campaign.getNominalPrice()) + " — " + campaign.getName());
        getLblTotal().setText(MoneyFormat.format(controller.totalFor(quantity)));
    }

    /**
     * Shows the first problem of format of the data typed (an empty field is
     * shown as a hint, not as an error) and enables "Register &amp; buy" when
     * there is none. Whether the data is already registered is checked when
     * the button is pressed.
     */
    private void updateStatus() {
        List<String> problems = readRegistration().findFormatProblems();
        CardValidator.findProblem(getTxtCard().getText()).ifPresent(problems::add);
        Optional<String> problem = problems.stream().findFirst();

        if (problem.isPresent()) {
            boolean hint = problem.get().endsWith(" is required.") || problem.get().equals("Enter the bank card number.");
            getLblStatus().setForeground(hint ? Branding.TEXT_MUTED : Branding.ERROR);
            getLblStatus().setText(problem.get());
        } else {
            getLblStatus().setForeground(Branding.SUCCESS);
            getLblStatus().setText("Ready: press 'Register & buy' to become a shareholder and pay "
                    + getLblTotal().getText() + ".");
        }
        getBtnRegister().setEnabled(problem.isEmpty());
    }

    private void registerAndBuy() {
        CampaignPurchase purchase;
        try {
            purchase = controller.registerAndBuy(quantityText, readRegistration(), getTxtCard().getText());
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Register as Shareholder",
                    JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Register as Shareholder",
                    JOptionPane.WARNING_MESSAGE);
            dispose(); // the campaign or the remaining shares changed: back to the previous window
            return;
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "You could not be registered:\n" + exception.getMessage(),
                    "Register as Shareholder", JOptionPane.ERROR_MESSAGE);
            return;
        }
        purchaseCompleted = true;
        new CampaignPurchaseCompletedDialog(purchase, this).setVisible(true);
        dispose();
    }
}
