package com.ips2026.pl11;

import com.ips2026.pl11.datos.ConexionBD;
import com.ips2026.pl11.datos.EmployeeDAO;
import com.ips2026.pl11.modelo.Employee;
import com.ips2026.pl11.vista.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.sql.SQLException;
import java.util.List;

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
    	
    	try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
    	catch (Exception ignored) {
        }
    	
        SwingUtilities.invokeLater(App::iniciarAplicacion);
    }

    private static void iniciarAplicacion() {
        try {
            ConexionBD.inicializar();
            imprimirEmpleadosIniciales();
        } catch (SQLException excepcion) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo inicializar la base de datos:\n" + excepcion.getMessage(),
                    "Error al arrancar",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new VentanaPrincipal().setVisible(true);
    }
    
    private static void imprimirEmpleadosIniciales() {
        try {
            EmployeeDAO employeeDAO = new EmployeeDAO();
            List<Employee> employees = employeeDAO.getAll();

            System.out.println("       CURRENT EMPLOYEES IN DATABASE (STARTUP)      ");
            System.out.println("====================================================");

            if (employees.isEmpty()) {
                System.out.println("No employees found in the database.");
            } else {
                for (Employee emp : employees) {
                    System.out.printf("[%d] %s %s | DNI: %s | Role: %s (%s) \n" ,
                            emp.getId(),
                            emp.getFirstName(),
                            emp.getLastName(),
                            emp.getNationalId(),
                            emp.getPosition(),
                            emp.getCategory());
                }
            }

        } catch (SQLException e) {
            System.err.println("Error reading employees on startup: " + e.getMessage());
        }
    }
}
