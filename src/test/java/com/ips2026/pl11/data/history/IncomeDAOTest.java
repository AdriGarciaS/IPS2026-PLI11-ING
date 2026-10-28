package com.ips2026.pl11.data.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.history.Income;
import com.ips2026.pl11.model.history.IncomeType;
import com.ips2026.pl11.model.history.SaleLine;

/**
 * Tests of IncomeDAO on a temporary SQLite database created with the real
 * schema.sql. The period searched is 10/10/2026 - 12/10/2026, and there are
 * operations just inside and just outside it.
 */
@DisplayName("IncomeDAO")
class IncomeDAOTest {

    private static final LocalDate FROM = LocalDate.of(2026, 10, 10);
    private static final LocalDate TO = LocalDate.of(2026, 10, 12);

    @TempDir
    Path folder;

    private final IncomeDAO dao = new IncomeDAO();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);

        // Merchandising: the scarf costs 20 € now, but it was sold at 14,95 €.
        TestDatabase.execute("INSERT INTO MERCHANDISING (id, name, type, available_units, price) VALUES "
                + "(1, 'Home Shirt', 'Clothing', 5, 59.99), (2, 'Club Scarf', 'Accessories', 10, 20.00)");
        TestDatabase.execute("INSERT INTO MERCHANDISING_SALE (sale_number, merch_id, units_sold, total_price, sale_date) VALUES "
                + "(1, 1, 2, 119.98, '2026-10-10 00:00:00'), (1, 2, 1, 14.95, '2026-10-10 00:00:00'), " // first second: inside
                + "(2, 2, 3, 44.85, '2026-10-12 23:59:59'), "                                           // last second: inside
                + "(3, 1, 1, 59.99, '2026-10-09 23:59:59'), "                                           // the day before: outside
                + "(4, 1, 1, 59.99, '2026-10-13 00:00:00')");                                           // the day after: outside

        // Tickets: purchase 1 has 3 contiguous seats, purchase 2 one seat.
        TestDatabase.execute("INSERT INTO TEAM (id, name, category, gender, first_coach_id, second_coach_id, created_at) VALUES "
                + "(1, 'First Team', 'FIRST_TEAM', 'MALE', 101, 102, '2026-10-01 10:00:00')");
        TestDatabase.execute("INSERT INTO FACILITY (id, name) VALUES (1, 'Main Pitch'), (4, 'Gym')");
        TestDatabase.execute("INSERT INTO matches (id, team_id, match_date, start_time, end_time, rival, is_home, facility_id) VALUES "
                + "(1, 1, '2026-10-30', '17:00', '19:00', 'Real Madrid', 1, 1)");
        TestDatabase.execute("INSERT INTO MATCH_TICKET (purchase_number, match_id, grandstand, section, row_num, seat_num, price, created_at) VALUES "
                + "(1, 1, 'A', 'B', 4, 7, 30.0, '2026-10-11 10:15:00'), (1, 1, 'A', 'B', 4, 8, 30.0, '2026-10-11 10:15:00'), "
                + "(1, 1, 'A', 'B', 4, 9, 30.0, '2026-10-11 10:15:00'), (2, 1, 'C', 'A', 1, 3, 30.0, '2026-10-11 20:30:00'), "
                + "(3, 1, 'D', 'F', 10, 1, 30.0, '2026-10-20 09:00:00')");                             // outside

        TestDatabase.execute("INSERT INTO SEASON_TICKET (season, holder_name, price, created_at) VALUES "
                + "('2026/27', 'Carlos Medina', 350.0, '2026-10-12 09:45:00'), "
                + "('2026/27', 'Pablo Ortiz', 280.0, '2026-09-01 19:00:00')");                         // outside

        TestDatabase.execute("INSERT INTO FACILITY_RESERVATION (facility_id, holder_name, card_number, reservation_date, "
                + "start_time, end_time, hours, total_price, created_at) VALUES "
                + "(4, 'Mario Blanco', '**** **** **** 2222', '2026-10-31', '10:00', '12:00', 2, 100.0, '2026-10-10 16:05:00')");

        // Share purchases: one with date and time, and an old one with only the date.
        TestDatabase.execute("INSERT INTO SHAREHOLDER (id, first_name, last_name, national_id) VALUES "
                + "(1, 'Laura', 'Pérez Gil', '11111111H'), (2, 'Marcos', 'Ruiz Soto', '22222222J')");
        TestDatabase.execute("INSERT INTO SHARE_CAMPAIGN (id, name, nominal_price, total_shares, status) VALUES "
                + "(1, 'Capital increase 2026', 14.0, 1000, 'FINISHED')");
        TestDatabase.execute("INSERT INTO SHARE_HOLDING (id, shareholder_id, campaign_id, purchase_date, purchase_price) VALUES "
                + "(1, 1, 1, '2026-10-11 12:30:00', 14.20), (2, 2, 1, '2026-10-12', 10.0)");
        TestDatabase.execute("INSERT INTO SHARE (id, holding_id) VALUES (1, 1), (2, 1), (3, 1), (4, 2)");
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    private List<Income> find() throws SQLException {
        return dao.findBetween(FROM, TO);
    }

    private static Income find(List<Income> incomes, IncomeType type, int reference) {
        return incomes.stream().filter(i -> i.getType() == type && i.getReference() == reference).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("finds the operations of every concept in the period, both days included")
    void everyConceptInThePeriod() throws SQLException {
        List<Income> incomes = find();

        assertEquals(8, incomes.size());
        assertEquals(2, incomes.stream().filter(i -> i.getType() == IncomeType.MERCHANDISING).count());
        assertEquals(2, incomes.stream().filter(i -> i.getType() == IncomeType.TICKET).count());
        assertEquals(1, incomes.stream().filter(i -> i.getType() == IncomeType.SEASON_TICKET).count());
        assertEquals(1, incomes.stream().filter(i -> i.getType() == IncomeType.FACILITY).count());
        assertEquals(2, incomes.stream().filter(i -> i.getType() == IncomeType.CAMPAIGN).count());
    }

    @Test
    @DisplayName("the first second of the first day and the last second of the last day are included")
    void limitsOfThePeriod() throws SQLException {
        List<Income> incomes = find();

        assertEquals(LocalDateTime.of(2026, 10, 10, 0, 0, 0), find(incomes, IncomeType.MERCHANDISING, 1).getMoment());
        assertEquals(LocalDateTime.of(2026, 10, 12, 23, 59, 59), find(incomes, IncomeType.MERCHANDISING, 2).getMoment());
        assertTrue(incomes.stream().noneMatch(i -> i.getType() == IncomeType.MERCHANDISING && i.getReference() > 2),
                "the sales of the day before and the day after are not included");
    }

    @Test
    @DisplayName("a merchandising purchase is one operation with all its products")
    void merchandisingPurchase() throws SQLException {
        Income sale = find(find(), IncomeType.MERCHANDISING, 1);

        assertEquals("Sale #1 · 2 products", sale.getConcept());
        assertEquals(new BigDecimal("134.93"), sale.getAmount());
        assertEquals("Sale #2 · 1 product", find(find(), IncomeType.MERCHANDISING, 2).getConcept());
    }

    @Test
    @DisplayName("a ticket purchase is one operation with all its seats")
    void ticketPurchase() throws SQLException {
        List<Income> incomes = find();

        Income three = find(incomes, IncomeType.TICKET, 1);
        assertEquals("Real Madrid · A-B, R 4, S 7-9", three.getConcept());
        assertEquals(new BigDecimal("90.00"), three.getAmount());
        assertEquals(LocalDateTime.of(2026, 10, 11, 10, 15), three.getMoment());

        Income one = find(incomes, IncomeType.TICKET, 2);
        assertEquals("Real Madrid · C-A, R 1, S 3", one.getConcept());
        assertEquals(new BigDecimal("30.00"), one.getAmount());
    }

    @Test
    @DisplayName("season tickets and facility bookings show their own concept")
    void seasonTicketAndFacility() throws SQLException {
        List<Income> incomes = find();

        Income seasonTicket = incomes.stream().filter(i -> i.getType() == IncomeType.SEASON_TICKET).findFirst().orElseThrow();
        assertEquals("2026/27 · Carlos Medina", seasonTicket.getConcept());
        assertEquals(new BigDecimal("350.00"), seasonTicket.getAmount());
        assertEquals(LocalDateTime.of(2026, 10, 12, 9, 45), seasonTicket.getMoment());

        Income booking = incomes.stream().filter(i -> i.getType() == IncomeType.FACILITY).findFirst().orElseThrow();
        assertEquals("Gym · 31/10 10:00", booking.getConcept());
        assertEquals(new BigDecimal("100.00"), booking.getAmount());
        assertEquals(LocalDateTime.of(2026, 10, 10, 16, 5), booking.getMoment(), "the moment of the booking, not the day booked");
    }

    @Test
    @DisplayName("a share purchase is worth its shares x the price of one share")
    void sharePurchase() throws SQLException {
        List<Income> incomes = find();

        Income three = find(incomes, IncomeType.CAMPAIGN, 1);
        assertEquals("Capital increase 2026 · 3 shares", three.getConcept());
        assertEquals(new BigDecimal("42.60"), three.getAmount());
        assertEquals(LocalDateTime.of(2026, 10, 11, 12, 30), three.getMoment());

        Income one = find(incomes, IncomeType.CAMPAIGN, 2);
        assertEquals("Capital increase 2026 · 1 share", one.getConcept());
        assertEquals(LocalDateTime.of(2026, 10, 12, 0, 0), one.getMoment(), "a date without time counts as its first minute");
    }

    @Test
    @DisplayName("a period without operations gives an empty list")
    void emptyPeriod() throws SQLException {
        assertTrue(dao.findBetween(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)).isEmpty());
    }

    @Test
    @DisplayName("the details of a sale are its products in order, with the unit price of the moment of the sale")
    void saleLines() throws SQLException {
        List<SaleLine> lines = dao.findSaleLines(1);

        assertEquals(2, lines.size());
        assertEquals("Home Shirt", lines.get(0).getProductName());
        assertEquals(2, lines.get(0).getUnits());
        assertEquals(new BigDecimal("59.99"), lines.get(0).getUnitPrice());
        assertEquals(new BigDecimal("119.98"), lines.get(0).getTotal());

        assertEquals("Club Scarf", lines.get(1).getProductName());
        assertEquals(new BigDecimal("14.95"), lines.get(1).getUnitPrice(), "not the current catalog price (20 €)");
    }

    @Test
    @DisplayName("a sale that does not exist has no products")
    void unknownSale() throws SQLException {
        assertTrue(dao.findSaleLines(99).isEmpty());
    }
}
