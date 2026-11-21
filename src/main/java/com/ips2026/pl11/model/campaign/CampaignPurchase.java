package com.ips2026.pl11.model.campaign;

import java.math.BigDecimal;

/**
 * US 35947: shares bought by a shareholder in a campaign. They get new
 * consecutive IDs, from {@code firstShareId} to {@code lastShareId}.
 */
public class CampaignPurchase {

    private final String shareholderName;
    private final String campaignName;
    private final int quantity;
    private final int firstShareId;
    private final int lastShareId;
    private final BigDecimal pricePerShare;

    public CampaignPurchase(String shareholderName, String campaignName, int quantity, int firstShareId,
            int lastShareId, BigDecimal pricePerShare) {
        this.shareholderName = shareholderName;
        this.campaignName = campaignName;
        this.quantity = quantity;
        this.firstShareId = firstShareId;
        this.lastShareId = lastShareId;
        this.pricePerShare = pricePerShare;
    }

    public String getShareholderName() {
        return shareholderName;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getFirstShareId() {
        return firstShareId;
    }

    public int getLastShareId() {
        return lastShareId;
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    /** Price per share times the number of shares. */
    public BigDecimal getTotal() {
        return pricePerShare.multiply(BigDecimal.valueOf(quantity));
    }
}
