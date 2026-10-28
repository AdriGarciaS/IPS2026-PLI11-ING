package com.ips2026.pl11.model.history;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One operation that produced income: a merchandising purchase, a ticket
 * purchase, a season ticket, a facility booking or a share purchase.
 */
public class Income {

    private final IncomeType type;
    private final LocalDateTime moment;
    private final String concept;
    private final BigDecimal amount;
    private final int reference;

    /**
     * @param type      concept of the income
     * @param moment    when it was registered in the database
     * @param concept   short description (for example "Sale #3 · 2 products")
     * @param amount    total amount of the operation
     * @param reference number of the operation in its own table (for a
     *                  merchandising purchase, its sale number)
     */
    public Income(IncomeType type, LocalDateTime moment, String concept, BigDecimal amount, int reference) {
        this.type = type;
        this.moment = moment;
        this.concept = concept;
        this.amount = amount;
        this.reference = reference;
    }

    public IncomeType getType() {
        return type;
    }

    public LocalDateTime getMoment() {
        return moment;
    }

    public String getConcept() {
        return concept;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int getReference() {
        return reference;
    }

    /** Only merchandising purchases have details (the products sold). */
    public boolean hasDetails() {
        return type == IncomeType.MERCHANDISING;
    }
}
