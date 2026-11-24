package com.ips2026.pl11.data.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.log.LogAction;
import com.ips2026.pl11.model.log.LogFilter;
import com.ips2026.pl11.model.log.LogRecord;
import com.ips2026.pl11.model.log.UserSession;
import com.ips2026.pl11.model.log.UserType;

/**
 * Tests of SystemLogDAO (US 36717) on a temporary SQLite database created
 * with the real schema.sql. It starts with three sessions: a guest, the
 * general manager and a shareholder, with some records on 22 and 23 November.
 */
@DisplayName("SystemLogDAO")
class SystemLogDAOTest {

    private static final LocalDateTime DAY_22 = LocalDateTime.of(2026, 11, 22, 9, 0);
    private static final LocalDateTime DAY_23 = LocalDateTime.of(2026, 11, 23, 18, 30);

    @TempDir
    Path folder;

    private final SystemLogDAO dao = new SystemLogDAO();
    private UserSession guest;
    private UserSession manager;
    private UserSession shareholder;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
        guest = dao.insertSession(UserType.GUEST, null, null, "Guest", DAY_22);
        manager = dao.insertSession(UserType.EMPLOYEE, 5, "general_manager", "General Manager", DAY_22.plusMinutes(1));
        shareholder = dao.insertSession(UserType.SHAREHOLDER, 1, "shareholder", "Shareholder", DAY_23);

        dao.insertRecord(guest.getId(), DAY_22, LogAction.LOGIN_FAILED, "Failed login for 'pepe': unknown username");
        dao.insertRecord(manager.getId(), DAY_22.plusMinutes(1), LogAction.LOGIN, "Logged in as General Manager");
        dao.insertRecord(manager.getId(), DAY_22.plusMinutes(5), LogAction.INSERT, "Created campaign Capital increase 2027");
        dao.insertRecord(shareholder.getId(), DAY_23, LogAction.LOGIN, "Logged in as Shareholder");
        dao.insertRecord(shareholder.getId(), DAY_23.plusMinutes(2), LogAction.QUERY, "Viewed shares wallet");
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    @Test
    @DisplayName("a session is stored with its user, and the guest session has no user")
    void insertSession() throws SQLException {
        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM USER_SESSION WHERE id = " + manager.getId()
                + " AND user_type = 'EMPLOYEE' AND user_id = 5 AND username = 'general_manager'"
                + " AND role = 'General Manager' AND started_at = '2026-11-22 09:01:00'"));
        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM USER_SESSION WHERE id = " + guest.getId()
                + " AND user_type = 'GUEST' AND user_id IS NULL AND username IS NULL"));
        assertEquals("general_manager (General Manager)", manager.getUserLabel());
        assertEquals("Guest", guest.getUserLabel());
    }

    @Test
    @DisplayName("without filters every record is found, the newest first, with the user of its session")
    void findAll() throws SQLException {
        List<LogRecord> records = dao.find(LogFilter.all());

        assertEquals(List.of("Viewed shares wallet", "Logged in as Shareholder", "Created campaign Capital increase 2027",
                "Logged in as General Manager", "Failed login for 'pepe': unknown username"),
                records.stream().map(LogRecord::getDescription).toList());
        LogRecord newest = records.get(0);
        assertEquals(LocalDateTime.of(2026, 11, 23, 18, 32), newest.getLoggedAt());
        assertEquals(shareholder.getId(), newest.getSessionId());
        assertEquals("shareholder (Shareholder)", newest.getUserLabel());
        assertEquals(LogAction.QUERY, newest.getAction());
        assertNull(records.get(4).getUsername());
        assertEquals("Guest", records.get(4).getUserLabel());
    }

    @Test
    @DisplayName("the dates filter includes the whole first and last day")
    void filterByDates() throws SQLException {
        assertEquals(3, dao.find(new LogFilter(LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 22), null, null)).size());
        assertEquals(2, dao.find(new LogFilter(LocalDate.of(2026, 11, 23), null, null, null)).size());
        assertEquals(3, dao.find(new LogFilter(null, LocalDate.of(2026, 11, 22), null, null)).size());
        assertEquals(0, dao.find(new LogFilter(LocalDate.of(2026, 11, 24), null, null, null)).size());
    }

    @Test
    @DisplayName("the user filter finds one user (ignoring case) or only the guests, and can be combined with the action")
    void filterByUserAndAction() throws SQLException {
        assertEquals(2, dao.find(new LogFilter(null, null, "GENERAL_MANAGER", null)).size());
        assertEquals(List.of("Failed login for 'pepe': unknown username"),
                dao.find(new LogFilter(null, null, LogFilter.GUESTS, null)).stream().map(LogRecord::getDescription).toList());
        assertEquals(2, dao.find(new LogFilter(null, null, null, LogAction.LOGIN)).size());
        assertEquals(1, dao.find(new LogFilter(null, null, "shareholder", LogAction.LOGIN)).size());
        assertEquals(0, dao.find(new LogFilter(null, null, "shareholder", LogAction.DELETE)).size());
    }

    @Test
    @DisplayName("the usernames for the filter are every user with a session, in alphabetical order, without guests")
    void usernames() throws SQLException {
        dao.insertSession(UserType.EMPLOYEE, 5, "general_manager", "General Manager", DAY_23); // second session
        assertEquals(List.of("general_manager", "shareholder"), dao.findUsernames());
    }

    @Test
    @DisplayName("a record must belong to an existing session and have a known action")
    void constraints() {
        assertThrows(SQLException.class, () -> TestDatabase.execute("INSERT INTO SYSTEM_LOG "
                + "(session_id, logged_at, action, description) VALUES (" + guest.getId() + ", '2026-11-22 10:00:00', "
                + "'SOMETHING', 'x')"));
        assertThrows(SQLException.class, () -> TestDatabase.execute("INSERT INTO USER_SESSION "
                + "(user_type, user_id, username, role, started_at) VALUES ('GUEST', NULL, 'pepe', 'Guest', "
                + "'2026-11-22 10:00:00')"));
    }
}
