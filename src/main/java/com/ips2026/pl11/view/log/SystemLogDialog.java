package com.ips2026.pl11.view.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.ips2026.pl11.controller.log.SystemLogController;
import com.ips2026.pl11.model.log.LogAction;
import com.ips2026.pl11.model.log.LogFilter;
import com.ips2026.pl11.model.log.LogRecord;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.toedter.calendar.JDateChooser;

/**
 * US 36717: the general manager sees the system log (logins, failed logins,
 * data queries and database changes of every user), newest first, and
 * filters it by dates, user and action. The table is refreshed as soon as a
 * filter changes.
 *
 * <p>Modal dialog opened from the general manager menu, in the lazy
 * WindowBuilder style.</p>
 */
public class SystemLogDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String ALL_USERS = "All users";
    private static final String GUEST_USERS = "Guest (not logged in)";
    private static final String ALL_ACTIONS = "All actions";

    private final transient SystemLogController controller;
    private final LogRecordsTableModel recordsModel = new LogRecordsTableModel();
    /** True while the filters are being changed by the program (no search for each change). */
    private boolean updatingFilters;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnFilters;
    private JDateChooser dateFrom;
    private JDateChooser dateTo;
    private JComboBox<String> cbUser;
    private JComboBox<Object> cbAction;
    private JButton btnReset;
    private JPanel pnRecords;
    private JScrollPane scrRecords;
    private JTable tblRecords;
    private JLabel lblCount;
    private JPanel pnButtons;
    private JButton btnClose;

    /**
     * @param controller controller of the system log
     * @param owner      window that opens it (the general manager menu)
     * @throws SQLException if the users for the filter cannot be read
     */
    public SystemLogDialog(SystemLogController controller, Window owner) throws SQLException {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setTitle("System Log");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 520));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);

        loadUsers();
        search();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("System Log", "Logins, data queries and database changes made by any user");
        }
        return pnHeader;
    }

    private JPanel getPnBody() {
        if (pnBody == null) {
            pnBody = new JPanel(new BorderLayout(0, 12));
            pnBody.setOpaque(false);
            pnBody.setBorder(new EmptyBorder(14, 14, 14, 14));
            pnBody.add(getPnFilters(), BorderLayout.NORTH);
            pnBody.add(getPnRecords(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnFilters() {
        if (pnFilters == null) {
            pnFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            pnFilters.setBorder(BorderFactory.createCompoundBorder(titled("Filters"), new EmptyBorder(2, 2, 2, 2)));
            pnFilters.add(label("From:", 'F', getDateFrom().getDateEditor().getUiComponent()));
            pnFilters.add(getDateFrom());
            pnFilters.add(label("To:", 'T', getDateTo().getDateEditor().getUiComponent()));
            pnFilters.add(getDateTo());
            pnFilters.add(label("User:", 'U', getCbUser()));
            pnFilters.add(getCbUser());
            pnFilters.add(label("Action:", 'A', getCbAction()));
            pnFilters.add(getCbAction());
            pnFilters.add(getBtnReset());
        }
        return pnFilters;
    }

    private JDateChooser getDateFrom() {
        if (dateFrom == null) {
            dateFrom = newDateChooser("First day (included)");
        }
        return dateFrom;
    }

    private JDateChooser getDateTo() {
        if (dateTo == null) {
            dateTo = newDateChooser("Last day (included)");
        }
        return dateTo;
    }

    private JDateChooser newDateChooser(String tooltip) {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString(DATE_PATTERN);
        chooser.setPreferredSize(new Dimension(140, 28));
        chooser.setToolTipText(tooltip);
        chooser.addPropertyChangeListener("date", event -> filtersChanged());
        return chooser;
    }

    /** "All users", "Guest (not logged in)" and then every username. */
    private JComboBox<String> getCbUser() {
        if (cbUser == null) {
            cbUser = new JComboBox<>();
            cbUser.setPreferredSize(new Dimension(220, 28));
            cbUser.addActionListener(event -> filtersChanged());
        }
        return cbUser;
    }

    /** "All actions" and then every action. */
    private JComboBox<Object> getCbAction() {
        if (cbAction == null) {
            cbAction = new JComboBox<>();
            cbAction.addItem(ALL_ACTIONS);
            for (LogAction action : LogAction.values()) {
                cbAction.addItem(action);
            }
            cbAction.setPreferredSize(new Dimension(150, 28));
            cbAction.addActionListener(event -> filtersChanged());
        }
        return cbAction;
    }

    private JButton getBtnReset() {
        if (btnReset == null) {
            btnReset = new JButton("Reset");
            btnReset.setMnemonic('R');
            btnReset.addActionListener(event -> resetFilters());
        }
        return btnReset;
    }

    private JPanel getPnRecords() {
        if (pnRecords == null) {
            pnRecords = new JPanel(new BorderLayout(0, 6));
            pnRecords.setBorder(BorderFactory.createCompoundBorder(titled("Records"), new EmptyBorder(6, 8, 6, 8)));
            pnRecords.add(getScrRecords(), BorderLayout.CENTER);
            pnRecords.add(getLblCount(), BorderLayout.SOUTH);
        }
        return pnRecords;
    }

    private JScrollPane getScrRecords() {
        if (scrRecords == null) {
            scrRecords = new JScrollPane(getTblRecords());
        }
        return scrRecords;
    }

    private JTable getTblRecords() {
        if (tblRecords == null) {
            tblRecords = new JTable(recordsModel);
            tblRecords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblRecords.setAutoCreateRowSorter(true);
            tblRecords.setRowHeight(24);
            tblRecords.getTableHeader().setReorderingAllowed(false);
            tblRecords.setDefaultRenderer(LocalDateTime.class, new DefaultTableCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void setValue(Object value) {
                    setText(value == null ? "" : ((LocalDateTime) value).format(DATE_TIME));
                }
            });
            TableColumnModel columns = tblRecords.getColumnModel();
            int[] widths = { 150, 230, 110, 560, 70 };
            for (int i = 0; i < widths.length; i++) {
                columns.getColumn(i).setPreferredWidth(widths[i]);
            }
        }
        return tblRecords;
    }

    private JLabel getLblCount() {
        if (lblCount == null) {
            lblCount = new JLabel(" ");
            lblCount.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            lblCount.setForeground(Branding.TEXT_MUTED);
        }
        return lblCount;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.setOpaque(false);
            pnButtons.add(getBtnClose());
        }
        return pnButtons;
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

    private static JLabel label(String text, char mnemonic, java.awt.Component target) {
        JLabel label = new JLabel(text);
        label.setDisplayedMnemonic(mnemonic);
        label.setLabelFor(target);
        return label;
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    private void loadUsers() throws SQLException {
        updatingFilters = true;
        try {
            getCbUser().removeAllItems();
            getCbUser().addItem(ALL_USERS);
            getCbUser().addItem(GUEST_USERS);
            for (String username : controller.getUsernames()) {
                getCbUser().addItem(username);
            }
        } finally {
            updatingFilters = false;
        }
    }

    private void filtersChanged() {
        if (!updatingFilters) {
            search();
        }
    }

    private void resetFilters() {
        updatingFilters = true;
        try {
            getDateFrom().setDate(null);
            getDateTo().setDate(null);
            getCbUser().setSelectedIndex(0);
            getCbAction().setSelectedIndex(0);
        } finally {
            updatingFilters = false;
        }
        search();
    }

    private LogFilter readFilter() {
        String user = (String) getCbUser().getSelectedItem();
        String username = user == null || ALL_USERS.equals(user) ? null
                : GUEST_USERS.equals(user) ? LogFilter.GUESTS : user;
        Object action = getCbAction().getSelectedItem();
        return new LogFilter(toLocalDate(getDateFrom().getDate()), toLocalDate(getDateTo().getDate()), username,
                action instanceof LogAction ? (LogAction) action : null);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Reads the records that pass the filters and shows them (newest first). */
    private void search() {
        LogFilter filter = readFilter();
        List<LogRecord> records;
        try {
            records = controller.search(filter);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "The system log could not be read:\n" + exception.getMessage(),
                    "System Log", JOptionPane.ERROR_MESSAGE);
            return;
        }
        recordsModel.setRecords(records);
        getTblRecords().getRowSorter().setSortKeys(null); // newest first, as they come
        if (filter.hasWrongDates()) {
            getLblCount().setForeground(Branding.ERROR);
            getLblCount().setText("The 'From' date must be on or before the 'To' date.");
        } else {
            getLblCount().setForeground(Branding.TEXT_MUTED);
            getLblCount().setText(records.size() + (records.size() == 1 ? " record" : " records"));
        }
    }
}
