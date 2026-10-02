package com.ips2026.pl11.view.common;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Formats prices in euros (for example "1.234,50 €") the same way in every
 * window.
 */
public final class MoneyFormat {

    private static final Locale LOCALE_EURO = Locale.of("es", "ES");

    private MoneyFormat() {
        // Utility class: not instantiated.
    }

    public static String format(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(LOCALE_EURO).format(amount);
    }

    /** Table cell renderer that shows a BigDecimal as a price, right aligned. */
    public static DefaultTableCellRenderer tableRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setText(value instanceof BigDecimal amount ? format(amount) : "");
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        return renderer;
    }
}
