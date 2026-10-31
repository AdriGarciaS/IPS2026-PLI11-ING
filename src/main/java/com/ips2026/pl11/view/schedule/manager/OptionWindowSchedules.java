package com.ips2026.pl11.view.schedule.manager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MenuCardButton;
import com.ips2026.pl11.view.schedule.ScheduleWindow;

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
public class OptionWindowSchedules extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private DayScheduleWindow managerWorkScheduleWindow = new DayScheduleWindow(this);
    private GardeningScheduleWindow managerGardeningScheduleWindow = new GardeningScheduleWindow(this);

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnFooter;
    private JLabel lblFooter;
    private JButton selectedButton;
    
    private ScheduleWindow sw;
    
    private JPanel pnBody;
    private JPanel pnPositions;
    private MenuCardButton btnGeneralManager;
    private MenuCardButton btnManager;
    private JButton btnReturn;
    private MenuCardButton btnNonSportingEmp;

    public OptionWindowSchedules(ScheduleWindow sw) {
        setResizable(false);
        this.sw = sw;
        
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
        
        setLocationRelativeTo(sw);
         
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Manager: Schedule Management",
                    "Select an option", false);
        }
        return pnHeader;
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
        	pnPositions.setBounds(10, 0, 608, 296);
        	pnPositions.setOpaque(false);
        	pnPositions.setBorder(new EmptyBorder(24, 24, 16, 24));
        	pnPositions.setLayout(new GridLayout(0, 1, 0, 0));
        	pnPositions.add(getBtnOption1_1());
        	pnPositions.add(getBtnOption2_1());
        }
        return pnPositions;
    }
    private MenuCardButton getBtnOption2_1() {
        if (btnGeneralManager == null) {
        	btnGeneralManager = new MenuCardButton("Schedule gardening sessions", "for available club facilities");
        	btnGeneralManager.addActionListener(new ActionListener() {
        	    @Override
                public void actionPerformed(ActionEvent e) {
        	        managerGardeningScheduleWindow.loadFacilities();
        	        managerGardeningScheduleWindow.loadGardeners();
        	        managerGardeningScheduleWindow.setVisible(true);
        	        
        	    }
        	});
        	btnGeneralManager.setFont(new Font("Tahoma", Font.PLAIN, 16));
        }
        return btnGeneralManager;
    }
    private MenuCardButton getBtnOption1_1() {
        if (btnManager == null) {
        	btnManager =  new MenuCardButton("Add specific day schedules", "for non sporting employees");
        	btnManager.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    managerWorkScheduleWindow.refreshShiftsTable();
                    managerWorkScheduleWindow.refreshWorkersTable();
                    managerWorkScheduleWindow.setVisible(true);
        	    }
        	});
        	btnManager.setFont(new Font("Tahoma", Font.PLAIN, 16));
        	btnManager.setEnabled(true);
        }
        return btnManager;
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
    
    // ---- auxiliary methods ----
    
    
    
   
}
