package com.ips2026.pl11.data;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper for the DAO tests: creates a new database in a temporary folder
 * (so {@code data/demo.db} is never touched) with the real
 * {@code schema.sql}, and runs SQL on it.
 */
public final class TestDatabase {

    private TestDatabase() {
        // Utility class: not instantiated.
    }

    /**
     * Creates an empty database in {@code folder}: same tables as the
     * application but without the sample merchandising data.
     */
    public static void create(Path folder) throws SQLException {
        System.setProperty(ConexionBD.PROPIEDAD_RUTA, folder.resolve("test.db").toString());
        ConexionBD.inicializar();
        execute("DELETE FROM MERCHANDISING_SALE");
        execute("DELETE FROM MERCHANDISING");
    }

    /** Goes back to the normal database of the application. */
    public static void close() {
        System.clearProperty(ConexionBD.PROPIEDAD_RUTA);
    }

    public static void execute(String sql) throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    /** Runs a query that returns a single integer (for example a COUNT). */
    public static int queryInt(String sql) throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }
}
