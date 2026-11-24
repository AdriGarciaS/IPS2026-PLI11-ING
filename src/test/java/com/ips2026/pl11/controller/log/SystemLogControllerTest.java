package com.ips2026.pl11.controller.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ips2026.pl11.data.log.SystemLogDAO;
import com.ips2026.pl11.model.log.LogAction;
import com.ips2026.pl11.model.log.LogFilter;
import com.ips2026.pl11.model.log.LogRecord;

/**
 * Tests of the System Log window controller (US 36717) with a fake DAO (no
 * database). The records of SystemLog are only printed (there is no session).
 */
@DisplayName("SystemLogController")
class SystemLogControllerTest {

    private static final LogRecord LOGIN = new LogRecord(1, LocalDateTime.of(2026, 11, 22, 9, 1), 2,
            "general_manager", "General Manager", LogAction.LOGIN, "Logged in as General Manager");

    /** Remembers the last filter. */
    private static class FakeSystemLogDAO extends SystemLogDAO {
        LogFilter lastFilter;

        @Override
        public List<LogRecord> find(LogFilter filter) {
            lastFilter = filter;
            return List.of(LOGIN);
        }

        @Override
        public List<String> findUsernames() {
            return List.of("general_manager", "shareholder");
        }
    }

    private final FakeSystemLogDAO dao = new FakeSystemLogDAO();
    private final SystemLogController controller = new SystemLogController(dao);
    private final ByteArrayOutputStream consoleBytes = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        SystemLog.configure(new SystemLogDAO(), () -> LocalDateTime.of(2026, 11, 24, 10, 0),
                new PrintStream(consoleBytes, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        SystemLog.reset();
    }

    @Test
    @DisplayName("the search returns the records of the DAO and records the query in the log")
    void searchIsRecorded() throws SQLException {
        LogFilter filter = new LogFilter(LocalDate.of(2026, 11, 1), null, "shareholder", LogAction.LOGIN);

        assertEquals(List.of(LOGIN), controller.search(filter));
        assertEquals(filter, dao.lastFilter);
        assertTrue(consoleBytes.toString(StandardCharsets.UTF_8).contains(
                "| QUERY | Viewed system log (from 01/11/2026, user shareholder, action LOGIN)"));
    }

    @Test
    @DisplayName("with 'from' after 'to' nothing is searched")
    void wrongDates() throws SQLException {
        LogFilter filter = new LogFilter(LocalDate.of(2026, 11, 24), LocalDate.of(2026, 11, 1), null, null);

        assertTrue(controller.search(filter).isEmpty());
        assertNull(dao.lastFilter);
    }

    @Test
    @DisplayName("the description of the query lists only the filters used")
    void describe() {
        assertEquals("Viewed system log", SystemLogController.describe(LogFilter.all()));
        assertEquals("Viewed system log (to 30/11/2026, user Guest)", SystemLogController.describe(
                new LogFilter(null, LocalDate.of(2026, 11, 30), LogFilter.GUESTS, null)));
    }

    @Test
    @DisplayName("the usernames come from the DAO")
    void usernames() throws SQLException {
        assertEquals(List.of("general_manager", "shareholder"), controller.getUsernames());
    }
}
