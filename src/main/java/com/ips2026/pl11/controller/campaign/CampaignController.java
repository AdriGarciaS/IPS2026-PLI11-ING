package com.ips2026.pl11.controller.campaign;

import com.ips2026.pl11.controller.log.SystemLog;
import com.ips2026.pl11.model.log.LogAction;
import com.ips2026.pl11.data.campaign.CampaignDAO;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller of the shareholder campaigns: the manager creates a new
 * campaign with a name, a nominal price per share and the number of shares
 * offered. It is always created inactive, and only one campaign can be open
 * (not finished) at a time. Then the manager moves the open campaign
 * forward through its phases (US 35778).
 */
public class CampaignController {

    private final CampaignDAO campaignDAO;

    public CampaignController(CampaignDAO campaignDAO) {
        this.campaignDAO = campaignDAO;
    }

    /** The campaign that is not finished yet, if any (while there is one, no other can be created). */
    public Optional<Campaign> findOpenCampaign() throws SQLException {
        return campaignDAO.findOpen();
    }

    /** Number of shares of the campaign already sold. */
    public int countSoldShares(Campaign campaign) throws SQLException {
        return campaignDAO.countSoldShares(campaign.getId());
    }

    /**
     * US 35778: moves the open campaign to the next phase (inactive, phase 1,
     * phase 2, phase 3, finished) and records the change. Phases only move
     * forward and a finished campaign cannot be reactivated.
     *
     * @param campaign the open campaign, as the manager sees it
     * @return the campaign in its new phase
     * @throws IllegalStateException if the campaign is finished, or it is no
     *                               longer the open campaign in that phase
     */
    public Campaign moveToNextPhase(Campaign campaign) throws SQLException {
        CampaignStatus next = campaign.getStatus().next();
        if (next == null) {
            throw new IllegalStateException("Campaign '" + campaign.getName()
                    + "' is finished: it cannot be reactivated.");
        }
        Optional<Campaign> open = campaignDAO.findOpen();
        if (open.isEmpty() || open.get().getId() != campaign.getId()
                || open.get().getStatus() != campaign.getStatus()) {
            throw new IllegalStateException("Campaign '" + campaign.getName()
                    + "' has changed meanwhile. Close the window and open it again.");
        }
        campaignDAO.changePhase(campaign.getId(), campaign.getStatus(), next, LocalDateTime.now());
        SystemLog.record(LogAction.UPDATE, "Campaign " + campaign.getName() + ": "
                + campaign.getStatus().getLabel() + " -> " + next.getLabel());
        return campaign.withStatus(next);
    }

    /**
     * Checks the data typed by the manager and saves the new campaign as
     * inactive.
     *
     * @param name         campaign name (required, unique ignoring upper/lower case)
     * @param nominalPrice price of one share, greater than 0 and with at most
     *                     2 decimals ("10,50" and "10.50" are both accepted)
     * @param totalShares  whole number of shares offered, at least 1
     * @return the campaign created
     * @throws IllegalStateException    if another campaign is not finished yet
     * @throws IllegalArgumentException if some field is wrong; the message
     *                                  has one line for each problem
     */
    public Campaign create(String name, String nominalPrice, String totalShares) throws SQLException {
        Optional<Campaign> open = campaignDAO.findOpen();
        if (open.isPresent()) {
            throw new IllegalStateException("Campaign '" + open.get().getName() + "' is still open ("
                    + open.get().getStatus().getLabel() + "). Finish it before creating a new one.");
        }

        List<String> problems = new ArrayList<>();
        String cleanName = name == null ? "" : name.strip();
        if (cleanName.isEmpty()) {
            problems.add("The campaign name is required.");
        } else if (campaignDAO.existsName(cleanName)) {
            problems.add("There is already a campaign called '" + cleanName + "'.");
        }
        BigDecimal price = parsePrice(nominalPrice, problems);
        Integer shares = parseShares(totalShares, problems);

        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join(System.lineSeparator(), problems));
        }
        Campaign created = campaignDAO.insert(cleanName, price, shares);
        SystemLog.record(LogAction.INSERT, "Created campaign " + cleanName + " (" + shares + " shares at "
                + price.toPlainString() + " € each)");
        return created;
    }

    /** The price with 2 decimals, or null (and a problem added) if it is not valid. */
    private static BigDecimal parsePrice(String text, List<String> problems) {
        String clean = text == null ? "" : text.strip().replace(',', '.');
        if (clean.isEmpty()) {
            problems.add("The nominal price per share is required.");
            return null;
        }
        if (!clean.matches("\\d+(\\.\\d+)?")) {
            problems.add("The nominal price per share must be a number, for example 10,50.");
            return null;
        }
        BigDecimal price = new BigDecimal(clean);
        if (price.signum() <= 0) {
            problems.add("The nominal price per share must be greater than 0.");
            return null;
        }
        if (price.stripTrailingZeros().scale() > 2) {
            problems.add("The nominal price per share can have at most 2 decimals.");
            return null;
        }
        return price.setScale(2, RoundingMode.UNNECESSARY);
    }

    /** The number of shares, or null (and a problem added) if it is not valid. */
    private static Integer parseShares(String text, List<String> problems) {
        String clean = text == null ? "" : text.strip();
        if (clean.isEmpty()) {
            problems.add("The total number of shares is required.");
            return null;
        }
        if (!clean.matches("\\d+")) {
            problems.add("The total number of shares must be a whole number, for example 1000.");
            return null;
        }
        int shares;
        try {
            shares = Integer.parseInt(clean);
        } catch (NumberFormatException exception) {
            problems.add("The total number of shares is too large.");
            return null;
        }
        if (shares <= 0) {
            problems.add("The total number of shares must be at least 1.");
            return null;
        }
        return shares;
    }
}
