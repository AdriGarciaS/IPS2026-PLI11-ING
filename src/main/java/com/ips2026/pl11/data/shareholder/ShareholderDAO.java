package com.ips2026.pl11.data.shareholder;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.shareholder.Shareholder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL queries to the SHAREHOLDER table.
 */
public class ShareholderDAO {

    /** Every shareholder, ordered by surname and name. */
    public List<Shareholder> findAll() throws SQLException {
        String sql = "SELECT id, first_name, last_name, national_id FROM SHAREHOLDER ORDER BY last_name, first_name";
        List<Shareholder> shareholders = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                shareholders.add(new Shareholder(
                        result.getInt("id"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getString("national_id")
                ));
            }
        }
        return shareholders;
    }
}
