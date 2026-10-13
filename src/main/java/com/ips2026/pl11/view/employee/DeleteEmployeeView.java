package com.ips2026.pl11.view.employee;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.toedter.calendar.JDateChooser;

public class DeleteEmployeeView extends JDialog {

    private static final long serialVersionUID = 1L;

    private final EmployeeController controller = new EmployeeController(new EmployeeDAO());
    private List<Employee> employeeList;
    private Employee selectedEmployee;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JSplitPane splitPane;

    private JPanel pnTableContainer;
    private JScrollPane spTable;
    private JTable tblEmployees;
    private DefaultTableModel tableModel;

    private JPanel pnFormContainer;
    private JPanel pnFields;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtNationalId;
    private JDateChooser dateBirth;
    private JTextField txtPhone;
    private JTextField txtPosition; 
    private JTextField txtSalary;
    private JComboBox<String> cbCategory;

    private JPanel pnActions;
    private JButton btnDelete;
    private JButton btnClose;

    public DeleteEmployeeView(JDialog parent) {
        super(parent, "Delete Employee", true);
        setSize(950, 640);
        setLocationRelativeTo(parent);

        Branding.applyTo(this);

        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(Branding.BACKGROUND);
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);

        pnBody = new JPanel(new BorderLayout(0, 12));
        pnBody.setOpaque(false);
        pnBody.setBorder(new EmptyBorder(16, 20, 16, 20));
        pnBody.add(getSplitPane(), BorderLayout.CENTER);
        pnBody.add(getPnActions(), BorderLayout.SOUTH);

        contentPane.add(pnBody, BorderLayout.CENTER);

