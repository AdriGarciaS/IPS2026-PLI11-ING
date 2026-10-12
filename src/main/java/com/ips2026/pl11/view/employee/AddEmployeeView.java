package com.ips2026.pl11.view.employee;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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

import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class AddEmployeeView extends JDialog {

    private static final long serialVersionUID = 1L;

    private final EmployeeController controller = new EmployeeController(new EmployeeDAO());

    private final String[] sportsPositions = {"Player", "Coach"};
    private final String[] nonSportsPositions = {
            "General Manager", "Ticket Seller", "Store Employee",
            "Gardener", "Director of Communications", "Facility Manager", "Social Networks"
    };

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
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
        setSize(520, 640);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Aplica iconos y fondo de ventana segun Branding
        Branding.applyTo(this);

        contentPane = new JPanel();
        contentPane.setBackground(Branding.BACKGROUND);
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);

        // Panel intermedio con margenes internos para formulario y boton
        pnBody = new JPanel(new BorderLayout(0, 16));
        pnBody.setOpaque(false);
        pnBody.setBorder(new EmptyBorder(20, 28, 20, 28));
        pnBody.add(getPnForm(), BorderLayout.CENTER);
        pnBody.add(getPnFooter(), BorderLayout.SOUTH);

        contentPane.add(pnBody, BorderLayout.CENTER);
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(
                    "New Employee",
                    "Enter personal and contractual information below"
            );
        }
        return pnHeader;
    }

    private JPanel getPnForm() {
        if (pnForm == null) {
            pnForm = new JPanel();
            pnForm.setOpaque(false);
            pnForm.setLayout(new GridLayout(8, 2, 12, 14));

            pnForm.add(crearLabel("First Name:"));
            pnForm.add(getTxtFirstName());

            pnForm.add(crearLabel("Last Name:"));
            pnForm.add(getTxtLastName());

            pnForm.add(crearLabel("National ID / Passport:"));
            pnForm.add(getTxtNationalId());

            pnForm.add(crearLabel("Birth Date (YYYY-MM-DD):"));
            pnForm.add(getTxtBirthDate());

            pnForm.add(crearLabel("Phone Number:"));
            pnForm.add(getTxtPhone());

            pnForm.add(crearLabel("Staff Category:"));
            pnForm.add(getCbCategory());

            pnForm.add(crearLabel("Position:"));
            pnForm.add(getCbPosition());

            pnForm.add(crearLabel("Gross Annual Salary (€):"));
            pnForm.add(getTxtSalary());
        }
        return pnForm;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel();
            pnFooter.setOpaque(false);
            pnFooter.setLayout(new BorderLayout());
            pnFooter.setBorder(new EmptyBorder(8, 0, 0, 0));
            pnFooter.add(getBtnSubmit(), BorderLayout.CENTER);
        }
        return pnFooter;
    }

    private JTextField getTxtFirstName() {
        if (txtFirstName == null) {
            txtFirstName = crearTextField();
        }
        return txtFirstName;
    }

    private JTextField getTxtLastName() {
        if (txtLastName == null) {
            txtLastName = crearTextField();
        }
        return txtLastName;
    }

    private JTextField getTxtNationalId() {
        if (txtNationalId == null) {
            txtNationalId = crearTextField();
        }
        return txtNationalId;
    }

    private JTextField getTxtBirthDate() {
        if (txtBirthDate == null) {
            txtBirthDate = crearTextField();
            txtBirthDate.setToolTipText("Format: YYYY-MM-DD");
        }
        return txtBirthDate;
    }

    private JTextField getTxtPhone() {
        if (txtPhone == null) {
            txtPhone = crearTextField();
        }
        return txtPhone;
    }

    private JTextField getTxtSalary() {
        if (txtSalary == null) {
            txtSalary = crearTextField();
        }
        return txtSalary;
    }

    private JComboBox<String> getCbCategory() {
        if (cbCategory == null) {
            cbCategory = new JComboBox<>(new String[]{"SPORTS", "NON_SPORTS"});
            cbCategory.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 13));
            cbCategory.setBackground(Color.WHITE);
            cbCategory.setForeground(Branding.NAVY);
            cbCategory.setBorder(new LineBorder(Branding.BORDER, 1, true));
            cbCategory.addActionListener(e -> actualizarPuestos());
        }
        return cbCategory;
    }

    private JComboBox<String> getCbPosition() {
        if (cbPosition == null) {
            cbPosition = new JComboBox<>(sportsPositions);
            cbPosition.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 13));
            cbPosition.setBackground(Color.WHITE);
            cbPosition.setForeground(Branding.NAVY);
            cbPosition.setBorder(new LineBorder(Branding.BORDER, 1, true));
        }
        return cbPosition;
    }

    private JButton getBtnSubmit() {
        if (btnSubmit == null) {
            btnSubmit = new JButton("Save Employee");
            btnSubmit.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
            btnSubmit.setFocusPainted(false);
            btnSubmit.setContentAreaFilled(false);
            btnSubmit.setOpaque(true);
            btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSubmit.setBackground(Branding.NAVY);
            btnSubmit.setForeground(Color.WHITE);
            btnSubmit.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.NAVY, 1, true),
                    BorderFactory.createEmptyBorder(12, 20, 12, 20)
            ));

            btnSubmit.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnSubmit.setBackground(new Color(0x1F, 0x4E, 0x8C)); // Tono azul medio en hover
                    btnSubmit.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.GOLD, 1, true),
                            BorderFactory.createEmptyBorder(12, 20, 12, 20)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnSubmit.setBackground(Branding.NAVY);
                    btnSubmit.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.NAVY, 1, true),
                            BorderFactory.createEmptyBorder(12, 20, 12, 20)
                    ));
                }
            });

            btnSubmit.addActionListener(e -> saveEmployee());
        }
        return btnSubmit;
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        label.setForeground(Branding.NAVY);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JTextField crearTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 13));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Branding.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Branding.GOLD, 2, true),
                        BorderFactory.createEmptyBorder(5, 7, 5, 7)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Branding.BORDER, 1, true),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)
                ));
            }
        });

        return tf;
    }

    private void actualizarPuestos() {
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

            controller.registerEmployee(
                    getTxtFirstName().getText(),
                    getTxtLastName().getText(),
                    getTxtNationalId().getText(),
                    birthDate,
                    getTxtPhone().getText(),
                    (String) getCbCategory().getSelectedItem(),
                    (String) getCbPosition().getSelectedItem(),
                    salary
            );

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