package com.ips2026.pl11.controller.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.data.log.SystemLogDAO;
import com.ips2026.pl11.model.log.LogAction;
import com.ips2026.pl11.model.log.LogFilter;
import com.ips2026.pl11.model.log.LogRecord;
import com.ips2026.pl11.model.log.UserSession;
import com.ips2026.pl11.model.log.UserType;

/**
 * Tests of SystemLog (US 36717) on a temporary SQLite database, with a fixed
 * clock and the console captured.
 */
@DisplayName("SystemLog")
class SystemLogTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 24, 10, 15, 3);

    @TempDir
    Path folder;

    private final SystemLogDAO dao = new SystemLogDAO();
    private final ByteArrayOutputStream consoleBytes = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
        SystemLog.configure(dao, () -> NOW, new PrintStream(consoleBytes, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        SystemLog.reset();
        TestDatabase.close();
    }

    private String console() {
        return consoleBytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("before any session the record is only printed, not stored")
    void withoutSession() throws SQLException {
        SystemLog.record(LogAction.QUERY, "Viewed shares wallet");

        assertNull(SystemLog.getCurrentSession());
        assertEquals("[SYSTEM LOG] 2026-11-24 10:15:03 | no session | - | QUERY | Viewed shares wallet"
                + System.lineSeparator(), console());
        assertEquals(0, TestDatabase.queryInt("SELECT COUNT(*) FROM SYSTEM_LOG"));
    }

    @Test
    @DisplayName("in the guest session a failed login is stored and printed")
    void guestSession() throws SQLException {
        UserSession guest = SystemLog.startGuestSession();
        SystemLog.record(LogAction.LOGIN_FAILED, "Failed login for 'pepe': unknown username");

        assertEquals(UserType.GUEST, guest.getUserType());
        List<LogRecord> records = dao.find(LogFilter.all());
        assertEquals(1, records.size());
        assertEquals(guest.getId(), records.get(0).getSessionId());
        assertEquals(LogAction.LOGIN_FAILED, records.get(0).getAction());
        assertEquals(NOW, records.get(0).getLoggedAt());
        assertTrue(console().contains("| session " + guest.getId() + " | Guest | LOGIN_FAILED | "
                + "Failed login for 'pepe': unknown username"));
    }

    @Test
    @DisplayName("a login starts a new session, records LOGIN, and the next records go to that session")
    void userSession() throws SQLException {
        UserSession guest = SystemLog.startGuestSession();
        UserSession manager = SystemLog.startUserSession(UserType.EMPLOYEE, 5, "general_manager", "General Manager");
        SystemLog.record(LogAction.INSERT, "Created campaign Capital increase 2027");

        assertTrue(manager.getId() != guest.getId());
        assertEquals(manager, SystemLog.getCurrentSession());
        List<LogRecord> records = dao.find(LogFilter.all());
        assertEquals(List.of(LogAction.INSERT, LogAction.LOGIN), records.stream().map(LogRecord::getAction).toList());
        assertEquals("Logged in as General Manager", records.get(1).getDescription());
        assertTrue(records.stream().allMatch(r -> r.getSessionId() == manager.getId()));
        assertTrue(console().contains("| general_manager (General Manager) | LOGIN | Logged in as General Manager"));
    }

    @Test
    @DisplayName("if the record cannot be stored, the action is not stopped: the problem is only printed")
    void recordFailure() throws SQLException {
        SystemLog.startGuestSession();
        TestDatabase.execute("DROP TABLE SYSTEM_LOG");

        SystemLog.record(LogAction.QUERY, "Viewed shares wallet"); // must not throw

        assertTrue(console().contains("[SYSTEM LOG] The record could not be stored"));
    }
}
