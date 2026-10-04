package com.ips2026.pl11.data.reservation;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.reservation.Facility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL queries to the FACILITY table.
 */
public class FacilityDAO {

    public List<Facility> findAll() throws SQLException {
        String sql = "SELECT id, name FROM FACILITY ORDER BY name";
        List<Facility> facilities = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                facilities.add(new Facility(result.getInt("id"), result.getString("name")));
            }
        }

        return facilities;
    }
}
