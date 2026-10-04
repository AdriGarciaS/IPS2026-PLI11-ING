package com.ips2026.pl11.model.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A reservation of a facility made by an external person (table
 * FACILITY_RESERVATION).
 */
public final class Reservation {

    private final Integer id;
    private final Facility facility;
    private final String holderName;
    private final String maskedCardNumber;
    private final LocalDate date;
    private final LocalTime start;
    private final int hours;
    private final BigDecimal totalPrice;
    private final LocalDateTime createdAt;

    /**
     * @param id               null for a reservation that is not stored yet
     * @param maskedCardNumber card number with all digits hidden except the last 4
     */
    public Reservation(Integer id, Facility facility, String holderName, String maskedCardNumber,
            LocalDate date, LocalTime start, int hours, BigDecimal totalPrice, LocalDateTime createdAt) {
        this.id = id;
        this.facility = facility;
        this.holderName = holderName;
        this.maskedCardNumber = maskedCardNumber;
        this.date = date;
        this.start = start;
        this.hours = hours;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public Facility getFacility() {
        return facility;
    }

    public String getHolderName() {
        return holderName;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStart() {
        return start;
    }

    /** End time: start + hours. */
    public LocalTime getEnd() {
        return start.plusHours(hours);
    }

    public int getHours() {
        return hours;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** The same reservation with the id given by the database. */
    public Reservation withId(int newId) {
        return new Reservation(newId, facility, holderName, maskedCardNumber, date, start, hours, totalPrice, createdAt);
    }
}
