
package com.ips2026.pl11.data.injury;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ips2026.pl11.data.TestDatabase;
import com.ips2026.pl11.model.injury.Injury;

@DisplayName("InjuryDAO")
class InjuryDAOTest {

    @TempDir
    Path folder;

    private final InjuryDAO dao = new InjuryDAO();

    private int playerId;
    private int doctorId;
    private int facilityId;
    private int matchId;
    private int trainingId;

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
    // FIXTURE
    // --------------------------------------------------

    private void createFixtureData() throws SQLException {

        // Facility
        TestDatabase.execute(
            "INSERT INTO FACILITY (name) VALUES ('Test Facility')"
        );

        facilityId = TestDatabase.queryInt(
            "SELECT id FROM FACILITY WHERE name = 'Test Facility'"
        );

        // Player
        TestDatabase.execute(
            "INSERT INTO employees "
            + "(first_name, last_name, national_id, birth_date, "
            + "phone_number, category, position, gross_annual_salary, gender) "
            + "VALUES ('Test', 'Player', 'TEST-PLAYER', "
            + "'1990-01-01', '600000001', 'SPORTS', "
            + "'Player', 20000, 'MALE')"
        );

        playerId = TestDatabase.queryInt(
            "SELECT id FROM employees WHERE national_id = 'TEST-PLAYER'"
        );

        // Doctor
        TestDatabase.execute(
            "INSERT INTO employees "
            + "(first_name, last_name, national_id, birth_date, "
            + "phone_number, category, position, gross_annual_salary, gender) "
            + "VALUES ('Test', 'Doctor', 'TEST-DOCTOR', "
            + "'1985-01-01', '600000002', 'NON_SPORTS', "
            + "'Doctor', 30000, 'FEMALE')"
        );

        doctorId = TestDatabase.queryInt(
            "SELECT id FROM employees WHERE national_id = 'TEST-DOCTOR'"
        );

        // Match
        TestDatabase.execute(
            "INSERT INTO matches "
            + "(team_id, match_date, start_time, end_time, rival, "
            + "is_home, is_special_match, facility_id) "
            + "VALUES (NULL, '2026-12-01', '10:00', '12:00', "
            + "'Test Rival', 1, 0, " + facilityId + ")"
        );

        matchId = TestDatabase.queryInt(
            "SELECT id FROM matches WHERE rival = 'Test Rival'"
        );

        // Training
        TestDatabase.execute(
            "INSERT INTO trainings "
            + "(team_id, training_date, start_time, end_time, facility_id, coach_id) "
            + "VALUES (NULL, '2026-12-02', '10:00', '12:00', "
            + facilityId + ", " + doctorId + ")"
        );

        trainingId = TestDatabase.queryInt(
            "SELECT id FROM trainings WHERE training_date = '2026-12-02'"
        );
    }

    private Injury createInjury(
        int id,
        String description,
        boolean healed
    ) {
        return new Injury(
            id,
            playerId,
            matchId,
            trainingId,
            description,
            LocalDate.of(2026, 12, 1),
            healed,
            doctorId,
            LocalTime.of(11, 30)
        );
    }

    // --------------------------------------------------
    // REGISTER INJURY
    // --------------------------------------------------

    @Test
    @DisplayName("registerInjury inserts an injury")
    void registerInjury() throws SQLException {

        Injury injury = createInjury(
            0, "Ankle sprain", false
        );

        dao.registerInjury(injury);

        List<Injury> injuries = dao.getAllInjuries();

        assertEquals(1, injuries.size());
        assertEquals("Ankle sprain", injuries.get(0).getDescription());
    }

    @Test
    @DisplayName("registerInjury saves all injury fields correctly")
    void registerInjurySavesCorrectData() throws SQLException {

        Injury injury = createInjury(
            0, "Muscle injury", false
        );

        dao.registerInjury(injury);

        Injury stored = dao.getAllInjuries().get(0);

        assertEquals(playerId, stored.getPlayerId());
        assertEquals(matchId, stored.getMatchId());
        assertEquals(trainingId, stored.getTrainingId());
        assertEquals("Muscle injury", stored.getDescription());
        assertEquals(LocalDate.of(2026, 12, 1), stored.getInjuryDate());
        assertFalse(stored.isHealed());
        assertEquals(doctorId, stored.getDoctorId());
        assertEquals(LocalTime.of(11, 30), stored.getInjuryTime());
    }

    @Test
    @DisplayName("registerInjury allows registering multiple injuries")
    void registerMultipleInjuries() throws SQLException {

        dao.registerInjury(createInjury(
            0, "Ankle sprain", false
        ));

        dao.registerInjury(createInjury(
            0, "Muscle injury", true
        ));

        assertEquals(2, dao.getAllInjuries().size());
    }

    // --------------------------------------------------
    // GET ALL INJURIES
    // --------------------------------------------------

    @Test
    @DisplayName("getAllInjuries returns all registered injuries")
    void getAllInjuries() throws SQLException {

        dao.registerInjury(createInjury(
            0, "Ankle sprain", false
        ));

        dao.registerInjury(createInjury(
            0, "Muscle injury", true
        ));

        List<Injury> injuries = dao.getAllInjuries();

        assertEquals(2, injuries.size());
    }

    @Test
    @DisplayName("getAllInjuries returns an empty list when there are no injuries")
    void getAllInjuriesEmpty() throws SQLException {

        List<Injury> injuries = dao.getAllInjuries();

        assertTrue(injuries.isEmpty());
    }

    // --------------------------------------------------
    // UPDATE INJURY
    // --------------------------------------------------

    @Test
    @DisplayName("updateInjury modifies an existing injury")
    void updateInjury() throws SQLException {

        dao.registerInjury(createInjury(
            0, "Ankle sprain", false
        ));

        Injury stored = dao.getAllInjuries().get(0);

        stored.setDescription("Severe ankle sprain");
        stored.setHealed(true);

        dao.updateInjury(stored);

        Injury updated = dao.getAllInjuries().get(0);

        assertEquals("Severe ankle sprain", updated.getDescription());
        assertTrue(updated.isHealed());
    }

    @Test
    @DisplayName("updateInjury does not modify other injuries")
    void updateOnlySpecifiedInjury() throws SQLException {

        dao.registerInjury(createInjury(
            0, "Ankle sprain", false
        ));

        dao.registerInjury(createInjury(
            0, "Muscle injury", false
        ));

        List<Injury> injuries = dao.getAllInjuries();

        Injury first = injuries.get(0);

        first.setDescription("Updated ankle sprain");
        first.setHealed(true);

        dao.updateInjury(first);

        List<Injury> updatedInjuries = dao.getAllInjuries();

        assertEquals("Updated ankle sprain",
            updatedInjuries.get(0).getDescription());
        assertTrue(updatedInjuries.get(0).isHealed());

        assertEquals("Muscle injury",
            updatedInjuries.get(1).getDescription());
        assertFalse(updatedInjuries.get(1).isHealed());
    }
}
