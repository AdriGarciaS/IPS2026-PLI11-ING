package com.ips2026.pl11.model.team;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A team that is going to be created, with everything the general manager
 * chose. {@link #findProblems(int)} checks all the rules of the user story.
 */
public final class NewTeam {

    private final String name;
    private final TeamCategory category;
    private final TeamGender gender;
    private final SportsEmployee firstCoach;
    private final SportsEmployee secondCoach;
    private final List<SportsEmployee> players;
    private final List<StaffAssignment> staff;

    public NewTeam(String name, TeamCategory category, TeamGender gender, SportsEmployee firstCoach,
            SportsEmployee secondCoach, List<SportsEmployee> players, List<StaffAssignment> staff) {
        this.name = name == null ? "" : name.strip();
        this.category = category;
        this.gender = gender;
        this.firstCoach = firstCoach;
        this.secondCoach = secondCoach;
        this.players = List.copyOf(players);
        this.staff = List.copyOf(staff);
    }

    public String getName() {
        return name;
    }

    public TeamCategory getCategory() {
        return category;
    }

    public TeamGender getGender() {
        return gender;
    }

    public SportsEmployee getFirstCoach() {
        return firstCoach;
    }

    public SportsEmployee getSecondCoach() {
        return secondCoach;
    }

    public List<SportsEmployee> getPlayers() {
        return players;
    }

    public List<StaffAssignment> getStaff() {
        return staff;
    }

    /**
     * Checks every rule: name, category, gender, two different technical
     * coaches, at least {@value TeamRules#MIN_PLAYERS} players of the right
     * age and gender that are in no other team, and the extra staff.
     *
     * @param year year used to count the ages
     * @return every problem found (empty if the team can be created)
     */
    public List<String> findProblems(int year) {
        List<String> problems = new ArrayList<>();

        if (name.isEmpty()) {
            problems.add("Enter the name of the team.");
        } else if (name.length() > TeamRules.MAX_NAME_LENGTH) {
            problems.add("The name can not be longer than " + TeamRules.MAX_NAME_LENGTH + " characters.");
        }
        if (category == null) {
            problems.add("Choose if it is the first or subsidiary team, or the youth category.");
        }
        if (gender == null) {
            problems.add("Choose the gender of the team.");
        }

        checkCoach(firstCoach, "first", problems);
        checkCoach(secondCoach, "second", problems);
        if (firstCoach != null && firstCoach.equals(secondCoach)) {
            problems.add("The first and the second coach must be different people.");
        }

        if (players.size() < TeamRules.MIN_PLAYERS) {
            problems.add("A team needs at least " + TeamRules.MIN_PLAYERS + " players (it has " + players.size() + ").");
        }
        for (SportsEmployee player : players) {
            Optional<String> problem = TeamRules.findPlayerProblem(player, category, gender, year);
            problem.ifPresent(reason -> problems.add(player.getFullName() + ": " + reason + "."));
        }

        for (StaffAssignment assignment : staff) {
            SportsEmployee employee = assignment.getEmployee();
            if (employee.isPlayer()) {
                problems.add(employee.getFullName() + " is a player, not a technical sports employee.");
            }
            if (employee.equals(firstCoach) || employee.equals(secondCoach)) {
                problems.add(employee.getFullName() + " is already a coach of the team.");
            }
            String task = assignment.getTask() == null ? "" : assignment.getTask().strip();
            if (task.isEmpty()) {
                problems.add("Enter the task of " + employee.getFullName() + ".");
            } else if (task.length() > TeamRules.MAX_TASK_LENGTH) {
                problems.add("The task of " + employee.getFullName() + " can not be longer than "
                        + TeamRules.MAX_TASK_LENGTH + " characters.");
            }
        }
        return problems;
    }

    private static void checkCoach(SportsEmployee coach, String which, List<String> problems) {
        if (coach == null) {
            problems.add("Choose the " + which + " coach.");
        } else if (coach.isPlayer()) {
            problems.add("The " + which + " coach must be a technical sports employee, not a player.");
        }
    }
}
