package com.ips2026.pl11.model.history;

/**
 * Concepts that produce income for the club, in the order they are shown
 * in the sales history.
 */
public enum IncomeType {

    MERCHANDISING("Merchandising", "Merchandising"),
    TICKET("Ticket", "Tickets"),
    SEASON_TICKET("Season ticket", "Season tickets"),
    FACILITY("Facility", "Facility bookings"),
    CAMPAIGN("Campaign", "Shareholder campaigns");

    private final String label;
    private final String pluralLabel;

    IncomeType(String label, String pluralLabel) {
        this.label = label;
        this.pluralLabel = pluralLabel;
    }

    /** Name of one operation of this type (for example "Ticket"), for the table. */
    public String getLabel() {
        return label;
    }

    /** Name of the whole type (for example "Tickets"), for the filter and the summary. */
    public String getPluralLabel() {
        return pluralLabel;
    }
}
