package com.ips2026.pl11.view.financialreports;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.menu.MenuCardButton;

public class FinancialReportsMenu extends JFrame {

    private static final long serialVersionUID = 1L;

    private JButton btnIncomesGlossary;
    private JButton btnIncomesBarChart;
    private JButton btnIncomesVsOutcomes;
    private final JFrame owner;

    public FinancialReportsMenu(JFrame owner) {
        this.owner = owner;
        initComponents();
    }

    private void initComponents() {
        setTitle("Football Club Management - Financial Reports");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Branding.setInitialSize(this, 460, 520, null);
        setResizable(false);
        Branding.applyTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(Branding.BACKGROUND);

        HeaderPanel headerPanel = new HeaderPanel("FINANCIAL REPORTS", "Select a financial report view");
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        buttonsPanel.setBackground(Branding.BACKGROUND);
        buttonsPanel.setBorder(new EmptyBorder(10, 35, 28, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        btnIncomesGlossary = new MenuCardButton(
                "INCOMES GLOSSARY",
                "Total income, breakdown by category and detailed transactions"
        );
        btnIncomesGlossary.setPreferredSize(new Dimension(320, 82));
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 0, 12, 0);
        buttonsPanel.add(btnIncomesGlossary, gbc);

        btnIncomesBarChart = new MenuCardButton(
                "INCOMES EVOLUTION",
                "Bar chart of incomes over time and detailed income summary"
        );
        btnIncomesBarChart.setPreferredSize(new Dimension(320, 82));
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 12, 0);
        buttonsPanel.add(btnIncomesBarChart, gbc);

        btnIncomesVsOutcomes = new MenuCardButton(
                "INCOMES VS OUTCOMES",
                "Compare income, expenses and net balance over time"
        );
        btnIncomesVsOutcomes.setPreferredSize(new Dimension(320, 82));
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 8, 0);
        buttonsPanel.add(btnIncomesVsOutcomes, gbc);

        mainPanel.add(buttonsPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        initListeners();
    }

    private void initListeners() {
        btnIncomesGlossary.addActionListener(e -> {
            new IncomesGlossaryView(this).setVisible(true);
        });

        btnIncomesBarChart.addActionListener(e -> {
            new IncomesBarChartView(this).setVisible(true);
        });

        btnIncomesVsOutcomes.addActionListener(e -> {
            new IncomesVsOutcomesView(this).setVisible(true);
        });
    }
}