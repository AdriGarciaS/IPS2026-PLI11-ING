package com.ips2026.pl11.data.campaign;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * SQL queries to the SHARE_CAMPAIGN table.
 */
public class CampaignDAO {

    private static final String COLUMNS = "id, name, nominal_price, total_shares, status";

    /** The campaign that is not finished yet (there is at most one), if any. */
    public Optional<Campaign> findOpen() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM SHARE_CAMPAIGN WHERE status <> 'FINISHED'";

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            return result.next() ? Optional.of(read(result)) : Optional.empty();
        }
    }

    /** Whether a campaign with this name already exists (ignoring upper/lower case). */
    public boolean existsName(String name) throws SQLException {
        String sql = "SELECT 1 FROM SHARE_CAMPAIGN WHERE name = ? COLLATE NOCASE";

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /** Saves a new campaign in the {@link CampaignStatus#INACTIVE} state and returns it with its id. */
    public Campaign insert(String name, BigDecimal nominalPrice, int totalShares) throws SQLException {
        String sql = "INSERT INTO SHARE_CAMPAIGN (name, nominal_price, total_shares, status) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            statement.setBigDecimal(2, nominalPrice);
            statement.setInt(3, totalShares);
            statement.setString(4, CampaignStatus.INACTIVE.name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return new Campaign(keys.getInt(1), name, nominalPrice, totalShares, CampaignStatus.INACTIVE);
            }
        }
    }

    private static Campaign read(ResultSet result) throws SQLException {
        return new Campaign(
                result.getInt("id"),
                result.getString("name"),
                BigDecimal.valueOf(result.getDouble("nominal_price")),
                result.getInt("total_shares"),
                CampaignStatus.valueOf(result.getString("status"))
        );
    }
}
