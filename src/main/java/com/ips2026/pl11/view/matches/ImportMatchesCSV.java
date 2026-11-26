package com.ips2026.pl11.view.matches;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.ips2026.pl11.data.ticket.MatchDAO;
import com.ips2026.pl11.model.matches.MatchCSV;
import com.ips2026.pl11.service.matches.ScheduleImportMatch;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

public class ImportMatchesCSV extends JDialog {

    private static final long serialVersionUID = 1L;

    private HeaderPanel pnHeader;
    private JTextField txtFilePath;
    private DefaultTableModel previewTableModel;
    private JTable tblPreview;
    private JTextArea txtSummaryArea;
    private File selectedFile;
    private ScheduleImportMatch importService = new ScheduleImportMatch();
    private MatchDAO matchDAO = new MatchDAO();

    public ImportMatchesCSV(JFrame parent) {
        super(parent, "Import Match Schedule", true);

        Branding.applyTo(this);
        Branding.setInitialSize(this, 980, 650, parent);
        getContentPane().setLayout(new BorderLayout(0, 0));

        initComponents();
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("IMPORT MATCH SCHEDULE",
                "Upload a CSV schedule file to preview and import the seasonal match schedule in bulk", true);
        }
        return pnHeader;
    }

    private void initComponents() {
        getContentPane().add(getPnHeader(), BorderLayout.NORTH);

        JPanel pnMain = new JPanel(new BorderLayout(0, 15));
        pnMain.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnMain.setOpaque(false);

        JPanel pnlFileSelector = new JPanel(new BorderLayout(10, 0));
        pnlFileSelector.setOpaque(false);
        pnlFileSelector.setBorder(new RoundedTitledBorder("SELECT SCHEDULE FILE (.csv)", 16));

        txtFilePath = new JTextField();
        txtFilePath.setEditable(false);
        txtFilePath.setFont(Branding.DEFAULT_FONT.deriveFont(Font.PLAIN, 12f));

        JButton btnBrowse = new JButton("Browse...");
        btnBrowse.addActionListener(e -> handleBrowseFile());

        pnlFileSelector.add(new JLabel("File: "), BorderLayout.WEST);
        pnlFileSelector.add(txtFilePath, BorderLayout.CENTER);
        pnlFileSelector.add(btnBrowse, BorderLayout.EAST);

        JPanel pnTopWrapper = new JPanel(new BorderLayout());
        pnTopWrapper.setOpaque(false);
        pnTopWrapper.add(pnlFileSelector, BorderLayout.CENTER);
        pnMain.add(pnTopWrapper, BorderLayout.NORTH);

        JPanel pnPreviewAndSummary = new JPanel(new GridLayout(1, 2, 15, 0));
        pnPreviewAndSummary.setOpaque(false);

        // Left panel: Table of matches to import
        JPanel pnlPreview = new JPanel(new BorderLayout(0, 5));
        pnlPreview.setOpaque(false);
        pnlPreview.setBorder(new RoundedTitledBorder("MATCHES TO IMPORT (PREVIEW)", 16));

        String[] columns = { "TEAM", "DATE", "START", "END", "RIVAL", "HOME/AWAY", "SPECIAL", "FACILITY" };
        previewTableModel = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblPreview = new JTable(previewTableModel);
        tblPreview.setFont(Branding.DEFAULT_FONT.deriveFont(Font.PLAIN, 12f));
        tblPreview.setRowHeight(24);
        tblPreview.getTableHeader().setFont(Branding.DEFAULT_FONT.deriveFont(Font.BOLD, 12f));
        tblPreview.getTableHeader().setReorderingAllowed(false);

        JScrollPane spPreview = new JScrollPane(tblPreview);
        spPreview.getViewport().setBackground(Branding.WHITE);
        pnlPreview.add(spPreview, BorderLayout.CENTER);

        // Right panel: Validation summary & conflicts
        JPanel pnlSummary = new JPanel(new BorderLayout(0, 5));
        pnlSummary.setOpaque(false);
        pnlSummary.setBorder(new RoundedTitledBorder("VALIDATION SUMMARY & CONFLICTS", 16));

        txtSummaryArea = new JTextArea();
        txtSummaryArea.setEditable(false);
        txtSummaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        txtSummaryArea.setForeground(Branding.NAVY_DARKER);
        txtSummaryArea.setText("No file loaded yet.\nSelect a CSV file to preview valid matches and check conflicts.");
        pnlSummary.add(new JScrollPane(txtSummaryArea), BorderLayout.CENTER);

        pnPreviewAndSummary.add(pnlPreview);
        pnPreviewAndSummary.add(pnlSummary);
        pnMain.add(pnPreviewAndSummary, BorderLayout.CENTER);

        getContentPane().add(pnMain, BorderLayout.CENTER);

        JPanel pnFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnFooter.setOpaque(false);

        JButton btnCancel = new JButton("CLOSE");
        JButton btnImport = new JButton("VALIDATE & IMPORT");
        btnImport.setBackground(Branding.NAVY);
        btnImport.setForeground(Branding.WHITE);

        btnCancel.addActionListener(e -> dispose());
        btnImport.addActionListener(e -> handleImportAction());

        pnFooter.add(btnCancel);
        pnFooter.add(btnImport);
        getContentPane().add(pnFooter, BorderLayout.SOUTH);
    }

    private void handleBrowseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Match Schedule File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Schedule Files (*.csv)", "csv"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            txtFilePath.setText(selectedFile.getAbsolutePath());

            int defaultTeamId = 1;
            int defaultFacilityId = -1;
            ScheduleImportMatch.ImportResult previewResult =
                importService.processFile(selectedFile, defaultTeamId, defaultFacilityId);

            populatePreviewTable(previewResult.getValidMatches());

            StringBuilder status = new StringBuilder();
            status.append("Preview ready: ").append(previewResult.getValidMatches().size())
                  .append(" valid match(es) to import.\n");
            if (!previewResult.getErrorLogs().isEmpty()) {
                status.append("Errors detected: ").append(previewResult.getErrorLogs().size())
                      .append(" (click VALIDATE & IMPORT for details).\n");
            }
            if (!previewResult.getWarningLogs().isEmpty()) {
                status.append("Warnings detected: ").append(previewResult.getWarningLogs().size()).append("\n");
            }
            txtSummaryArea.setText(status.toString());
        }
    }

    private void populatePreviewTable(List<MatchCSV> matches) {
        previewTableModel.setRowCount(0);
        for (MatchCSV m : matches) {
            Object[] row = {
                m.getTeamId(),
                m.getMatchDate(),
                m.getStartTime(),
                m.getEndTime(),
                m.getRival(),
                m.isHome() ? "HOME" : "AWAY",
                m.isSpecialMatch() ? "YES" : "NO",
                m.getFacilityId() != null ? m.getFacilityId() : "-"
            };
            previewTableModel.addRow(row);
        }
    }

    private void handleImportAction() {
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(this, "Please select a valid CSV file first.",
                "File Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int defaultTeamId = 1;
        int defaultFacilityId = -1;

        ScheduleImportMatch.ImportResult result = importService.processFile(selectedFile, defaultTeamId, defaultFacilityId);
        populatePreviewTable(result.getValidMatches());

        StringBuilder summary = new StringBuilder();
        summary.append("--- IMPORT VALIDATION & PERSISTENCE REPORT ---\n\n");

        if (!result.getErrorLogs().isEmpty()) {
            summary.append("Errors found:\n");
            for (String err : result.getErrorLogs()) {
                summary.append(err).append("\n");
            }
            summary.append("\n");
        }

        if (!result.getWarningLogs().isEmpty()) {
            summary.append("Warnings (Conflicts detected):\n");
            for (String warn : result.getWarningLogs()) {
                summary.append(warn).append("\n");
            }
            summary.append("\n");
        }

        int savedCount = 0;
        if (!result.getValidMatches().isEmpty()) {
            MatchDAO.BatchImportResult dbResult = matchDAO.insertBatchMatchesWithReport(result.getValidMatches());
            savedCount = dbResult.insertedCount();

            if (!dbResult.logLines().isEmpty()) {
                summary.append("Persistence details:\n");
                for (String line : dbResult.logLines()) {
                    summary.append(line).append("\n");
                }
                summary.append("\n");
            }

            summary.append("[SUCCESS] Successfully imported and saved ").append(savedCount).append(" matches to the database!\n");
        } else {
            summary.append("[INFO] No valid matches were found to import.\n");
        }

        txtSummaryArea.setText(summary.toString());

        if (savedCount > 0) {
            JOptionPane.showMessageDialog(this,
                "Schedule imported successfully! " + savedCount + " matches are now available for ticket sales.",
                "Import Finished", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static class RoundedTitledBorder extends AbstractBorder {
        private static final long serialVersionUID = 1L;
        private final String title;
        private final int cornerRadius;

        public RoundedTitledBorder(String title, int cornerRadius) {
            this.title = title;
            this.cornerRadius = cornerRadius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Font titleFont = Branding.DEFAULT_FONT.deriveFont(Font.BOLD, 12f);
            g2.setFont(titleFont);

            int titleHeight = g2.getFontMetrics().getAscent();
            int titleWidth = g2.getFontMetrics().stringWidth(title);

            int lineY = y + titleHeight / 2 + 2;
            int rectHeight = height - (lineY - y) - 2;

            g2.setColor(new Color(203, 213, 225));
            g2.drawRoundRect(x + 1, lineY, width - 3, rectHeight - 1, cornerRadius, cornerRadius);

            g2.setColor(c.getBackground());
            g2.fillRect(x + 12, y, titleWidth + 6, titleHeight + 2);

            g2.setColor(Branding.NAVY);
            g2.drawString(title, x + 15, y + titleHeight);

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(20, 12, 12, 12);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(20, 12, 12, 12);
            return insets;
        }
    }
}