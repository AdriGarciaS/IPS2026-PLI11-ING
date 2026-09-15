package com.ips2026.pl11.vista;

import com.ips2026.pl11.controlador.PersonaControlador;
import com.ips2026.pl11.modelo.Persona;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.List;

/**
 * Ventana principal (la "V" de MVC).
 *
 * <p>Solo construye los componentes de Swing y reacciona a los eventos
 * (como pulsar el boton); no sabe nada de SQL. Para obtener datos siempre
 * le pregunta al controlador.</p>
 */
public class VentanaPrincipal extends JFrame {

    private final PersonaControlador controlador;
    private final DefaultTableModel modeloTabla;

    public VentanaPrincipal(PersonaControlador controlador) {
        super("IPS2026-PL11-ING");
        this.controlador = controlador;
        this.modeloTabla = new DefaultTableModel(new Object[]{"Id", "Nombre", "Email"}, 0);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null); // centra la ventana en pantalla
        setLayout(new BorderLayout(10, 10));

        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelDatos(), BorderLayout.CENTER);
    }

    private JPanel crearPanelSuperior() {
        JLabel etiquetaEstado = new JLabel("La aplicacion funciona correctamente", SwingConstants.CENTER);

        JButton botonMostrar = new JButton("Mostrar datos base de datos");
        botonMostrar.addActionListener(evento -> cargarDatos());

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(etiquetaEstado, BorderLayout.NORTH);
        panel.add(botonMostrar, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane crearPanelDatos() {
        JTable tabla = new JTable(modeloTabla);
        return new JScrollPane(tabla);
    }

    /**
     * Pide al controlador los datos de la base de datos y los muestra en la
     * tabla de debajo del boton. Si algo falla (por ejemplo, no se puede
     * leer la base de datos), se avisa con un dialogo en vez de romper la
     * aplicacion.
     */
    private void cargarDatos() {
        try {
            List<Persona> personas = controlador.obtenerPersonas();
            modeloTabla.setRowCount(0);
            for (Persona persona : personas) {
                modeloTabla.addRow(new Object[]{persona.getId(), persona.getNombre(), persona.getEmail()});
            }
        } catch (SQLException excepcion) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron leer los datos de la base de datos:\n" + excepcion.getMessage(),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
