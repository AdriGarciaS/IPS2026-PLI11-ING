package com.ips2026.pl11.data.gardening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

@DisplayName("GardeningSessionEmployeeDAO")
class GardeningSessionEmployeeDAOTest {

    @TempDir
    Path folder;

    private final GardeningSessionEmployeeDAO dao = new GardeningSessionEmployeeDAO();

    private final GardeningSessionDAO sessionDAO = new GardeningSessionDAO();

    private long facilityId;
    private long gardener1Id;
    private long gardener2Id;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabase.create(folder);

        createFixtureData();
    }

    @AfterEach
    void tearDown() {
        TestDatabase.close();
    }

    // --------------------------------------------------
    // Fixture
    // --------------------------------------------------

    private void createFixtureData() throws SQLException {

        TestDatabase.execute("INSERT INTO FACILITY (name) VALUES ('Test Facility')");

        facilityId = TestDatabase.queryInt("SELECT id FROM FACILITY WHERE name = 'Test Facility'");

        TestDatabase.execute("INSERT INTO employees " + "(first_name, last_name, national_id, birth_date, "
            + "phone_number, category, position, gross_annual_salary, gender) "
            + "VALUES ('Gardener', 'One', 'TEST-GARDENER-1', " + "'1990-01-01', '600000001', 'NON_SPORTS', "
            + "'Gardener', 20000, 'MALE')");

        gardener1Id = TestDatabase.queryInt("SELECT id FROM employees " + "WHERE national_id = 'TEST-GARDENER-1'");

        TestDatabase.execute("INSERT INTO employees " + "(first_name, last_name, national_id, birth_date, "
            + "phone_number, category, position, gross_annual_salary, gender) "
            + "VALUES ('Gardener', 'Two', 'TEST-GARDENER-2', " + "'1992-01-01', '600000002', 'NON_SPORTS', "
            + "'Gardener', 20000, 'FEMALE')");

        gardener2Id = TestDatabase.queryInt("SELECT id FROM employees " + "WHERE national_id = 'TEST-GARDENER-2'");
    }

    private long createSession(String description) throws SQLException {
        return sessionDAO.insert(new com.ips2026.pl11.model.gardening.GardeningSession(0L, facilityId, description,
            "Interior", "2026-12-01", "10:00", "12:00"));
    }

    private boolean assignmentExists(long sessionId, long employeeId) throws SQLException {

        return TestDatabase.queryInt("SELECT COUNT(*) FROM gardening_session_employee "
            + "WHERE gardening_session_id = " + sessionId + " AND employee_id = " + employeeId) > 0;
    }

    private int assignmentCount(long sessionId) throws SQLException {
        return TestDatabase
            .queryInt("SELECT COUNT(*) FROM gardening_session_employee " + "WHERE gardening_session_id = " + sessionId);
    }

    // --------------------------------------------------
    // INSERT
    // --------------------------------------------------

    @Test
    @DisplayName("insert creates a gardener assignment")
    void insert() throws SQLException {

        long sessionId = createSession("Test session");

        dao.insert(sessionId, gardener1Id);

        assertTrue(assignmentExists(sessionId, gardener1Id));
    }

    @Test
    @DisplayName("insert stores the correct session and employee IDs")
    void insertSavesCorrectData() throws SQLException {

        long sessionId = createSession("Test session");

        dao.insert(sessionId, gardener1Id);

        assertEquals(1, TestDatabase.queryInt("SELECT COUNT(*) FROM gardening_session_employee "
            + "WHERE gardening_session_id = " + sessionId + " AND employee_id = " + gardener1Id));
    }

    @Test
    @DisplayName("insert allows different employees to be assigned to the same session")
    void insertMultipleEmployees() throws SQLException {

        long sessionId = createSession("Shared session");

        dao.insert(sessionId, gardener1Id);
        dao.insert(sessionId, gardener2Id);

        assertEquals(2, assignmentCount(sessionId));
    }

    @Test
    @DisplayName("insert does not allow duplicate assignments")
    void insertDuplicate() throws SQLException {

        long sessionId = createSession("Duplicate assignment");

        dao.insert(sessionId, gardener1Id);

        assertThrows(SQLException.class, () -> dao.insert(sessionId, gardener1Id));
    }

    // --------------------------------------------------
    // FIND EMPLOYEE IDs BY SESSION
    // --------------------------------------------------

    @Test
    @DisplayName("findEmployeeIdsBySessionId returns assigned employees")
    void findEmployeeIdsBySessionId() throws SQLException {

        long sessionId = createSession("Session with gardeners");

        dao.insert(sessionId, gardener1Id);
        dao.insert(sessionId, gardener2Id);

        List<Long> employeeIds = dao.findEmployeeIdsBySessionId(sessionId);

        assertEquals(2, employeeIds.size());
        assertTrue(employeeIds.contains(gardener1Id));
        assertTrue(employeeIds.contains(gardener2Id));
    }

    @Test
    @DisplayName("findEmployeeIdsBySessionId returns an empty list when no employees are assigned")
    void findEmployeeIdsBySessionIdEmpty() throws SQLException {

        long sessionId = createSession("Unassigned session");

        List<Long> employeeIds = dao.findEmployeeIdsBySessionId(sessionId);

        assertTrue(employeeIds.isEmpty());
    }

    @Test
    @DisplayName("findEmployeeIdsBySessionId only returns employees of the specified session")
    void findEmployeeIdsBySessionIdFiltersBySession() throws SQLException {

        long session1 = createSession("Session 1");
        long session2 = createSession("Session 2");

        dao.insert(session1, gardener1Id);
        dao.insert(session2, gardener2Id);

        List<Long> session1Employees = dao.findEmployeeIdsBySessionId(session1);

        List<Long> session2Employees = dao.findEmployeeIdsBySessionId(session2);

        assertEquals(1, session1Employees.size());
        assertTrue(session1Employees.contains(gardener1Id));
        assertFalse(session1Employees.contains(gardener2Id));

        assertEquals(1, session2Employees.size());
        assertTrue(session2Employees.contains(gardener2Id));
        assertFalse(session2Employees.contains(gardener1Id));
    }

    // --------------------------------------------------
    // DELETE ONE ASSIGNMENT
    // --------------------------------------------------

    @Test
    @DisplayName("delete removes the specified employee assignment")
    void delete() throws SQLException {

        long sessionId = createSession("Session to modify");

        dao.insert(sessionId, gardener1Id);

        assertTrue(assignmentExists(sessionId, gardener1Id));

        dao.delete(sessionId, gardener1Id);

        assertFalse(assignmentExists(sessionId, gardener1Id));
    }

    @Test
    @DisplayName("delete does not remove other employee assignments")
    void deleteOnlySpecifiedAssignment() throws SQLException {

        long sessionId = createSession("Session with two gardeners");

        dao.insert(sessionId, gardener1Id);
        dao.insert(sessionId, gardener2Id);

        dao.delete(sessionId, gardener1Id);

        assertFalse(assignmentExists(sessionId, gardener1Id));
        assertTrue(assignmentExists(sessionId, gardener2Id));
    }

    @Test
    @DisplayName("delete of a nonexistent assignment leaves existing assignments unchanged")
    void deleteNonexistent() throws SQLException {

        long sessionId = createSession("Session with gardener");

        dao.insert(sessionId, gardener1Id);

        int initialCount = assignmentCount(sessionId);

        dao.delete(sessionId, sessionId + 1000);

        assertEquals(initialCount, assignmentCount(sessionId));
        assertTrue(assignmentExists(sessionId, gardener1Id));
    }

    // --------------------------------------------------
    // DELETE BY SESSION ID
    // --------------------------------------------------

    @Test
    @DisplayName("deleteBySessionId removes all assignments from the session")
    void deleteBySessionId() throws SQLException {

        long sessionId = createSession("Session to clear");

        dao.insert(sessionId, gardener1Id);
        dao.insert(sessionId, gardener2Id);

        assertEquals(2, assignmentCount(sessionId));

        dao.deleteBySessionId(sessionId);

        assertEquals(0, assignmentCount(sessionId));
    }

    @Test
    @DisplayName("deleteBySessionId does not remove assignments from other sessions")
    void deleteBySessionIdOnlySpecifiedSession() throws SQLException {

        long session1 = createSession("Session 1");
        long session2 = createSession("Session 2");

        dao.insert(session1, gardener1Id);
        dao.insert(session2, gardener2Id);

        dao.deleteBySessionId(session1);

        assertEquals(0, assignmentCount(session1));
        assertEquals(1, assignmentCount(session2));

        assertTrue(assignmentExists(session2, gardener2Id));
    }

    @Test
    @DisplayName("deleteBySessionId does nothing when the session has no assignments")
    void deleteBySessionIdEmpty() throws SQLException {

        long sessionId = createSession("Empty session");

        dao.deleteBySessionId(sessionId);

        assertEquals(0, assignmentCount(sessionId));
    }
}
