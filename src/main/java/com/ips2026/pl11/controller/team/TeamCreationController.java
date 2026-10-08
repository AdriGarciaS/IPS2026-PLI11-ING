package com.ips2026.pl11.controller.team;

import com.ips2026.pl11.data.team.SportsEmployeeDAO;
import com.ips2026.pl11.data.team.TeamDAO;
import com.ips2026.pl11.model.team.NewTeam;
import com.ips2026.pl11.model.team.SportsEmployee;
import com.ips2026.pl11.model.team.StaffAssignment;
import com.ips2026.pl11.model.team.TeamCategory;
import com.ips2026.pl11.model.team.TeamGender;
import com.ips2026.pl11.model.team.TeamRules;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller of the team creation window: keeps the players and technical
 * staff chosen for the new team and creates it.
 */
public class TeamCreationController {

    private final SportsEmployeeDAO employeeDAO;
    private final TeamDAO teamDAO;
    private final Clock clock;

    private List<SportsEmployee> allPlayers = new ArrayList<>();
    private List<SportsEmployee> technicalStaff = new ArrayList<>();
    private final List<SportsEmployee> teamPlayers = new ArrayList<>();
    private final List<StaffAssignment> teamStaff = new ArrayList<>();

    public TeamCreationController(SportsEmployeeDAO employeeDAO, TeamDAO teamDAO) {
        this(employeeDAO, teamDAO, Clock.systemDefaultZone());
    }

    /** @param clock gives the current date (tests use a fixed one) */
    public TeamCreationController(SportsEmployeeDAO employeeDAO, TeamDAO teamDAO, Clock clock) {
        this.employeeDAO = employeeDAO;
        this.teamDAO = teamDAO;
        this.clock = clock;
    }

    /** Loads the players and the technical sports employees. */
    public void load() throws SQLException {
        allPlayers = employeeDAO.findPlayers();
        technicalStaff = employeeDAO.findTechnicalStaff();
    }

    /** Year used to count the ages (the current one). */
    public int getReferenceYear() {
        return Year.now(clock).getValue();
    }

    public List<SportsEmployee> getTechnicalStaff() {
        return List.copyOf(technicalStaff);
    }

    /** Why the player can not join a team of that category and gender, or empty if they can. */
    public Optional<String> findPlayerProblem(SportsEmployee player, TeamCategory category, TeamGender gender) {
        return TeamRules.findPlayerProblem(player, category, gender, getReferenceYear());
    }

    /**
     * Players that are not in the new team yet and whose name contains
     * {@code search} (ignoring case).
     *
     * @param onlyEligible true to show only the players that can join a team
     *                     of that category and gender
     */
    public List<SportsEmployee> searchAvailablePlayers(String search, TeamCategory category, TeamGender gender,
            boolean onlyEligible) {
        String text = search == null ? "" : search.strip().toLowerCase(Locale.ROOT);
        List<SportsEmployee> result = new ArrayList<>();
        for (SportsEmployee player : allPlayers) {
            boolean matches = player.getFullName().toLowerCase(Locale.ROOT).contains(text);
            boolean eligible = findPlayerProblem(player, category, gender).isEmpty();
            if (matches && !teamPlayers.contains(player) && (eligible || !onlyEligible)) {
                result.add(player);
            }
        }
        return result;
    }

    /**
     * Adds a player to the new team.
     *
     * @throws IllegalArgumentException if the player can not join it (the message says why)
     */
    public void addPlayer(SportsEmployee player, TeamCategory category, TeamGender gender) {
        Optional<String> problem = findPlayerProblem(player, category, gender);
        if (problem.isPresent()) {
            throw new IllegalArgumentException(player.getFullName() + " can not join the team: " + problem.get() + ".");
        }
        if (!teamPlayers.contains(player)) {
            teamPlayers.add(player);
        }
    }

    public void removePlayer(SportsEmployee player) {
        teamPlayers.remove(player);
    }

    public List<SportsEmployee> getTeamPlayers() {
        return List.copyOf(teamPlayers);
    }

    /**
     * Adds an extra technical sports employee to the new team.
     *
     * @throws IllegalArgumentException if the task is empty, the employee is
     *         one of the coaches or is already in the staff
     */
    public void addStaff(SportsEmployee employee, String task, SportsEmployee firstCoach, SportsEmployee secondCoach) {
        String cleanTask = task == null ? "" : task.strip();
        if (cleanTask.isEmpty()) {
            throw new IllegalArgumentException("Enter the task of " + employee.getFullName() + " in the team.");
        }
        if (cleanTask.length() > TeamRules.MAX_TASK_LENGTH) {
            throw new IllegalArgumentException("The task can not be longer than " + TeamRules.MAX_TASK_LENGTH + " characters.");
        }
        if (employee.equals(firstCoach) || employee.equals(secondCoach)) {
            throw new IllegalArgumentException(employee.getFullName() + " is already a coach of the team.");
        }
        for (StaffAssignment assignment : teamStaff) {
            if (assignment.getEmployee().equals(employee)) {
                throw new IllegalArgumentException(employee.getFullName() + " is already in the technical staff.");
            }
        }
        teamStaff.add(new StaffAssignment(employee, cleanTask));
    }

    public void removeStaff(SportsEmployee employee) {
        teamStaff.removeIf(assignment -> assignment.getEmployee().equals(employee));
    }

    public List<StaffAssignment> getTeamStaff() {
        return List.copyOf(teamStaff);
    }

    /** The team with the data given and the players and staff added so far. */
    public NewTeam buildTeam(String name, TeamCategory category, TeamGender gender, SportsEmployee firstCoach,
            SportsEmployee secondCoach) {
        return new NewTeam(name, category, gender, firstCoach, secondCoach, teamPlayers, teamStaff);
    }

    /** Every problem of the team, including a name that is already used. */
    public List<String> findProblems(NewTeam team) throws SQLException {
        List<String> problems = new ArrayList<>(team.findProblems(getReferenceYear()));
        if (!team.getName().isEmpty() && teamDAO.existsName(team.getName())) {
            problems.add(0, "There is already a team called \"" + team.getName() + "\".");
        }
        return problems;
    }

    /**
     * Creates the team. Before storing it, the players are read again from
     * the database, in case any of them joined another team meanwhile.
     *
     * @return the id of the new team
     * @throws IllegalArgumentException if the team breaks any rule (the
     *         message lists every problem)
     */
    public int create(NewTeam team) throws SQLException {
        List<SportsEmployee> current = employeeDAO.findPlayers();
        List<SportsEmployee> refreshed = new ArrayList<>();
        for (SportsEmployee player : team.getPlayers()) {
            refreshed.add(current.stream().filter(player::equals).findFirst().orElse(player));
        }
        NewTeam checked = new NewTeam(team.getName(), team.getCategory(), team.getGender(), team.getFirstCoach(),
                team.getSecondCoach(), refreshed, team.getStaff());

        List<String> problems = findProblems(checked);
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", problems));
        }
        int id = teamDAO.insert(checked, LocalDateTime.now(clock).withNano(0));
        teamPlayers.clear();
        teamStaff.clear();
        allPlayers = current;
        return id;
    }
}
