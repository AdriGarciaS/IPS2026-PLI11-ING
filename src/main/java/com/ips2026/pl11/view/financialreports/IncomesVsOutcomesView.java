package com.ips2026.pl11.view.financialreports;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.ips2026.pl11.data.financialreports.FinancialReportDAO;
import com.ips2026.pl11.data.financialreports.FinancialReportDAO.BucketComparison;
import com.ips2026.pl11.data.financialreports.FinancialReportDAO.CashFlowTransaction;
import com.ips2026.pl11.data.financialreports.FinancialReportDAO.IncomesVsOutcomesData;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

public class IncomesVsOutcomesView extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String SERIES_INCOME = "INCOME";
    private static final String SERIES_OUTCOME = "OUTCOME";

    private final JFrame owner;
    private final FinancialReportDAO reportDAO;

    private JComboBox<String> cmbViewPeriod;
    private JTextField txtFrom;
    private JTextField txtTo;
    private JButton btnGenerateReport;

    private JLabel lblTotalIncomeValue;
    private JLabel lblTotalOutcomeValue;
    private JLabel lblNetBalanceValue;

    private DefaultCategoryDataset barDataset;
    private DefaultTableModel tableModel;
    private JTable tblTransactions;

    public IncomesVsOutcomesView(JFrame owner) {
        this.owner = owner;
        this.reportDAO = new FinancialReportDAO();
        initComponents();
        applyPeriodRange(false);
        generateReport();
    }

    private void initComponents() {
        setTitle("Football Club Management - Incomes vs Outcomes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Branding.setInitialSize(this, 1080, 780, owner);
        Branding.applyTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(Branding.BACKGROUND);

        HeaderPanel headerPanel = new HeaderPanel(
                "INCOMES VS OUTCOMES",
                "Compare income, outcome and net balance over time"
        );
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setBackground(Branding.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(0, 16, 16, 16));

        contentPanel.add(createTopSection(), BorderLayout.NORTH);
        contentPanel.add(createBarChartCard(), BorderLayout.CENTER);
        contentPanel.add(createTransactionsTableCard(), BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        initListeners();
    }

    private JPanel createTopSection() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setBackground(Branding.BACKGROUND);

        // Period and date filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBackground(Branding.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Branding.BORDER, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel lblPeriod = new JLabel("VIEW PERIOD:");
        lblPeriod.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        lblPeriod.setForeground(Branding.NAVY);

        cmbViewPeriod = new JComboBox<>(new String[] { "MONTHLY", "WEEKLY", "YEARLY" });
        cmbViewPeriod.setFont(Branding.DEFAULT_FONT);

        LocalDate today = LocalDate.now();
        JLabel lblFrom = new JLabel("FROM:");
        lblFrom.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        lblFrom.setForeground(Branding.NAVY);

        txtFrom = new JTextField(today.withDayOfMonth(1).format(DATE_FORMATTER), 10);
        txtFrom.setFont(Branding.DEFAULT_FONT);

        JLabel lblTo = new JLabel("TO:");
        lblTo.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        lblTo.setForeground(Branding.NAVY);

        txtTo = new JTextField(today.withDayOfMonth(today.lengthOfMonth()).format(DATE_FORMATTER), 10);
        txtTo.setFont(Branding.DEFAULT_FONT);

        btnGenerateReport = new JButton("GENERATE REPORT");
        btnGenerateReport.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        btnGenerateReport.setBackground(Branding.NAVY);
        btnGenerateReport.setForeground(Branding.WHITE);
        btnGenerateReport.setFocusPainted(false);

        filterPanel.add(lblPeriod);
        filterPanel.add(cmbViewPeriod);
        filterPanel.add(lblFrom);
        filterPanel.add(txtFrom);
        filterPanel.add(lblTo);
        filterPanel.add(txtTo);
        filterPanel.add(btnGenerateReport);

        // Summary KPI cards: TOTAL INCOME | TOTAL OUTCOME | NET BALANCE
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        kpiPanel.setBackground(Branding.BACKGROUND);

        lblTotalIncomeValue = new JLabel(MoneyFormat.format(BigDecimal.ZERO));
        lblTotalOutcomeValue = new JLabel(MoneyFormat.format(BigDecimal.ZERO));
        lblNetBalanceValue = new JLabel(MoneyFormat.format(BigDecimal.ZERO));

        kpiPanel.add(createKpiCard("TOTAL INCOME", lblTotalIncomeValue, Branding.SUCCESS));
        kpiPanel.add(createKpiCard("TOTAL OUTCOME", lblTotalOutcomeValue, Branding.ERROR));
        kpiPanel.add(createKpiCard("NET BALANCE", lblNetBalanceValue, Branding.NAVY));

        topPanel.add(filterPanel, BorderLayout.NORTH);
        topPanel.add(kpiPanel, BorderLayout.CENTER);
        return topPanel;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color valueColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4));
        card.setBackground(Branding.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Branding.BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
        lblTitle.setForeground(Branding.NAVY);

        valueLabel.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 24));
        valueLabel.setForeground(valueColor);

        card.add(lblTitle);
        card.add(valueLabel);
        return card;
    }

    private JPanel createBarChartCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Branding.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Branding.BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        barDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart(
                "INCOME VS OUTCOME",
                null,
                "Amount (€)",
                barDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        barChart.setBackgroundPaint(Branding.WHITE);

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(Branding.WHITE);
        plot.setRangeGridlinePaint(Branding.BORDER);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Branding.NAVY);
        renderer.setSeriesPaint(1, Branding.IPSWICH_RED);
        renderer.setItemMargin(0.08);

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setBackground(Branding.WHITE);
        chartPanel.setMouseWheelEnabled(true);
        card.add(chartPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTransactionsTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Branding.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Branding.BORDER, 1),
                new EmptyBorder(10, 14, 12, 14)
        ));
        card.setPreferredSize(new Dimension(0, 210));

        JLabel lblTitle = new JLabel("DETAIL TRANSACTION SUMMARY");
        lblTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
        lblTitle.setForeground(Branding.NAVY);
        card.add(lblTitle, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[] { "DATE", "CATEGORY", "DESCRIPTION", "TYPE", "AMOUNT" },
                0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 4 ? BigDecimal.class : String.class;
            }
        };

        tblTransactions = new JTable(tableModel);
        tblTransactions.setFont(Branding.DEFAULT_FONT);
        tblTransactions.setRowHeight(24);
        tblTransactions.getTableHeader().setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        tblTransactions.getColumnModel().getColumn(4).setCellRenderer(MoneyFormat.tableRenderer());

        JScrollPane scrollPane = new JScrollPane(tblTransactions);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private void initListeners() {
        cmbViewPeriod.addActionListener(e -> {
            applyPeriodRange(true);
            generateReport();
        });

        btnGenerateReport.addActionListener(e -> generateReport());
    }

    private void applyPeriodRange(boolean showWarningIfInvalidDate) {
        LocalDate baseDate = parseDateOrDefault(txtFrom.getText().trim(), LocalDate.now(), showWarningIfInvalidDate);
        String period = (String) cmbViewPeriod.getSelectedItem();

        LocalDate from;
        LocalDate to;

        if ("WEEKLY".equals(period)) {
            from = baseDate.with(DayOfWeek.MONDAY);
            to = baseDate.with(DayOfWeek.SUNDAY);
        } else if ("YEARLY".equals(period)) {
            // Season year runs from June 1st to May 31st of the following year
            int seasonStartYear = (baseDate.getMonthValue() >= 6)
                    ? baseDate.getYear()
                    : baseDate.getYear() - 1;
            from = LocalDate.of(seasonStartYear, 6, 1);
            to = LocalDate.of(seasonStartYear + 1, 5, 31);
        } else { // MONTHLY
            from = baseDate.withDayOfMonth(1);
            to = baseDate.withDayOfMonth(baseDate.lengthOfMonth());
        }

        txtFrom.setText(from.format(DATE_FORMATTER));
        txtTo.setText(to.format(DATE_FORMATTER));
    }

    private LocalDate parseDateOrDefault(String text, LocalDate defaultDate, boolean showWarning) {
        if (text.isEmpty()) {
            return defaultDate;
        }
        try {
            return LocalDate.parse(text, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            if (showWarning) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid date format. Use dd/MM/yyyy (e.g. 01/11/2026).",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            return defaultDate;
        }
    }

    private void generateReport() {
        LocalDate fromDate;
        LocalDate toDate;

        try {
            fromDate = LocalDate.parse(txtFrom.getText().trim(), DATE_FORMATTER);
            toDate = LocalDate.parse(txtTo.getText().trim(), DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid dates in dd/MM/yyyy format.",
                    "Date Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (toDate.isBefore(fromDate)) {
            JOptionPane.showMessageDialog(
                    this,
                    "'TO' date cannot be earlier than 'FROM' date.",
                    "Range Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            String period = (String) cmbViewPeriod.getSelectedItem();
            IncomesVsOutcomesData data = reportDAO.getIncomesVsOutcomesReport(fromDate, toDate, period);

            lblTotalIncomeValue.setText(MoneyFormat.format(data.totalIncome()));
            lblTotalOutcomeValue.setText(MoneyFormat.format(data.totalOutcome()));
            lblNetBalanceValue.setText(MoneyFormat.format(data.netBalance()));
            lblNetBalanceValue.setForeground(
                    data.netBalance().compareTo(BigDecimal.ZERO) >= 0 ? Branding.NAVY : Branding.ERROR
            );

            // Populate chart buckets in the exact chronological order returned by the DAO
            barDataset.clear();
            for (BucketComparison bucket : data.buckets()) {
                barDataset.addValue(bucket.income().doubleValue(), SERIES_INCOME, bucket.label());
                barDataset.addValue(bucket.outcome().doubleValue(), SERIES_OUTCOME, bucket.label());
            }

            tableModel.setRowCount(0);
            for (CashFlowTransaction tx : data.transactions()) {
                tableModel.addRow(new Object[] {
                        tx.date(),
                        tx.category(),
                        tx.description(),
                        tx.type(),
                        tx.amount()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error generating Incomes vs Outcomes report: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}