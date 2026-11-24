package com.ips2026.pl11.model.campaign;

import java.util.Optional;

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
 * US 35828: an external user (not a shareholder yet) can only buy in phase
 * 3, any number of the remaining shares (see {@link #forExternalUser}).
 * The maximum is never greater than the remaining shares.
 */
public class CampaignPurchaseLimit {

    private final CampaignStatus status;
    private final int remainingShares;
    private final int ownedShares;
    private final int ownedOutsideCampaign;
    private final int boughtInCampaign;
    private final boolean externalUser;

    /**
     * @param status               current phase of the campaign
     * @param remainingShares      shares of the campaign not sold yet
     * @param ownedShares          shares the shareholder owns now (all campaigns)
     * @param ownedOutsideCampaign shares the shareholder owns now from other campaigns
     * @param boughtInCampaign     shares the shareholder has bought in this campaign
     */
    public CampaignPurchaseLimit(CampaignStatus status, int remainingShares, int ownedShares,
            int ownedOutsideCampaign, int boughtInCampaign) {
        this(status, remainingShares, ownedShares, ownedOutsideCampaign, boughtInCampaign, false);
    }

    private CampaignPurchaseLimit(CampaignStatus status, int remainingShares, int ownedShares,
            int ownedOutsideCampaign, int boughtInCampaign, boolean externalUser) {
        this.externalUser = externalUser;
        this.status = status;
        this.remainingShares = remainingShares;
        this.ownedShares = ownedShares;
        this.ownedOutsideCampaign = ownedOutsideCampaign;
        this.boughtInCampaign = boughtInCampaign;
    }

    /** US 35828: limit of an external user (not a shareholder yet, so they own no shares). */
    public static CampaignPurchaseLimit forExternalUser(CampaignStatus status, int remainingShares) {
        return new CampaignPurchaseLimit(status, remainingShares, 0, 0, 0, true);
    }

    /** Whether this is the limit of an external user (US 35828). */
    public boolean isExternalUser() {
        return externalUser;
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
        if (externalUser ? !status.allowsExternalPurchases() : !status.allowsShareholderPurchases()) {
            return 0;
        }
        int max = remainingShares;
        if (status == CampaignStatus.PHASE_1 && !externalUser) {
            max = Math.min(max, ownedOutsideCampaign - boughtInCampaign);
        }
        return Math.max(0, max);
    }

    /** Why the shareholder cannot buy any share now (only meaningful when {@link #getMax()} is 0). */
    public String getReasonForNone() {
        if (externalUser && !status.allowsExternalPurchases()) {
            return "External users can buy shares when the campaign reaches phase 3 (now: "
                    + status.getLabel() + ").";
        }
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

    /**
     * @return why the quantity typed is not valid, or empty if it is a whole
     *         number between 1 and {@link #getMax()}
     */
    public Optional<String> findQuantityProblem(String text) {
        String clean = text == null ? "" : text.strip();
        int max = getMax();
        if (max == 0) {
            return Optional.of(getReasonForNone());
        }
        if (clean.isEmpty()) {
            return Optional.of("Enter the number of shares to buy.");
        }
        if (!clean.matches("\\d+")) {
            return Optional.of("The number of shares must be a whole number.");
        }
        if (clean.length() > 9 || Integer.parseInt(clean) > max) {
            return Optional.of("You can buy at most " + max + " shares.");
        }
        if (Integer.parseInt(clean) < 1) {
            return Optional.of("You must buy at least 1 share.");
        }
        return Optional.empty();
    }
}
