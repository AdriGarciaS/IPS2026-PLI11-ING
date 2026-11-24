package com.ips2026.pl11.view.menu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

/**
 * Common part of the main menu of every role (employees of each position and
 * shareholders): the large header with the "Log out" button, the panel with
 * the option cards and the footer.
 *
 * <p>Each menu only gives its subtitle and adds its cards:</p>
 * <pre>
 *     public CoachMenuWindow() {
 *         super("Coach main menu — choose an option to start");
 *         setCards(new GridLayout(2, 1, 18, 18), getBtnOption1(), getBtnOption13());
 *         Branding.setInitialSize(this, 640, 480, null);
 *     }
 * </pre>
 *
 * <p>The cards are added by the subclass at the end of its own constructor
 * (and not from here), so the fields of the subclass are already created when
 * its cards are built.</p>
 */
public abstract class RoleMenuWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private final String subtitle;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JButton btnLogout;
    private JPanel pnButtons;
    private JPanel pnFooter;
    private JLabel lblFooter;

    /** @param subtitle subtitle of the header, for example "Coach main menu — choose an option to start" */
    protected RoleMenuWindow(String subtitle) {
        this.subtitle = subtitle;
        setTitle("Football Club Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
    }

    /** Shows the option cards of the menu with this layout (for example a {@code GridLayout}). */
    protected void setCards(LayoutManager layout, Component... cards) {
        getPnButtons().removeAll();
        getPnButtons().setLayout(layout);
        for (Component card : cards) {
            getPnButtons().add(card);
        }
        getPnButtons().revalidate();
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Football Club Management", subtitle, true);
            pnHeader.setRightComponent(getBtnLogout());
        }
        return pnHeader;
    }

    private JButton getBtnLogout() {
        if (btnLogout == null) {
            btnLogout = new JButton("Log out");
            btnLogout.setMnemonic('O');
            btnLogout.setToolTipText("Close this menu and go back to the login window");
            btnLogout.addActionListener(event -> logOut());
        }
        return btnLogout;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setBorder(new EmptyBorder(24, 24, 16, 24));
        }
        return pnButtons;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel();
            pnFooter.setOpaque(false);
            pnFooter.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(1, 0, 0, 0, Branding.BORDER),
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

    /**
     * Asks for confirmation, closes this menu (and every window it owns) and
     * opens the login window again.
     */
    private void logOut() {
        int answer = JOptionPane.showConfirmDialog(this, "Do you want to log out?", "Log out",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        dispose();
        new LoginWindow().setVisible(true);
    }
}
