package com.ips2026.pl11.view.employee;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MenuCardButton;

public class EmployeeMenuDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnButtons;
    private MenuCardButton btnAdd;
    private MenuCardButton btnModify;
    private MenuCardButton btnDelete;

    public EmployeeMenuDialog(JFrame parent) {
        super(parent, "Employee Management", true);
        setSize(480, 480);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Aplica el icono de la ventana y el fondo corporativo
        Branding.applyTo(this);

        contentPane = new JPanel(new BorderLayout(0, 0));
        contentPane.setBackground(Branding.BACKGROUND);
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(
                    "Employee Operations",
                    "Select an operation to manage club personnel"
            );
        }
        return pnHeader;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setBorder(new EmptyBorder(24, 30, 24, 30));
            pnButtons.setLayout(new GridLayout(3, 1, 0, 16));
            pnButtons.add(getBtnAdd());
            pnButtons.add(getBtnModify());
            pnButtons.add(getBtnDelete());
        }
        return pnButtons;
    }

    private MenuCardButton getBtnAdd() {
        if (btnAdd == null) {
            btnAdd = new MenuCardButton(
                    "Add New Employee",
                    "Register a new coach, player, or administrative club staff"
            );
            btnAdd.addActionListener(e -> {
                new AddEmployeeView((JFrame) getOwner()).setVisible(true);
            });
        }
        return btnAdd;
    }

    private MenuCardButton getBtnModify() {
        if (btnModify == null) {
            btnModify = new MenuCardButton(
                    "Modify Employee",
                    "Update personal, contractual, and role details of registered staff"
            );
            btnModify.addActionListener(e -> {
                ModifyEmployeeView modifyView = new ModifyEmployeeView(this);
                modifyView.setVisible(true);
            });
        }
        return btnModify;
    }

    private MenuCardButton getBtnDelete() {
        if (btnDelete == null) {
            btnDelete = new MenuCardButton(
                    "Delete Employee",
                    "Remove personnel and finalize contract terminations"
            );
            btnDelete.addActionListener(e -> {
                DeleteEmployeeView deleteView = new DeleteEmployeeView(this);
                deleteView.setVisible(true);
            });
        }
        return btnDelete;
    }
}