package com.ips2026.pl11.controller.market;

import com.ips2026.pl11.data.market.MarketDAO;
import com.ips2026.pl11.model.market.CartItem;
import com.ips2026.pl11.model.market.MarketFilter;
import com.ips2026.pl11.model.market.MarketPurchase;
import com.ips2026.pl11.model.market.SaleType;
import com.ips2026.pl11.model.market.ShareCart;
import com.ips2026.pl11.model.market.ShareListing;
import com.ips2026.pl11.model.reservation.CardValidator;
import com.ips2026.pl11.model.shareholder.Shareholder;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Controller of the share market: a shareholder sees the shares that the
 * other shareholders sell, filters them, adds packs or individual shares
 * to a cart and buys them with a card.
 */
public class ShareMarketController {

    private final MarketDAO marketDAO;
    private final Shareholder buyer;
    private final Supplier<LocalDateTime> now;
    private final ShareCart cart = new ShareCart();

    private List<ShareListing> listings = new ArrayList<>();

    /** @param buyer the shareholder chosen in the portal */
    public ShareMarketController(MarketDAO marketDAO, Shareholder buyer) {
        this(marketDAO, buyer, LocalDateTime::now);
    }

    /** @param now gives the current moment (the tests use a fixed one) */
    public ShareMarketController(MarketDAO marketDAO, Shareholder buyer, Supplier<LocalDateTime> now) {
        this.marketDAO = marketDAO;
        this.buyer = buyer;
        this.now = now;
    }

    public Shareholder getBuyer() {
        return buyer;
    }

    /** Reads again the listings of the other shareholders. */
    public void load() throws SQLException {
        listings = marketDAO.findOpenListings(buyer.getId());
    }

    /** The listings that match the filter, from the newest to the oldest. */
    public List<ShareListing> getListings(MarketFilter filter) {
        return listings.stream().filter(filter::matches).toList();
    }

    /**
     * Creates the filter from the texts of the window (empty = no limit; the
     * prices can use a comma or a dot).
     *
     * @throws IllegalArgumentException if a value is not a number or the limits are wrong
     */
    public MarketFilter createFilter(String minPrice, String maxPrice, String minShares, String maxShares,
            SaleType saleType) {
        return new MarketFilter(parsePrice(minPrice, "minimum price"), parsePrice(maxPrice, "maximum price"),
                parseShares(minShares, "minimum number of shares"), parseShares(maxShares, "maximum number of shares"),
                saleType);
    }

    // ------------------------------------------------------------------
    // Cart
    // ------------------------------------------------------------------

    public ShareCart getCart() {
        return cart;
    }

    /** @throws IllegalStateException if the pack cannot be added (see {@link ShareCart#addPack}) */
    public void addPack(ShareListing listing) {
        cart.addPack(listing);
    }

    /** @throws IllegalStateException if the pack of the listing is in the cart */
    public void addShares(ShareListing listing, Collection<Integer> shareIds) {
        cart.addShares(listing, shareIds);
    }

    public void removeFromCart(CartItem item) {
        cart.remove(item);
    }

    public void emptyCart() {
        cart.clear();
    }

    // ------------------------------------------------------------------
    // Checkout
    // ------------------------------------------------------------------

    /**
     * Buys the cart: the shares change their owner to the buyer (the
     * shareholder chosen in the portal). Then the cart is emptied and the
     * listings are read again.
     *
     * @param cardNumber credit card number
     * @throws IllegalArgumentException if the cart is empty or the card is wrong
     * @throws IllegalStateException    if a share is no longer available (nothing is bought; the cart is
     *                                  emptied and the listings are read again)
     */
    public MarketPurchase checkout(String cardNumber) throws SQLException {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("The cart is empty.");
        }
        Optional<String> cardProblem = CardValidator.findProblem(cardNumber == null ? "" : cardNumber);
        if (cardProblem.isPresent()) {
            throw new IllegalArgumentException(cardProblem.get());
        }

        MarketPurchase purchase;
        try {
            purchase = marketDAO.buy(buyer.getId(), cart.getItems(), now.get());
        } catch (IllegalStateException exception) {
            cart.clear();
            load();
            throw new IllegalStateException(exception.getMessage() + System.lineSeparator()
                    + "Nothing has been bought: the cart has been emptied and the sales have been updated.");
        }
        cart.clear();
        load();
        return purchase;
    }

    // ------------------------------------------------------------------
    // Parsing of the filter
    // ------------------------------------------------------------------

    private static BigDecimal parsePrice(String text, String name) {
        String clean = text == null ? "" : text.strip().replace(',', '.');
        if (clean.isEmpty()) {
            return null;
        }
        if (!clean.matches("\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException("The " + name + " must be a number, for example 12,50.");
        }
        return new BigDecimal(clean);
    }

    private static Integer parseShares(String text, String name) {
        String clean = text == null ? "" : text.strip();
        if (clean.isEmpty()) {
            return null;
        }
        if (!clean.matches("\\d{1,9}")) {
            throw new IllegalArgumentException("The " + name + " must be a whole number, for example 10.");
        }
        return Integer.valueOf(clean);
    }
}
