package com.ips2026.pl11.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 *
 * <p>Tiene una cabecera (logo + titulo), cuatro botones en una rejilla de
 * 2x2 y un pie de pagina. De momento los botones no hacen nada; se iran conectando con las
 * funcionalidades de cada sprint.</p>
 *
 * <p>El codigo sigue la generacion "lazy" de Eclipse WindowBuilder: cada
 * componente es un atributo privado que se crea la primera vez que se llama
 * a su getter, asi que la ventana se puede seguir editando desde la pestana
 * Design de WindowBuilder.</p>
 */
public class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private JButton selectedButton;

    private JPanel contentPane;
    private JPanel pnHeader;
    private JLabel lblLogo;
    private JLabel lblTitle;
    private JPanel pnButtons;
    private JButton btnOption1;
    private JButton btnOption2;
    private JButton btnOption3;
    private JButton btnOption4;
    private JPanel pnFooter;
    private JLabel lblFooter;

    public VentanaPrincipal() {
        setTitle("IPS2026-PL11-ING");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 450);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(0, 15));
        setContentPane(contentPane);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }

    private JPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new JPanel();
            pnHeader.setLayout(new BorderLayout(15, 0));
            pnHeader.add(getLblLogo(), BorderLayout.WEST);
            pnHeader.add(getLblTitle(), BorderLayout.CENTER);
        }
        return pnHeader;
    }

    private JLabel getLblLogo() {
        if (lblLogo == null) {
            lblLogo = new JLabel("LOGO");
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            lblLogo.setPreferredSize(new Dimension(80, 80));
            lblLogo.setBorder(new LineBorder(Color.GRAY));
        }
        return lblLogo;
    }

    private JLabel getLblTitle() {
        if (lblTitle == null) {
            lblTitle = new JLabel("Football Club Management");
            lblTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 26));
            lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return lblTitle;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setLayout(new GridLayout(2, 2, 15, 15));
            pnButtons.add(getBtnOption1());
            pnButtons.add(getBtnOption2());
            pnButtons.add(getBtnOption3());
            pnButtons.add(getBtnOption4());
        }
        return pnButtons;
    }

    private JButton getBtnOption1() {
        if (btnOption1 == null) {
            btnOption1 = new JButton("Employees Management");
            btnOption1.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
            btnOption1.setFocusPainted(false);
            btnOption1.setContentAreaFilled(false);
            btnOption1.setOpaque(true);
            btnOption1.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOption1.setBackground(new Color(225, 238, 252));
            btnOption1.setForeground(Color.BLACK);
            btnOption1.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(140, 185, 235), 2, true),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            selectedButton = btnOption1;

            btnOption1.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedButton != btnOption1) {
                        btnOption1.setBackground(new Color(241, 245, 249));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedButton != btnOption1) {
                        btnOption1.setBackground(Color.WHITE);
                    }
                }
            });

            btnOption1.addActionListener(e -> {
                activateButton(btnOption1);
                new EmployeeMenuDialog(this).setVisible(true);
            });
        }
        return btnOption1;
    }

    private JButton getBtnOption2() {
        if (btnOption2 == null) {
            btnOption2 = new JButton("Option 2");
            btnOption2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
            btnOption2.setFocusPainted(false);
            btnOption2.setContentAreaFilled(false);
            btnOption2.setOpaque(true);
            btnOption2.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOption2.setBackground(Color.WHITE);
            btnOption2.setForeground(Color.BLACK);
            btnOption2.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));

            btnOption2.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedButton != btnOption2) {
                        btnOption2.setBackground(new Color(241, 245, 249));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedButton != btnOption2) {
                        btnOption2.setBackground(Color.WHITE);
                    }
                }
            });

            btnOption2.addActionListener(e -> activateButton(btnOption2));
        }
        return btnOption2;
    }

    private JButton getBtnOption3() {
        if (btnOption3 == null) {
            btnOption3 = new JButton("Option 3");
            btnOption3.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
            btnOption3.setFocusPainted(false);
            btnOption3.setContentAreaFilled(false);
            btnOption3.setOpaque(true);
            btnOption3.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOption3.setBackground(Color.WHITE);
            btnOption3.setForeground(Color.BLACK);
            btnOption3.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));

            btnOption3.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedButton != btnOption3) {
                        btnOption3.setBackground(new Color(241, 245, 249));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedButton != btnOption3) {
                        btnOption3.setBackground(Color.WHITE);
                    }
                }
            });

            btnOption3.addActionListener(e -> activateButton(btnOption3));
        }
        return btnOption3;
    }

    private JButton getBtnOption4() {
        if (btnOption4 == null) {
            btnOption4 = new JButton("Option 4");
            btnOption4.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
            btnOption4.setFocusPainted(false);
            btnOption4.setContentAreaFilled(false);
            btnOption4.setOpaque(true);
            btnOption4.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOption4.setBackground(Color.WHITE);
            btnOption4.setForeground(Color.BLACK);
            btnOption4.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));

            btnOption4.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (selectedButton != btnOption4) {
                        btnOption4.setBackground(new Color(241, 245, 249));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (selectedButton != btnOption4) {
                        btnOption4.setBackground(Color.WHITE);
                    }
                }
            });

            btnOption4.addActionListener(e -> activateButton(btnOption4));
        }
        return btnOption4;
    }
        

    private void activateButton(JButton buttonToActivate) {
        JButton[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};
        for (JButton btn : buttons) {
            if (btn != null) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(51, 65, 85));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(226, 232, 240), 1, true),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
            }
        }

        buttonToActivate.setBackground(new Color(24, 76, 120));
        buttonToActivate.setForeground(Color.WHITE);
        buttonToActivate.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        this.selectedButton = buttonToActivate;
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel();
            pnFooter.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
            pnFooter.setLayout(new BorderLayout(0, 0));
            pnFooter.add(getLblFooter(), BorderLayout.CENTER);
        }
        return pnFooter;
    }

    private JLabel getLblFooter() {
        if (lblFooter == null) {
            lblFooter = new JLabel("IPS 2026 - Team PL11");
            lblFooter.setBorder(new EmptyBorder(5, 0, 0, 0));
            lblFooter.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
            lblFooter.setForeground(Color.GRAY);
            lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return lblFooter;
    }
}