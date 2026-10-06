package com.ips2026.pl11.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

    private JPanel contentPane;
    private JPanel pnHeader;
    private JLabel lblLogo;
    private JLabel lblTitle;
    private JPanel pnButtons;
    private JButton btnGeneralManager;
    private JButton btnOption2;
    private JButton btnOption3;
    private JButton btnOption4;
    private JPanel pnFooter;
    private JLabel lblFooter;
    
    private GMWorkScheduleWindow gmWorkScheduleWindow = new GMWorkScheduleWindow(this);

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
        setLocationRelativeTo(null); // centra la ventana en pantalla
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

    /**
     * Hueco reservado para el logo del club. Cuando tengamos la imagen,
     * basta con quitar el texto y el borde y usar setIcon(...).
     */
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
            lblTitle.setFont(new Font("Tahoma", Font.BOLD, 26));
            lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return lblTitle;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setLayout(new GridLayout(2, 2, 15, 15));
            pnButtons.add(getBtnOption1());
            pnButtons.add(getBtnGeneralManager());
            pnButtons.add(getBtnOption3());
            pnButtons.add(getBtnOption4());
        }
        return pnButtons;
    }

    private JButton getBtnOption1() {
        if (btnOption2 == null) {
            btnOption2 = new JButton("Option 1");
            btnOption2.setFont(new Font("Tahoma", Font.PLAIN, 16));
        }
        return btnOption2;
    }
    
    private JButton getBtnGeneralManager() {
        if (btnGeneralManager == null) {
            btnGeneralManager = new JButton("General Manager");
            btnGeneralManager.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    
                    gmWorkScheduleWindow.setVisible(true);
                }
            });
            btnGeneralManager.setFont(new Font("Tahoma", Font.PLAIN, 16));
        }
        return btnGeneralManager;
    }


    private JButton getBtnOption3() {
        if (btnOption3 == null) {
            btnOption3 = new JButton("Option 3");
            btnOption3.setFont(new Font("Tahoma", Font.PLAIN, 16));
        }
        return btnOption3;
    }

    private JButton getBtnOption4() {
        if (btnOption4 == null) {
            btnOption4 = new JButton("Option 4");
            btnOption4.setFont(new Font("Tahoma", Font.PLAIN, 16));
        }
        return btnOption4;
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
            lblFooter.setFont(new Font("Tahoma", Font.PLAIN, 11));
            lblFooter.setForeground(Color.GRAY);
            lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return lblFooter;
    }
}
