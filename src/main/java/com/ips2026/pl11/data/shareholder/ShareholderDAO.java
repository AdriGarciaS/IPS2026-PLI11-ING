package com.ips2026.pl11.data.shareholder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * SQL queries to the SHAREHOLDER table.
 */
public class ShareholderDAO {

    /** Every shareholder, ordered by surname and name. */
    public List<Shareholder> findAll() throws SQLException {
        String sql = "SELECT id, first_name, last_name, national_id, username, password FROM SHAREHOLDER ORDER BY last_name, first_name";
        List<Shareholder> shareholders = new ArrayList<>();

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                shareholders.add(new Shareholder(
                        result.getInt("id"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getString("national_id"),
                        result.getString("username"),
                        result.getString("password")
                ));
            }
        }
        return shareholders;
    }

    /** US 35828: whether a shareholder already has this national ID (ignoring upper/lower case). */
    public boolean existsNationalId(String nationalId) throws SQLException {
        return exists("SELECT 1 FROM SHAREHOLDER WHERE national_id = ? COLLATE NOCASE", nationalId);
    }

    /** US 35828: whether a shareholder already has this email (ignoring upper/lower case). */
    public boolean existsEmail(String email) throws SQLException {
        return exists("SELECT 1 FROM SHAREHOLDER WHERE email = ? COLLATE NOCASE", email);
    }

    /**
     * US 35828: whether this username is already used by a shareholder or by
     * an employee (ignoring upper/lower case). Both log in from the same
     * window, so a username cannot be repeated between them.
     */
    public boolean existsUsername(String username) throws SQLException {
        return exists("SELECT 1 FROM SHAREHOLDER WHERE username = ? COLLATE NOCASE "
                + "UNION ALL SELECT 1 FROM employees WHERE username = ? COLLATE NOCASE", username, username);
    }

    private static boolean exists(String sql, String... values) throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setString(i + 1, values[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /**
     * US 36748: removes the shareholder from the system if they no longer own
     * any share (they have sold all of them). Their row of SHAREHOLDER is
     * deleted; their old holdings, listings and transfers are kept, because
     * they are the history of the campaigns and of the market, with the
     * shareholder set to NULL ("removed shareholder").
     *
     * <p>It uses the connection given, so it runs inside the transaction of
     * whoever calls it (the market purchase).</p>
     *
     * @return true if the shareholder has been removed
     */
    public static boolean deleteIfWithoutShares(Connection connection, int shareholderId) throws SQLException {
        try (PreparedStatement count = connection.prepareStatement(SQL_COUNT_SHARES)) {
            count.setInt(1, shareholderId);
            try (ResultSet result = count.executeQuery()) {
                result.next();
                if (result.getInt(1) > 0) {
                    return false;
                }
            }
        }
        for (String sql : SQL_FORGET_SHAREHOLDER) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, shareholderId);
                statement.executeUpdate();
            }
        }
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM SHAREHOLDER WHERE id = ?")) {
            delete.setInt(1, shareholderId);
            return delete.executeUpdate() == 1;
        }
    }

    private static final String SQL_COUNT_SHARES = "SELECT COUNT(*) FROM SHARE s "
            + "JOIN SHARE_HOLDING h ON h.id = s.holding_id WHERE h.shareholder_id = ?";

    // The history of the shareholder is kept, but without them.
    private static final String[] SQL_FORGET_SHAREHOLDER = {
            "UPDATE SHARE_HOLDING SET shareholder_id = NULL WHERE shareholder_id = ?",
            "UPDATE SHARE_LISTING SET seller_id = NULL WHERE seller_id = ?",
            "UPDATE SHARE_TRANSFER SET seller_id = NULL WHERE seller_id = ?",
            "UPDATE SHARE_TRANSFER SET buyer_id = NULL WHERE buyer_id = ?"
    };
}
