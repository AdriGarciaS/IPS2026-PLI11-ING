package com.ips2026.pl11.model.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit Test Suite for MatchTicket model.
 * Verifies field assignments, constructor initialization, and getter methods.
 */
public class MatchTicketTest {

    @Test
    @DisplayName("MatchTicket: correctly initializes and exposes all fields via getters")
    void testMatchTicket_GettersMatchConstructorValues() {
        int id = 1001;
        int matchId = 50;
        String grandstand = "A";
        String section = "C";
        int rowNum = 5;
        int seatNum = 12;
        double price = 30.0;

        MatchTicket ticket = new MatchTicket(id, matchId, grandstand, section, rowNum, seatNum, price);

        assertNotNull(ticket, "The created MatchTicket instance must not be null.");
        assertEquals(id, ticket.getId(), "Ticket ID must match constructor argument.");
        assertEquals(matchId, ticket.getMatchId(), "Match ID must match constructor argument.");
        assertEquals(grandstand, ticket.getGrandstand(), "Grandstand must match constructor argument.");
        assertEquals(section, ticket.getSection(), "Section must match constructor argument.");
        assertEquals(rowNum, ticket.getRowNum(), "Row number must match constructor argument.");
        assertEquals(seatNum, ticket.getSeatNum(), "Seat number must match constructor argument.");
        assertEquals(price, ticket.getPrice(), 0.001, "Price must match constructor argument.");
    }
}
