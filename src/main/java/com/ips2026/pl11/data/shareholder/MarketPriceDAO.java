package com.ips2026.pl11.data.shareholder;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.shareholder.MarketPrice;
import com.ips2026.pl11.model.shareholder.MarketPriceGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * SQL operations on the MARKET_PRICE table (one price per day).
 */
public class MarketPriceDAO {

    /**
     * Creates the random price of {@code day} if that day has no price yet,
     * starting from the last price before it. If the day already has a
     * price nothing changes, so it is created only once per day.
     *
     * @return true if a new price was created
     */
    public boolean ensurePriceFor(LocalDate day, Random random) throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            try (PreparedStatement exists = connection.prepareStatement(
                    "SELECT COUNT(*) FROM MARKET_PRICE WHERE price_date = ?")) {
                exists.setString(1, day.toString());
                try (ResultSet result = exists.executeQuery()) {
                    result.next();
                    if (result.getInt(1) > 0) {
                        return false;
                    }
                }
            }

            BigDecimal lastPrice = null;
            try (PreparedStatement last = connection.prepareStatement(
                    "SELECT price FROM MARKET_PRICE WHERE price_date < ? ORDER BY price_date DESC LIMIT 1")) {
                last.setString(1, day.toString());
                try (ResultSet result = last.executeQuery()) {
                    if (result.next()) {
                        lastPrice = toPrice(result.getDouble("price"));
                    }
                }
            }

            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO MARKET_PRICE (price_date, price) VALUES (?, ?)")) {
                insert.setString(1, day.toString());
                insert.setDouble(2, MarketPriceGenerator.nextPrice(lastPrice, random).doubleValue());
                insert.executeUpdate();
            }
            return true;
        }
    }

    /** The {@code count} most recent prices, the newest first. */
    public List<MarketPrice> findLatest(int count) throws SQLException {
        String sql = "SELECT price_date, price FROM MARKET_PRICE ORDER BY price_date DESC LIMIT ?";
        List<MarketPrice> prices = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, count);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    prices.add(new MarketPrice(LocalDate.parse(result.getString("price_date")),
                            toPrice(result.getDouble("price"))));
                }
            }
        }
        return prices;
    }

    private static BigDecimal toPrice(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
