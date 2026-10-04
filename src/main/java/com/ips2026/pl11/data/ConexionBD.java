package com.ips2026.pl11.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Punto unico de acceso a la base de datos.
 *
 * <p>Usamos SQLite: toda la base de datos vive en un unico archivo
 * ({@code data/demo.db}), asi que no hace falta instalar ni arrancar ningun
 * servidor de base de datos aparte.</p>
 *
 * <p>Al arrancar se ejecutan los scripts de {@code src/main/resources/db/}:
 * {@code schema.sql} (creacion de tablas) y, si la base de datos es nueva,
 * {@code data.sql} (datos de ejemplo). Segun {@code config.properties}, la
 * base de datos se mantiene entre ejecuciones o se borra al cerrar la
 * aplicacion.</p>
 *
 * <p>La ruta del archivo se puede cambiar con la propiedad del sistema
 * {@code bd.ruta} (por ejemplo, los tests usan una base de datos temporal
 * para no tocar {@code data/demo.db}).</p>
 */
public final class ConexionBD {

    private static final Path RUTA_POR_DEFECTO = Path.of("data", "demo.db");
    public static final String PROPIEDAD_RUTA = "bd.ruta";

    private static final String SCRIPT_ESQUEMA = "/db/schema.sql";
    private static final String SCRIPT_DATOS = "/db/data.sql";
    private static final String ARCHIVO_CONFIG = "/db/config.properties";
    private static final String PROPIEDAD_BORRAR_AL_CERRAR = "bd.borrarAlCerrar";

    private ConexionBD() {
        // Clase de utilidades: no se instancia.
    }

    /** Ruta del archivo de la base de datos: {@code bd.ruta} si esta definida, si no {@code data/demo.db}. */
    private static Path rutaBD() {
        String ruta = System.getProperty(PROPIEDAD_RUTA);
        return ruta == null || ruta.isBlank() ? RUTA_POR_DEFECTO : Path.of(ruta);
    }

    /**
     * Abre una conexion nueva a la base de datos.
     *
     * <p>Quien llame a este metodo es responsable de cerrar la conexion,
     * normalmente con try-with-resources, para no dejar conexiones abiertas
     * sin usar.</p>
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + rutaBD());
    }

    /**
     * Se llama una vez, al arrancar la aplicacion: crea la base de datos a
     * partir de {@code schema.sql}, carga {@code data.sql} si la base de
     * datos es nueva y, si esta configurado, registra su borrado al cerrar.
     */
    public static void inicializar() throws SQLException {
        boolean borrarAlCerrar = debeBorrarseAlCerrar();
        Path rutaBD = rutaBD();

        try {
            if (rutaBD.getParent() != null) {
                Files.createDirectories(rutaBD.getParent());
            }
            if (borrarAlCerrar) {
                // Por si una ejecucion anterior termino de forma brusca y no
                // llego a borrar el archivo: siempre empezamos desde cero.
                Files.deleteIfExists(rutaBD);
            }
        } catch (IOException excepcion) {
            throw new SQLException("No se pudo preparar el archivo de la base de datos", excepcion);
        }

        boolean esNueva = Files.notExists(rutaBD);

        try (Connection conexion = obtenerConexion()) {
            ejecutarScript(conexion, SCRIPT_ESQUEMA);
            if (esNueva) {
                ejecutarScript(conexion, SCRIPT_DATOS);
            }
        }

        if (borrarAlCerrar) {
            // Los shutdown hooks se ejecutan al cerrar la JVM (por ejemplo,
            // al cerrar la ventana con EXIT_ON_CLOSE, que llama a System.exit).
            Runtime.getRuntime().addShutdownHook(new Thread(() -> borrarBaseDeDatos(rutaBD)));
        }
    }

    /**
     * Lee la opcion {@code bd.borrarAlCerrar}: primero de las propiedades del
     * sistema ({@code -Dbd.borrarAlCerrar=true}) y, si no esta, de
     * {@code config.properties}. Por defecto la base de datos se mantiene.
     */
    private static boolean debeBorrarseAlCerrar() throws SQLException {
        String valor = System.getProperty(PROPIEDAD_BORRAR_AL_CERRAR);
        if (valor == null) {
            Properties config = new Properties();
            try (InputStream entrada = ConexionBD.class.getResourceAsStream(ARCHIVO_CONFIG)) {
                if (entrada != null) {
                    config.load(entrada);
                }
            } catch (IOException excepcion) {
                throw new SQLException("No se pudo leer " + ARCHIVO_CONFIG, excepcion);
            }
            valor = config.getProperty(PROPIEDAD_BORRAR_AL_CERRAR, "false");
        }
        return Boolean.parseBoolean(valor.trim());
    }

    /**
     * Ejecuta un script SQL del classpath, sentencia a sentencia (separadas
     * por ";"). Las lineas que empiezan por "--" se tratan como comentarios.
     * Todo el script va en una transaccion: o se aplica entero o nada.
     */
    private static void ejecutarScript(Connection conexion, String rutaScript) throws SQLException {
        String contenido;
        try (InputStream entrada = ConexionBD.class.getResourceAsStream(rutaScript)) {
            if (entrada == null) {
                throw new SQLException("No se encontro el script " + rutaScript);
            }
            contenido = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException excepcion) {
            throw new SQLException("No se pudo leer el script " + rutaScript, excepcion);
        }

        StringBuilder sinComentarios = new StringBuilder();
        for (String linea : contenido.split("\\R")) {
            if (!linea.strip().startsWith("--")) {
                sinComentarios.append(linea).append('\n');
            }
        }

        boolean autoCommitPrevio = conexion.getAutoCommit();
        conexion.setAutoCommit(false);
        try (Statement sentencia = conexion.createStatement()) {
            for (String sql : sinComentarios.toString().split(";")) {
                if (!sql.isBlank()) {
                    sentencia.execute(sql);
                }
            }
            conexion.commit();
        } catch (SQLException excepcion) {
            conexion.rollback();
            throw new SQLException("Error ejecutando " + rutaScript + ": " + excepcion.getMessage(), excepcion);
        } finally {
            conexion.setAutoCommit(autoCommitPrevio);
        }
    }

    private static void borrarBaseDeDatos(Path rutaBD) {
        try {
            Files.deleteIfExists(rutaBD);
        } catch (IOException excepcion) {
            System.err.println("No se pudo borrar la base de datos " + rutaBD + ": " + excepcion.getMessage());
        }
    }
}
