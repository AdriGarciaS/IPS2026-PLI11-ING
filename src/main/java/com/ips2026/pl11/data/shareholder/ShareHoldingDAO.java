package com.ips2026.pl11.data.shareholder;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.shareholder.ShareHolding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL queries to the SHARE_HOLDING table, with the name of each campaign and
 * the IDs of its shares (table SHARE).
 */
public class ShareHoldingDAO {

    /** Holdings of a shareholder (one per campaign), ordered by purchase date, with their share IDs. */
    public List<ShareHolding> findByShareholder(int shareholderId) throws SQLException {
        String sql = "SELECT h.id, c.name AS campaign_name, h.purchase_date, h.purchase_price, s.id AS share_id "
                + "FROM SHARE_HOLDING h "
                + "JOIN SHARE_CAMPAIGN c ON c.id = h.campaign_id "
                + "JOIN SHARE s ON s.holding_id = h.id "
                + "WHERE h.shareholder_id = ? ORDER BY h.purchase_date, h.id, s.id";

        // Rows come one per share: they are grouped by holding, keeping the order.
        Map<Integer, Row> rows = new LinkedHashMap<>();
        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shareholderId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int holdingId = result.getInt("id");
                    Row row = rows.get(holdingId);
                    if (row == null) {
                        row = new Row(result.getString("campaign_name"),
                                LocalDate.parse(result.getString("purchase_date").substring(0, 10)), // date part of the moment
                                BigDecimal.valueOf(result.getDouble("purchase_price")).setScale(2, RoundingMode.HALF_UP));
                        rows.put(holdingId, row);
                    }
                    row.shareIds.add(result.getInt("share_id"));
                }
            }
        }

        List<ShareHolding> holdings = new ArrayList<>();
        for (Map.Entry<Integer, Row> entry : rows.entrySet()) {
            Row row = entry.getValue();
            holdings.add(new ShareHolding(entry.getKey(), row.campaignName, row.purchaseDate, row.purchasePrice,
                    row.shareIds));
        }
        return holdings;
    }

    /** Data of a holding while its shares are being read. */
    private static final class Row {
        private final String campaignName;
        private final LocalDate purchaseDate;
        private final BigDecimal purchasePrice;
        private final List<Integer> shareIds = new ArrayList<>();

        private Row(String campaignName, LocalDate purchaseDate, BigDecimal purchasePrice) {
            this.campaignName = campaignName;
            this.purchaseDate = purchaseDate;
            this.purchasePrice = purchasePrice;
        }
    }
}
