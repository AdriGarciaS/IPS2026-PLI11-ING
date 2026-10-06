package com.ips2026.pl11.view.reservation;

import com.ips2026.pl11.controller.reservation.FacilityReservationController;
import com.ips2026.pl11.model.reservation.DayAvailability;
import com.ips2026.pl11.model.reservation.Facility;
import com.ips2026.pl11.model.reservation.Reservation;
import com.ips2026.pl11.model.reservation.ReservationRules;
import com.ips2026.pl11.model.reservation.TimeSlot;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

/**
 * Facility reservations window: the facility manager chooses a facility and
 * a day, sees its availability (time bar + free periods) and books a period
 * of whole hours for an external person.
 *
 * <p>It is a modal dialog: the main menu is blocked while it is open. It
 * follows the lazy WindowBuilder style of the main menu
 * ({@code VentanaPrincipal}).</p>
 */
public class FacilityReservationWindow extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DAY_TITLE_FORMAT = DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int LAST_START_HOUR = ReservationRules.CLOSING_TIME.getHour() - ReservationRules.MIN_HOURS;

    private final transient FacilityReservationController controller;
    private final FreePeriodsTableModel freePeriodsModel = new FreePeriodsTableModel();

    /** True while the spinners are changed by code, so it is not taken as a user change. */
    private boolean updating;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnDay;
    private JLabel lblFacility;
    private JComboBox<Facility> cbFacility;
    private JLabel lblDay;
    private JSpinner spDay;
    private JLabel lblDayLimit;
    private JPanel pnAvailability;
    private JPanel pnBar;
    private AvailabilityBar availabilityBar;
    private JPanel pnLegend;
    private JPanel pnFreePeriods;
    private JLabel lblFreePeriods;
    private JScrollPane scrFreePeriods;
    private JTable tblFreePeriods;
    private JPanel pnSouth;
    private JPanel pnForms;
    private JPanel pnReservation;
    private JLabel lblStart;
    private JPanel pnStart;
    private JSpinner spStartHour;
    private JSpinner spStartMinute;
    private JLabel lblHours;
    private JPanel pnHours;
    private JSpinner spHours;
    private JLabel lblMaxHours;
    private JLabel lblName;
    private JTextField txtName;
    private JLabel lblCard;
    private JTextField txtCard;
    private JPanel pnSummary;
    private JLabel lblSummaryTime;
    private JPanel pnPrice;
    private JLabel lblPriceText;
    private JLabel lblPrice;
    private JLabel lblStatus;
    private JPanel pnButtons;
    private JButton btnClose;
    private JButton btnBook;

    /**
     * @param controller controller with the facilities already loaded
     * @param owner      window that opens it (the main menu); it stays
     *                   blocked until this window is closed
     */
    public FacilityReservationWindow(FacilityReservationController controller, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setTitle("Facility Reservations");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 640));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);

        reloadDay();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Facility Reservations",
                    "Book club facilities for external people (" + MoneyFormat.format(ReservationRules.PRICE_PER_HOUR)
                            + " per hour)");
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
            pnBody.add(getPnDay(), BorderLayout.NORTH);
            pnBody.add(getPnAvailability(), BorderLayout.CENTER);
            pnBody.add(getPnSouth(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnDay() {
        if (pnDay == null) {
            pnDay = new JPanel();
            pnDay.setBorder(titled("1. Facility and day"));
            pnDay.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
            pnDay.add(getLblFacility());
            pnDay.add(getCbFacility());
            pnDay.add(getLblDay());
            pnDay.add(getSpDay());
            pnDay.add(getLblDayLimit());
        }
        return pnDay;
    }

    private JLabel getLblFacility() {
        if (lblFacility == null) {
            lblFacility = new JLabel("Facility:");
            lblFacility.setDisplayedMnemonic('F');
            lblFacility.setLabelFor(getCbFacility());
        }
        return lblFacility;
    }

    private JComboBox<Facility> getCbFacility() {
        if (cbFacility == null) {
            cbFacility = new JComboBox<>();
            for (Facility facility : controller.getFacilities()) {
                cbFacility.addItem(facility);
            }
            cbFacility.setPreferredSize(new Dimension(230, cbFacility.getPreferredSize().height));
            cbFacility.addActionListener(event -> reloadDay());
        }
        return cbFacility;
    }

    private JLabel getLblDay() {
        if (lblDay == null) {
            lblDay = new JLabel("Day:");
            lblDay.setBorder(new EmptyBorder(0, 14, 0, 0));
            lblDay.setDisplayedMnemonic('D');
            lblDay.setLabelFor(getSpDay());
        }
        return lblDay;
    }

    /** Day spinner limited to the days that can be booked (today ... today + booking window). */
    private JSpinner getSpDay() {
        if (spDay == null) {
            Date today = toDate(controller.getToday());
            Date lastDay = toDate(controller.getLastBookableDay().plusDays(1)); // exclusive: whole last day
            spDay = new JSpinner(new SpinnerDateModel(today, today, new Date(lastDay.getTime() - 1), Calendar.DAY_OF_MONTH));
            spDay.setEditor(new JSpinner.DateEditor(spDay, "dd/MM/yyyy"));
            spDay.setPreferredSize(new Dimension(130, spDay.getPreferredSize().height));
            spDay.addChangeListener(event -> reloadDay());
        }
        return spDay;
    }

    private JLabel getLblDayLimit() {
        if (lblDayLimit == null) {
            lblDayLimit = new JLabel("Bookable from today until " + controller.getLastBookableDay().format(DATE_FORMAT));
            lblDayLimit.setForeground(Branding.TEXT_MUTED);
        }
        return lblDayLimit;
    }

    private JPanel getPnAvailability() {
        if (pnAvailability == null) {
            pnAvailability = new JPanel();
            pnAvailability.setBorder(titled("2. Availability"));
            pnAvailability.setLayout(new BorderLayout(0, 10));
            pnAvailability.add(getPnBar(), BorderLayout.NORTH);
            pnAvailability.add(getPnFreePeriods(), BorderLayout.CENTER);
        }
        return pnAvailability;
    }

    private JPanel getPnBar() {
        if (pnBar == null) {
            pnBar = new JPanel();
            pnBar.setOpaque(false);
            pnBar.setLayout(new BorderLayout(0, 4));
            pnBar.add(getAvailabilityBar(), BorderLayout.CENTER);
            pnBar.add(getPnLegend(), BorderLayout.SOUTH);
        }
        return pnBar;
    }

    private AvailabilityBar getAvailabilityBar() {
        if (availabilityBar == null) {
            availabilityBar = new AvailabilityBar();
            availabilityBar.setTimeClickedListener(this::setStartTime);
        }
        return availabilityBar;
    }

    private JPanel getPnLegend() {
        if (pnLegend == null) {
            pnLegend = new JPanel();
            pnLegend.setOpaque(false);
            pnLegend.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));
            pnLegend.add(legend(TimeSlot.Type.FREE, "Free"));
            pnLegend.add(legend(TimeSlot.Type.TEAM_USE, "Team use"));
            pnLegend.add(legend(TimeSlot.Type.BLOCKED_AFTER_TEAM,
                    "Blocked " + DayAvailability.formatDuration(ReservationRules.BLOCKED_AFTER_TEAM_USE) + " after team use"));
            pnLegend.add(legend(TimeSlot.Type.RESERVED, "Reserved"));
            pnLegend.add(legend(TimeSlot.Type.PAST, "Past"));
        }
        return pnLegend;
    }

    private static JLabel legend(TimeSlot.Type type, String text) {
        JLabel label = new JLabel(text, AvailabilityBar.legendIcon(type), SwingConstants.LEFT);
        label.setForeground(Branding.TEXT_MUTED);
        return label;
    }

    private JPanel getPnFreePeriods() {
        if (pnFreePeriods == null) {
            pnFreePeriods = new JPanel();
            pnFreePeriods.setOpaque(false);
            pnFreePeriods.setLayout(new BorderLayout(0, 6));
            pnFreePeriods.add(getLblFreePeriods(), BorderLayout.NORTH);
            pnFreePeriods.add(getScrFreePeriods(), BorderLayout.CENTER);
        }
        return pnFreePeriods;
    }

    private JLabel getLblFreePeriods() {
        if (lblFreePeriods == null) {
            lblFreePeriods = new JLabel("Free periods of at least 1 hour (click one to use its start time):");
        }
        return lblFreePeriods;
    }

    private JScrollPane getScrFreePeriods() {
        if (scrFreePeriods == null) {
            scrFreePeriods = new JScrollPane();
            scrFreePeriods.setViewportView(getTblFreePeriods());
            scrFreePeriods.setPreferredSize(new Dimension(400, 120));
        }
        return scrFreePeriods;
    }

    private JTable getTblFreePeriods() {
        if (tblFreePeriods == null) {
            tblFreePeriods = new JTable(freePeriodsModel);
            tblFreePeriods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblFreePeriods.setAutoCreateRowSorter(true);
            tblFreePeriods.setFillsViewportHeight(true);
            tblFreePeriods.setRowHeight(24);
            tblFreePeriods.getTableHeader().setReorderingAllowed(false);
            tblFreePeriods.getSelectionModel().addListSelectionListener(event -> {
                int viewRow = tblFreePeriods.getSelectedRow();
                if (!event.getValueIsAdjusting() && viewRow >= 0 && !updating) {
                    TimeSlot period = freePeriodsModel.getPeriodAt(tblFreePeriods.convertRowIndexToModel(viewRow));
                    setStartTime(period.getStart());
                }
            });
        }
        return tblFreePeriods;
    }

    private JPanel getPnSouth() {
        if (pnSouth == null) {
            pnSouth = new JPanel();
            pnSouth.setOpaque(false);
            pnSouth.setLayout(new BorderLayout(0, 12));
            pnSouth.add(getPnForms(), BorderLayout.CENTER);
            pnSouth.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnSouth;
    }

    private JPanel getPnForms() {
        if (pnForms == null) {
            pnForms = new JPanel();
            pnForms.setOpaque(false);
            pnForms.setLayout(new BorderLayout(12, 0));
            pnForms.add(getPnReservation(), BorderLayout.CENTER);
            pnForms.add(getPnSummary(), BorderLayout.EAST);
        }
        return pnForms;
    }

    private JPanel getPnReservation() {
        if (pnReservation == null) {
            pnReservation = new JPanel();
            pnReservation.setBorder(titled("3. Reservation"));
            pnReservation.setLayout(new GridBagLayout());
            addFormRow(pnReservation, 0, getLblStart(), getPnStart());
            addFormRow(pnReservation, 1, getLblHours(), getPnHours());
            addFormRow(pnReservation, 2, getLblName(), getTxtName());
            addFormRow(pnReservation, 3, getLblCard(), getTxtCard());
        }
        return pnReservation;
    }

    private static void addFormRow(JPanel panel, int row, JLabel label, java.awt.Component field) {
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(0, 0, 8, 10);
        gbcLabel.gridx = 0;
        gbcLabel.gridy = row;
        panel.add(label, gbcLabel);

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.weightx = 1.0;
        gbcField.insets = new Insets(0, 0, 8, 0);
        gbcField.gridx = 1;
        gbcField.gridy = row;
        panel.add(field, gbcField);
    }

    private JLabel getLblStart() {
        if (lblStart == null) {
            lblStart = new JLabel("Start time:");
            lblStart.setDisplayedMnemonic('S');
            lblStart.setLabelFor(getSpStartHour());
        }
        return lblStart;
    }

    private JPanel getPnStart() {
        if (pnStart == null) {
            pnStart = new JPanel();
            pnStart.setOpaque(false);
            pnStart.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
            pnStart.add(getSpStartHour());
            pnStart.add(new JLabel(":"));
            pnStart.add(getSpStartMinute());
            JLabel hint = new JLabel("  (any minute, from " + ReservationRules.OPENING_TIME + ")");
            hint.setForeground(Branding.TEXT_MUTED);
            pnStart.add(hint);
        }
        return pnStart;
    }

    private JSpinner getSpStartHour() {
        if (spStartHour == null) {
            spStartHour = timeSpinner(ReservationRules.OPENING_TIME.getHour(), ReservationRules.OPENING_TIME.getHour(),
                    LAST_START_HOUR);
            spStartHour.setToolTipText("Hour of the start time");
        }
        return spStartHour;
    }

    private JSpinner getSpStartMinute() {
        if (spStartMinute == null) {
            spStartMinute = timeSpinner(0, 0, 59);
            spStartMinute.setToolTipText("Minute of the start time");
        }
        return spStartMinute;
    }

    /** Spinner of hours or minutes shown with two digits ("08"). */
    private JSpinner timeSpinner(int value, int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "00"));
        spinner.setPreferredSize(new Dimension(60, spinner.getPreferredSize().height));
        spinner.addChangeListener(event -> {
            if (!updating) {
                startTimeChanged();
            }
        });
        return spinner;
    }

    private JLabel getLblHours() {
        if (lblHours == null) {
            lblHours = new JLabel("Hours:");
            lblHours.setDisplayedMnemonic('H');
            lblHours.setLabelFor(getSpHours());
        }
        return lblHours;
    }

    private JPanel getPnHours() {
        if (pnHours == null) {
            pnHours = new JPanel();
            pnHours.setOpaque(false);
            pnHours.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
            pnHours.add(getSpHours());
            pnHours.add(getLblMaxHours());
        }
        return pnHours;
    }

    private JSpinner getSpHours() {
        if (spHours == null) {
            spHours = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
            spHours.setPreferredSize(new Dimension(60, spHours.getPreferredSize().height));
            spHours.setToolTipText("Whole hours to book");
            spHours.addChangeListener(event -> {
                if (!updating) {
                    updateSummary();
                }
            });
        }
        return spHours;
    }

    private JLabel getLblMaxHours() {
        if (lblMaxHours == null) {
            lblMaxHours = new JLabel();
            lblMaxHours.setForeground(Branding.TEXT_MUTED);
        }
        return lblMaxHours;
    }

    private JLabel getLblName() {
        if (lblName == null) {
            lblName = new JLabel("Name:");
            lblName.setDisplayedMnemonic('N');
            lblName.setLabelFor(getTxtName());
        }
        return lblName;
    }

    private JTextField getTxtName() {
        if (txtName == null) {
            txtName = new JTextField(24);
            txtName.setToolTipText("Name of the person requesting the reservation");
        }
        return txtName;
    }

    private JLabel getLblCard() {
        if (lblCard == null) {
            lblCard = new JLabel("Card number:");
            lblCard.setDisplayedMnemonic('C');
            lblCard.setLabelFor(getTxtCard());
        }
        return lblCard;
    }

    private JTextField getTxtCard() {
        if (txtCard == null) {
            txtCard = new JTextField(24);
            txtCard.setToolTipText("Bank card number for the payment (spaces and dashes are allowed)");
        }
        return txtCard;
    }

    private JPanel getPnSummary() {
        if (pnSummary == null) {
            pnSummary = new JPanel();
            pnSummary.setBorder(titled("Summary"));
            pnSummary.setPreferredSize(new Dimension(340, 10));
            pnSummary.setLayout(new BorderLayout(0, 8));
            pnSummary.add(getLblSummaryTime(), BorderLayout.NORTH);
            pnSummary.add(getPnPrice(), BorderLayout.CENTER);
            pnSummary.add(getLblStatus(), BorderLayout.SOUTH);
        }
        return pnSummary;
    }

    private JLabel getLblSummaryTime() {
        if (lblSummaryTime == null) {
            lblSummaryTime = new JLabel();
            lblSummaryTime.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 15));
        }
        return lblSummaryTime;
    }

    private JPanel getPnPrice() {
        if (pnPrice == null) {
            pnPrice = new JPanel();
            pnPrice.setOpaque(false);
            pnPrice.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(1, 0, 1, 0, Branding.BORDER),
                    new EmptyBorder(6, 0, 6, 0)));
            pnPrice.setLayout(new BorderLayout(10, 0));
            pnPrice.add(getLblPriceText(), BorderLayout.WEST);
            pnPrice.add(getLblPrice(), BorderLayout.EAST);
        }
        return pnPrice;
    }

    private JLabel getLblPriceText() {
        if (lblPriceText == null) {
            lblPriceText = new JLabel("PRICE:");
            lblPriceText.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 16));
        }
        return lblPriceText;
    }

    private JLabel getLblPrice() {
        if (lblPrice == null) {
            lblPrice = new JLabel();
            lblPrice.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 18));
            lblPrice.setForeground(Branding.NAVY);
            lblPrice.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return lblPrice;
    }

    private JLabel getLblStatus() {
        if (lblStatus == null) {
            lblStatus = new JLabel();
            lblStatus.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        }
        return lblStatus;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnClose());
            pnButtons.add(getBtnBook());
        }
        return pnButtons;
    }

    private JButton getBtnClose() {
        if (btnClose == null) {
            btnClose = new JButton("Close");
            btnClose.setMnemonic('l');
            btnClose.addActionListener(event -> dispose());
        }
        return btnClose;
    }

    private JButton getBtnBook() {
        if (btnBook == null) {
            btnBook = new JButton("Book");
            btnBook.setMnemonic('B');
            btnBook.setFont(btnBook.getFont().deriveFont(Font.BOLD));
            btnBook.addActionListener(event -> book());
        }
        return btnBook;
    }

    private static TitledBorder titled(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        return border;
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    private Facility getSelectedFacility() {
        return (Facility) getCbFacility().getSelectedItem();
    }

    private LocalDate getSelectedDay() {
        return ((Date) getSpDay().getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toDate(LocalDate day) {
        return Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalTime getStartTime() {
        return LocalTime.of((Integer) getSpStartHour().getValue(), (Integer) getSpStartMinute().getValue());
    }

    private int getHours() {
        return (Integer) getSpHours().getValue();
    }

    /** Loads the availability of the chosen facility and day and shows it. */
    private void reloadDay() {
        Facility facility = getSelectedFacility();
        if (facility == null) {
            showDayNotLoaded("There are no facilities in the database.");
            return;
        }
        try {
            controller.selectDay(facility, getSelectedDay());
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The availability could not be loaded:\n" + exception.getMessage(),
                    "Facility Reservations", JOptionPane.ERROR_MESSAGE);
            showDayNotLoaded("The availability could not be loaded.");
            return;
        }
        showDay();
    }

    private void showDay() {
        DayAvailability availability = controller.getAvailability();
        ((TitledBorder) getPnAvailability().getBorder()).setTitle("2. Availability of "
                + getSelectedFacility().getName() + " on " + availability.getDate().format(DAY_TITLE_FORMAT));
        getPnAvailability().repaint();
        getAvailabilityBar().setTimeline(availability.getTimeline());

        List<TimeSlot> freePeriods = availability.getFreePeriods();
        updating = true;
        freePeriodsModel.setPeriods(freePeriods);
        updating = false;
        getLblFreePeriods().setText(freePeriods.isEmpty()
                ? "There are no free periods of at least 1 hour on this day."
                : "Free periods of at least 1 hour (click one to use its start time):");

        // Proposes the start of the first free period.
        setStartTime(freePeriods.isEmpty() ? ReservationRules.OPENING_TIME : freePeriods.get(0).getStart());
    }

    private void showDayNotLoaded(String message) {
        getBtnBook().setEnabled(false);
        getLblStatus().setForeground(Branding.ERROR);
        getLblStatus().setText(html(message));
    }

    /** Puts {@code time} in the start time spinners (limited to the possible start hours). */
    private void setStartTime(LocalTime time) {
        int hour = Math.max(ReservationRules.OPENING_TIME.getHour(), Math.min(LAST_START_HOUR, time.getHour()));
        updating = true;
        getSpStartHour().setValue(hour);
        getSpStartMinute().setValue(time.getMinute());
        updating = false;
        startTimeChanged();
    }

    /** The hours spinner goes from 1 to the maximum hours that fit from the start time. */
    private void startTimeChanged() {
        if (controller.getAvailability() == null) {
            return;
        }
        LocalTime start = getStartTime();
        int maxHours = controller.getAvailability().maxHoursFrom(start);
        updating = true;
        if (maxHours >= ReservationRules.MIN_HOURS) {
            int hours = Math.min(getHours(), maxHours);
            getSpHours().setModel(new SpinnerNumberModel(hours, ReservationRules.MIN_HOURS, maxHours, 1));
            getLblMaxHours().setText("  (max " + maxHours + " from " + start + ")");
        } else {
            getSpHours().setModel(new SpinnerNumberModel(1, 1, 1, 1));
            getLblMaxHours().setText("  (not available from " + start + ")");
        }
        updating = false;
        getSpHours().setEnabled(maxHours >= ReservationRules.MIN_HOURS);
        updateSummary();
    }

    /** Updates the summary (time, price, available or not), the bar and the Book button. */
    private void updateSummary() {
        if (controller.getAvailability() == null) {
            return;
        }
        LocalTime start = getStartTime();
        int hours = getHours();
        Optional<String> problem = controller.findTimeProblem(start, hours);

        getLblSummaryTime().setText(start + " – " + start.plusHours(hours) + "   (" + hours + " h)");
        getLblPrice().setText(MoneyFormat.format(controller.getPrice(hours)));
        if (problem.isPresent()) {
            getLblStatus().setForeground(Branding.ERROR);
            getLblStatus().setText(html("Not available: " + problem.get()));
        } else {
            getLblStatus().setForeground(Branding.SUCCESS);
            getLblStatus().setText(html("Available"));
        }
        getAvailabilityBar().setSelection(start, hours, problem.isEmpty());
        getBtnBook().setEnabled(problem.isEmpty());
    }

    /** HTML so long messages wrap inside the summary panel. */
    private static String html(String text) {
        return "<html><body style='width: 230px'>" + text.replace("&", "&amp;").replace("<", "&lt;") + "</body></html>";
    }

    private void book() {
        String name = getTxtName().getText();
        String card = getTxtCard().getText();
        try {
            controller.checkHolderData(name, card);
        } catch (IllegalArgumentException exception) {
            showWarning(exception.getMessage());
            (name.isBlank() || exception.getMessage().contains("name") ? getTxtName() : getTxtCard()).requestFocusInWindow();
            return;
        }

        LocalTime start = getStartTime();
        int hours = getHours();
        int option = JOptionPane.showConfirmDialog(this,
                "Book " + getSelectedFacility().getName() + " on " + getSelectedDay().format(DATE_FORMAT)
                        + " from " + start + " to " + start.plusHours(hours) + " (" + hours + " h)\nfor "
                        + name.strip() + ", for a total of " + MoneyFormat.format(controller.getPrice(hours)) + "?",
                "Confirm reservation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        Reservation reservation;
        try {
            reservation = controller.book(name, card, start, hours);
        } catch (IllegalArgumentException exception) {
            // The availability changed since it was shown.
            showWarning(exception.getMessage());
            showDay();
            return;
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "The reservation could not be stored:\n" + exception.getMessage(),
                    "Reservation error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        getTxtName().setText("");
        getTxtCard().setText("");
        showDay();
        JOptionPane.showMessageDialog(this, buildSummary(reservation),
                "Reservation completed", JOptionPane.INFORMATION_MESSAGE);
    }

    /** HTML summary of a completed reservation. */
    private static String buildSummary(Reservation reservation) {
        return "<html><body style='width: 300px'>"
                + "<b>Reservation nº " + reservation.getId() + "</b>"
                + "<table cellpadding='2' style='margin-top: 8px'>"
                + row("Facility", reservation.getFacility().getName())
                + row("Day", reservation.getDate().format(DAY_TITLE_FORMAT))
                + row("Time", reservation.getStart() + " – " + reservation.getEnd()
                        + " (" + reservation.getHours() + " h)")
                + row("Name", reservation.getHolderName())
                + row("Card", reservation.getMaskedCardNumber())
                + "</table><hr><b style='font-size: 13px'>TOTAL PRICE: "
                + MoneyFormat.format(reservation.getTotalPrice()) + "</b></body></html>";
    }

    private static String row(String label, String value) {
        return "<tr><td style='color: #6B7280'>" + label + "</td><td>"
                + value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</td></tr>";
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Facility Reservations", JOptionPane.WARNING_MESSAGE);
    }
}
