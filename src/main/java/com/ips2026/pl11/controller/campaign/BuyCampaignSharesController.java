package com.ips2026.pl11.controller.campaign;

import com.ips2026.pl11.controller.log.SystemLog;
import com.ips2026.pl11.model.log.LogAction;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import com.ips2026.pl11.data.campaign.CampaignDAO;
import com.ips2026.pl11.data.campaign.CampaignPurchaseDAO;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignPurchase;
import com.ips2026.pl11.model.campaign.CampaignPurchaseLimit;
import com.ips2026.pl11.model.reservation.CardValidator;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * US 35947: a shareholder buys shares of the active campaign (in phase 1, 2
 * or 3) and pays them with a credit card. See {@link CampaignPurchaseLimit}
 * for how many shares they can buy in each phase.
 */
public class BuyCampaignSharesController {

    private final CampaignDAO campaignDAO;
    private final CampaignPurchaseDAO purchaseDAO;
    private final Shareholder shareholder;
    private final Supplier<LocalDateTime> now;

    private Campaign campaign;
    private CampaignPurchaseLimit limit;

    public BuyCampaignSharesController(CampaignDAO campaignDAO, CampaignPurchaseDAO purchaseDAO,
            Shareholder shareholder) {
        this(campaignDAO, purchaseDAO, shareholder, LocalDateTime::now);
    }

    /** @param now gives the moment of the purchase (the tests pass a fixed one) */
    public BuyCampaignSharesController(CampaignDAO campaignDAO, CampaignPurchaseDAO purchaseDAO,
            Shareholder shareholder, Supplier<LocalDateTime> now) {
        this.campaignDAO = campaignDAO;
        this.purchaseDAO = purchaseDAO;
        this.shareholder = shareholder;
        this.now = now;
    }

    /**
     * Reads the active campaign and how many shares the shareholder can buy.
     *
     * @return false if there is no campaign in phase 1, 2 or 3 (nothing can be bought)
     */
    public boolean load() throws SQLException {
        Optional<Campaign> open = campaignDAO.findOpen();
        if (open.isEmpty() || !open.get().getStatus().allowsShareholderPurchases()) {
            campaign = null;
            limit = null;
            return false;
        }
        campaign = open.get();
        limit = purchaseDAO.findLimit(campaign.getId(), shareholder.getId());
        SystemLog.record(LogAction.QUERY, "Viewed campaign " + campaign.getName() + " to buy shares ("
                + campaign.getStatus().getLabel() + ", " + limit.getRemainingShares() + " shares remaining)");
        return true;
    }

    public Shareholder getShareholder() {
        return shareholder;
    }

    /** The active campaign (after a successful {@link #load()}). */
    public Campaign getCampaign() {
        return campaign;
    }

    /** How many shares the shareholder can buy (after a successful {@link #load()}). */
    public CampaignPurchaseLimit getLimit() {
        return limit;
    }

    /**
     * @return why the quantity typed is not valid, or empty if it is a whole
     *         number between 1 and the maximum the shareholder can buy
     */
    public Optional<String> findQuantityProblem(String text) {
        return limit.findQuantityProblem(text);
    }

    /** Price of {@code quantity} shares at the nominal price of the campaign. */
    public BigDecimal totalFor(int quantity) {
        return campaign.getNominalPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Buys the shares and adds them to the shareholder's wallet. Afterwards
     * the limit is read again (fewer shares remain).
     *
     * @throws IllegalArgumentException if the quantity or the card is not valid;
     *                                  the message has one line for each problem
     * @throws IllegalStateException    if the shareholder can no longer buy that many
     *                                  shares (nothing is bought)
     */
    public CampaignPurchase buy(String quantityText, String cardNumber) throws SQLException {
        StringBuilder problems = new StringBuilder();
        findQuantityProblem(quantityText).ifPresent(problems::append);
        CardValidator.findProblem(cardNumber).ifPresent(problem -> {
            if (problems.length() > 0) {
                problems.append(System.lineSeparator());
            }
            problems.append(problem);
        });
        if (problems.length() > 0) {
            throw new IllegalArgumentException(problems.toString());
        }

        int quantity = Integer.parseInt(quantityText.strip());
        try {
            CampaignPurchase purchase = purchaseDAO.buy(campaign, shareholder, quantity, now.get());
            SystemLog.record(LogAction.INSERT, "Bought " + quantity + " shares of campaign " + campaign.getName()
                    + " (IDs " + purchase.getFirstShareId() + " - " + purchase.getLastShareId() + ")");
            return purchase;
        } finally {
            load();
        }
    }
}
