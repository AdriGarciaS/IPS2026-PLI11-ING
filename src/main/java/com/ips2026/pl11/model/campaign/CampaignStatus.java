package com.ips2026.pl11.model.campaign;

/**
 * Phases of a shareholder campaign, in order. A new campaign is
 * {@link #INACTIVE}; the manager moves it forward until it is
 * {@link #FINISHED}, and it never goes back.
 */
public enum CampaignStatus {

    INACTIVE("Inactive", "nobody can buy shares yet"),
    PHASE_1("Phase 1", "only shareholders can buy shares, up to the number they already own"),
    PHASE_2("Phase 2", "only shareholders can buy the remaining shares, with no limit"),
    PHASE_3("Phase 3", "anyone (shareholders or external users) can buy the remaining shares, with no limit"),
    FINISHED("Finished", "no more shares can be bought");

    private final String label;
    private final String rule;

    CampaignStatus(String label, String rule) {
        this.label = label;
        this.rule = rule;
    }

    /** Text shown to the user (for example "Phase 2"). */
    public String getLabel() {
        return label;
    }

    /** Who can buy shares in this phase, and how many (US 35778). */
    public String getRule() {
        return rule;
    }

    /**
     * The phase that comes after this one (phases only move forward, one at
     * a time), or null for {@link #FINISHED}, which cannot be reactivated.
     */
    public CampaignStatus next() {
        return this == FINISHED ? null : values()[ordinal() + 1];
    }
}
