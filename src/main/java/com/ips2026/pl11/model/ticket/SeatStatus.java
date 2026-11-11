package com.ips2026.pl11.model.ticket;

import java.awt.Color;

public enum SeatStatus {
    AVAILABLE(new Color(46, 204, 113), "Disponible"),       
    OCCUPIED(new Color(231, 76, 60), "Vendido"),           
    SEASON_PASS_RESERVED(new Color(241, 196, 15), "Reserva Socio"); 

    private final Color color;
    private final String description;

    SeatStatus(Color color, String description) {
        this.color = color;
        this.description = description;
    }

    public Color getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }
}
