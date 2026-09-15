package com.ips2026.pl11.datos;

import com.ips2026.pl11.modelo.Persona;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class PersonaDAO {

    public List<Persona> obtenerTodas() throws SQLException {
        String sql = "SELECT id, nombre, email FROM personas ORDER BY id";
        List<Persona> personas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {

            while (resultado.next()) {
                personas.add(new Persona(
                        resultado.getInt("id"),
                        resultado.getString("nombre"),
                        resultado.getString("email")
                ));
            }
        }

        return personas;
    }
}
