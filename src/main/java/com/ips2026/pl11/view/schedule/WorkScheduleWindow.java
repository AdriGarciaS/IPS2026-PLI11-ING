package com.ips2026.pl11.view.schedule;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import com.ips2026.pl11.controller.reservation.FacilityReservationController;
import com.ips2026.pl11.controller.store.StoreSalesController;
import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.data.store.MerchandiseDAO;
import com.ips2026.pl11.data.store.MerchandiseSaleDAO;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MainWindow;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.reservation.FacilityReservationWindow;
import com.ips2026.pl11.view.store.StoreSalesWindow;

/**
 * Ventana principal (la "V" de MVC): menu principal de la aplicacion.
 *
 * <p>Tiene la cabecera del club ({@link HeaderPanel}), cuatro
 * botones tipo "tarjeta" en una rejilla de 2x2 y un pie de pagina. Los
 * botones se iran conectando con las funcionalidades de cada sprint (el
 * boton 1 abre el registro de empleados, el boton 3 la venta de
 * merchandising y el boton 4 la reserva de instalaciones); mientras no
 * tengan funcionalidad se muestran deshabilitados.</p>
 *
 * <p>El codigo sigue la generacion "lazy" de Eclipse WindowBuilder: cada
 * componente es un atributo privado que se crea la primera vez que se llama
 * a su getter, asi que la ventana se puede seguir editando desde la pestana
 * Design de WindowBuilder.</p>
 */
public class WorkScheduleWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JButton selectedButton;
    
    private MainWindow mw;
    private GMWorkScheduleWindow gmWorkScheduleWindow = new GMWorkScheduleWindow(this);
    private ManagerWorkScheduleWindow managerWorkScheduleWindow = new ManagerWorkScheduleWindow(this);
    
    private JPanel pnBody;
    private JPanel pnPositions;
    private MenuCardButton btnOption1;
    private MenuCardButton btnOption2;
    private JButton btnReturn;

    public WorkScheduleWindow(MainWindow mw) {
        setResizable(false);
        this.mw = mw;
        
        setTitle("Football Club Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnFooter(), BorderLayout.SOUTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        
        setLocationRelativeTo(mw);
         
    }

    /** Cabecera del club, version grande (solo el menu principal). */
    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Work Schedule Management",
                    "Select your work position", false);
        }
        return pnHeader;
    }

    private void openFacilityReservations() {
        FacilityReservationController controller = new FacilityReservationController(
                new FacilityDAO(), new TeamUseDAO(), new ReservationDAO());
        try {
            controller.loadFacilities();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The facilities could not be loaded:\n" + exception.getMessage(),
                    "Facility Reservations", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new FacilityReservationWindow(controller, this).setVisible(true);
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
	
    private JPanel getPnBody() {
        if (pnBody == null) {
        	pnBody = new JPanel();
        	pnBody.setLayout(null);
        	pnBody.add(getPnButtons_1());
        	pnBody.add(getBtnReturn());
        }
        return pnBody;
    }
    private JPanel getPnButtons_1() {
        if (pnPositions == null) {
        	pnPositions = new JPanel();
        	pnPositions.setBounds(10, 11, 608, 252);
        	pnPositions.setOpaque(false);
        	pnPositions.setBorder(new EmptyBorder(24, 24, 16, 24));
        	pnPositions.setLayout(new GridLayout(0, 1, 0, 0));
        	pnPositions.add(getBtnOption1_1());
        	pnPositions.add(getBtnOption2_1());
        }
        return pnPositions;
    }
    private MenuCardButton getBtnOption1_1() {
        if (btnOption1 == null) {
        	btnOption1 = new MenuCardButton("General Manager\r\n", "Add recurring schedules to non sporting employees");
        	btnOption1.addActionListener(new ActionListener() {
        	    @Override
                public void actionPerformed(ActionEvent e) {
        	        gmWorkScheduleWindow.setVisible(true);
        	    }
        	});
        	btnOption1.setFont(new Font("Tahoma", Font.PLAIN, 16));
        }
        return btnOption1;
    }
    private MenuCardButton getBtnOption2_1() {
        if (btnOption2 == null) {
        	btnOption2 = new MenuCardButton("Manager", "Add specific day schedules to non sporting employees");
        	btnOption2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
        	        managerWorkScheduleWindow.setVisible(true);
        	    }
        	});
        	btnOption2.setFont(new Font("Tahoma", Font.PLAIN, 16));
        	btnOption2.setEnabled(true);
        }
        return btnOption2;
    }
    private JButton getBtnReturn() {
        if (btnReturn == null) {
        	btnReturn = new JButton("Return");
        	btnReturn.addActionListener(new ActionListener() {
        	    @Override
                public void actionPerformed(ActionEvent e) {
        	        dispose();
        	    }
        	});
        	btnReturn.setMnemonic('R');
        	btnReturn.setBounds(529, 302, 89, 23);
        }
        return btnReturn;
    }
}
