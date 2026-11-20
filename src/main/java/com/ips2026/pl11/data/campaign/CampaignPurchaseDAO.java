package com.ips2026.pl11.data.campaign;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.campaign.Campaign;
import com.ips2026.pl11.model.campaign.CampaignPurchase;
import com.ips2026.pl11.model.campaign.CampaignPurchaseLimit;
import com.ips2026.pl11.model.campaign.CampaignStatus;
import com.ips2026.pl11.model.campaign.ShareholderRegistration;
import com.ips2026.pl11.model.shareholder.Shareholder;

/**
 * US 35947: SQL queries to buy shares of a campaign (tables SHARE_CAMPAIGN,
 * SHARE_HOLDING, SHARE and SHARE_TRANSFER). US 35828: an external user
 * registers as a shareholder (SHAREHOLDER) and buys shares at the same time.
 */
public class CampaignPurchaseDAO {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SQL_CAMPAIGN = "SELECT status, total_shares FROM SHARE_CAMPAIGN WHERE id = ?";

    private static final String SQL_SOLD = "SELECT COUNT(*) FROM SHARE s "
            + "JOIN SHARE_HOLDING h ON h.id = s.holding_id WHERE h.campaign_id = ?";

    // Shares the shareholder owns now: all of them, and the ones of other campaigns.
    private static final String SQL_OWNED = "SELECT COUNT(*), "
            + "COALESCE(SUM(CASE WHEN h.campaign_id <> ? THEN 1 ELSE 0 END), 0) FROM SHARE s "
            + "JOIN SHARE_HOLDING h ON h.id = s.holding_id WHERE h.shareholder_id = ?";

    // Holdings bought in the campaign itself (a holding that received shares in the market is not one).
    private static final String CAMPAIGN_HOLDINGS = "SELECT h.id FROM SHARE_HOLDING h "
            + "WHERE h.shareholder_id = ? AND h.campaign_id = ? "
            + "AND NOT EXISTS (SELECT 1 FROM SHARE_TRANSFER t WHERE t.to_holding_id = h.id)";

    // Shares bought in the campaign: the ones still in those holdings and the ones sold later in the market.
    private static final String SQL_BOUGHT = "SELECT "
            + "(SELECT COUNT(*) FROM SHARE s WHERE s.holding_id IN (" + CAMPAIGN_HOLDINGS + ")) "
            + "+ (SELECT COUNT(*) FROM SHARE_TRANSFER t WHERE t.from_holding_id IN (" + CAMPAIGN_HOLDINGS + "))";

    private static final String SQL_NEXT_SHARE_ID = "SELECT COALESCE(MAX(id), 0) + 1 FROM SHARE";

    private static final String SQL_INSERT_HOLDING = "INSERT INTO SHARE_HOLDING "
            + "(shareholder_id, campaign_id, purchase_date, purchase_price) VALUES (?, ?, ?, ?)";

    private static final String SQL_INSERT_SHARE = "INSERT INTO SHARE (id, holding_id) VALUES (?, ?)";

    private static final String SQL_INSERT_SHAREHOLDER = "INSERT INTO SHAREHOLDER "
            + "(first_name, last_name, national_id, email, username, password) VALUES (?, ?, ?, ?, ?, ?)";

