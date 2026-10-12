package com.ips2026.pl11.view.employee;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class EmployeeMenuDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnButtons;
    private JButton btnAdd;
    private JButton btnModify;
    private JButton btnDelete;

    public EmployeeMenuDialog(JFrame parent) {
        super(parent, "Employee Management", true);
        setSize(480, 450);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Aplica el icono de la ventana y el fondo corporativo
        Branding.applyTo(this);

        contentPane = new JPanel();
        contentPane.setBackground(Branding.BACKGROUND);
        contentPane.setLayout(new BorderLayout(0, 0));
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

    private JButton getBtnAdd() {
        if (btnAdd == null) {
            btnAdd = new JButton("Add New Employee");
            btnAdd.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 15));
            btnAdd.setFocusPainted(false);
            btnAdd.setContentAreaFilled(false);
            btnAdd.setOpaque(true);
            btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAdd.setBackground(Color.WHITE);
            btnAdd.setForeground(Branding.NAVY);
            btnAdd.setHorizontalAlignment(SwingConstants.CENTER);
            btnAdd.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            btnAdd.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnAdd.setBackground(new Color(241, 245, 249));
                    btnAdd.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.GOLD, 1, true),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnAdd.setBackground(Color.WHITE);
                    btnAdd.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.BORDER, 1, true),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                }
            });

            btnAdd.addActionListener(e -> {
                new AddEmployeeView((JFrame) getOwner()).setVisible(true);
            });
        }
        return btnAdd;
    }

    private JButton getBtnModify() {
        if (btnModify == null) {
            btnModify = new JButton("Modify Employee");
            btnModify.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 15));
            btnModify.setFocusPainted(false);
            btnModify.setContentAreaFilled(false);
            btnModify.setOpaque(true);
            btnModify.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnModify.setBackground(Color.WHITE);
            btnModify.setForeground(Branding.NAVY);
            btnModify.setHorizontalAlignment(SwingConstants.CENTER);
            btnModify.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            btnModify.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnModify.setBackground(new Color(241, 245, 249));
                    btnModify.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.GOLD, 1, true),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnModify.setBackground(Color.WHITE);
                    btnModify.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.BORDER, 1, true),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                }
            });

            btnModify.addActionListener(e -> {
                ModifyEmployeeView modifyView = new ModifyEmployeeView(this);
                modifyView.setVisible(true);
            });
        }
        return btnModify;
    }

    private JButton getBtnDelete() {
        if (btnDelete == null) {
            btnDelete = new JButton("Delete Employee");
            btnDelete.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 15));
            btnDelete.setFocusPainted(false);
            btnDelete.setContentAreaFilled(false);
            btnDelete.setOpaque(true);
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.setBackground(Color.WHITE);
            btnDelete.setForeground(Branding.NAVY);
            btnDelete.setHorizontalAlignment(SwingConstants.CENTER);
            btnDelete.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Branding.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            btnDelete.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnDelete.setBackground(new Color(254, 226, 226));
                    btnDelete.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.ERROR, 1, true),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnDelete.setBackground(Color.WHITE);
                    btnDelete.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(Branding.BORDER, 1, true),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                }
            });

            btnDelete.addActionListener(e -> {
                DeleteEmployeeView deleteView = new DeleteEmployeeView(this);
                deleteView.setVisible(true);
            });
        }
        return btnDelete;
    }
}