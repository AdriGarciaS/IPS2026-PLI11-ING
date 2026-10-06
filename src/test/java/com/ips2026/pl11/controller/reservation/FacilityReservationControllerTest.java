package com.ips2026.pl11.controller.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.model.reservation.Reservation;
import com.ips2026.pl11.model.reservation.TeamUse;

/**
 * Tests of the facility reservation controller without a database: the DAOs
 * are small fakes that keep the data in lists, and "now" is fixed with a
 * {@link Clock}.
 */
@DisplayName("FacilityReservationController")
class FacilityReservationControllerTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Madrid");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 5, 9, 30, 15);
    private static final LocalDate TOMORROW = NOW.toLocalDate().plusDays(1);
    private static final String VALID_CARD = "4111 1111 1111 1111";

    private static final Facility PITCH = new Facility(2, "Training Pitch 1");
    private static final Facility GYM = new Facility(4, "Gym");

    private static class FakeFacilityDAO extends FacilityDAO {
        @Override
        public List<Facility> findAll() {
            return List.of(GYM, PITCH);
        }
    }

    private static class FakeTeamUseDAO extends TeamUseDAO {
        final List<TeamUse> uses = new ArrayList<>();

        @Override
        public List<TeamUse> findByFacilityAndDate(int facilityId, LocalDate date) {
            return uses.stream().filter(u -> u.getFacilityId() == facilityId && u.getDate().equals(date)).toList();
        }
    }

    /** Keeps the inserted reservations in a list and gives them consecutive ids. */
    private static class FakeReservationDAO extends ReservationDAO {
        final List<Reservation> stored = new ArrayList<>();

        @Override
        public List<Reservation> findByFacilityAndDate(Facility facility, LocalDate date) {
            return stored.stream().filter(r -> r.getFacility().getId() == facility.getId() && r.getDate().equals(date)).toList();
        }

        @Override
        public Reservation insert(Reservation reservation) {
            Reservation withId = reservation.withId(stored.size() + 1);
            stored.add(withId);
            return withId;
        }
    }

    private FakeTeamUseDAO teamUseDAO;
    private FakeReservationDAO reservationDAO;
    private FacilityReservationController controller;

    @BeforeEach
    void setUp() throws SQLException {
        teamUseDAO = new FakeTeamUseDAO();
        reservationDAO = new FakeReservationDAO();
        Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        controller = new FacilityReservationController(new FakeFacilityDAO(), teamUseDAO, reservationDAO, clock);
        controller.loadFacilities();

        teamUseDAO.uses.add(new TeamUse(PITCH.getId(), "First Team", TOMORROW, LocalTime.of(10, 0), LocalTime.of(12, 0)));
        controller.selectDay(PITCH, TOMORROW);
    }

    @Test
    @DisplayName("the facilities come from the database")
    void facilitiesAreLoaded() {
        assertEquals(List.of(GYM, PITCH), controller.getFacilities());
    }

    @Test
    @DisplayName("the bookable days go from today to today + 1 month")
    void bookableDays() {
        assertEquals(LocalDate.of(2026, 10, 5), controller.getToday());
        assertEquals(LocalDate.of(2026, 11, 5), controller.getLastBookableDay());
    }

    @Test
    @DisplayName("the availability shown is the one of the chosen facility and day")
    void availabilityOfChosenDay() throws SQLException {
        assertEquals(2, controller.getAvailability().maxHoursFrom(LocalTime.of(8, 0)));

        controller.selectDay(GYM, TOMORROW); // no team uses in the gym
        assertEquals(14, controller.getAvailability().maxHoursFrom(LocalTime.of(8, 0)));
    }

    @Test
    @DisplayName("booking stores the reservation with the masked card, the price and when it was made")
    void bookStoresReservation() throws SQLException {
        Reservation reservation = controller.book("  Marta Alonso ", "4111-1111-1111-1111", LocalTime.of(16, 13), 2);

        assertEquals(1, reservationDAO.stored.size());
        assertEquals(1, reservation.getId());
        assertEquals(PITCH, reservation.getFacility());
        assertEquals("Marta Alonso", reservation.getHolderName());
        assertEquals("**** **** **** 1111", reservation.getMaskedCardNumber());
        assertEquals(TOMORROW, reservation.getDate());
        assertEquals(LocalTime.of(16, 13), reservation.getStart());
        assertEquals(LocalTime.of(18, 13), reservation.getEnd());
        assertEquals(0, new BigDecimal("100").compareTo(reservation.getTotalPrice()));
        assertEquals(NOW.withNano(0), reservation.getCreatedAt());
    }

    @Test
    @DisplayName("after booking, the period appears as reserved")
    void bookedPeriodIsNoLongerFree() throws SQLException {
        controller.book("Marta Alonso", VALID_CARD, LocalTime.of(16, 13), 1);

        assertTrue(controller.findTimeProblem(LocalTime.of(16, 13), 1).orElse("").contains("already reserved"));
    }

    @Test
    @DisplayName("a period that is not available can not be booked")
    void unavailablePeriodIsRejected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> controller.book("Marta Alonso", VALID_CARD, LocalTime.of(13, 0), 1));

        assertTrue(error.getMessage().contains("until 13:30"));
        assertTrue(reservationDAO.stored.isEmpty());
    }

    @Test
    @DisplayName("before booking the availability is read again: a period taken meanwhile is rejected")
    void availabilityIsReloadedBeforeBooking() {
        // Someone else books 16:00-18:00 after the day was loaded in this window.
        reservationDAO.stored.add(new Reservation(99, PITCH, "Other", "**** **** **** 4444", TOMORROW,
                LocalTime.of(16, 0), 2, new BigDecimal("100"), NOW));

        assertThrows(IllegalArgumentException.class,
                () -> controller.book("Marta Alonso", VALID_CARD, LocalTime.of(16, 30), 1));
        assertEquals(1, reservationDAO.stored.size());
    }

    @Test
    @DisplayName("the name of the person is required")
    void nameIsRequired() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> controller.book("   ", VALID_CARD, LocalTime.of(16, 0), 1));

        assertTrue(error.getMessage().contains("name"));
        assertTrue(reservationDAO.stored.isEmpty());
    }

    @Test
    @DisplayName("names longer than 100 characters are rejected")
    void nameTooLong() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.checkHolderData("x".repeat(101), VALID_CARD));
        controller.checkHolderData("x".repeat(100), VALID_CARD); // limit: accepted
    }

    @Test
    @DisplayName("an invalid card number is rejected and nothing is stored")
    void invalidCardIsRejected() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> controller.book("Marta Alonso", "4111 1111 1111 1112", LocalTime.of(16, 0), 1));

        assertTrue(error.getMessage().contains("not valid"));
        assertTrue(reservationDAO.stored.isEmpty());
    }

    @Test
    @DisplayName("the price is 50 EUR per hour")
    void priceIsFiftyPerHour() {
        assertEquals(0, new BigDecimal("200").compareTo(controller.getPrice(4)));
    }

    @Test
    @DisplayName("without choosing a facility and a day, nothing can be checked or booked")
    void dayMustBeChosenFirst() {
        FacilityReservationController fresh = new FacilityReservationController(
                new FakeFacilityDAO(), teamUseDAO, reservationDAO);

        assertNull(fresh.getAvailability());
        assertThrows(IllegalStateException.class, () -> fresh.findTimeProblem(LocalTime.of(10, 0), 1));
    }
}
