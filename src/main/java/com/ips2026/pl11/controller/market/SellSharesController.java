package com.ips2026.pl11.controller.market;

import com.ips2026.pl11.data.market.ListingDAO;
import com.ips2026.pl11.model.market.OwnedShare;
import com.ips2026.pl11.model.reservation.CardValidator;
import com.ips2026.pl11.model.shareholder.Shareholder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Controller of "Sell Shares": the shareholder chooses shares of their
 * wallet and puts them up for sale as one listing, with a price per share
 * and, optionally, a pack price. The card number (where the money is
 * received) is checked but not stored.
 */
public class SellSharesController {

    private final ListingDAO listingDAO;
    private final Shareholder seller;
    private final Supplier<LocalDateTime> now;

    private List<OwnedShare> shares = new ArrayList<>();

    /** @param seller the shareholder chosen in the portal */
    public SellSharesController(ListingDAO listingDAO, Shareholder seller) {
        this(listingDAO, seller, LocalDateTime::now);
    }

    /** @param now gives the current moment (the tests use a fixed one) */
    public SellSharesController(ListingDAO listingDAO, Shareholder seller, Supplier<LocalDateTime> now) {
        this.listingDAO = listingDAO;
        this.seller = seller;
        this.now = now;
    }

    public Shareholder getSeller() {
        return seller;
    }

    /** Reads again the shares of the wallet of the seller. */
    public void load() throws SQLException {
        shares = listingDAO.findOwnedShares(seller.getId());
    }

    /** The campaigns of the shares of the wallet, in alphabetical order. */
    public List<String> getCampaigns() {
        return shares.stream().map(OwnedShare::getCampaignName).distinct().sorted().toList();
    }

    /** The shares of the wallet of one campaign (or of all of them if {@code campaign} is null), by ID. */
    public List<OwnedShare> getShares(String campaign) {
        return shares.stream().filter(share -> campaign == null || share.getCampaignName().equals(campaign)).toList();
    }

    /**
     * IDs of the shares of the campaign (or all) between two IDs, both
     * included, that can be sold (the ones already on sale are skipped).
     *
     * @throws IllegalArgumentException if a limit is missing, is not a whole number, or "from" is greater than "to"
     */
    public List<Integer> getIdsInRange(String campaign, String fromText, String toText) {
        Integer from = parseId(fromText, "first ID");
        Integer to = parseId(toText, "last ID");
        if (from > to) {
            throw new IllegalArgumentException("The first ID cannot be greater than the last ID.");
        }
        return getShares(campaign).stream()
                .filter(share -> !share.isOnSale() && share.getShareId() >= from && share.getShareId() <= to)
                .map(OwnedShare::getShareId).toList();
    }

    /** Number of selected shares x price per share, or null if the price is not valid yet. */
    public BigDecimal getSumOfPrices(int selectedShares, String pricePerShare) {
        BigDecimal price = parsePrice(pricePerShare);
        return price == null ? null : price.multiply(BigDecimal.valueOf(selectedShares));
    }

    /**
     * Everything that prevents putting the shares up for sale, one problem per
     * element (empty if they can be put up for sale).
     *
     * @param packPrice optional: empty if there is no pack
     */
    public List<String> findProblems(Collection<Integer> shareIds, String cardNumber, String pricePerShare, String packPrice) {
        List<String> problems = new ArrayList<>();
        if (shareIds.isEmpty()) {
            problems.add("Select at least one share.");
        }
        Map<Integer, OwnedShare> owned = shares.stream()
                .collect(Collectors.toMap(OwnedShare::getShareId, Function.identity()));
        for (int shareId : shareIds) {
            OwnedShare share = owned.get(shareId);
            if (share == null) {
                problems.add("Share " + shareId + " is not in your wallet.");
            } else if (share.isOnSale()) {
                problems.add("Share " + shareId + " is already on sale.");
            }
        }
        CardValidator.findProblem(cardNumber == null ? "" : cardNumber).ifPresent(problems::add);
        priceProblem(pricePerShare, "price per share", true).ifPresent(problems::add);
        if (!isBlank(packPrice)) {
            priceProblem(packPrice, "pack price", false).ifPresent(problems::add);
            if (shareIds.size() == 1) {
                problems.add("A pack price needs at least 2 shares.");
            }
        }
        return problems;
    }

    /**
     * Puts the shares up for sale as one listing, then reads the wallet again.
     *
     * @return the id of the new listing
     * @throws IllegalArgumentException if something is wrong (see {@link #findProblems}); one line per problem
     * @throws IllegalStateException    if a share is no longer in the wallet or is already on sale
     */
    public int putUpForSale(Collection<Integer> shareIds, String cardNumber, String pricePerShare, String packPrice)
            throws SQLException {
        List<String> problems = findProblems(shareIds, cardNumber, pricePerShare, packPrice);
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join(System.lineSeparator(), problems));
        }
        BigDecimal pack = isBlank(packPrice) ? null : parsePrice(packPrice);
        int listingId = listingDAO.insertListing(seller.getId(), shareIds.stream().sorted().toList(),
                parsePrice(pricePerShare), pack, now.get());
        load();
        return listingId;
    }

    // ------------------------------------------------------------------
    // Parsing
    // ------------------------------------------------------------------

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    /** The price with 2 decimals (comma or dot), or null if it is not a valid price greater than 0. */
    private static BigDecimal parsePrice(String text) {
        return priceProblem(text, "price", true).isPresent() ? null
                : new BigDecimal(text.strip().replace(',', '.')).setScale(2, RoundingMode.UNNECESSARY);
    }

    private static Optional<String> priceProblem(String text, String name, boolean required) {
        String clean = text == null ? "" : text.strip().replace(',', '.');
        if (clean.isEmpty()) {
            return required ? Optional.of("The " + name + " is required.") : Optional.empty();
        }
        if (!clean.matches("\\d+(\\.\\d+)?")) {
            return Optional.of("The " + name + " must be a number, for example 12,50.");
        }
        BigDecimal price = new BigDecimal(clean);
        if (price.signum() <= 0) {
            return Optional.of("The " + name + " must be greater than 0.");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            return Optional.of("The " + name + " can have at most 2 decimals.");
        }
        return Optional.empty();
    }

    private static Integer parseId(String text, String name) {
        String clean = text == null ? "" : text.strip();
        if (!clean.matches("\\d{1,9}")) {
            throw new IllegalArgumentException("The " + name + " of the range must be a whole number.");
        }
        return Integer.valueOf(clean);
    }
}
