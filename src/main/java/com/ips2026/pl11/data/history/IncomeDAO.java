package com.ips2026.pl11.data.history;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.history.Income;
import com.ips2026.pl11.model.history.IncomeType;
import com.ips2026.pl11.model.history.SaleLine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL queries of the sales history: reads the income of every concept
 * (merchandising, tickets, season tickets, facility bookings and share
 * purchases) from its own table. The moment of each income is when it was
 * registered in the database.
 */
public class IncomeDAO {

    private static final DateTimeFormatter BOOKING_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    // Merchandising: one income per purchase (all its lines share the sale_number).
    private static final String SQL_MERCHANDISING = "SELECT sale_number, MIN(sale_date) AS moment, "
            + "COUNT(*) AS products, SUM(total_price) AS amount FROM MERCHANDISING_SALE "
            + "WHERE sale_date >= ? AND sale_date < ? GROUP BY sale_number";

    // Tickets: one income per purchase (the seats of a purchase are contiguous, in the same row).
    private static final String SQL_TICKETS = "SELECT t.purchase_number, MIN(t.created_at) AS moment, m.rival, "
            + "t.grandstand, t.section, t.row_num, MIN(t.seat_num) AS first_seat, MAX(t.seat_num) AS last_seat, "
            + "SUM(t.price) AS amount FROM MATCH_TICKET t JOIN matches m ON m.id = t.match_id "
            + "WHERE t.created_at >= ? AND t.created_at < ? "
            + "GROUP BY t.purchase_number, m.rival, t.grandstand, t.section, t.row_num";

    private static final String SQL_SEASON_TICKETS = "SELECT id, created_at AS moment, season, holder_name, "
            + "price AS amount FROM SEASON_TICKET WHERE created_at >= ? AND created_at < ?";

    private static final String SQL_FACILITIES = "SELECT r.id, r.created_at AS moment, f.name, r.reservation_date, "
            + "r.start_time, r.total_price AS amount FROM FACILITY_RESERVATION r JOIN FACILITY f ON f.id = r.facility_id "
            + "WHERE r.created_at >= ? AND r.created_at < ?";

    // Share purchases: the amount is the number of shares x the price of one share.
    private static final String SQL_CAMPAIGNS = "SELECT h.id, h.purchase_date AS moment, c.name, "
            + "COUNT(s.id) AS shares, h.purchase_price FROM SHARE_HOLDING h "
            + "JOIN SHARE_CAMPAIGN c ON c.id = h.campaign_id JOIN SHARE s ON s.holding_id = h.id "
            + "WHERE h.purchase_date >= ? AND h.purchase_date < ? GROUP BY h.id, h.purchase_date, c.name, h.purchase_price";

    private static final String SQL_SALE_LINES = "SELECT m.name, s.units_sold, s.total_price FROM MERCHANDISING_SALE s "
            + "JOIN MERCHANDISING m ON m.id = s.merch_id WHERE s.sale_number = ? ORDER BY s.id";

    /** Every income registered between {@code from} and {@code to} (both days included), in no order. */
    public List<Income> findBetween(LocalDate from, LocalDate to) throws SQLException {
        String start = from.toString();
        String end = to.plusDays(1).toString(); // the moments are compared as text: "YYYY-MM-DD HH:MM:SS"
        List<Income> incomes = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion()) {
            read(connection, SQL_MERCHANDISING, start, end, incomes, result -> {
                int products = result.getInt("products");
                return new Income(IncomeType.MERCHANDISING, moment(result),
                        "Sale #" + result.getInt("sale_number") + " · " + products + (products == 1 ? " product" : " products"),
                        money(result.getDouble("amount")), result.getInt("sale_number"));
            });
            read(connection, SQL_TICKETS, start, end, incomes, result -> {
                int first = result.getInt("first_seat");
                int last = result.getInt("last_seat");
                String seats = first == last ? String.valueOf(first) : first + "-" + last;
                return new Income(IncomeType.TICKET, moment(result),
                        result.getString("rival") + " · " + result.getString("grandstand") + "-" + result.getString("section")
                                + ", R " + result.getInt("row_num") + ", S " + seats,
                        money(result.getDouble("amount")), result.getInt("purchase_number"));
            });
            read(connection, SQL_SEASON_TICKETS, start, end, incomes, result -> new Income(IncomeType.SEASON_TICKET,
                    moment(result), result.getString("season") + " · " + result.getString("holder_name"),
                    money(result.getDouble("amount")), result.getInt("id")));
            read(connection, SQL_FACILITIES, start, end, incomes, result -> {
                LocalDateTime booking = LocalDate.parse(result.getString("reservation_date"))
                        .atTime(LocalTime.parse(result.getString("start_time")));
                return new Income(IncomeType.FACILITY, moment(result),
                        result.getString("name") + " · " + booking.format(BOOKING_FORMAT),
                        money(result.getDouble("amount")), result.getInt("id"));
            });
            read(connection, SQL_CAMPAIGNS, start, end, incomes, result -> {
                int shares = result.getInt("shares");
                BigDecimal amount = BigDecimal.valueOf(result.getDouble("purchase_price"))
                        .multiply(BigDecimal.valueOf(shares)).setScale(2, RoundingMode.HALF_UP);
                return new Income(IncomeType.CAMPAIGN, moment(result),
                        result.getString("name") + " · " + shares + (shares == 1 ? " share" : " shares"),
                        amount, result.getInt("id"));
            });
        }
        return incomes;
    }

    /** The products of a merchandising purchase, in the order they were sold. */
    public List<SaleLine> findSaleLines(int saleNumber) throws SQLException {
        List<SaleLine> lines = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_SALE_LINES)) {

            statement.setInt(1, saleNumber);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int units = result.getInt("units_sold");
                    BigDecimal total = money(result.getDouble("total_price"));
                    // The price of one unit at the moment of the sale (the catalog price may have changed since)
                    BigDecimal unitPrice = total.divide(BigDecimal.valueOf(units), 2, RoundingMode.HALF_UP);
                    lines.add(new SaleLine(result.getString("name"), units, unitPrice, total));
                }
            }
        }
        return lines;
    }

    /** Creates one income from the current row of a query. */
    @FunctionalInterface
    private interface RowReader {
        Income read(ResultSet result) throws SQLException;
    }

    private static void read(Connection connection, String sql, String start, String end, List<Income> incomes,
            RowReader reader) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, start);
            statement.setString(2, end);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    incomes.add(reader.read(result));
                }
            }
        }
    }

    /** The "moment" column: "YYYY-MM-DD HH:MM:SS" (a date alone counts as its first minute). */
    private static LocalDateTime moment(ResultSet result) throws SQLException {
        String text = result.getString("moment");
        if (text.length() == 10) {
            return LocalDate.parse(text).atStartOfDay();
        }
        return LocalDateTime.parse(text.replace(' ', 'T'));
    }

    private static BigDecimal money(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }
}
