package com.ips2026.pl11.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PersonaTest {

    @Test
    void losGettersDevuelvenLosValoresPasadosAlConstructor() {
        Persona persona = new Persona(1, "Ada Lovelace", "ada@example.com");

        assertEquals(1, persona.getId());
        assertEquals("Ada Lovelace", persona.getNombre());
        assertEquals("ada@example.com", persona.getEmail());
    }
}
