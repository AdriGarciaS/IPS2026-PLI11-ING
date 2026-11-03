package com.ips2026.pl11.model.market;

/**
 * A share of the wallet of a shareholder, as shown when choosing the shares
 * to sell: its ID, the campaign it comes from and whether it is already in
 * an open sale (then it cannot be put up for sale again).
 */
public class OwnedShare {

    private final int shareId;
    private final String campaignName;
    private final boolean onSale;

    public OwnedShare(int shareId, String campaignName, boolean onSale) {
        this.shareId = shareId;
        this.campaignName = campaignName;
        this.onSale = onSale;
    }

    public int getShareId() {
        return shareId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    /** Whether the share is already in a sale that has not sold it yet. */
    public boolean isOnSale() {
        return onSale;
    }
}
