package com.ips2026.pl11.view.history;

import com.ips2026.pl11.controller.history.SalesHistoryController;
import com.ips2026.pl11.model.history.Income;
import com.ips2026.pl11.model.history.IncomeType;
import com.ips2026.pl11.model.history.SaleLine;
import com.ips2026.pl11.model.history.SalesHistory;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;
import com.toedter.calendar.JDateChooser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Sales history for the general manager: every operation that produced
 * income between two dates (by default the last 30 days), newest first,
 * with the income of each type and the total. The details of a
 * merchandising sale (its products) are opened with "View details..." or
 * a double click.
 *
 * <p>It is a modal dialog. It follows the lazy WindowBuilder style of the
 * main menu.</p>
 */
public class SalesHistoryDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String TITLE = "Sales History";
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter MOMENT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
    private static final String ALL_TYPES = "All types";

    private final transient SalesHistoryController controller;
    private final IncomeTableModel incomeModel = new IncomeTableModel();

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnFilters;
    private JLabel lblFrom;
    private JDateChooser dateFrom;
    private JLabel lblTo;
    private JDateChooser dateTo;
    private JLabel lblType;
    private JComboBox<Object> cbType;
    private JButton btnSearch;
    private JButton btnReset;
    private JPanel pnCenter;
    private JPanel pnIncomes;
    private JScrollPane scrIncomes;
    private JTable tblIncomes;
    private JPanel pnSummary;
    private final Map<IncomeType, JLabel> lblCounts = new EnumMap<>(IncomeType.class);
    private final Map<IncomeType, JLabel> lblAmounts = new EnumMap<>(IncomeType.class);
    private JLabel lblTotalTitle;
    private JLabel lblTotal;
    private JLabel lblPeriod;
    private JPanel pnButtons;
    private JButton btnDetails;
    private JButton btnClose;

    /**
     * @param controller sales history controller
     * @param history    operations to show first (the last 30 days)
     * @param owner      window that opens it; it stays blocked until this
     *                   window is closed
     */
    public SalesHistoryDialog(SalesHistoryController controller, SalesHistory history, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setTitle(TITLE);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(980, 600));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);

        setDates(controller.getDefaultFrom(), controller.getDefaultTo());
        showHistory(history);
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel(TITLE, "Income of the club between two dates — general manager");
        }
        return pnHeader;
    }

    /** Everything below the header, with the window margins. */
    private JPanel getPnBody() {
        if (pnBody == null) {
            pnBody = new JPanel();
            pnBody.setOpaque(false);
            pnBody.setBorder(new EmptyBorder(14, 14, 14, 14));
            pnBody.setLayout(new BorderLayout(0, 12));
            pnBody.add(getPnFilters(), BorderLayout.NORTH);
            pnBody.add(getPnCenter(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnFilters() {
        if (pnFilters == null) {
            pnFilters = new JPanel();
            pnFilters.setBorder(BorderFactory.createCompoundBorder(titled("Filters"), new EmptyBorder(4, 4, 4, 4)));
            pnFilters.setLayout(new BorderLayout(0, 0));

            JPanel pnFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            pnFields.setOpaque(false);
            pnFields.add(getLblFrom());
            pnFields.add(getDateFrom());
            pnFields.add(getLblTo());
            pnFields.add(getDateTo());
            pnFields.add(getLblType());
            pnFields.add(getCbType());

            JPanel pnFilterButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnFilterButtons.setOpaque(false);
            pnFilterButtons.add(getBtnReset());
            pnFilterButtons.add(getBtnSearch());

            pnFilters.add(pnFields, BorderLayout.CENTER);
            pnFilters.add(pnFilterButtons, BorderLayout.EAST);
        }
        return pnFilters;
    }

    private JLabel getLblFrom() {
        if (lblFrom == null) {
            lblFrom = new JLabel("From:");
            lblFrom.setDisplayedMnemonic('F');
            lblFrom.setLabelFor(getDateFrom().getDateEditor().getUiComponent());
        }
        return lblFrom;
    }

    private JDateChooser getDateFrom() {
        if (dateFrom == null) {
            dateFrom = newDateChooser("First day (included)");
        }
        return dateFrom;
    }

    private JLabel getLblTo() {
        if (lblTo == null) {
            lblTo = new JLabel("To:");
            lblTo.setDisplayedMnemonic('T');
            lblTo.setLabelFor(getDateTo().getDateEditor().getUiComponent());
        }
        return lblTo;
    }

    private JDateChooser getDateTo() {
        if (dateTo == null) {
            dateTo = newDateChooser("Last day (included)");
        }
        return dateTo;
    }

    private static JDateChooser newDateChooser(String tooltip) {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString(DATE_PATTERN);
        chooser.setPreferredSize(new Dimension(140, 28));
        chooser.setToolTipText(tooltip);
        return chooser;
    }

    private JLabel getLblType() {
        if (lblType == null) {
            lblType = new JLabel("Type:");
            lblType.setDisplayedMnemonic('Y');
            lblType.setLabelFor(getCbType());
        }
        return lblType;
    }

    /** "All types" and then every income type. */
    private JComboBox<Object> getCbType() {
        if (cbType == null) {
            cbType = new JComboBox<>();
            cbType.addItem(ALL_TYPES);
            for (IncomeType type : IncomeType.values()) {
                cbType.addItem(type);
            }
            cbType.setRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    Object text = value instanceof IncomeType type ? type.getPluralLabel() : value;
                    return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                }
            });
            cbType.setPreferredSize(new Dimension(220, 28));
        }
        return cbType;
    }

    private JButton getBtnSearch() {
        if (btnSearch == null) {
            btnSearch = new JButton("Search");
            btnSearch.setFont(btnSearch.getFont().deriveFont(Font.BOLD));
            btnSearch.setMnemonic('S');
            btnSearch.addActionListener(event -> search());
        }
        return btnSearch;
    }

    private JButton getBtnReset() {
        if (btnReset == null) {
            btnReset = new JButton("Reset");
            btnReset.setMnemonic('R');
            btnReset.setToolTipText("Back to the last " + SalesHistoryController.DEFAULT_DAYS + " days, all types");
            btnReset.addActionListener(event -> reset());
        }
        return btnReset;
    }

    /** Operations on the left, summary on the right. */
    private JPanel getPnCenter() {
        if (pnCenter == null) {
            pnCenter = new JPanel();
            pnCenter.setOpaque(false);
            pnCenter.setLayout(new BorderLayout(14, 0));
            pnCenter.add(getPnIncomes(), BorderLayout.CENTER);
            pnCenter.add(getPnSummary(), BorderLayout.EAST);
        }
        return pnCenter;
    }

    private JPanel getPnIncomes() {
        if (pnIncomes == null) {
            pnIncomes = new JPanel();
            pnIncomes.setBorder(BorderFactory.createCompoundBorder(titled("Income operations"), new EmptyBorder(6, 6, 6, 6)));
            pnIncomes.setLayout(new BorderLayout(0, 0));
            pnIncomes.add(getScrIncomes(), BorderLayout.CENTER);
        }
        return pnIncomes;
    }

    private JScrollPane getScrIncomes() {
        if (scrIncomes == null) {
            scrIncomes = new JScrollPane();
            scrIncomes.setViewportView(getTblIncomes());
        }
        return scrIncomes;
    }

    private JTable getTblIncomes() {
        if (tblIncomes == null) {
            tblIncomes = new JTable(incomeModel);
            tblIncomes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblIncomes.setAutoCreateRowSorter(true);
            tblIncomes.setFillsViewportHeight(true);
            tblIncomes.setRowHeight(24);
            tblIncomes.getTableHeader().setReorderingAllowed(false);
            tblIncomes.getColumnModel().getColumn(IncomeTableModel.COLUMN_MOMENT).setCellRenderer(new DefaultTableCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void setValue(Object value) {
                    setText(value instanceof LocalDateTime moment ? moment.format(MOMENT_FORMAT) : "");
                }
            });
            tblIncomes.getColumnModel().getColumn(IncomeTableModel.COLUMN_AMOUNT).setCellRenderer(MoneyFormat.tableRenderer());
            tblIncomes.getColumnModel().getColumn(0).setPreferredWidth(120);
            tblIncomes.getColumnModel().getColumn(1).setPreferredWidth(100);
            tblIncomes.getColumnModel().getColumn(2).setPreferredWidth(300);
            tblIncomes.getColumnModel().getColumn(3).setPreferredWidth(100);
            tblIncomes.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()) {
                    updateDetailsButton();
                }
            });
            tblIncomes.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    if (event.getClickCount() == 2 && tblIncomes.rowAtPoint(event.getPoint()) >= 0) {
                        openDetails();
                    }
                }
            });
        }
        return tblIncomes;
    }

    /** Income and number of operations of each type, and the total income of the period. */
    private JPanel getPnSummary() {
        if (pnSummary == null) {
            pnSummary = new JPanel();
            pnSummary.setBorder(BorderFactory.createCompoundBorder(titled("Summary"), new EmptyBorder(10, 14, 10, 14)));
            pnSummary.setLayout(new GridBagLayout());
            pnSummary.setPreferredSize(new Dimension(400, 400));

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(4, 0, 4, 0);
            constraints.fill = GridBagConstraints.HORIZONTAL;

            JLabel lblByType = new JLabel("Income by type");
            lblByType.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            lblByType.setForeground(Branding.TEXT_MUTED);
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 3;
            constraints.insets = new Insets(0, 0, 8, 0);
            pnSummary.add(lblByType, constraints);

            constraints.gridwidth = 1;
            constraints.insets = new Insets(8, 0, 8, 0);
            int row = 1;
            for (IncomeType type : IncomeType.values()) {
                constraints.gridy = row++;

                constraints.gridx = 0;
                constraints.weightx = 1;
                pnSummary.add(new JLabel(type.getPluralLabel()), constraints);

                JLabel lblCount = new JLabel();
                lblCount.setForeground(Branding.TEXT_MUTED);
                lblCount.setToolTipText("Number of operations");
                lblCounts.put(type, lblCount);
                constraints.gridx = 1;
                constraints.weightx = 0;
                constraints.insets = new Insets(8, 12, 8, 24);
                pnSummary.add(lblCount, constraints);

                JLabel lblAmount = new JLabel();
                lblAmount.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 14));
                lblAmount.setHorizontalAlignment(SwingConstants.RIGHT);
                lblAmounts.put(type, lblAmount);
                constraints.gridx = 2;
                constraints.insets = new Insets(8, 0, 8, 0);
                pnSummary.add(lblAmount, constraints);
            }

            constraints.gridx = 0;
            constraints.gridwidth = 3;
            constraints.gridy = row++;
            constraints.weighty = 1;
            constraints.anchor = GridBagConstraints.SOUTH;
            constraints.insets = new Insets(16, 0, 8, 0);
            pnSummary.add(new JSeparator(), constraints);

            constraints.weighty = 0;
            constraints.insets = new Insets(4, 0, 4, 0);
            constraints.gridy = row++;
            pnSummary.add(getLblTotalTitle(), constraints);
            constraints.gridy = row++;
            pnSummary.add(getLblTotal(), constraints);
            constraints.gridy = row;
            pnSummary.add(getLblPeriod(), constraints);
        }
        return pnSummary;
    }

    private JLabel getLblTotalTitle() {
        if (lblTotalTitle == null) {
            lblTotalTitle = new JLabel("TOTAL INCOME");
            lblTotalTitle.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            lblTotalTitle.setForeground(Branding.TEXT_MUTED);
        }
        return lblTotalTitle;
    }

    private JLabel getLblTotal() {
        if (lblTotal == null) {
            lblTotal = new JLabel();
            lblTotal.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 32));
            lblTotal.setForeground(Branding.NAVY);
            lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return lblTotal;
    }

    private JLabel getLblPeriod() {
        if (lblPeriod == null) {
            lblPeriod = new JLabel();
            lblPeriod.setForeground(Branding.TEXT_MUTED);
            lblPeriod.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return lblPeriod;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnClose());
            pnButtons.add(getBtnDetails());
        }
        return pnButtons;
    }

    private JButton getBtnDetails() {
        if (btnDetails == null) {
            btnDetails = new JButton("View details...");
            btnDetails.setFont(btnDetails.getFont().deriveFont(Font.BOLD));
            btnDetails.setMnemonic('V');
            btnDetails.setEnabled(false);
            btnDetails.setToolTipText("Products of the selected merchandising sale (also with a double click)");
            btnDetails.addActionListener(event -> openDetails());
        }
        return btnDetails;
    }

    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton("Close");
            btnClose.setMnemonic('C');
            btnClose.addActionListener(event -> dispose());
        }
        return btnClose;
    }

    private static TitledBorder titled(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        return border;
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    private void search() {
        Object selected = getCbType().getSelectedItem();
        IncomeType type = selected instanceof IncomeType incomeType ? incomeType : null;
        try {
            showHistory(controller.search(toLocalDate(getDateFrom().getDate()), toLocalDate(getDateTo().getDate()), type));
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), TITLE, JOptionPane.WARNING_MESSAGE);
        } catch (SQLException exception) {
            showError("The sales could not be loaded:", exception);
        }
    }

    /** Back to the last 30 days and all types. */
    private void reset() {
        setDates(controller.getDefaultFrom(), controller.getDefaultTo());
        getCbType().setSelectedItem(ALL_TYPES);
        try {
            showHistory(controller.searchDefault());
        } catch (SQLException exception) {
            showError("The sales could not be loaded:", exception);
        }
    }

    private void showHistory(SalesHistory history) {
        incomeModel.setIncomes(history.getIncomes());
        for (IncomeType type : IncomeType.values()) {
            lblCounts.get(type).setText("(" + history.getCount(type) + ")");
            lblAmounts.get(type).setText(MoneyFormat.format(history.getTotal(type)));
        }
        getLblTotal().setText(MoneyFormat.format(history.getTotal()));
        getLblPeriod().setText(history.getFrom().format(DATE_FORMAT) + " – " + history.getTo().format(DATE_FORMAT));
        updateDetailsButton();
    }

    /** "View details..." is only enabled for a merchandising sale. */
    private void updateDetailsButton() {
        Income selected = getSelectedIncome();
        getBtnDetails().setEnabled(selected != null && selected.hasDetails());
    }

    private Income getSelectedIncome() {
        int viewRow = getTblIncomes().getSelectedRow();
        return viewRow < 0 ? null : incomeModel.getIncomeAt(getTblIncomes().convertRowIndexToModel(viewRow));
    }

    private void openDetails() {
        Income selected = getSelectedIncome();
        if (selected == null || !selected.hasDetails()) {
            return; // the rest of the concepts have no details
        }
        List<SaleLine> lines;
        try {
            lines = controller.getSaleLines(selected);
        } catch (SQLException exception) {
            showError("The details of the sale could not be loaded:", exception);
            return;
        }
        new SaleDetailsDialog(selected, lines, this).setVisible(true);
    }

    private void setDates(LocalDate from, LocalDate to) {
        getDateFrom().setDate(toDate(from));
        getDateTo().setDate(toDate(to));
    }

    private void showError(String message, SQLException exception) {
        JOptionPane.showMessageDialog(this, message + System.lineSeparator() + exception.getMessage(),
                TITLE, JOptionPane.ERROR_MESSAGE);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
