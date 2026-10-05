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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.controller.employee.EmployeeController;
import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.model.employee.Employee;

public class ModifyEmployeeView extends JDialog {

    private static final long serialVersionUID = 1L;

    private final EmployeeController controller = new EmployeeController(new EmployeeDAO());
    private List<Employee> employeeList;
    private Employee selectedEmployee;

    private JPanel contentPane;
    private JPanel pnHeader;
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
    private JTextField txtBirthDate;
    private JTextField txtPhone;
    private JTextField txtPosition; 
    private JTextField txtSalary;
    private JComboBox<String> cbCategory;

    private JPanel pnActions;
    private JButton btnSave;
    private JButton btnClose;

    public ModifyEmployeeView(JDialog parent) {
        super(parent, "Modify Employee", true);
        setSize(950, 620);
        setLocationRelativeTo(parent);

        contentPane = new JPanel(new BorderLayout(0, 12));
        contentPane.setBackground(new Color(248, 250, 252));
        contentPane.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getSplitPane(), BorderLayout.CENTER);
        contentPane.add(getPnActions(), BorderLayout.SOUTH);

        loadEmployees();
    }

    private JPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new JPanel(new BorderLayout(0, 4));
            pnHeader.setOpaque(false);
            pnHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

            JLabel lblTitle = new JLabel("Modify Employee");
            lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
            lblTitle.setForeground(new Color(30, 41, 59));

            JLabel lblSubtitle = new JLabel("Select an employee from the table to edit their details. Job position cannot be altered.");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSubtitle.setForeground(new Color(100, 116, 139));
            lblSubtitle.setBorder(new EmptyBorder(0, 0, 8, 0));

            pnHeader.add(lblTitle, BorderLayout.NORTH);
            pnHeader.add(lblSubtitle, BorderLayout.SOUTH);
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
            lblTableTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            lblTableTitle.setForeground(new Color(51, 65, 85));

            pnTableContainer.add(lblTableTitle, BorderLayout.NORTH);
            pnTableContainer.add(getSpTable(), BorderLayout.CENTER);
        }
        return pnTableContainer;
    }

    private JScrollPane getSpTable() {
        if (spTable == null) {
            spTable = new JScrollPane(getTblEmployees());
            spTable.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
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
            tblEmployees.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tblEmployees.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
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
            lblFormTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            lblFormTitle.setForeground(new Color(51, 65, 85));

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

            pnFields.add(createLabel("Birth Date (YYYY-MM-DD):"));
            pnFields.add(getTxtBirthDate());

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

    private JTextField getTxtBirthDate() {
        if (txtBirthDate == null) {
            txtBirthDate = createTextField();
        }
        return txtBirthDate;
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
            txtPosition.setForeground(new Color(100, 116, 139));
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
            cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cbCategory.setBackground(Color.WHITE);
            cbCategory.setForeground(Color.BLACK);
            cbCategory.setEnabled(false);
        }
        return cbCategory;
    }

    // --- ACCIONES Y BOTONES ---
    private JPanel getPnActions() {
        if (pnActions == null) {
            pnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            pnActions.setOpaque(false);
            pnActions.add(getBtnClose());
            pnActions.add(getBtnSave());
        }
        return pnActions;
    }

    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton("Close");
            btnClose.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            btnClose.setFocusPainted(false);
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.setBackground(Color.WHITE);
            btnClose.setForeground(new Color(51, 65, 85));
            btnClose.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1, true),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
            ));
            btnClose.addActionListener(e -> dispose());
        }
        return btnClose;
    }

    private JButton getBtnSave() {
        if (btnSave == null) {
            btnSave = new JButton("Save Changes");
            btnSave.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            btnSave.setFocusPainted(false);
            btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSave.setBackground(new Color(24, 76, 120));
            btnSave.setForeground(Color.BLACK);
            btnSave.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
            btnSave.setEnabled(false); // Deshabilitado hasta que seleccionen un empleado

            btnSave.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (btnSave.isEnabled()) {
                        btnSave.setBackground(new Color(32, 101, 160));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (btnSave.isEnabled()) {
                        btnSave.setBackground(new Color(24, 76, 120));
                    }
                }
            });

            btnSave.addActionListener(e -> saveChanges());
        }
        return btnSave;
    }

    private JLabel createLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        label.setForeground(new Color(51, 65, 85));
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setEnabled(false); 
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
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
        getTxtBirthDate().setText(emp.getBirthDate().toString());
        getTxtPhone().setText(emp.getPhoneNumber());
        getCbCategory().setSelectedItem(emp.getCategory());
        getTxtPosition().setText(emp.getPosition());
        getTxtSalary().setText(String.valueOf(emp.getGrossAnnualSalary()));

        getTxtFirstName().setEnabled(true);
        getTxtLastName().setEnabled(true);
        getTxtNationalId().setEnabled(true);
        getTxtBirthDate().setEnabled(true);
        getTxtPhone().setEnabled(true);
        getCbCategory().setEnabled(false);
        getTxtSalary().setEnabled(true);

        getBtnSave().setEnabled(true);
    }

    private void saveChanges() {
        if (selectedEmployee == null) {
        	return;
        }

        try {
            LocalDate birthDate = LocalDate.parse(getTxtBirthDate().getText().trim());
            double salary = Double.parseDouble(getTxtSalary().getText().trim());

            controller.updateEmployee(
                    selectedEmployee.getId(),
                    getTxtFirstName().getText(),
                    getTxtLastName().getText(),
                    getTxtNationalId().getText(),
                    birthDate,
                    getTxtPhone().getText(),
                    (String) getCbCategory().getSelectedItem(),
                    selectedEmployee.getPosition(), 
                    salary
            );

            JOptionPane.showMessageDialog(this, "Employee updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                      
            loadEmployees();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD (e.g. 1998-05-15).", "Date Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Gross annual salary must be a valid number.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }     
    }
}