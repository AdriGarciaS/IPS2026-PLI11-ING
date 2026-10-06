package com.ips2026.pl11.model.team;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Rules of the sports teams, all in one place so they are easy to change.
 * The age limits of each category are in {@link TeamCategory}.
 */
public final class TeamRules {

    /** A team must have at least this many players when it is created. */
    public static final int MIN_PLAYERS = 7;

    public static final int MAX_NAME_LENGTH = 60;
    public static final int MAX_TASK_LENGTH = 60;

    /** Value of the position column of the employees that are players. */
    public static final String PLAYER_POSITION = "Player";

    private TeamRules() {
        // Constants and rules only: not instantiated.
    }

    /**
     * Age counted by year, as in the sports categories: the age the person
     * has or will have during {@code year} (year - year of birth).
     */
    public static int ageInYear(LocalDate birthDate, int year) {
        return year - birthDate.getYear();
    }

    /**
     * Checks whether a player can join a new team of that category and gender.
     *
     * @return the reason why not, or empty if the player can join
     */
    public static Optional<String> findPlayerProblem(SportsEmployee player, TeamCategory category, TeamGender gender,
            int year) {
        if (!player.isPlayer()) {
            return Optional.of("Not a player");
        }
        if (player.getCurrentTeam() != null) {
            return Optional.of("Already in " + player.getCurrentTeam());
        }
        if (category != null && !category.acceptsAge(player.getAgeIn(year))) {
            return Optional.of("Age " + player.getAgeIn(year) + " in " + year + ", " + category.getLabel()
                    + " is " + category.getAgeRange());
        }
        if (gender != null && !gender.accepts(player.getGender())) {
            return Optional.of(player.getGender() == null
                    ? "Gender not registered (only mixed teams)"
                    : genderLabel(player.getGender()) + " player, the team is " + gender.toString().toLowerCase());
        }
        return Optional.empty();
    }

    /** "Male", "Female" or "-" (not registered). */
    public static String genderLabel(String gender) {
        if ("MALE".equals(gender)) {
            return "Male";
        }
        if ("FEMALE".equals(gender)) {
            return "Female";
        }
        return "-";
    }
}
