package com.ips2026.pl11.controller;

import com.ips2026.pl11.datos.PersonaDAO;
import com.ips2026.pl11.model.Persona;

import java.sql.SQLException;
import java.util.List;


public class PersonaControlador {

    private final PersonaDAO personaDAO;

    public PersonaControlador(PersonaDAO personaDAO) {
        this.personaDAO = personaDAO;
    }

    public List<Persona> obtenerPersonas() throws SQLException {
        return personaDAO.obtenerTodas();
    }
}