        loadEmployees();
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(
                    "Delete Employee",
                    "Select an employee from the table to delete from the system."
            );
        }
        return pnHeader;
    }

    private JSplitPane getSplitPane() {
        if (splitPane == null) {
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getPnTableContainer(), getPnFormContainer());
            splitPane.setResizeWeight(0.55);
            splitPane.setDividerSize(8);
            splitPane.setOpaque(false);
            splitPane.setBorder(null);
        }
        return splitPane;
    }

    private JPanel getPnTableContainer() {
        if (pnTableContainer == null) {
            pnTableContainer = new JPanel(new BorderLayout(0, 8));
            pnTableContainer.setOpaque(false);

            JLabel lblTableTitle = new JLabel("Employees List");
            lblTableTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
            lblTableTitle.setForeground(Branding.NAVY);

            pnTableContainer.add(lblTableTitle, BorderLayout.NORTH);
            pnTableContainer.add(getSpTable(), BorderLayout.CENTER);
        }
        return pnTableContainer;
    }

    private JScrollPane getSpTable() {
        if (spTable == null) {
            spTable = new JScrollPane(getTblEmployees());
            spTable.setBorder(new LineBorder(Branding.BORDER, 1, true));
        }
        return spTable;
    }

    private JTable getTblEmployees() {
        if (tblEmployees == null) {
            String[] columns = {"ID", "Name", "DNI/Passport", "Position", "Category"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblEmployees = new JTable(tableModel);
            tblEmployees.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblEmployees.setRowHeight(26);
            tblEmployees.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 13));
            tblEmployees.getTableHeader().setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            tblEmployees.getTableHeader().setBackground(new Color(241, 245, 249));
           
            tblEmployees.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = tblEmployees.getSelectedRow();
                    if (selectedRow != -1 && employeeList != null && selectedRow < employeeList.size()) {
                        loadEmployeeToForm(employeeList.get(selectedRow));
                    }
                }
            });
        }
        return tblEmployees;
    }

    private JPanel getPnFormContainer() {
        if (pnFormContainer == null) {
            pnFormContainer = new JPanel(new BorderLayout(0, 8));
            pnFormContainer.setOpaque(false);
            pnFormContainer.setBorder(new EmptyBorder(0, 12, 0, 0));

            JLabel lblFormTitle = new JLabel("Employee Details");
            lblFormTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
            lblFormTitle.setForeground(Branding.NAVY);

            pnFormContainer.add(lblFormTitle, BorderLayout.NORTH);
            pnFormContainer.add(getPnFields(), BorderLayout.CENTER);
        }
        return pnFormContainer;
    }

    private JPanel getPnFields() {
        if (pnFields == null) {
            pnFields = new JPanel(new GridLayout(8, 2, 8, 10));
            pnFields.setOpaque(false);

            pnFields.add(createLabel("First Name:"));
            pnFields.add(getTxtFirstName());

            pnFields.add(createLabel("Last Name:"));
            pnFields.add(getTxtLastName());

            pnFields.add(createLabel("National ID / Passport:"));
            pnFields.add(getTxtNationalId());

            pnFields.add(createLabel("Birth Date:"));
            pnFields.add(getDateBirth());

            pnFields.add(createLabel("Phone Number:"));
            pnFields.add(getTxtPhone());

            pnFields.add(createLabel("Staff Category:"));
            pnFields.add(getCbCategory());

            pnFields.add(createLabel("Position (Immutable):"));
            pnFields.add(getTxtPosition());

            pnFields.add(createLabel("Gross Annual Salary (€):"));
            pnFields.add(getTxtSalary());
        }
        return pnFields;
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

    private JDateChooser getDateBirth() {
        if (dateBirth == null) {
            dateBirth = new JDateChooser();
            dateBirth.setDateFormatString("yyyy-MM-dd");
            dateBirth.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 13));
            dateBirth.setEnabled(false); // Siempre de solo lectura en la vista de borrado
            dateBirth.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
        }
        return dateBirth;
    }

    private JTextField getTxtPhone() {
        if (txtPhone == null) {
            txtPhone = createTextField();
        }
        return txtPhone;
    }

    private JTextField getTxtPosition() {
        if (txtPosition == null) {
            txtPosition = createTextField();
            txtPosition.setEditable(false);
            txtPosition.setEnabled(false); 
            txtPosition.setBackground(new Color(241, 245, 249));
            txtPosition.setForeground(Branding.TEXT_MUTED);
        }
        return txtPosition;
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
            cbCategory.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 13));
            cbCategory.setBackground(Color.WHITE);
            cbCategory.setForeground(Branding.NAVY);
            cbCategory.setBorder(new LineBorder(Branding.BORDER, 1, true));
            cbCategory.setEnabled(false);
        }
        return cbCategory;
    }

    private JPanel getPnActions() {
        if (pnActions == null) {
            pnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnActions.setOpaque(false);
            pnActions.add(getBtnClose());
            pnActions.add(getBtnDelete());
        }
        return pnActions;
    }

    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton("Close");
            btnClose.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            btnClose.setFocusPainted(false);
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.setBackground(Color.WHITE);
            btnClose.setForeground(Branding.NAVY);
            btnClose.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));

            btnClose.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnClose.setBackground(new Color(241, 245, 249));
                    btnClose.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.GOLD, 1, true),
                            BorderFactory.createEmptyBorder(8, 16, 8, 16)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnClose.setBackground(Color.WHITE);
                    btnClose.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.BORDER, 1, true),
                            BorderFactory.createEmptyBorder(8, 16, 8, 16)
                    ));
                }
            });

            btnClose.addActionListener(e -> dispose());
        }
        return btnClose;
    }

    private JButton getBtnDelete() {
        if (btnDelete == null) {
            btnDelete = new JButton("Delete Employee");
            btnDelete.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            btnDelete.setFocusPainted(false);
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.setBackground(Branding.ERROR);
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.ERROR, 1, true),
                    BorderFactory.createEmptyBorder(8, 18, 8, 18)
            ));
            btnDelete.setEnabled(false);

            btnDelete.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (btnDelete.isEnabled()) {
                        btnDelete.setBackground(new Color(0xDC, 0x26, 0x26));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (btnDelete.isEnabled()) {
                        btnDelete.setBackground(Branding.ERROR);
                    }
                }
            });

            btnDelete.addActionListener(e -> saveChanges());
        }
        return btnDelete;
    }

    private JLabel createLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        label.setForeground(Branding.NAVY);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 13));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setEnabled(false); 
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Branding.BORDER, 1, true),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
        return tf;
    }

    private void loadEmployees() {
        tableModel.setRowCount(0);
        try {
            employeeList = controller.getEmployees();
            for (Employee emp : employeeList) {
                tableModel.addRow(new Object[]{
                        emp.getId(),
                        emp.getFirstName() + " " + emp.getLastName(),
                        emp.getNationalId(),
                        emp.getPosition(),
                        emp.getCategory()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading employees: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEmployeeToForm(Employee emp) {
        this.selectedEmployee = emp;

        getTxtFirstName().setText(emp.getFirstName());
        getTxtLastName().setText(emp.getLastName());
        getTxtNationalId().setText(emp.getNationalId());
        
        if (emp.getBirthDate() != null) {
            Date date = Date.from(emp.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            getDateBirth().setDate(date);
        } else {
            getDateBirth().setDate(null);
        }

        getTxtPhone().setText(emp.getPhoneNumber());
        getCbCategory().setSelectedItem(emp.getCategory());
        getTxtPosition().setText(emp.getPosition());
        getTxtSalary().setText(String.valueOf(emp.getGrossAnnualSalary()));

        getBtnDelete().setEnabled(true);
    }

    private void saveChanges() {
        int selectedRow = tblEmployees.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
    	
        if (selectedEmployee == null) {
            return;
        }
    	
        Employee selected = employeeList.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to terminate and delete employee #" + selected.getId() + " (" + selected.getFirstName() + " " + selected.getLastName() + ")?",
                "Confirm Termination",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteEmployee(selected.getId());
                JOptionPane.showMessageDialog(this, "Employee successfully deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadEmployees();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}