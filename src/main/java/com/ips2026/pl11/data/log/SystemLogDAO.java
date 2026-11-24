package com.ips2026.pl11.data.log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.log.LogAction;
import com.ips2026.pl11.model.log.LogFilter;
import com.ips2026.pl11.model.log.LogRecord;
import com.ips2026.pl11.model.log.UserSession;
import com.ips2026.pl11.model.log.UserType;

/**
 * US 36717: SQL queries to the tables USER_SESSION and SYSTEM_LOG.
 */
public class SystemLogDAO {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SQL_INSERT_SESSION = "INSERT INTO USER_SESSION "
            + "(user_type, user_id, username, role, started_at) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_RECORD = "INSERT INTO SYSTEM_LOG "
            + "(session_id, logged_at, action, description) VALUES (?, ?, ?, ?)";

    private static final String SQL_RECORDS = "SELECT l.id, l.logged_at, l.session_id, s.username, s.role, "
            + "l.action, l.description FROM SYSTEM_LOG l JOIN USER_SESSION s ON s.id = l.session_id WHERE 1 = 1";

    /** Stores a new session and returns it with its id (the session id). */
    public UserSession insertSession(UserType userType, Integer userId, String username, String role,
            LocalDateTime startedAt) throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_SESSION,
                     Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, userType.name());
            if (userId == null) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setInt(2, userId);
            }
            statement.setString(3, username);
            statement.setString(4, role);
            statement.setString(5, startedAt.format(DATE_TIME_FORMAT));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new UserSession(keys.getInt(1), userType, userId, username, role, startedAt);
            }
        }
    }

    /** Stores one record of the log in the session given. */
    public void insertRecord(int sessionId, LocalDateTime moment, LogAction action, String description)
            throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_RECORD)) {

            statement.setInt(1, sessionId);
            statement.setString(2, moment.format(DATE_TIME_FORMAT));
            statement.setString(3, action.name());
            statement.setString(4, description);
            statement.executeUpdate();
        }
    }

    /** The records that pass the filter, the newest first. */
    public List<LogRecord> find(LogFilter filter) throws SQLException {
        StringBuilder sql = new StringBuilder(SQL_RECORDS);
        List<String> values = new ArrayList<>();
        if (filter.getFrom() != null) {
            sql.append(" AND l.logged_at >= ?");
            values.add(filter.getFrom() + " 00:00:00");
        }
        if (filter.getTo() != null) {
            sql.append(" AND l.logged_at <= ?");
            values.add(filter.getTo() + " 23:59:59");
        }
        if (LogFilter.GUESTS.equals(filter.getUsername())) {
            sql.append(" AND s.username IS NULL");
        } else if (filter.getUsername() != null) {
            sql.append(" AND s.username = ? COLLATE NOCASE");
            values.add(filter.getUsername());
        }
        if (filter.getAction() != null) {
            sql.append(" AND l.action = ?");
            values.add(filter.getAction().name());
        }
        sql.append(" ORDER BY l.logged_at DESC, l.id DESC");

        List<LogRecord> records = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) {
                statement.setString(i + 1, values.get(i));
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    records.add(new LogRecord(
                            result.getInt("id"),
                            LocalDateTime.parse(result.getString("logged_at"), DATE_TIME_FORMAT),
                            result.getInt("session_id"),
                            result.getString("username"),
                            result.getString("role"),
                            LogAction.valueOf(result.getString("action")),
                            result.getString("description")));
                }
            }
        }
        return records;
    }

    /** Every username that has a session, in alphabetical order (for the user filter). */
    public List<String> findUsernames() throws SQLException {
        String sql = "SELECT DISTINCT username FROM USER_SESSION WHERE username IS NOT NULL "
                + "ORDER BY username COLLATE NOCASE";
        List<String> usernames = new ArrayList<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                usernames.add(result.getString(1));
            }
        }
        return usernames;
    }
}
