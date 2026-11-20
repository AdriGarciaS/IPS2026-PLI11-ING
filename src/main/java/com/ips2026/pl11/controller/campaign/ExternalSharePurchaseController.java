package com.ips2026.pl11.controller.campaign;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.ips2026.pl11.controller.PasswordEncrypting;
import com.ips2026.pl11.data.campaign.CampaignDAO;
import com.ips2026.pl11.data.campaign.CampaignPurchaseDAO;
import com.ips2026.pl11.data.shareholder.ShareholderDAO;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignPurchase;
import com.ips2026.pl11.model.campaign.CampaignPurchaseLimit;
import com.ips2026.pl11.model.campaign.ShareholderRegistration;
import com.ips2026.pl11.model.reservation.CardValidator;

/**
 * US 35828: an external user (not a shareholder yet) views the active
 * campaign and, when it is in phase 3, buys any number of the remaining
 * shares. To buy them they register as a shareholder (first name, last name,
 * email, national ID, username and password) and pay with a credit card.
 */
public class ExternalSharePurchaseController {

    private final CampaignDAO campaignDAO;
    private final CampaignPurchaseDAO purchaseDAO;
    private final ShareholderDAO shareholderDAO;
    private final Supplier<LocalDateTime> now;
    private final UnaryOperator<String> passwordHasher;

    private Campaign campaign;
    private CampaignPurchaseLimit limit;

    public ExternalSharePurchaseController(CampaignDAO campaignDAO, CampaignPurchaseDAO purchaseDAO,
            ShareholderDAO shareholderDAO) {
        this(campaignDAO, purchaseDAO, shareholderDAO, LocalDateTime::now, PasswordEncrypting::encryptPassword);
    }

    /**
     * @param now            gives the moment of the purchase (the tests pass a fixed one)
     * @param passwordHasher hashes the password before storing it (the tests pass a fast one)
     */
    public ExternalSharePurchaseController(CampaignDAO campaignDAO, CampaignPurchaseDAO purchaseDAO,
            ShareholderDAO shareholderDAO, Supplier<LocalDateTime> now, UnaryOperator<String> passwordHasher) {
        this.campaignDAO = campaignDAO;
        this.purchaseDAO = purchaseDAO;
        this.shareholderDAO = shareholderDAO;
        this.now = now;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Reads the active campaign (the one not finished, in any phase) and how
     * many shares an external user can buy in it.
     *
     * @return false if there is no active campaign
     */
    public boolean load() throws SQLException {
        Optional<Campaign> open = campaignDAO.findOpen();
        if (open.isEmpty()) {
            campaign = null;
            limit = null;
            return false;
        }
        campaign = open.get();
        limit = purchaseDAO.findExternalLimit(campaign.getId());
        return true;
    }

    /** The active campaign (after a successful {@link #load()}). */
    public Campaign getCampaign() {
        return campaign;
    }

    /** How many shares an external user can buy (after a successful {@link #load()}). */
    public CampaignPurchaseLimit getLimit() {
        return limit;
    }

    /** @return why the quantity typed is not valid, or empty if it can be bought */
    public Optional<String> findQuantityProblem(String text) {
        return limit.findQuantityProblem(text);
    }

    /** Price of {@code quantity} shares at the nominal price of the campaign. */
    public BigDecimal totalFor(int quantity) {
        return campaign.getNominalPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Every problem of the registration data and the card, one message per
     * problem: format problems, and a national ID, email or username already
     * registered (the username cannot be the one of an employee either).
     */
    public List<String> findRegistrationProblems(ShareholderRegistration registration, String cardNumber)
            throws SQLException {
        List<String> problems = registration.findFormatProblems();
        if (!problems.isEmpty()) {
            CardValidator.findProblem(cardNumber).ifPresent(problems::add);
            return problems;
        }
        if (shareholderDAO.existsEmail(registration.getEmail())) {
            problems.add("There is already a shareholder with this email.");
        }
        if (shareholderDAO.existsNationalId(registration.getNationalId())) {
            problems.add("There is already a shareholder with this national ID: log in to buy shares.");
        }
        if (shareholderDAO.existsUsername(registration.getUsername())) {
            problems.add("This username is already taken. Please choose another one.");
        }
        CardValidator.findProblem(cardNumber).ifPresent(problems::add);
        return problems;
    }

    /**
     * Registers the user as a new shareholder (with the password hashed) and
     * buys the shares, which go to their wallet. Afterwards the campaign and
     * the limit are read again.
     *
     * @throws IllegalArgumentException if the quantity, the registration data or the
     *                                  card are not valid; one line for each problem
     * @throws IllegalStateException    if the shares can no longer be bought (nothing is stored)
     */
    public CampaignPurchase registerAndBuy(String quantityText, ShareholderRegistration registration,
            String cardNumber) throws SQLException {
        List<String> problems = findRegistrationProblems(registration, cardNumber);
        findQuantityProblem(quantityText).ifPresent(problem -> problems.add(0, problem));
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join(System.lineSeparator(), problems));
        }

        int quantity = Integer.parseInt(quantityText.strip());
        try {
            return purchaseDAO.registerAndBuy(campaign, registration, passwordHasher.apply(registration.getPassword()),
                    quantity, now.get());
        } finally {
            load();
        }
    }
}
