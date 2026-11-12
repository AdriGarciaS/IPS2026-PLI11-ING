package com.ips2026.pl11.view.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.controller.PasswordEncrypting;
import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 */
public class LoginWindow extends JFrame {
    
    private EmployeeController controller = new EmployeeController(new EmployeeDAO());

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private HeaderPanel pnHeader;

    private JPanel pnFooter;
    private JLabel lblFooter;

    private JPanel pnBody;
    private MenuCardButton btnOption1;
    private JPasswordField pswPassword;
    private JTextField txtUsername;
    private JLabel lblUsername;
    private JLabel lblPassword;

    public LoginWindow() {
        setResizable(false);
        setTitle("Login window");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 500, 400, null);

        rootPane.setDefaultButton(getBtnLogin());
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Log into the system", "Please enter your username and password", false);
        }
        return pnHeader;
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

    private JPanel getPnBody() {
        if (pnBody == null) {
            pnBody = new JPanel();
            pnBody.setLayout(null);
            pnBody.add(getBtnLogin());
            pnBody.add(getPswPassword());
            pnBody.add(getTxtUsername());
            pnBody.add(getLblUsername());
            pnBody.add(getLblPassword());
        }
        return pnBody;
    }

    private MenuCardButton getBtnLogin() {
        if (btnOption1 == null) {
            btnOption1 = new MenuCardButton("Log In", "");
            btnOption1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (getTxtUsername().getText().isEmpty() || getPswPassword().getPassword().length == 0) {
                        JOptionPane.showMessageDialog(LoginWindow.this, "Please enter both username and password.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        String password = new String(getPswPassword().getPassword());
                        Optional<Employee> oe = controller.getEmployeeByUsername(getTxtUsername().getText());
                        
                        if(oe.isEmpty()) {
                            JOptionPane.showMessageDialog(LoginWindow.this, "Invalid username.",
                                "Authentication Error", JOptionPane.ERROR_MESSAGE);
                            initialize();
                            return;
                            
                        }

                        if(!PasswordEncrypting.checkPassword(password, oe.get().getPassword())) {
                            JOptionPane.showMessageDialog(LoginWindow.this, "Invalid password.",
                                "Authentication Error", JOptionPane.ERROR_MESSAGE);
                            getPswPassword().setText("");;
                            return;
                            
                        }
                        
                        if(PasswordEncrypting.checkPassword(password, oe.get().getPassword())) {
                            JOptionPane.showMessageDialog(LoginWindow.this, "Welcome, " + oe.get().getFirstName() + "!",
                                "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                            new MainWindow().setVisible(true);
                        }
                        
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(LoginWindow.this, "Database error: " + ex.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                        initialize();
                        return;
                    }
                }
            });
            btnOption1.setBounds(165, 159, 171, 57);
            btnOption1.setFont(new Font("Tahoma", Font.PLAIN, 16));
        }
        return btnOption1;
    }

    private JPasswordField getPswPassword() {
        if (pswPassword == null) {
            pswPassword = new JPasswordField();
            pswPassword.setBounds(257, 81, 186, 48);
        }
        return pswPassword;
    }

    private JTextField getTxtUsername() {
        if (txtUsername == null) {
            txtUsername = new JTextField();
            txtUsername.setBounds(257, 28, 186, 48);
            txtUsername.setColumns(10);
        }
        return txtUsername;
    }

    private JLabel getLblUsername() {
        if (lblUsername == null) {
            lblUsername = new JLabel("Username:");
            lblUsername.setDisplayedMnemonic('U');
            lblUsername.setLabelFor(getTxtUsername());
            lblUsername.setForeground(new Color(0, 64, 128));
            lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 21));
            lblUsername.setBounds(58, 39, 136, 26);
        }
        return lblUsername;
    }

    private JLabel getLblPassword() {
        if (lblPassword == null) {
            lblPassword = new JLabel("Password:");
            lblPassword.setDisplayedMnemonic('P');
            lblPassword.setLabelFor(getPswPassword());
            lblPassword.setForeground(new Color(0, 64, 128));
            lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 21));
            lblPassword.setBounds(60, 92, 136, 26);
        }
        return lblPassword;
    }
    
    private void initialize() {
        getTxtUsername().requestFocus();
        getTxtUsername().setText("");
        getPswPassword().setText("");
    }
}