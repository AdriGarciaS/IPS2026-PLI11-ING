package com.ips2026.pl11.data.financialreports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

    public record BucketComparison(
            String label,
            BigDecimal income,
            BigDecimal outcome
    ) {}

    public record CashFlowTransaction(
            String date,
            String category,
            String description,
            String type, // "INCOME" or "OUTCOME"
            BigDecimal amount
    ) {}

    public record IncomesVsOutcomesData(
            BigDecimal totalIncome,
            BigDecimal totalOutcome,
            BigDecimal netBalance,
            List<BucketComparison> buckets,
            List<CashFlowTransaction> transactions
    ) {}

    private static final String INCOME_UNION_SQL = """
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

            UNION ALL

            SELECT substr(h.purchase_date, 1, 10) AS tx_date,
                   'SHARE CAMPAIGN' AS category,
                   'Campaign shares (' || c.name || ') - ' || COUNT(s.id) || ' shares' AS description,
                   COUNT(s.id) * h.purchase_price AS amount
            FROM SHARE_HOLDING h
            JOIN SHARE_CAMPAIGN c ON c.id = h.campaign_id
            JOIN SHARE s ON s.holding_id = h.id
            WHERE h.purchase_date BETWEEN ? AND ?
            GROUP BY h.id, h.purchase_date, h.purchase_price, c.name
            """;

    public IncomeReportData getIncomesReport(LocalDate fromDate, LocalDate toDate) {
        String fromStr = fromDate.toString() + " 00:00:00";
        String toStr = toDate.toString() + " 23:59:59";

        String sql = INCOME_UNION_SQL + " ORDER BY tx_date DESC";

        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        List<IncomeTransaction> transactions = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindDateRangeParams(stmt, fromStr, toStr);

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

    public IncomesVsOutcomesData getIncomesVsOutcomesReport(LocalDate fromDate, LocalDate toDate, String period) {
        Map<String, BigDecimal[]> bucketMap = initBuckets(fromDate, toDate, period);
        List<CashFlowTransaction> transactions = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalOutcome = BigDecimal.ZERO;

        String fromStr = fromDate.toString() + " 00:00:00";
        String toStr = toDate.toString() + " 23:59:59";

        String incomeSql = INCOME_UNION_SQL + " ORDER BY tx_date ASC";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(incomeSql)) {

            bindDateRangeParams(stmt, fromStr, toStr);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate txDate = LocalDate.parse(rs.getString("tx_date").substring(0, 10));
                    String category = rs.getString("category");
                    String description = rs.getString("description");
                    BigDecimal amount = BigDecimal.valueOf(rs.getDouble("amount"));

                    totalIncome = totalIncome.add(amount);
                    transactions.add(new CashFlowTransaction(txDate.toString(), category, description, "INCOME", amount));

                    String key = resolveBucketKey(txDate, fromDate, period);
                    if (bucketMap.containsKey(key)) {
                        bucketMap.get(key)[0] = bucketMap.get(key)[0].add(amount);
                    }
                }
            }

            // Calculation of expenses (OUTCOME) based on employee salaries within the range
            BigDecimal annualSalaries = BigDecimal.ZERO;
            try (PreparedStatement salStmt = conn.prepareStatement(
                    "SELECT COALESCE(SUM(gross_annual_salary), 0) AS total_annual FROM employees");
                 ResultSet salRs = salStmt.executeQuery()) {
                if (salRs.next()) {
                    annualSalaries = BigDecimal.valueOf(salRs.getDouble("total_annual"));
                }
            }

            long daysInRange = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
            BigDecimal periodPayroll = annualSalaries
                    .multiply(BigDecimal.valueOf(Math.max(daysInRange, 0)))
                    .divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);

            if (periodPayroll.compareTo(BigDecimal.ZERO) > 0 && !bucketMap.isEmpty()) {
                totalOutcome = totalOutcome.add(periodPayroll);
                BigDecimal perBucket = periodPayroll.divide(
                        BigDecimal.valueOf(bucketMap.size()), 2, RoundingMode.HALF_UP);

                for (BigDecimal[] pair : bucketMap.values()) {
                    pair[1] = pair[1].add(perBucket);
                }

                transactions.add(new CashFlowTransaction(
                        toDate.toString(),
                        "SALARIES",
                        "Staff & players payroll (" + daysInRange + " days)",
                        "OUTCOME",
                        periodPayroll
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading Incomes vs Outcomes report: " + e.getMessage(), e);
        }

        List<BucketComparison> buckets = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> entry : bucketMap.entrySet()) {
            buckets.add(new BucketComparison(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }

        BigDecimal netBalance = totalIncome.subtract(totalOutcome);
        return new IncomesVsOutcomesData(totalIncome, totalOutcome, netBalance, buckets, transactions);
    }

    // Bind from/to parameters for all 6 income sources in INCOME_UNION_SQL
    private static void bindDateRangeParams(PreparedStatement stmt, String fromStr, String toStr) throws SQLException {
        for (int i = 1; i <= 12; i += 2) {
            stmt.setString(i, fromStr);
            stmt.setString(i + 1, toStr);
        }
    }

    // Initialize chart buckets strictly following fromDate -> toDate chronological order
    private Map<String, BigDecimal[]> initBuckets(LocalDate fromDate, LocalDate toDate, String period) {
        Map<String, BigDecimal[]> map = new LinkedHashMap<>();
        Locale en = Locale.ENGLISH;

        if ("YEARLY".equals(period)) {
            YearMonth start = YearMonth.from(fromDate);
            YearMonth end = YearMonth.from(toDate);
            YearMonth current = start;

            while (!current.isAfter(end)) {
                String label = current.getMonth().getDisplayName(TextStyle.SHORT, en);
                map.putIfAbsent(label, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
                current = current.plusMonths(1);
            }
        } else if ("WEEKLY".equals(period)) {
            LocalDate monday = fromDate.with(DayOfWeek.MONDAY);
            for (int i = 0; i < 7; i++) {
                DayOfWeek day = monday.plusDays(i).getDayOfWeek();
                map.put(day.getDisplayName(TextStyle.SHORT, en), new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            }
        } else { // MONTHLY -> weeks of the month
            int maxWeek = Math.max(4, ((toDate.getDayOfMonth() - 1) / 7) + 1);
            for (int w = 1; w <= maxWeek; w++) {
                map.put("Week " + w, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            }
        }
        return map;
    }

    private String resolveBucketKey(LocalDate date, LocalDate fromDate, String period) {
        Locale en = Locale.ENGLISH;
        if ("YEARLY".equals(period)) {
            return date.getMonth().getDisplayName(TextStyle.SHORT, en);
        } else if ("WEEKLY".equals(period)) {
            return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, en);
        } else {
            int week = ((date.getDayOfMonth() - 1) / 7) + 1;
            return "Week " + week;
        }
    }
}
