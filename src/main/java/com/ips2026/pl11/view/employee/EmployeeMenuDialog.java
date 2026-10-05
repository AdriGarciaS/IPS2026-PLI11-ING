package com.ips2026.pl11.view.employee;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EmployeeMenuDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JPanel pnHeader;
    private JPanel pnButtons;
    private JButton btnAdd;
    private JButton btnModify;
    private JButton btnDelete;

    public EmployeeMenuDialog(JFrame parent) {
        super(parent, "Employee Management", true);
        setSize(480, 420);
        setLocationRelativeTo(parent);
        setResizable(false);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(248, 250, 252));
        contentPane.setBorder(new EmptyBorder(25, 30, 25, 30));
        contentPane.setLayout(new BorderLayout(0, 20));
        setContentPane(contentPane);

        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
    }

    private JPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new JPanel();
            pnHeader.setOpaque(false);
            pnHeader.setLayout(new BorderLayout(0, 6));
            pnHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

            JLabel lblTitle = new JLabel("Employee Operations");
            lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
            lblTitle.setForeground(new Color(30, 41, 59));

            JLabel lblSubtitle = new JLabel("Select an operation to manage club personnel");
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSubtitle.setForeground(new Color(100, 116, 139));
            lblSubtitle.setBorder(new EmptyBorder(0, 0, 10, 0));

            pnHeader.add(lblTitle, BorderLayout.NORTH);
            pnHeader.add(lblSubtitle, BorderLayout.SOUTH);
        }
        return pnHeader;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
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
            btnAdd.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
            btnAdd.setFocusPainted(false);
            btnAdd.setContentAreaFilled(false);
            btnAdd.setOpaque(true);
            btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAdd.setBackground(Color.WHITE);
            btnAdd.setForeground(Color.BLACK);
            btnAdd.setHorizontalAlignment(SwingConstants.CENTER);
            btnAdd.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            btnAdd.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnAdd.setBackground(new Color(225, 238, 252));
                    btnAdd.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(140, 185, 235), 1, true),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnAdd.setBackground(Color.WHITE);
                    btnAdd.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(226, 232, 240), 1, true),
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
            btnModify.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
            btnModify.setFocusPainted(false);
            btnModify.setContentAreaFilled(false);
            btnModify.setOpaque(true);
            btnModify.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnModify.setBackground(Color.WHITE);
            btnModify.setForeground(Color.BLACK);
            btnModify.setHorizontalAlignment(SwingConstants.CENTER);
            btnModify.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            btnModify.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnModify.setBackground(new Color(225, 238, 252));
                    btnModify.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(140, 185, 235), 1, true),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnModify.setBackground(Color.WHITE);
                    btnModify.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(226, 232, 240), 1, true),
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
            btnDelete.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
            btnDelete.setFocusPainted(false);
            btnDelete.setContentAreaFilled(false);
            btnDelete.setOpaque(true);
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.setBackground(Color.WHITE);
            btnDelete.setForeground(Color.BLACK);
            btnDelete.setHorizontalAlignment(SwingConstants.CENTER);
            btnDelete.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            btnDelete.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnDelete.setBackground(new Color(254, 226, 226)); // Rojo suave para acción destructiva
                    btnDelete.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(248, 113, 113), 1, true),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btnDelete.setBackground(Color.WHITE);
                    btnDelete.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(new Color(226, 232, 240), 1, true),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)
                    ));
                }
            });

            btnDelete.addActionListener(e -> {
                // Abre la tabla donde seleccionas y eliminas
            	DeleteEmployeeView deleteView = new DeleteEmployeeView(this);
                deleteView.setVisible(true);
            });
        }
        return btnDelete;
    }
}
