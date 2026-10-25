package com.ips2026.pl11.view.shareholder;

import com.ips2026.pl11.model.shareholder.ShareHolding;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * List of the IDs of the shares of one holding, one below the other. The
 * first ID after a gap (when the numbers are not consecutive) is shown in
 * bold with the numbers that are missing, so gaps are easy to see.
 *
 * <p>It is a small modal dialog owned by the shares wallet.</p>
 */
public class ShareIdsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final transient ShareHolding holding;

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JLabel lblRanges;
    private JPanel pnList;
    private JScrollPane scrIds;
    private JList<Integer> lstIds;
    private JPanel pnButtons;
    private JButton btnClose;

    public ShareIdsDialog(ShareHolding holding, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.holding = holding;

        setTitle("Share IDs");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(420, 420));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 520, 640, owner);
    }

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("Share IDs",
                    holding.getCampaignName() + " — " + holding.getQuantity() + " shares");
        }
        return pnHeader;
    }

    private JPanel getPnBody() {
        if (pnBody == null) {
            pnBody = new JPanel();
            pnBody.setOpaque(false);
            pnBody.setBorder(new EmptyBorder(14, 14, 14, 14));
            pnBody.setLayout(new BorderLayout(0, 10));
            pnBody.add(getLblRanges(), BorderLayout.NORTH);
            pnBody.add(getPnList(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    /** Summary of the IDs as ranges, for example "1 - 300, 601 - 800". */
    private JLabel getLblRanges() {
        if (lblRanges == null) {
            lblRanges = new JLabel("<html><body style='width: 400px'><b>Ranges:</b> "
                    + holding.getShareIdRanges() + "</body></html>");
            lblRanges.setForeground(Branding.NAVY);
        }
        return lblRanges;
    }

    private JPanel getPnList() {
        if (pnList == null) {
            pnList = new JPanel();
            TitledBorder border = BorderFactory.createTitledBorder("IDs of the shares");
            border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
            pnList.setBorder(border);
            pnList.setLayout(new BorderLayout());
            pnList.add(getScrIds(), BorderLayout.CENTER);
        }
        return pnList;
    }

    private JScrollPane getScrIds() {
        if (scrIds == null) {
            scrIds = new JScrollPane();
            scrIds.setViewportView(getLstIds());
        }
        return scrIds;
    }

    private JList<Integer> getLstIds() {
        if (lstIds == null) {
            List<Integer> ids = holding.getShareIds();
            lstIds = new JList<>(ids.toArray(new Integer[0]));
            lstIds.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            lstIds.setFixedCellHeight(22);
            lstIds.setCellRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    int id = (Integer) value;
                    boolean afterGap = index > 0 && ids.get(index - 1) != id - 1;
                    if (afterGap) {
                        int missingFrom = ids.get(index - 1) + 1;
                        int missingTo = id - 1;
                        String missing = missingFrom == missingTo ? String.valueOf(missingFrom) : missingFrom + " - " + missingTo;
                        setText(id + "      (gap: " + missing + " not in this holding)");
                        setFont(getFont().deriveFont(Font.BOLD));
                        if (!isSelected) {
                            setForeground(Branding.ERROR);
                        }
                    } else {
                        setText(String.valueOf(id));
                    }
                    setBorder(new EmptyBorder(0, 8, 0, 8));
                    return this;
                }
            });
        }
        return lstIds;
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
}
