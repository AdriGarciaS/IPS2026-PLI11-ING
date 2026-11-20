package com.ips2026.pl11.model.campaign;

/**
 * US 35947: how many shares of the open campaign a shareholder can buy now.
 * <ul>
 *   <li>Phase 1: up to as many shares as the shareholder already owns. The
 *       shares of this same campaign do not count, and the shares already
 *       bought in it are subtracted: owning 100, the shareholder can buy 100
 *       in total in phase 1, at once or in several purchases.</li>
 *   <li>Phases 2 and 3: any number of the remaining shares.</li>
 *   <li>Other phases: nothing.</li>
 * </ul>
 * The maximum is never greater than the remaining shares.
 */
public class CampaignPurchaseLimit {

    private final CampaignStatus status;
    private final int remainingShares;
    private final int ownedShares;
    private final int ownedOutsideCampaign;
    private final int boughtInCampaign;

    /**
     * @param status               current phase of the campaign
     * @param remainingShares      shares of the campaign not sold yet
     * @param ownedShares          shares the shareholder owns now (all campaigns)
     * @param ownedOutsideCampaign shares the shareholder owns now from other campaigns
     * @param boughtInCampaign     shares the shareholder has bought in this campaign
     */
    public CampaignPurchaseLimit(CampaignStatus status, int remainingShares, int ownedShares,
            int ownedOutsideCampaign, int boughtInCampaign) {
        this.status = status;
        this.remainingShares = remainingShares;
        this.ownedShares = ownedShares;
        this.ownedOutsideCampaign = ownedOutsideCampaign;
        this.boughtInCampaign = boughtInCampaign;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public int getRemainingShares() {
        return remainingShares;
    }

    /** Shares the shareholder owns now, of every campaign. */
    public int getOwnedShares() {
        return ownedShares;
    }

    public int getOwnedOutsideCampaign() {
        return ownedOutsideCampaign;
    }

    public int getBoughtInCampaign() {
        return boughtInCampaign;
    }

    /** The largest number of shares the shareholder can buy now (0 if none). */
    public int getMax() {
        if (!status.allowsShareholderPurchases()) {
            return 0;
        }
        int max = remainingShares;
        if (status == CampaignStatus.PHASE_1) {
            max = Math.min(max, ownedOutsideCampaign - boughtInCampaign);
        }
        return Math.max(0, max);
    }

    /** Why the shareholder cannot buy any share now (only meaningful when {@link #getMax()} is 0). */
    public String getReasonForNone() {
        if (!status.allowsShareholderPurchases()) {
            return "The campaign is not open for purchases (" + status.getLabel() + ").";
        }
        if (remainingShares <= 0) {
            return "All the shares of the campaign have been sold.";
        }
        if (boughtInCampaign > 0) {
            return "In phase 1 you can buy up to the " + ownedOutsideCampaign
                    + " shares you already own, and you have already bought " + boughtInCampaign + ".";
        }
        return "In phase 1 you can only buy as many shares as you already own, and you have none.";
    }
}
