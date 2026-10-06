package com.ips2026.pl11.data.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.model.reservation.Reservation;
import com.ips2026.pl11.model.reservation.TeamUse;

/**
 * Tests of FacilityDAO, TeamUseDAO and ReservationDAO on a temporary SQLite
 * database created with the real schema.sql.
 */
@DisplayName("Facility reservation DAOs")
class ReservationDAOsTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 6);
    private static final Facility PITCH = new Facility(2, "Training Pitch 1");

    @TempDir
    Path folder;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
        TestDatabase.execute("INSERT INTO FACILITY (id, name) VALUES (1, 'Main Pitch'), (2, 'Training Pitch 1'), (4, 'Gym')");
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    @Test
    @DisplayName("FacilityDAO returns every facility ordered by name")
    void facilitiesOrderedByName() throws SQLException {
        List<Facility> facilities = new FacilityDAO().findAll();

        assertEquals(List.of("Gym", "Main Pitch", "Training Pitch 1"), facilities.stream().map(Facility::getName).toList());
        assertEquals(4, facilities.get(0).getId());
    }

    @Test
    @DisplayName("TeamUseDAO returns only the uses of that facility and day, ordered by start time")
    void teamUsesOfFacilityAndDay() throws SQLException {
        TestDatabase.execute("INSERT INTO FACILITY_TEAM_USE (facility_id, team_name, use_date, start_time, end_time) VALUES "
                + "(2, 'U19 Team', '2026-10-06', '18:00', '20:00'), "
                + "(2, 'First Team', '2026-10-06', '10:00', '12:00'), "
                + "(2, 'First Team', '2026-10-07', '10:00', '12:00'), "  // another day
                + "(1, 'First Team', '2026-10-06', '17:00', '19:00')");  // another facility

        List<TeamUse> uses = new TeamUseDAO().findByFacilityAndDate(PITCH.getId(), DAY);

        assertEquals(2, uses.size());
        assertEquals("First Team", uses.get(0).getTeamName());
        assertEquals(LocalTime.of(10, 0), uses.get(0).getStart());
        assertEquals(LocalTime.of(12, 0), uses.get(0).getEnd());
        assertEquals(DAY, uses.get(0).getDate());
        assertEquals("U19 Team", uses.get(1).getTeamName());
    }

    @Test
    @DisplayName("ReservationDAO stores one row with the total price and the data to track it back")
    void insertStoresOneRow() throws SQLException {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 5, 9, 30, 15);
        Reservation stored = new ReservationDAO().insert(new Reservation(null, PITCH, "Marta Alonso",
                "**** **** **** 1111", DAY, LocalTime.of(16, 13), 2, new BigDecimal("100.00"), createdAt));

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM FACILITY_RESERVATION"));
        try (Connection connection = ConexionBD.obtenerConexion();
             Statement statement = connection.createStatement();
             ResultSet row = statement.executeQuery("SELECT * FROM FACILITY_RESERVATION")) {
            row.next();
            assertEquals(stored.getId(), row.getInt("id"));
            assertEquals(2, row.getInt("facility_id"));
            assertEquals("Marta Alonso", row.getString("holder_name"));
            assertEquals("**** **** **** 1111", row.getString("card_number"));
            assertEquals("2026-10-06", row.getString("reservation_date"));
            assertEquals("16:13", row.getString("start_time"));
            assertEquals("18:13", row.getString("end_time"));
            assertEquals(2, row.getInt("hours"));
            assertEquals(100.0, row.getDouble("total_price"), 0.001);
            assertEquals("2026-10-05 09:30:15", row.getString("created_at"));
        }
    }

    @Test
    @DisplayName("ReservationDAO gives a new id to each reservation")
    void insertGivesNewIds() throws SQLException {
        ReservationDAO dao = new ReservationDAO();
        Reservation first = dao.insert(reservationAt("10:00", 1));
        Reservation second = dao.insert(reservationAt("16:00", 1));

        assertTrue(second.getId() > first.getId());
    }

    @Test
    @DisplayName("ReservationDAO finds the reservations of a facility and day, ordered by start time")
    void findReservationsOfFacilityAndDay() throws SQLException {
        ReservationDAO dao = new ReservationDAO();
        dao.insert(reservationAt("16:00", 2));
        dao.insert(reservationAt("08:30", 1));
        dao.insert(new Reservation(null, new Facility(1, "Main Pitch"), "Other", "**** **** **** 4444", DAY,
                LocalTime.of(9, 0), 1, new BigDecimal("50"), LocalDateTime.now()));

        List<Reservation> found = dao.findByFacilityAndDate(PITCH, DAY);

        assertEquals(2, found.size());
        assertEquals(LocalTime.of(8, 30), found.get(0).getStart());
        assertEquals(LocalTime.of(16, 0), found.get(1).getStart());
        assertEquals(2, found.get(1).getHours());
        assertEquals(new BigDecimal("100.00"), found.get(1).getTotalPrice());
        assertTrue(dao.findByFacilityAndDate(PITCH, DAY.plusDays(1)).isEmpty());
    }

    private static Reservation reservationAt(String start, int hours) {
        return new Reservation(null, PITCH, "Marta Alonso", "**** **** **** 1111", DAY, LocalTime.parse(start), hours,
                new BigDecimal(50 * hours), LocalDateTime.of(2026, 10, 5, 9, 0));
    }
}
