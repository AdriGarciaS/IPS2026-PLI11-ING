package com.ips2026.pl11.model.campaign;

import java.math.BigDecimal;

/**
 * Shareholder campaign (table SHARE_CAMPAIGN): the club offers a number of
 * shares at a nominal price. The shares themselves, with their IDs, are
 * created only when someone buys them.
 */
public class Campaign {

    private final int id;
    private final String name;
    private final BigDecimal nominalPrice;
    private final int totalShares;
    private final CampaignStatus status;

    public Campaign(int id, String name, BigDecimal nominalPrice, int totalShares, CampaignStatus status) {
        this.id = id;
        this.name = name;
        this.nominalPrice = nominalPrice;
        this.totalShares = totalShares;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Price of one share in this campaign. */
    public BigDecimal getNominalPrice() {
        return nominalPrice;
    }

    /** Number of shares offered for sale. */
    public int getTotalShares() {
        return totalShares;
    }

    public CampaignStatus getStatus() {
        return status;
    }
}
