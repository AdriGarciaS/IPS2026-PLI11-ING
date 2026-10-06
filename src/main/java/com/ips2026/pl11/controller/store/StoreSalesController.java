package com.ips2026.pl11.controller.store;

import com.ips2026.pl11.data.store.MerchandiseDAO;
import com.ips2026.pl11.data.store.MerchandiseSaleDAO;
import com.ips2026.pl11.model.store.Cart;
import com.ips2026.pl11.model.store.Merchandise;
import com.ips2026.pl11.model.store.SaleReceipt;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller of the store sales window: keeps the catalog and the cart and
 * registers the purchases.
 */
public class StoreSalesController {

    private final MerchandiseDAO merchandiseDAO;
    private final MerchandiseSaleDAO saleDAO;
    private final Cart cart = new Cart();

    private List<Merchandise> catalog = new ArrayList<>();
    private List<String> types = new ArrayList<>();

    public StoreSalesController(MerchandiseDAO merchandiseDAO, MerchandiseSaleDAO saleDAO) {
        this.merchandiseDAO = merchandiseDAO;
        this.saleDAO = saleDAO;
    }

    /** (Re)loads the catalog and the product types from the database. */
    public void loadCatalog() throws SQLException {
        catalog = merchandiseDAO.findAll();
        types = merchandiseDAO.findTypes();
    }

    public List<String> getTypes() {
        return List.copyOf(types);
    }

    /**
     * Products of the catalog whose name contains {@code nameText} (ignoring
     * case) and whose type is {@code type}. A blank text or a null type mean
     * "no filter".
     */
    public List<Merchandise> filterCatalog(String nameText, String type) {
        String search = nameText == null ? "" : nameText.strip().toLowerCase(Locale.ROOT);
        List<Merchandise> result = new ArrayList<>();
        for (Merchandise product : catalog) {
            boolean nameMatches = product.getName().toLowerCase(Locale.ROOT).contains(search);
            boolean typeMatches = type == null || product.getType().equals(type);
            if (nameMatches && typeMatches) {
                result.add(product);
            }
        }
        return result;
    }

    public Cart getCart() {
        return cart;
    }

    public void addToCart(Merchandise product, int units) {
        cart.add(product, units);
    }

    public void changeUnits(int merchandiseId, int units) {
        cart.setUnits(merchandiseId, units);
    }

    public void removeFromCart(int merchandiseId) {
        cart.remove(merchandiseId);
    }

    /**
     * Buys the cart: stores the sale, empties the cart and reloads the
     * catalog so that the new stock is shown.
     *
     * @throws IllegalStateException if the cart is empty
     */
    public SaleReceipt purchase() throws SQLException {
        if (cart.isEmpty()) {
            throw new IllegalStateException("The cart is empty");
        }
        LocalDate today = LocalDate.now();
        BigDecimal finalPrice = cart.getTotal();
        int saleNumber = saleDAO.registerSale(cart.getLines(), today);
        SaleReceipt receipt = new SaleReceipt(saleNumber, today, cart.getLines(), finalPrice);

        cart.clear();
        loadCatalog();
        return receipt;
    }
}
