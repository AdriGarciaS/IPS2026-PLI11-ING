package com.ips2026.pl11.view.history;

import com.ips2026.pl11.model.history.Income;
import com.ips2026.pl11.model.history.SaleLine;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;
import com.ips2026.pl11.view.common.MoneyFormat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

/**
 * Details of a merchandising sale (read only): the products sold, how many
 * units of each one, the price of one unit at the moment of the sale and
 * the total, and the total of the sale.
 *
 * <p>It is a modal dialog. It follows the lazy WindowBuilder style of the
 * main menu.</p>
 */
public class SaleDetailsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter MOMENT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final transient Income sale;
    private final transient List<SaleLine> lines;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnProducts;
    private JScrollPane scrLines;
    private JTable tblLines;
    private JPanel pnTotals;
    private JLabel lblCounts;
    private JLabel lblSaleTotal;
    private JPanel pnButtons;
    private JButton btnClose;

    /**
     * @param sale  the merchandising sale
     * @param lines its products
     * @param owner window that opens it; it stays blocked until this window is closed
     */
    public SaleDetailsDialog(Income sale, List<SaleLine> lines, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.sale = sale;
        this.lines = lines;

        setTitle("Sale Details");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(560, 420));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        getRootPane().setDefaultButton(getBtnClose());
        Branding.setInitialSize(this, 720, 560, owner); // small: only the products of one sale
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Sale Details", "Merchandising sale #" + sale.getReference() + " — "
                    + sale.getMoment().format(MOMENT_FORMAT));
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
            pnBody.add(getPnProducts(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    private JPanel getPnProducts() {
        if (pnProducts == null) {
            pnProducts = new JPanel();
            pnProducts.setBorder(BorderFactory.createCompoundBorder(titled("Products sold"), new EmptyBorder(6, 6, 6, 6)));
            pnProducts.setLayout(new BorderLayout(0, 8));
            pnProducts.add(getScrLines(), BorderLayout.CENTER);
            pnProducts.add(getPnTotals(), BorderLayout.SOUTH);
        }
        return pnProducts;
    }

    private JScrollPane getScrLines() {
        if (scrLines == null) {
            scrLines = new JScrollPane();
            scrLines.setViewportView(getTblLines());
        }
        return scrLines;
    }

    private JTable getTblLines() {
        if (tblLines == null) {
            tblLines = new JTable(new SaleLinesTableModel(lines));
            tblLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblLines.setAutoCreateRowSorter(true);
            tblLines.setFillsViewportHeight(true);
            tblLines.setRowHeight(24);
            tblLines.getTableHeader().setReorderingAllowed(false);
            tblLines.getColumnModel().getColumn(SaleLinesTableModel.COLUMN_UNIT_PRICE).setCellRenderer(MoneyFormat.tableRenderer());
            tblLines.getColumnModel().getColumn(SaleLinesTableModel.COLUMN_TOTAL).setCellRenderer(MoneyFormat.tableRenderer());
            tblLines.getColumnModel().getColumn(0).setPreferredWidth(280);
        }
        return tblLines;
    }

    /** Number of products and units on the left, total of the sale on the right. */
    private JPanel getPnTotals() {
        if (pnTotals == null) {
            pnTotals = new JPanel();
            pnTotals.setOpaque(false);
            pnTotals.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(1, 0, 0, 0, Branding.BORDER), new EmptyBorder(8, 0, 0, 0)));
            pnTotals.setLayout(new BorderLayout(12, 0));
            pnTotals.add(getLblCounts(), BorderLayout.WEST);
            pnTotals.add(getLblSaleTotal(), BorderLayout.EAST);
        }
        return pnTotals;
    }

    private JLabel getLblCounts() {
        if (lblCounts == null) {
            int units = lines.stream().mapToInt(SaleLine::getUnits).sum();
            lblCounts = new JLabel(lines.size() + (lines.size() == 1 ? " product" : " products") + " · "
                    + units + (units == 1 ? " unit" : " units"));
            lblCounts.setForeground(Branding.TEXT_MUTED);
        }
        return lblCounts;
    }

    private JLabel getLblSaleTotal() {
        if (lblSaleTotal == null) {
            lblSaleTotal = new JLabel("SALE TOTAL:  " + MoneyFormat.format(sale.getAmount()));
            lblSaleTotal.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 16));
        }
        return lblSaleTotal;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
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
}
