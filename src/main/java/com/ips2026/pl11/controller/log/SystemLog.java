package com.ips2026.pl11.controller.log;

import java.io.PrintStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import com.ips2026.pl11.data.log.SystemLogDAO;
import com.ips2026.pl11.model.log.LogAction;
import com.ips2026.pl11.model.log.UserSession;
import com.ips2026.pl11.model.log.UserType;

/**
 * US 36717: the system log of the application. It keeps the current session
 * (who is using the application) and records what they do: logins (also the
 * failed ones), data queries and database changes.
 *
 * <p>How to use it from any controller, after the action has been done:</p>
 * <pre>
 *     SystemLog.record(LogAction.INSERT, "Created employee Ana López (12345678Z)");
 * </pre>
 *
 * <p>Every record is stored in SYSTEM_LOG and also printed in the console.
 * Recording never stops the action: if the record cannot be stored, the
 * problem is only printed in the console. Before any session has been started
 * (for example in the tests of other controllers) the records are only
 * printed.</p>
 */
public final class SystemLog {

    private static final DateTimeFormatter CONSOLE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static SystemLogDAO dao = new SystemLogDAO();
    private static Supplier<LocalDateTime> clock = LocalDateTime::now;
    private static PrintStream console = System.out;
    private static UserSession current;

    private SystemLog() {
        // Only static methods.
    }

    /** Called when the application starts: nobody has logged in yet. */
    public static synchronized UserSession startGuestSession() {
        return startSession(UserType.GUEST, null, null, UserSession.GUEST_ROLE);
    }

    /**
     * Called after a successful login: starts the session of the user and
     * records the login ("Logged in as General Manager").
     *
     * @param role position of the employee, or "Shareholder"
     */
    public static synchronized UserSession startUserSession(UserType userType, int userId, String username,
            String role) {
        UserSession session = startSession(userType, userId, username, role);
        record(LogAction.LOGIN, "Logged in as " + role);
        return session;
    }

    private static UserSession startSession(UserType userType, Integer userId, String username, String role) {
        try {
            current = dao.insertSession(userType, userId, username, role, clock.get());
        } catch (SQLException exception) {
            current = null;
            console.println("[SYSTEM LOG] The session of " + UserSession.userLabel(username, role)
                    + " could not be stored: " + exception.getMessage());
        }
        return current;
    }

    /** The current session, or null if none has been started. */
    public static synchronized UserSession getCurrentSession() {
        return current;
    }

    /** Records an action of the current session (stored in SYSTEM_LOG and printed in the console). */
    public static synchronized void record(LogAction action, String description) {
        LocalDateTime moment = clock.get();
        String session = current == null ? "no session" : "session " + current.getId();
        String user = current == null ? "-" : current.getUserLabel();
        console.println("[SYSTEM LOG] " + moment.format(CONSOLE_FORMAT) + " | " + session + " | " + user + " | "
                + action + " | " + description);
        if (current == null) {
            return;
        }
        try {
            dao.insertRecord(current.getId(), moment, action, description);
        } catch (SQLException exception) {
            console.println("[SYSTEM LOG] The record could not be stored: " + exception.getMessage());
        }
    }

    /**
     * For the tests: uses these DAO, clock and console, and forgets the
     * current session. {@link #reset()} goes back to the normal ones.
     */
    public static synchronized void configure(SystemLogDAO newDao, Supplier<LocalDateTime> newClock, PrintStream newConsole) {
        dao = newDao;
        clock = newClock;
        console = newConsole;
        current = null;
    }

    /** For the tests: the normal DAO, clock and console, and no session. */
    public static synchronized void reset() {
        configure(new SystemLogDAO(), LocalDateTime::now, System.out);
    }
}
