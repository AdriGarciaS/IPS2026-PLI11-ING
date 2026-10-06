package com.ips2026.pl11.model.team;

import java.time.LocalDate;

/**
 * A sports employee as seen when creating teams: a player or a technical
 * sports employee (coach...). Read from the employees table.
 */
public final class SportsEmployee {

    private final int id;
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDate;
    private final String position;
    private final String gender;
    private final String currentTeam;

    /**
     * @param gender      "MALE", "FEMALE" or null if it is not registered
     * @param currentTeam name of the team the player already belongs to, or null
     */
    public SportsEmployee(int id, String firstName, String lastName, LocalDate birthDate, String position,
            String gender, String currentTeam) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.position = position;
        this.gender = gender;
        this.currentTeam = currentTeam;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPosition() {
        return position;
    }

    public boolean isPlayer() {
        return TeamRules.PLAYER_POSITION.equalsIgnoreCase(position);
    }

    public String getGender() {
        return gender;
    }

    public String getCurrentTeam() {
        return currentTeam;
    }

    /** Age counted by year: the age the employee has or will have in {@code year}. */
    public int getAgeIn(int year) {
        return TeamRules.ageInYear(birthDate, year);
    }

    /** "Name (position)", so it can be shown directly in a combo box. */
    @Override
    public String toString() {
        return getFullName() + " (" + position + ")";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SportsEmployee employee && employee.id == id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
