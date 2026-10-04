package com.ips2026.pl11.data.store;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.store.Merchandise;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL queries to the MERCHANDISING table.
 */
public class MerchandiseDAO {

    public List<Merchandise> findAll() throws SQLException {
        String sql = "SELECT id, name, type, available_units, price FROM MERCHANDISING ORDER BY name";
        List<Merchandise> products = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                products.add(new Merchandise(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("type"),
                        result.getInt("available_units"),
                        BigDecimal.valueOf(result.getDouble("price")).setScale(2, RoundingMode.HALF_UP)
                ));
            }
        }

        return products;
    }

    /** Distinct product types, in alphabetical order (for the type filter). */
    public List<String> findTypes() throws SQLException {
        String sql = "SELECT DISTINCT type FROM MERCHANDISING ORDER BY type";
        List<String> types = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                types.add(result.getString("type"));
            }
        }

        return types;
    }
}
