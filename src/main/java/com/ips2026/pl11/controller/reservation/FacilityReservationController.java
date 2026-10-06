package com.ips2026.pl11.controller.reservation;

import com.ips2026.pl11.data.reservation.FacilityDAO;
import com.ips2026.pl11.data.reservation.ReservationDAO;
import com.ips2026.pl11.data.reservation.TeamUseDAO;
import com.ips2026.pl11.model.reservation.CardValidator;
import com.ips2026.pl11.model.reservation.DayAvailability;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.model.reservation.Reservation;
import com.ips2026.pl11.model.reservation.ReservationRules;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller of the facility reservations window: loads the facilities and
 * the availability of the chosen facility and day, and books reservations.
 */
public class FacilityReservationController {

    public static final int MAX_NAME_LENGTH = 100;

    private final FacilityDAO facilityDAO;
    private final TeamUseDAO teamUseDAO;
    private final ReservationDAO reservationDAO;
    private final Clock clock;

    private List<Facility> facilities = new ArrayList<>();
    private Facility facility;
    private DayAvailability availability;

    public FacilityReservationController(FacilityDAO facilityDAO, TeamUseDAO teamUseDAO, ReservationDAO reservationDAO) {
        this(facilityDAO, teamUseDAO, reservationDAO, Clock.systemDefaultZone());
    }

    /** @param clock gives the current date and time (tests use a fixed one) */
    public FacilityReservationController(FacilityDAO facilityDAO, TeamUseDAO teamUseDAO, ReservationDAO reservationDAO,
            Clock clock) {
        this.facilityDAO = facilityDAO;
        this.teamUseDAO = teamUseDAO;
        this.reservationDAO = reservationDAO;
        this.clock = clock;
    }

    public void loadFacilities() throws SQLException {
        facilities = facilityDAO.findAll();
    }

    public List<Facility> getFacilities() {
        return List.copyOf(facilities);
    }

    public LocalDate getToday() {
        return LocalDate.now(clock);
    }

    /** Last day that can be booked (today + {@link ReservationRules#BOOKING_WINDOW}). */
    public LocalDate getLastBookableDay() {
        return ReservationRules.lastBookableDay(getToday());
    }

    /** Loads the team uses and reservations of a facility on a day. */
    public void selectDay(Facility selectedFacility, LocalDate date) throws SQLException {
        DayAvailability loaded = new DayAvailability(date,
                teamUseDAO.findByFacilityAndDate(selectedFacility.getId(), date),
                reservationDAO.findByFacilityAndDate(selectedFacility, date),
                LocalDateTime.now(clock));
        facility = selectedFacility;
        availability = loaded;
    }

    /** Availability of the facility and day chosen with {@link #selectDay}. */
    public DayAvailability getAvailability() {
        return availability;
    }

    public BigDecimal getPrice(int hours) {
        return ReservationRules.priceFor(hours);
    }

    /** @return why that period can not be booked, or empty if it can */
    public Optional<String> findTimeProblem(LocalTime start, int hours) {
        checkDaySelected();
        return availability.findProblem(start, hours);
    }

    /**
     * Books the facility and day chosen with {@link #selectDay}. Before
     * storing it, the availability is read again from the database, in case
     * it changed since it was shown.
     *
     * @return the stored reservation
     * @throws IllegalArgumentException if any data is not valid or the period
     *         is not available (the message says why)
     */
    public Reservation book(String holderName, String cardNumber, LocalTime start, int hours) throws SQLException {
        checkDaySelected();
        checkHolderData(holderName, cardNumber);
        String name = holderName.strip();

        LocalDate date = availability.getDate();
        LocalTime startMinute = start.withSecond(0).withNano(0);
        selectDay(facility, date);
        Optional<String> timeProblem = availability.findProblem(startMinute, hours);
        if (timeProblem.isPresent()) {
            throw new IllegalArgumentException(timeProblem.get());
        }

        Reservation reservation = new Reservation(null, facility, name, CardValidator.mask(cardNumber),
                date, startMinute, hours, getPrice(hours), LocalDateTime.now(clock).withNano(0));
        Reservation stored = reservationDAO.insert(reservation);
        selectDay(facility, date);
        return stored;
    }

    /**
     * Checks the name of the person and the card number.
     *
     * @throws IllegalArgumentException if any of them is not valid (the message says why)
     */
    public void checkHolderData(String holderName, String cardNumber) {
        String name = holderName == null ? "" : holderName.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Enter the name of the person requesting the reservation.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("The name can not be longer than " + MAX_NAME_LENGTH + " characters.");
        }
        Optional<String> cardProblem = CardValidator.findProblem(cardNumber);
        if (cardProblem.isPresent()) {
            throw new IllegalArgumentException(cardProblem.get());
        }
    }

    private void checkDaySelected() {
        if (availability == null) {
            throw new IllegalStateException("Choose a facility and a day first");
        }
    }
}
