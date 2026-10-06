package com.ips2026.pl11.modelo;


public final class Persona {

    private final int id;
    private final String nombre;
    private final String email;

    public Persona(int id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }
}
