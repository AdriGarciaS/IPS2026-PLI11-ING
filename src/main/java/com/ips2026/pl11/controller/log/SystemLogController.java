package com.ips2026.pl11.controller.log;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.log.SystemLogDAO;
import com.ips2026.pl11.model.log.LogAction;
import com.ips2026.pl11.model.log.LogFilter;
import com.ips2026.pl11.model.log.LogRecord;

/**
 * US 36717: controller of the System Log window, where the general manager
 * sees the records of the log (newest first) and filters them by dates, user
 * and action. Looking at the log is a data query too, so it is recorded.
 */
public class SystemLogController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SystemLogDAO dao;

    public SystemLogController(SystemLogDAO dao) {
        this.dao = dao;
    }

    /** Usernames for the user filter (alphabetical; the guests are a separate option of the window). */
    public List<String> getUsernames() throws SQLException {
        return dao.findUsernames();
    }

    /**
     * The records that pass the filter, newest first (none if "from" is after
     * "to"), and records this query in the log.
     */
    public List<LogRecord> search(LogFilter filter) throws SQLException {
        if (filter.hasWrongDates()) {
            return List.of();
        }
        List<LogRecord> records = dao.find(filter);
        SystemLog.record(LogAction.QUERY, describe(filter));
        return records;
    }

    /** For example "Viewed system log" or "Viewed system log (from 01/11/2026, user shareholder, action LOGIN)". */
    static String describe(LogFilter filter) {
        List<String> parts = new ArrayList<>();
        if (filter.getFrom() != null) {
            parts.add("from " + filter.getFrom().format(DAY));
        }
        if (filter.getTo() != null) {
            parts.add("to " + filter.getTo().format(DAY));
        }
        if (LogFilter.GUESTS.equals(filter.getUsername())) {
            parts.add("user Guest");
        } else if (filter.getUsername() != null) {
            parts.add("user " + filter.getUsername());
        }
        if (filter.getAction() != null) {
            parts.add("action " + filter.getAction());
        }
        return parts.isEmpty() ? "Viewed system log" : "Viewed system log (" + String.join(", ", parts) + ")";
    }
}
