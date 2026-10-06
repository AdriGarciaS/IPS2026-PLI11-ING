package com.ips2026.pl11.model.team;

/**
 * Categories of the sports teams and the ages of their players.
 *
 * <p>Professional teams are the first team and the subsidiary team; the rest
 * are youth teams. The age is counted by year: the age the player has or
 * will have during the current year (see {@link TeamRules#ageInYear}).
 * To change an age limit, change it here; {@code null} means no limit.</p>
 */
public enum TeamCategory {

    FIRST_TEAM("First team", true, null, null),
    SUBSIDIARY("Subsidiary team", true, null, null),
    JUVENIL("Juvenil", false, 16, 18),
    CADETE("Cadete", false, 14, 15),
    INFANTIL("Infantil", false, 12, 13),
    ALEVIN("Alevín", false, 10, 11),
    BENJAMIN("Benjamín", false, 8, 9),
    PREBENJAMIN("Pre-benjamín", false, 6, 7);

    private final String label;
    private final boolean professional;
    private final Integer minAge;
    private final Integer maxAge;

    TeamCategory(String label, boolean professional, Integer minAge, Integer maxAge) {
        this.label = label;
        this.professional = professional;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public String getLabel() {
        return label;
    }

    public boolean isProfessional() {
        return professional;
    }

    /** Minimum age, or null if there is no minimum. */
    public Integer getMinAge() {
        return minAge;
    }

    /** Maximum age, or null if there is no maximum. */
    public Integer getMaxAge() {
        return maxAge;
    }

    public boolean acceptsAge(int age) {
        return (minAge == null || age >= minAge) && (maxAge == null || age <= maxAge);
    }

    /** For example "14-15", "16 or older" or "any age". */
    public String getAgeRange() {
        if (minAge != null && maxAge != null) {
            return minAge + "-" + maxAge;
        }
        if (minAge != null) {
            return minAge + " or older";
        }
        if (maxAge != null) {
            return maxAge + " or younger";
        }
        return "any age";
    }

    /** Text that explains the age rule in {@code year}, for example "Players must be 14-15 years old in 2026 (born 2011-2012)". */
    public String describeAgeRule(int year) {
        if (minAge == null && maxAge == null) {
            return "No age limit for the players.";
        }
        String born;
        if (minAge != null && maxAge != null) {
            born = "born " + (year - maxAge) + "-" + (year - minAge);
        } else if (minAge != null) {
            born = "born in " + (year - minAge) + " or before";
        } else {
            born = "born in " + (year - maxAge) + " or later";
        }
        return "Players must be " + getAgeRange() + " years old in " + year + " (" + born + ").";
    }

    /** The label with the ages for youth teams, for example "Cadete (14-15)". */
    @Override
    public String toString() {
        return professional ? label : label + " (" + getAgeRange() + ")";
    }
}
