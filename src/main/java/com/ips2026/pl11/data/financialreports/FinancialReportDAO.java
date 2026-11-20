package com.ips2026.pl11.data.financialreports;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ips2026.pl11.data.ConexionBD;

public class FinancialReportDAO {

    public record IncomeTransaction(
            String date,
            String category,
            String description,
            BigDecimal amount
    ) {}

    public record IncomeReportData(
            BigDecimal totalIncome,
            Map<String, BigDecimal> breakdownByCategory,
            List<IncomeTransaction> transactions
    ) {}

    public IncomeReportData getIncomesReport(LocalDate fromDate, LocalDate toDate) {
        String fromStr = fromDate.toString() + " 00:00:00";
        String toStr = toDate.toString() + " 23:59:59";

        String sql = """
                SELECT substr(sale_date, 1, 10) AS tx_date,
                       'MERCHANDISING' AS category,
                       'Merchandising sale #' || sale_number AS description,
                       total_price AS amount
                FROM MERCHANDISING_SALE
                WHERE sale_date BETWEEN ? AND ?

                UNION ALL

                SELECT substr(created_at, 1, 10) AS tx_date,
                       'MATCH TICKETS' AS category,
                       'Match ticket (purchase #' || purchase_number || ')' AS description,
                       price AS amount
                FROM MATCH_TICKET
                WHERE created_at BETWEEN ? AND ?

                UNION ALL

                SELECT substr(created_at, 1, 10) AS tx_date,
                       'SEASON TICKETS' AS category,
                       'Season ticket (' || season || ') - ' || holder_name AS description,
                       price AS amount
                FROM SEASON_TICKET
                WHERE created_at BETWEEN ? AND ?

                UNION ALL

                SELECT start_date AS tx_date,
                       'SEASON PASSES' AS category,
                       'Season pass - ' || grandstand || section || ' R' || row_num || 'S' || seat_num AS description,
                       price AS amount
                FROM SEASON_PASS
                WHERE (start_date || ' 00:00:00') BETWEEN ? AND ?

                UNION ALL

                SELECT substr(created_at, 1, 10) AS tx_date,
                       'FACILITY RESERVATIONS' AS category,
                       'Facility reservation - ' || holder_name AS description,
                       total_price AS amount
                FROM FACILITY_RESERVATION
                WHERE created_at BETWEEN ? AND ?

                ORDER BY tx_date DESC
                """;

        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        List<IncomeTransaction> transactions = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 10; i += 2) {
                stmt.setString(i, fromStr);
                stmt.setString(i + 1, toStr);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("tx_date");
                    String category = rs.getString("category");
                    String description = rs.getString("description");
                    BigDecimal amount = BigDecimal.valueOf(rs.getDouble("amount"));

                    transactions.add(new IncomeTransaction(date, category, description, amount));
                    total = total.add(amount);
                    breakdown.merge(category, amount, BigDecimal::add);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading incomes report: " + e.getMessage(), e);
        }

        return new IncomeReportData(total, breakdown, transactions);
    }
}
