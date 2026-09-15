package com.ips2026.pl11.datos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Punto unico de acceso a la base de datos.
 *
 * <p>Usamos SQLite: toda la base de datos vive en un unico archivo
 * ({@code data/demo.db}), asi que no hace falta instalar ni arrancar ningun
 * servidor de base de datos aparte. El archivo y la tabla de ejemplo se
 * crean solos la primera vez que se arranca la aplicacion.</p>
 */
public final class ConexionBD {

    private static final Path RUTA_BD = Path.of("data", "demo.db");
    private static final String URL_BD = "jdbc:sqlite:" + RUTA_BD;

    private ConexionBD() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Abre una conexion nueva a la base de datos.
     *
     * <p>Quien llame a este metodo es responsable de cerrar la conexion,
     * normalmente con try-with-resources, para no dejar conexiones abiertas
     * sin usar.</p>
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL_BD);
    }

    /**
     * Se llama una vez, al arrancar la aplicacion: crea la carpeta y el
     * archivo de la base de datos si no existen, crea la tabla "personas"
     * si no existe, y la rellena con datos de ejemplo si esta vacia.
     */
    public static void inicializar() throws SQLException {
        try {
            Files.createDirectories(RUTA_BD.getParent());
        } catch (IOException excepcion) {
            throw new SQLException("No se pudo crear la carpeta de la base de datos", excepcion);
        }

        try (Connection conexion = obtenerConexion();
             Statement sentencia = conexion.createStatement()) {

            sentencia.execute("""
                    CREATE TABLE IF NOT EXISTS personas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT NOT NULL,
                        email TEXT NOT NULL
                    )
                    """);

            try (var resultado = sentencia.executeQuery("SELECT COUNT(*) AS total FROM personas")) {
                resultado.next();
                if (resultado.getInt("total") == 0) {
                    sentencia.execute("""
                            INSERT INTO personas (nombre, email) VALUES
                                ('Ada Lovelace', 'ada@example.com'),
                                ('Alan Turing', 'alan@example.com'),
                                ('Grace Hopper', 'grace@example.com')
                            """);
                }
            }
        }
    }
}
