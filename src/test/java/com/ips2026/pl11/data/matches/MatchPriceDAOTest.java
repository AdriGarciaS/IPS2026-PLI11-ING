package com.ips2026.pl11.data.matches;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.ConexionBD;

public class MatchPriceDAOTest {

    private Connection conn;
    private MatchPriceDAO matchPriceDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        matchPriceDAO = new MatchPriceDAO();
        conn = ConexionBD.obtenerConexion();
        
        // Ejecutamos DDL y hacemos commit inmediato para liberar el cerrojo de la BD
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS matches (id INTEGER PRIMARY KEY AUTOINCREMENT, match_date TEXT);");
            stmt.execute("CREATE TABLE IF NOT EXISTS MATCH_PRICE (match_id INTEGER, grandstand TEXT, section TEXT, price REAL, PRIMARY KEY(match_id, grandstand, section));");
            stmt.execute("INSERT OR IGNORE INTO matches (id, match_date) VALUES (1, '2026-11-20');");
        }
        
        if (!conn.getAutoCommit()) {
            conn.commit();
        }
        conn.close(); // Cerramos la conexión de setup para dejar el archivo libre al DAO
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Limpieza de datos entre tests
        try (Connection c = ConexionBD.obtenerConexion();
             Statement stmt = c.createStatement()) {
            stmt.execute("DELETE FROM MATCH_PRICE;");
            stmt.execute("DELETE FROM matches WHERE id = 1;");
        }
    }

    @Test
    @DisplayName("US 35775: Save custom price for grandstand and section")
    void testSaveMatchPrice_Success() throws SQLException {
        matchPriceDAO.saveMatchPrice(1, "A", "B", 55.00);

        double price = matchPriceDAO.getPriceForMatch(1, "A", "B");
        assertEquals(55.00, price, 0.001);
    }

    @Test
    @DisplayName("US 35775: Unconfigured section returns default rate 30.00€")
    void testGetPriceForMatch_Unconfigured_ReturnsDefault30() throws SQLException {
        double price = matchPriceDAO.getPriceForMatch(1, "C", "F");
        assertEquals(30.00, price, 0.001);
    }

    @Test
    @DisplayName("US 35775: Updating price for existing match and stand overwrites value")
    void testSaveMatchPrice_UpdateExisting_OverwritesPrice() throws SQLException {
        matchPriceDAO.saveMatchPrice(1, "B", "A", 40.00);
        matchPriceDAO.saveMatchPrice(1, "B", "A", 65.00);

        double price = matchPriceDAO.getPriceForMatch(1, "B", "A");
        assertEquals(65.00, price, 0.001);
    }
}
