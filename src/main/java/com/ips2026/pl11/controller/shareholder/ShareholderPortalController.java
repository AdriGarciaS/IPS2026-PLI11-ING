package com.ips2026.pl11.controller.shareholder;

import com.ips2026.pl11.data.shareholder.ShareholderDAO;
import com.ips2026.pl11.model.shareholder.Shareholder;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller of the shareholders portal: finds the shareholder (there is no
 * login, the shareholder types the national ID or searches by name) and
 * keeps the one chosen for the portal options.
 */
public class ShareholderPortalController {

    private final ShareholderDAO shareholderDAO;

    private List<Shareholder> shareholders = new ArrayList<>();
    private Shareholder selected;

    public ShareholderPortalController(ShareholderDAO shareholderDAO) {
        this.shareholderDAO = shareholderDAO;
    }

    public void load() throws SQLException {
        shareholders = shareholderDAO.findAll();
    }

    /**
     * Shareholders whose national ID or name contains {@code text}, ignoring
     * upper/lower case and accents ("perez" finds "Pérez"). An empty text
     * finds nobody.
     */
    public List<Shareholder> search(String text) {
        String search = normalize(text);
        List<Shareholder> result = new ArrayList<>();
        if (search.isEmpty()) {
            return result;
        }
        for (Shareholder shareholder : shareholders) {
            if (normalize(shareholder.getNationalId()).contains(search)
                    || normalize(shareholder.getFullName()).contains(search)) {
                result.add(shareholder);
            }
        }
        return result;
    }

    /** The shareholder whose national ID is exactly {@code text} (ignoring case and spaces), if any. */
    public Optional<Shareholder> findByNationalId(String text) {
        String search = normalize(text);
        return shareholders.stream().filter(s -> normalize(s.getNationalId()).equals(search)).findFirst();
    }

    public void select(Shareholder shareholder) {
        this.selected = shareholder;
    }

    /** The shareholder chosen in the portal, or null if none is chosen yet. */
    public Shareholder getSelected() {
        return selected;
    }

    /** Lower case, without accents and without spaces at the start and end. */
    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(text.strip(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }
}
