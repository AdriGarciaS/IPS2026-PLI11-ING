
package com.ips2026.pl11.data.gardening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.gardening.GardeningSession;

/**
 * Tests of GardeningSessionDAO on a temporary SQLite database created with the
 * real schema.sql.
 */
@DisplayName("GardeningSessionDAO")
class GardeningSessionDAOTest {

    @TempDir
    Path folder;

    private final GardeningSessionDAO dao = new GardeningSessionDAO();

    private long facilityId;
    private long gardener1Id;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    // --------------------------------------------------
    // Fixture
    // --------------------------------------------------

    private GardeningSession createSession(String description) {
        return new GardeningSession(0L, facilityId, description, "Interior", "2026-12-01", "10:00", "12:00");
    }

    private long insertSession(String description) throws SQLException {
        return dao.insert(createSession(description));
    }

    private void assignGardener(long sessionId, long gardenerId) throws SQLException {
        TestDatabase.execute("INSERT INTO gardening_session_employee " + "(gardening_session_id, employee_id) "
            + "VALUES (" + sessionId + ", " + gardenerId + ")");
    }

    private boolean sessionExists(long sessionId) throws SQLException {
        return TestDatabase.queryInt("SELECT COUNT(*) FROM gardening_session WHERE id = " + sessionId) > 0;
    }

    // --------------------------------------------------
    // INSERT
    // --------------------------------------------------

    @Test
    @DisplayName("insert creates a session and returns its generated ID")
    void insert() throws SQLException {
        long id = insertSession("Test gardening session");

        assertTrue(id > 0);
        assertTrue(sessionExists(id));
    }

    @Test
    @DisplayName("insert saves all session data correctly")
    void insertSavesData() throws SQLException {
        long id = insertSession("Plant maintenance");

        assertEquals(1,
            TestDatabase.queryInt("SELECT COUNT(*) FROM gardening_session " + "WHERE id = " + id + " AND facility_id = "
                + facilityId + " AND description = 'Plant maintenance'" + " AND type = 'Interior'"
                + " AND date = '2026-12-01'" + " AND start_time = '10:00'" + " AND end_time = '12:00'"));
    }

    @Test
    @DisplayName("insert does not create gardener assignments")
    void insertDoesNotAssignGardeners() throws SQLException {
        long id = insertSession("Unassigned session");

        assertEquals(0, TestDatabase
            .queryInt("SELECT COUNT(*) FROM gardening_session_employee " + "WHERE gardening_session_id = " + id));
    }

    // --------------------------------------------------
    // FIND ALL
    // --------------------------------------------------

    @Test
    @DisplayName("findAll returns all inserted sessions")
    void findAllMultiple() throws SQLException {
        int initialSize = dao.findAll().size();

        insertSession("Session 1");
        insertSession("Session 2");
        insertSession("Session 3");

        assertEquals(3, dao.findAll().size() - initialSize);
    }

    // --------------------------------------------------
    // FIND BY GARDENER
    // --------------------------------------------------

    @Test
    @DisplayName("findByGardenerId returns sessions assigned to the gardener")
    void findByGardenerId() throws SQLException {
        long sessionId = insertSession("Gardener session");
        assignGardener(sessionId, gardener1Id);

        List<GardeningSession> sessions = dao.findByGardenerId(gardener1Id);

        assertEquals(1, sessions.size());
        assertEquals("Gardener session", sessions.get(0).getDescription());
    }

    @Test
    @DisplayName("findByGardenerId returns an empty list when the gardener has no sessions")
    void findByGardenerIdEmpty() throws SQLException {
        assertTrue(dao.findByGardenerId(gardener1Id).isEmpty());
    }

    // --------------------------------------------------
    // DELETE
    // --------------------------------------------------

    @Test
    @DisplayName("delete removes the specified session")
    void delete() throws SQLException {
        long id = insertSession("Session to delete");

        assertTrue(sessionExists(id));

        dao.delete(id);

        assertFalse(sessionExists(id));
    }

    @Test
    @DisplayName("delete does not remove other sessions")
    void deleteOnlySpecifiedSession() throws SQLException {
        long session1 = insertSession("Session 1");
        long session2 = insertSession("Session 2");

        dao.delete(session1);

        assertFalse(sessionExists(session1));
        assertTrue(sessionExists(session2));
    }

    @Test
    @DisplayName("delete of a nonexistent session leaves existing sessions unchanged")
    void deleteNonexistent() throws SQLException {
        insertSession("Session that must remain");

        int initialSize = dao.findAll().size();
        dao.delete(Long.MAX_VALUE);

        assertEquals(initialSize, dao.findAll().size());
    }

    @Test
    @DisplayName("delete removes the session from gardener queries")
    void deleteRemovesGardenerAssignment() throws SQLException {
        long sessionId = insertSession("Assigned session");
        assignGardener(sessionId, gardener1Id);

        assertEquals(1, dao.findByGardenerId(gardener1Id).size());

        dao.delete(sessionId);

        assertTrue(dao.findByGardenerId(gardener1Id).isEmpty());
    }
}
