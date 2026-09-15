package com.ips2026.pl11;

import com.ips2026.pl11.controlador.PersonaControlador;
import com.ips2026.pl11.datos.ConexionBD;
import com.ips2026.pl11.datos.PersonaDAO;
import com.ips2026.pl11.vista.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.SQLException;

/**
 * Punto de entrada de la aplicacion.
 *
 * <p>Al arrancar: inicializa la base de datos (crea el archivo y la tabla
 * de ejemplo si no existen) y despues abre la ventana principal, montando
 * las tres capas del MVC (modelo, vista y controlador). A partir de aqui se
 * ira ampliando con las funcionalidades de las user stories de cada
 * sprint.</p>
 */
public final class App {

    private App() {
        // Clase de arranque: no se instancia.
    }

    public static void main(String[] args) {
        // Las interfaces Swing deben crearse y modificarse siempre en el
        // Event Dispatch Thread (EDT), no en el hilo main. invokeLater()
        // encola el arranque en ese hilo; es la forma estandar de arrancar
        // cualquier aplicacion Swing.
        SwingUtilities.invokeLater(App::iniciarAplicacion);
    }

    private static void iniciarAplicacion() {
        try {
            ConexionBD.inicializar();
        } catch (SQLException excepcion) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo inicializar la base de datos:\n" + excepcion.getMessage(),
                    "Error al arrancar",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        PersonaControlador controlador = new PersonaControlador(new PersonaDAO());
        new VentanaPrincipal(controlador).setVisible(true);
    }
}
