package com.ips2026.pl11.controller.shareholder;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.ips2026.pl11.data.shareholder.ShareholderDAO;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * Finds the shareholder who logs into the application by their username.
 * (The old Shareholders Portal, where the shareholder was searched by
 * national ID or name, no longer exists: the shareholder options are in the
 * shareholder main menu.)
 */
public class ShareholderPortalController {

    private final ShareholderDAO shareholderDAO;

    private List<Shareholder> shareholders = new ArrayList<>();

    public ShareholderPortalController(ShareholderDAO shareholderDAO) {
        this.shareholderDAO = shareholderDAO;
    }

    public void load() throws SQLException {
        shareholders = shareholderDAO.findAll();
    }

    /** The shareholder with this username (ignoring upper/lower case, accents and outer spaces), if any. */
    public Optional<Shareholder> findShareholderByUsername(String text) {
        String search = normalize(text);
        if (search.isEmpty()) {
            return Optional.empty(); // a shareholder without username can not log in
        }
        return shareholders.stream().filter(s -> normalize(s.getUsername()).equals(search)).findFirst();
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
