package com.ips2026.pl11.data.campaign;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * SQL queries to the SHARE_CAMPAIGN table (and SHARE_CAMPAIGN_PHASE_CHANGE).
 */
public class CampaignDAO {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    /**
     * Number of shares of the campaign already sold: every share bought in
     * it, also the ones sold later in the share market (they keep their
     * campaign).
     */
    public int countSoldShares(int campaignId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM SHARE s JOIN SHARE_HOLDING h ON h.id = s.holding_id WHERE h.campaign_id = ?";

        try (Connection connection = ConexionBD.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, campaignId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    /**
     * US 35778: moves the campaign from phase {@code from} to phase {@code to}
     * and records the change, both in a single transaction.
     *
     * @throws IllegalStateException if the campaign is no longer in phase {@code from}
     *                               (someone changed it meanwhile); nothing is stored
     */
    public void changePhase(int campaignId, CampaignStatus from, CampaignStatus to, LocalDateTime moment)
            throws SQLException {
        String update = "UPDATE SHARE_CAMPAIGN SET status = ? WHERE id = ? AND status = ?";
        String record = "INSERT INTO SHARE_CAMPAIGN_PHASE_CHANGE (campaign_id, from_status, to_status, changed_at) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = ConexionBD.obtenerConexion()) {
            connection.setAutoCommit(false);
            try (PreparedStatement updateStatus = connection.prepareStatement(update);
                 PreparedStatement insertChange = connection.prepareStatement(record)) {

                updateStatus.setString(1, to.name());
                updateStatus.setInt(2, campaignId);
                updateStatus.setString(3, from.name());
                if (updateStatus.executeUpdate() != 1) {
                    throw new IllegalStateException("The campaign is no longer in " + from.getLabel()
                            + ". Close the window and open it again.");
                }

                insertChange.setInt(1, campaignId);
                insertChange.setString(2, from.name());
                insertChange.setString(3, to.name());
                insertChange.setString(4, moment.format(DATE_TIME_FORMAT));
                insertChange.executeUpdate();

                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
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
