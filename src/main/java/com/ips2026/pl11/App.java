
package com.ips2026.pl11;

import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.menu.MainWindow;

/**
 * Punto de entrada de la aplicacion.
 *
 * <p>Al arrancar: inicializa la base de datos (crea el archivo y la tabla
 * de ejemplo si no existen) y despues abre la ventana principal con el
 * menu de la aplicacion. A partir de aqui se
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
        // Aspecto comun de todas las ventanas (Nimbus con los colores del club).
        Branding.installLookAndFeel();

        try {
            ConexionBD.inicializar();
        } catch (SQLException excepcion) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo inicializar la base de datos:\n" + excepcion.getMessage(),
                    "Error al arrancar",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new MainWindow().setVisible(true);
    }
}