    /** How many shares of the campaign the shareholder can buy now. */
    public CampaignPurchaseLimit findLimit(int campaignId, int shareholderId) throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            return readLimit(connection, campaignId, shareholderId);
        }
    }

    /** US 35828: how many shares of the campaign an external user (not a shareholder yet) can buy now. */
    public CampaignPurchaseLimit findExternalLimit(int campaignId) throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            return readExternalLimit(connection, campaignId);
        }
    }

    /**
     * Buys {@code quantity} shares of the campaign at its nominal price: they
     * get new consecutive IDs (after the largest ID that exists) and go to a
     * new holding of the shareholder, so they are in their wallet.
     *
     * <p>Everything runs in a single transaction, and the limit is checked
     * again inside it: if the campaign changed phase or the shares were sold
     * meanwhile, nothing is stored.</p>
     *
     * @throws IllegalStateException if the shareholder can no longer buy that many shares
     */
    public CampaignPurchase buy(Campaign campaign, Shareholder shareholder, int quantity, LocalDateTime moment)
            throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            connection.setAutoCommit(false);
            try {
                checkQuantity(readLimit(connection, campaign.getId(), shareholder.getId()), quantity);
                CampaignPurchase purchase = insertPurchase(connection, campaign, shareholder.getId(),
                        shareholder.getFullName(), quantity, moment);
                connection.commit();
                return purchase;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /**
     * US 35828: registers an external user as a new shareholder and buys the
     * shares for them (see {@link #buy}), all in a single transaction: if the
     * campaign is no longer in phase 3, there are not enough shares, or the
     * national ID, email or username were registered meanwhile, nothing is
     * stored.
     *
     * @param passwordHash the password already hashed (it is never stored as typed)
     * @throws IllegalStateException if the user can no longer buy that many shares
     */
    public CampaignPurchase registerAndBuy(Campaign campaign, ShareholderRegistration registration,
            String passwordHash, int quantity, LocalDateTime moment) throws SQLException {
        try (Connection connection = ConexionBD.obtenerConexion()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insertShareholder = connection.prepareStatement(SQL_INSERT_SHAREHOLDER,
                    Statement.RETURN_GENERATED_KEYS)) {
                checkQuantity(readExternalLimit(connection, campaign.getId()), quantity);

                insertShareholder.setString(1, registration.getFirstName());
                insertShareholder.setString(2, registration.getLastName());
                insertShareholder.setString(3, registration.getNationalId());
                insertShareholder.setString(4, registration.getEmail());
                insertShareholder.setString(5, registration.getUsername());
                insertShareholder.setString(6, passwordHash);
                insertShareholder.executeUpdate();
                int shareholderId;
                try (ResultSet keys = insertShareholder.getGeneratedKeys()) {
                    keys.next();
                    shareholderId = keys.getInt(1);
                }

                CampaignPurchase purchase = insertPurchase(connection, campaign, shareholderId,
                        registration.getFullName(), quantity, moment);
                connection.commit();
                return purchase;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /** @throws IllegalStateException if the quantity is not between 1 and the maximum of the limit */
    private static void checkQuantity(CampaignPurchaseLimit limit, int quantity) {
        if (quantity < 1 || quantity > limit.getMax()) {
            throw new IllegalStateException(limit.getMax() == 0 ? limit.getReasonForNone()
                    : "You can no longer buy " + quantity + " shares: the maximum now is " + limit.getMax() + ".");
        }
    }

    /** A new holding of the shareholder with {@code quantity} new shares with consecutive IDs. */
    private static CampaignPurchase insertPurchase(Connection connection, Campaign campaign, int shareholderId,
            String shareholderName, int quantity, LocalDateTime moment) throws SQLException {
        try (PreparedStatement insertHolding = connection.prepareStatement(SQL_INSERT_HOLDING,
                     Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertShare = connection.prepareStatement(SQL_INSERT_SHARE);
             Statement nextId = connection.createStatement()) {

            insertHolding.setInt(1, shareholderId);
            insertHolding.setInt(2, campaign.getId());
            insertHolding.setString(3, moment.format(DATE_TIME_FORMAT));
            insertHolding.setBigDecimal(4, campaign.getNominalPrice());
            insertHolding.executeUpdate();
            int holdingId;
            try (ResultSet keys = insertHolding.getGeneratedKeys()) {
                keys.next();
                holdingId = keys.getInt(1);
            }

            int firstId;
            try (ResultSet result = nextId.executeQuery(SQL_NEXT_SHARE_ID)) {
                result.next();
                firstId = result.getInt(1);
            }
            for (int id = firstId; id < firstId + quantity; id++) {
                insertShare.setInt(1, id);
                insertShare.setInt(2, holdingId);
                insertShare.addBatch();
            }
            insertShare.executeBatch();

            return new CampaignPurchase(shareholderName, campaign.getName(), quantity, firstId,
                    firstId + quantity - 1, campaign.getNominalPrice());
        }
    }

    private static CampaignPurchaseLimit readExternalLimit(Connection connection, int campaignId)
            throws SQLException {
        CampaignPurchaseLimit limit = readLimit(connection, campaignId, 0); // no shareholder has id 0
        return CampaignPurchaseLimit.forExternalUser(limit.getStatus(), limit.getRemainingShares());
    }

    private static CampaignPurchaseLimit readLimit(Connection connection, int campaignId, int shareholderId)
            throws SQLException {
        CampaignStatus status;
        int totalShares;
        try (PreparedStatement statement = connection.prepareStatement(SQL_CAMPAIGN)) {
            statement.setInt(1, campaignId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException("The campaign no longer exists.");
                }
                status = CampaignStatus.valueOf(result.getString("status"));
                totalShares = result.getInt("total_shares");
            }
        }

        int sold;
        try (PreparedStatement statement = connection.prepareStatement(SQL_SOLD)) {
            statement.setInt(1, campaignId);
            sold = queryInt(statement);
        }

        int owned;
        int ownedOutside;
        try (PreparedStatement statement = connection.prepareStatement(SQL_OWNED)) {
            statement.setInt(1, campaignId);
            statement.setInt(2, shareholderId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                owned = result.getInt(1);
                ownedOutside = result.getInt(2);
            }
        }

        int bought;
        try (PreparedStatement statement = connection.prepareStatement(SQL_BOUGHT)) {
            statement.setInt(1, shareholderId);
            statement.setInt(2, campaignId);
            statement.setInt(3, shareholderId);
            statement.setInt(4, campaignId);
            bought = queryInt(statement);
        }

        return new CampaignPurchaseLimit(status, totalShares - sold, owned, ownedOutside, bought);
    }

    private static int queryInt(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }
}
