package com.ips2026.pl11.data.ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.ticket.HomeMatch;

public class MatchDAO {

    /**
     * Retrieves only matches played as home team (is_home = 1)
     */
    public List<HomeMatch> getHomeMatches() throws SQLException {
        List<HomeMatch> matches = new ArrayList<>();
        String sql = "SELECT m.id, t.name AS team_name, m.rival, m.match_date, m.start_time " +
                     "FROM matches m " +
                     "JOIN TEAM t ON m.team_id = t.id " +
                     "WHERE m.is_home = 1 " +
                     "ORDER BY m.match_date ASC, m.start_time ASC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                matches.add(new HomeMatch(
                    rs.getInt("id"),
                    rs.getString("team_name"),
                    rs.getString("rival"),
                    rs.getString("match_date"),
                    rs.getString("start_time")
                ));
            }
        }
        return matches;
    }
}
