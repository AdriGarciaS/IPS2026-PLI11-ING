package com.ips2026.pl11.vista;

import com.ips2026.pl11.controlador.EmployeeController;
import com.ips2026.pl11.datos.EmployeeDAO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AddEmployeeView extends JDialog {

    private static final long serialVersionUID = 1L;

    private final EmployeeController controller = new EmployeeController(new EmployeeDAO());

    private final String[] sportsPositions = {"Player", "Coach"};
    private final String[] nonSportsPositions = {
            "General Manager", "Ticket Seller", "Store Employee",
            "Gardener", "Director of Communications", "Facility Manager", "Social Networks"
    };

    private JPanel contentPane;
    private JPanel pnHeader;
    private JPanel pnForm;
    private JPanel pnFooter;

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtNationalId;
    private JTextField txtBirthDate;
    private JTextField txtPhone;
    private JTextField txtSalary;

    private JComboBox<String> cbCategory;
    private JComboBox<String> cbPosition;
    private JButton btnSubmit;

    public AddEmployeeView(JFrame parent) {
        super(parent, "Employee Registration", true);
        setSize(520, 620);
        setLocationRelativeTo(parent);
        setResizable(false);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(248, 250, 252));
        contentPane.setBorder(new EmptyBorder(20, 25, 20, 25));
        contentPane.setLayout(new BorderLayout(0, 15));
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnForm(), BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
    }

    private JPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new JPanel();
            pnHeader.setBackground(new Color(248, 250, 252));
            pnHeader.setLayout(new BorderLayout(0, 4));
            pnHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));

            JLabel lblTitle = new JLabel("New Employee");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblTitle.setForeground(new Color(30, 41, 59));

            JLabel lblSubtitle = new JLabel("Enter personal and contractual information below");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSubtitle.setForeground(new Color(100, 116, 139));

            pnHeader.add(lblTitle, BorderLayout.NORTH);
            pnHeader.add(lblSubtitle, BorderLayout.SOUTH);
        }
        return pnHeader;
    }

    private JPanel getPnForm() {
        if (pnForm == null) {
            pnForm = new JPanel();
            pnForm.setBackground(new Color(248, 250, 252));
            pnForm.setLayout(new GridLayout(8, 2, 12, 14));
            pnForm.setBorder(new EmptyBorder(10, 5, 10, 5));

            pnForm.add(createLabel("First Name:"));
            pnForm.add(getTxtFirstName());

            pnForm.add(createLabel("Last Name:"));
            pnForm.add(getTxtLastName());

            pnForm.add(createLabel("National ID / Passport:"));
            pnForm.add(getTxtNationalId());

            pnForm.add(createLabel("Birth Date (YYYY-MM-DD):"));
            pnForm.add(getTxtBirthDate());

            pnForm.add(createLabel("Phone Number:"));
            pnForm.add(getTxtPhone());

            pnForm.add(createLabel("Staff Category:"));
            pnForm.add(getCbCategory());

            pnForm.add(createLabel("Position:"));
            pnForm.add(getCbPosition());

            pnForm.add(createLabel("Gross Annual Salary (€):"));
            pnForm.add(getTxtSalary());
        }
        return pnForm;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel();
            pnFooter.setBackground(new Color(248, 250, 252));
            pnFooter.setLayout(new BorderLayout());
            pnFooter.setBorder(new EmptyBorder(10, 0, 0, 0));
            pnFooter.add(getBtnSubmit(), BorderLayout.CENTER);
        }
        return pnFooter;
    }

    private JTextField getTxtFirstName() {
        if (txtFirstName == null) {
            txtFirstName = createTextField();
        }
        return txtFirstName;
    }

    private JTextField getTxtLastName() {
        if (txtLastName == null) {
            txtLastName = createTextField();
        }
        return txtLastName;
    }

    private JTextField getTxtNationalId() {
        if (txtNationalId == null) {
            txtNationalId = createTextField();
        }
        return txtNationalId;
    }

    private JTextField getTxtBirthDate() {
        if (txtBirthDate == null) {
            txtBirthDate = createTextField();
            txtBirthDate.setToolTipText("Format: YYYY-MM-DD");
        }
        return txtBirthDate;
    }

    private JTextField getTxtPhone() {
        if (txtPhone == null) {
            txtPhone = createTextField();
        }
        return txtPhone;
    }

    private JTextField getTxtSalary() {
        if (txtSalary == null) {
            txtSalary = createTextField();
        }
        return txtSalary;
    }

    private JComboBox<String> getCbCategory() {
        if (cbCategory == null) {
            cbCategory = new JComboBox<>(new String[]{"SPORTS", "NON_SPORTS"});
            cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cbCategory.setBackground(Color.WHITE);
            cbCategory.setForeground(Color.BLACK);
            cbCategory.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));
            cbCategory.addActionListener(e -> refreshPositions());
        }
        return cbCategory;
    }

    private JComboBox<String> getCbPosition() {
        if (cbPosition == null) {
            cbPosition = new JComboBox<>(sportsPositions);
            cbPosition.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cbPosition.setBackground(Color.WHITE);
            cbPosition.setForeground(Color.BLACK);
            cbPosition.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));
        }
        return cbPosition;
    }

    private JButton getBtnSubmit() {
        if (btnSubmit == null) {
            btnSubmit = new JButton("Save Employee");
            btnSubmit.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
            btnSubmit.setFocusPainted(false);
            btnSubmit.setContentAreaFilled(false);
            btnSubmit.setOpaque(true);
            btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSubmit.setBackground(new Color(24, 76, 120));
            btnSubmit.setForeground(Color.WHITE);
            btnSubmit.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

            btnSubmit.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnSubmit.setBackground(new Color(32, 101, 160));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnSubmit.setBackground(new Color(24, 76, 120));
                }
            });

            btnSubmit.addActionListener(e -> saveEmployee());
        }
        return btnSubmit;
    }

    private JLabel createLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        label.setForeground(new Color(51, 65, 85));
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(140, 185, 235), 2, true),
                        BorderFactory.createEmptyBorder(5, 7, 5, 7)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(203, 213, 225), 1, true),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)
                ));
            }
        });

        return tf;
    }

    private void refreshPositions() {
        JComboBox<String> comboPosition = getCbPosition();
        comboPosition.removeAllItems();
        String selectedCat = (String) getCbCategory().getSelectedItem();
        String[] positions = "SPORTS".equals(selectedCat) ? sportsPositions : nonSportsPositions;

        for (String pos : positions) {
            comboPosition.addItem(pos);
        }
    }

    private void saveEmployee() {
        try {
            LocalDate birthDate = LocalDate.parse(getTxtBirthDate().getText().trim());
            double salary = Double.parseDouble(getTxtSalary().getText().trim());

            controller.registerEmployee(getTxtFirstName().getText(),getTxtLastName().getText(),getTxtNationalId().getText(),birthDate,getTxtPhone().getText(),(String) getCbCategory().getSelectedItem(),(String) getCbPosition().getSelectedItem(),salary);

            JOptionPane.showMessageDialog(this, "Employee successfully registered!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD (e.g. 2005-08-20).", "Date Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Annual gross salary must be a valid number.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}