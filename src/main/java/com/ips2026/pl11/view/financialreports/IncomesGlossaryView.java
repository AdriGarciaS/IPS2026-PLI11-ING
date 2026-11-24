package com.ips2026.pl11.view.financialreports;

import com.ips2026.pl11.controller.log.SystemLog;
import com.ips2026.pl11.model.log.LogAction;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;

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
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import com.ips2026.pl11.data.financialreports.FinancialReportDAO;
import com.ips2026.pl11.data.financialreports.FinancialReportDAO.IncomeReportData;
import com.ips2026.pl11.data.financialreports.FinancialReportDAO.IncomeTransaction;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

public class IncomesGlossaryView extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale LOCALE_ES = new Locale("es", "ES");

    private final JFrame owner;
    private final FinancialReportDAO reportDAO;

    private JComboBox<String> cmbViewPeriod;
    private JTextField txtFrom;
    private JTextField txtTo;
    private JButton btnGenerateReport;

    private JLabel lblTotalIncomeValue;
    private DefaultPieDataset<String> pieDataset;
    private DefaultTableModel tableModel;
    private JTable tblTransactions;

    public IncomesGlossaryView(JFrame owner) {
        this.owner = owner;
        this.reportDAO = new FinancialReportDAO();
        initComponents();
        applyPeriodRange(false);
        generateReport();
    }

    private void initComponents() {
        setTitle("Football Club Management - Incomes Glossary");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Branding.setInitialSize(this, 1040, 740, owner);
        Branding.applyTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(Branding.BACKGROUND);

        HeaderPanel headerPanel = new HeaderPanel(
                "INCOMES GLOSSARY",
                "Total income, breakdown by income category and detailed transactions"
        );
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setBackground(Branding.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(0, 16, 16, 16));

        contentPanel.add(createFilterBar(), BorderLayout.NORTH);
        contentPanel.add(createCenterSection(), BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        initListeners();
    }

    private JPanel createFilterBar() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBackground(Branding.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Branding.BORDER, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel lblPeriod = new JLabel("VIEW PERIOD:");
        lblPeriod.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        lblPeriod.setForeground(Branding.NAVY);

        cmbViewPeriod = new JComboBox<>(new String[] { "MONTHLY", "WEEKLY", "SEASONAL" });
        cmbViewPeriod.setFont(Branding.DEFAULT_FONT);

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        JLabel lblFrom = new JLabel("FROM:");
        lblFrom.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        lblFrom.setForeground(Branding.NAVY);

        txtFrom = new JTextField(monthStart.format(DATE_FORMATTER), 10);
        txtFrom.setFont(Branding.DEFAULT_FONT);

        JLabel lblTo = new JLabel("TO:");
        lblTo.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        lblTo.setForeground(Branding.NAVY);

        txtTo = new JTextField(monthEnd.format(DATE_FORMATTER), 10);
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

        return filterPanel;
    }

    private JPanel createCenterSection() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setBackground(Branding.BACKGROUND);

        JPanel topSplitPanel = new JPanel(new GridBagLayout());
        topSplitPanel.setBackground(Branding.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        JPanel totalIncomeCard = createTotalIncomeCard();
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 8);
        topSplitPanel.add(totalIncomeCard, gbc);

        JPanel pieChartCard = createPieChartCard();
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 8, 0, 0);
        topSplitPanel.add(pieChartCard, gbc);

        topSplitPanel.setPreferredSize(new Dimension(0, 320));
        centerPanel.add(topSplitPanel, BorderLayout.CENTER);

        centerPanel.add(createTransactionsTableCard(), BorderLayout.SOUTH);

        return centerPanel;
    }

    private JPanel createTotalIncomeCard() {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 6));
        card.setBackground(Branding.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Branding.BORDER, 1),
                new EmptyBorder(24, 20, 24, 20)
        ));

        JLabel lblTitle = new JLabel("TOTAL INCOME");
        lblTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 16));
        lblTitle.setForeground(Branding.NAVY);

        lblTotalIncomeValue = new JLabel(MoneyFormat.format(BigDecimal.ZERO));
        lblTotalIncomeValue.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 28));
        lblTotalIncomeValue.setForeground(Branding.SUCCESS);

        card.add(lblTitle);
        card.add(lblTotalIncomeValue);
        return card;
    }

    private JPanel createPieChartCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Branding.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Branding.BORDER, 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblTitle = new JLabel("INCOME BREAKDOWN");
        lblTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 15));
        lblTitle.setForeground(Branding.NAVY);
        card.add(lblTitle, BorderLayout.NORTH);

        pieDataset = new DefaultPieDataset<>();
        JFreeChart pieChart = ChartFactory.createPieChart(
                null,
                pieDataset,
                true,
                true,
                false
        );
        pieChart.setBackgroundPaint(Branding.WHITE);

        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) pieChart.getPlot();
        plot.setBackgroundPaint(Branding.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})",
                NumberFormat.getCurrencyInstance(LOCALE_ES),
                NumberFormat.getPercentInstance(LOCALE_ES)
        ));

        ChartPanel chartPanel = new ChartPanel(pieChart);
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
        card.setPreferredSize(new Dimension(0, 220));

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
        } else if ("SEASONAL".equals(period)) {
            int seasonStartYear = baseDate.getMonthValue() >= 7 ? baseDate.getYear() : baseDate.getYear() - 1;
            from = LocalDate.of(seasonStartYear, 7, 1);
            to = LocalDate.of(seasonStartYear + 1, 6, 30);
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
            IncomeReportData data = reportDAO.getIncomesReport(fromDate, toDate);
            SystemLog.record(LogAction.QUERY, "Viewed incomes report " + fromDate + " - " + toDate);

            lblTotalIncomeValue.setText(MoneyFormat.format(data.totalIncome()));

            pieDataset.clear();
            for (Map.Entry<String, BigDecimal> entry : data.breakdownByCategory().entrySet()) {
                pieDataset.setValue(entry.getKey(), entry.getValue().doubleValue());
            }

            tableModel.setRowCount(0);
            for (IncomeTransaction tx : data.transactions()) {
                tableModel.addRow(new Object[] {
                        tx.date(),
                        tx.category(),
                        tx.description(),
                        "INCOME",
                        tx.amount()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error generating incomes report: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}