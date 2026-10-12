package com.ips2026.pl11.data.interview;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.interview.AvailableSlotRecord;

public class InterviewBookingDAO {

	/**
     * Retrieves all interview slots that currently have 'AVAILABLE' status.
     * Joins with employees to obtain the player's full name.
     */
    public List<AvailableSlotRecord> getAvailableSlots() throws SQLException {
        List<AvailableSlotRecord> list = new ArrayList<>();
        String sql = "SELECT s.id, s.player_id, " +
                     "(e.first_name || ' ' || e.last_name) AS player_name, " +
                     "s.slot_date, s.start_time, s.end_time " +
                     "FROM interview_slots s " +
                     "INNER JOIN employees e ON s.player_id = e.id " +
                     "WHERE s.status = 'AVAILABLE' " +
                     "ORDER BY s.slot_date ASC, s.start_time ASC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new AvailableSlotRecord(
                        rs.getInt("id"),
                        rs.getInt("player_id"),
                        rs.getString("player_name"),
                        rs.getString("slot_date"),
                        rs.getString("start_time"),
                        rs.getString("end_time")
                ));
            }
        }
        return list;
    }

    /**
     * Assigns the selected slot to a media outlet and deletes all other slots
     * created for that same player on the same date.
     * Executed within a single database transaction.
     */
    public void bookSlotAndRemoveRemaining(int slotId, int playerId, String slotDate, String mediaName, String mediaContact) throws SQLException {
        String updateSlotSql = "UPDATE interview_slots " +
                               "SET status = 'ASSIGNED', media_name = ?, media_contact = ? " +
                               "WHERE id = ? AND status = 'AVAILABLE'";

        String deleteOtherSlotsSql = "DELETE FROM interview_slots " +
                                     "WHERE player_id = ? " +
                                     "AND slot_date = ? " +
                                     "AND id != ?";

        Connection conn = null;
        try {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Assign the chosen slot to the media outlet
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSlotSql)) {
                psUpdate.setString(1, mediaName);
                psUpdate.setString(2, mediaContact != null ? mediaContact : "");
                psUpdate.setInt(3, slotId);

                int updatedRows = psUpdate.executeUpdate();
                if (updatedRows == 0) {
                    throw new SQLException("The selected slot is no longer available.");
                }
            }

            // 2. Remove all remaining slots for that player on that date
            try (PreparedStatement psDelete = conn.prepareStatement(deleteOtherSlotsSql)) {
                psDelete.setInt(1, playerId);
                psDelete.setString(2, slotDate);
                psDelete.setInt(3, slotId);
                psDelete.executeUpdate();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException ex) {
            if (conn != null) {
                conn.rollback(); // Roll back in case of failure
            }
            throw ex;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }
}
