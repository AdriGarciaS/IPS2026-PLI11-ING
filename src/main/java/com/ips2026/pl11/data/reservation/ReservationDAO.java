package com.ips2026.pl11.data.reservation;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.model.reservation.Reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL operations on the FACILITY_RESERVATION table.
 */
public class ReservationDAO {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Reservations of a facility on a day, ordered by start time. */
    public List<Reservation> findByFacilityAndDate(Facility facility, LocalDate date) throws SQLException {
        String sql = "SELECT id, holder_name, card_number, reservation_date, start_time, hours, total_price, created_at "
                + "FROM FACILITY_RESERVATION WHERE facility_id = ? AND reservation_date = ? ORDER BY start_time";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, facility.getId());
            statement.setString(2, date.toString());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    reservations.add(new Reservation(
                            result.getInt("id"),
                            facility,
                            result.getString("holder_name"),
                            result.getString("card_number"),
                            LocalDate.parse(result.getString("reservation_date")),
                            LocalTime.parse(result.getString("start_time")),
                            result.getInt("hours"),
                            BigDecimal.valueOf(result.getDouble("total_price")).setScale(2, RoundingMode.HALF_UP),
                            LocalDateTime.parse(result.getString("created_at"), DATE_TIME_FORMAT)
                    ));
                }
            }
        }

        return reservations;
    }

    /**
     * Stores a new reservation.
     *
     * @return the same reservation with the id given by the database
     */
    public Reservation insert(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO FACILITY_RESERVATION (facility_id, holder_name, card_number, reservation_date, "
                + "start_time, end_time, hours, total_price, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, reservation.getFacility().getId());
            statement.setString(2, reservation.getHolderName());
            statement.setString(3, reservation.getMaskedCardNumber());
            statement.setString(4, reservation.getDate().toString());
            statement.setString(5, reservation.getStart().format(TIME_FORMAT));
            statement.setString(6, reservation.getEnd().format(TIME_FORMAT));
            statement.setInt(7, reservation.getHours());
            statement.setDouble(8, reservation.getTotalPrice().doubleValue());
            statement.setString(9, reservation.getCreatedAt().format(DATE_TIME_FORMAT));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return reservation.withId(keys.getInt(1));
            }
        }
    }
}
