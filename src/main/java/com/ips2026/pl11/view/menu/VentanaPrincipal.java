package com.ips2026.pl11.view.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.controller.store.StoreSalesController;
import com.ips2026.pl11.data.store.MerchandiseDAO;
import com.ips2026.pl11.data.store.MerchandiseSaleDAO;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.store.StoreSalesWindow;
import com.ips2026.pl11.view.employee.AddEmployeeView;
import com.ips2026.pl11.view.employee.EmployeeMenuDialog;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 *
 * <p>Tiene la cabecera del club ({@link HeaderPanel}), cuatro
 * botones tipo "tarjeta" en una rejilla de 2x2 y un pie de pagina. Los
 * botones se iran conectando con las funcionalidades de cada sprint (el
 * boton 1 abre el registro de empleados y el boton 3 la venta de
 * merchandising); mientras no tengan funcionalidad
 * se muestran deshabilitados.</p>
 *
 * <p>El codigo sigue la generacion "lazy" de Eclipse WindowBuilder: cada
 * componente es un atributo privado que se crea la primera vez que se llama
 * a su getter, asi que la ventana se puede seguir editando desde la pestana
 * Design de WindowBuilder.</p>
 */
public class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String COMING_SOON = "Coming soon";

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnButtons;
    private MenuCardButton btnOption1;
    private MenuCardButton btnOption2;
    private MenuCardButton btnOption3;
    private MenuCardButton btnOption4;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JButton selectedButton;

    public VentanaPrincipal() {
        setTitle("Football Club Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnButtons(), BorderLayout.CENTER);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        Branding.setInitialSize(this, 1024, 720, null); // centrada en pantalla
    }

    /** Cabecera del club, version grande (solo el menu principal). */
    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Football Club Management",
                    "Main menu — choose an option to start", true);
        }
        return pnHeader;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setBorder(new EmptyBorder(24, 24, 16, 24));
            pnButtons.setLayout(new GridLayout(2, 2, 18, 18));
            pnButtons.add(getBtnOption1());
            pnButtons.add(getBtnOption2());
            pnButtons.add(getBtnOption3());
            pnButtons.add(getBtnOption4());
        }
        return pnButtons;
    }



    private MenuCardButton getBtnOption1() {
        if (btnOption1 == null) {
            btnOption1 = new MenuCardButton("Employees Management", "Register, modify and delete sports and non-sports staff");
            btnOption1.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption1.setMnemonic('E');
            
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
                new EmployeeMenuDialog(this).setVisible(true);
            });
        }
        return btnOption1;
    }

    private MenuCardButton getBtnOption2() {
        if (btnOption2 == null) {
            btnOption2 = new MenuCardButton("Option 2", COMING_SOON);
            btnOption2.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption2.setEnabled(false);
        }
        return btnOption2;
    }

    private MenuCardButton getBtnOption3() {
        if (btnOption3 == null) {
            btnOption3 = new MenuCardButton("Store Sales", "Sell merchandising products from the catalog");
            btnOption3.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption3.setMnemonic('S');
            btnOption3.addActionListener(event -> openStoreSales());
        }
        return btnOption3;
    }

    private MenuCardButton getBtnOption4() {
        if (btnOption4 == null) {
            btnOption4 = new MenuCardButton("Option 4", COMING_SOON);
            btnOption4.setFont(new Font(Branding.FONT_FAMILY, Font.PLAIN, 16));
            btnOption4.setEnabled(false);
        }
        return btnOption4;
    }

    private void openStoreSales() {
        StoreSalesController controller = new StoreSalesController(new MerchandiseDAO(), new MerchandiseSaleDAO());
        try {
            controller.loadCatalog();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The catalog could not be loaded:\n" + exception.getMessage(),
                    "Store Sales", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new StoreSalesWindow(controller, this).setVisible(true);
    }

    private JPanel getPnFooter() {
        if (pnFooter == null) {
            pnFooter = new JPanel();
            pnFooter.setOpaque(false);
            pnFooter.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(1, 0, 0, 0, Branding.BORDER),
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
}
