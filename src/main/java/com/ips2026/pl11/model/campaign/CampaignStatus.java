package com.ips2026.pl11.model.campaign;

/**
 * Phases of a shareholder campaign, in order. A new campaign is
 * {@link #INACTIVE}; the manager moves it forward until it is
 * {@link #FINISHED}, and it never goes back.
 */
public enum CampaignStatus {

    INACTIVE("Inactive"),
    PHASE_1("Phase 1"),
    PHASE_2("Phase 2"),
    PHASE_3("Phase 3"),
    FINISHED("Finished");

    private final String label;

    CampaignStatus(String label) {
        this.label = label;
    }

    /** Text shown to the user (for example "Phase 2"). */
    public String getLabel() {
        return label;
    }
}
