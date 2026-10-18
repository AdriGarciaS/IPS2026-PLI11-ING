package com.ips2026.pl11.model.team;

/**
 * Gender of a team: male or female (there are no mixed teams). The players
 * must have the gender of the team; coaches and technical staff are not
 * checked.
 */
public enum TeamGender {

    MALE("Male"), FEMALE("Female");

    private final String label;

    TeamGender(String label) {
        this.label = label;
    }

    /** @param playerGender "MALE", "FEMALE" or null if it is not registered */
    public boolean accepts(String playerGender) {
        return name().equals(playerGender);
    }

    @Override
    public String toString() {
        return label;
    }
}
