package com.ips2026.pl11.model.team;

/**
 * Gender of a team. The players of a male or female team must have that
 * gender; a mixed team accepts any player. Coaches and technical staff are
 * not checked.
 */
public enum TeamGender {

    MALE("Male"), FEMALE("Female"), MIXED("Mixed");

    private final String label;

    TeamGender(String label) {
        this.label = label;
    }

    /** @param playerGender "MALE", "FEMALE" or null if it is not registered */
    public boolean accepts(String playerGender) {
        return this == MIXED || name().equals(playerGender);
    }

    @Override
    public String toString() {
        return label;
    }
}
