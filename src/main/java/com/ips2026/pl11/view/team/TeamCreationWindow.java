package com.ips2026.pl11.view.team;

import com.ips2026.pl11.controller.team.TeamCreationController;
import com.ips2026.pl11.model.team.NewTeam;
import com.ips2026.pl11.model.team.SportsEmployee;
import com.ips2026.pl11.model.team.StaffAssignment;
import com.ips2026.pl11.model.team.TeamCategory;
import com.ips2026.pl11.model.team.TeamGender;
import com.ips2026.pl11.model.team.TeamRules;
import com.ips2026.pl11.view.common.Branding;
import com.ips2026.pl11.view.common.HeaderPanel;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;

/**
 * Window to create a sports team: name, professional (first / subsidiary)
 * or youth category, gender, the two mandatory coaches, optional extra
 * technical staff and at least {@value TeamRules#MIN_PLAYERS} players.
 *
 * <p>It is a modal dialog: the main menu is blocked while it is open. It
 * follows the lazy WindowBuilder style of the main menu.</p>
 */
public class TeamCreationWindow extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final Color NOT_ELIGIBLE = new Color(0x9CA3AF);

    private final transient TeamCreationController controller;
    private final PlayersTableModel availableModel;
    private final PlayersTableModel teamModel;
    private final StaffTableModel staffModel = new StaffTableModel();

    private JPanel contentPane;
    private HeaderPanel pnHeader;
    private JPanel pnBody;
    private JPanel pnLeft;
    private JPanel pnLeftTop;
    private JPanel pnTeam;
    private JLabel lblName;
    private JTextField txtName;
    private JLabel lblType;
    private JPanel pnType;
    private JRadioButton rbProfessional;
    private JRadioButton rbYouth;
    private ButtonGroup typeGroup;
    private JLabel lblProfessional;
    private JComboBox<TeamCategory> cbProfessional;
    private JLabel lblCategory;
    private JComboBox<TeamCategory> cbYouth;
    private JLabel lblGender;
    private JComboBox<TeamGender> cbGender;
    private JLabel lblAgeRule;
    private JPanel pnCoaches;
    private JLabel lblFirstCoach;
    private JComboBox<SportsEmployee> cbFirstCoach;
    private JLabel lblSecondCoach;
    private JComboBox<SportsEmployee> cbSecondCoach;
    private JPanel pnStaff;
    private JPanel pnStaffAdd;
    private JComboBox<SportsEmployee> cbStaff;
    private JTextField txtTask;
    private JButton btnAddStaff;
    private JScrollPane scrStaff;
    private JTable tblStaff;
    private JPanel pnStaffButtons;
    private JButton btnRemoveStaff;
    private JPanel pnPlayers;
    private JPanel pnSearch;
    private JLabel lblSearch;
    private JTextField txtSearch;
    private JCheckBox chkShowAll;
    private JScrollPane scrAvailable;
    private JTable tblAvailable;
    private JPanel pnMoveButtons;
    private JButton btnAddPlayer;
    private JButton btnRemovePlayer;
    private JScrollPane scrTeamPlayers;
    private JTable tblTeamPlayers;
    private JLabel lblPlayerCount;
    private JPanel pnButtons;
    private JButton btnCancel;
    private JButton btnCreate;

    /**
     * @param controller controller with the employees already loaded
     * @param owner      window that opens it (the main menu); it stays
     *                   blocked until this window is closed
     */
    public TeamCreationWindow(TeamCreationController controller, Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.availableModel = new PlayersTableModel(controller.getReferenceYear(),
                player -> controller.findPlayerProblem(player, getSelectedCategory(), getSelectedGender()));
        this.teamModel = new PlayersTableModel(controller.getReferenceYear(),
                player -> controller.findPlayerProblem(player, getSelectedCategory(), getSelectedGender()));

        setTitle("New Sports Team");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        Branding.applyTo(this);
        contentPane.add(getPnHeader(), BorderLayout.NORTH);
        contentPane.add(getPnBody(), BorderLayout.CENTER);
        Branding.setInitialSize(this, 1200, 760, owner);

        teamTypeChanged();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    private HeaderPanel getPnHeader() {
        if (pnHeader == null) {
            pnHeader = new HeaderPanel("New Sports Team",
                    "Create a team with its two coaches and at least " + TeamRules.MIN_PLAYERS + " players");
        }
        return pnHeader;
    }

    /** Everything below the header, with the window margins. */
    private JPanel getPnBody() {
        if (pnBody == null) {
            pnBody = new JPanel();
            pnBody.setOpaque(false);
            pnBody.setBorder(new EmptyBorder(14, 14, 14, 14));
            pnBody.setLayout(new BorderLayout(12, 12));
            pnBody.add(getPnLeft(), BorderLayout.WEST);
            pnBody.add(getPnPlayers(), BorderLayout.CENTER);
            pnBody.add(getPnButtons(), BorderLayout.SOUTH);
        }
        return pnBody;
    }

    /** Left column: team data, coaches and extra staff. */
    private JPanel getPnLeft() {
        if (pnLeft == null) {
            pnLeft = new JPanel();
            pnLeft.setOpaque(false);
            pnLeft.setPreferredSize(new Dimension(470, 10));
            pnLeft.setLayout(new BorderLayout(0, 12));
            pnLeft.add(getPnLeftTop(), BorderLayout.NORTH);
            pnLeft.add(getPnStaff(), BorderLayout.CENTER);
        }
        return pnLeft;
    }

    private JPanel getPnLeftTop() {
        if (pnLeftTop == null) {
            pnLeftTop = new JPanel();
            pnLeftTop.setOpaque(false);
            pnLeftTop.setLayout(new BoxLayout(pnLeftTop, BoxLayout.Y_AXIS));
            pnLeftTop.add(getPnTeam());
            pnLeftTop.add(javax.swing.Box.createVerticalStrut(12));
            pnLeftTop.add(getPnCoaches());
        }
        return pnLeftTop;
    }

    private JPanel getPnTeam() {
        if (pnTeam == null) {
            pnTeam = new JPanel();
            pnTeam.setBorder(titled("1. Team"));
            pnTeam.setLayout(new GridBagLayout());
            addFormRow(pnTeam, 0, getLblName(), getTxtName());
            addFormRow(pnTeam, 1, getLblType(), getPnType());
            addFormRow(pnTeam, 2, getLblProfessional(), getCbProfessional());
            addFormRow(pnTeam, 3, getLblCategory(), getCbYouth());
            addFormRow(pnTeam, 4, getLblGender(), getCbGender());

            GridBagConstraints gbcRule = new GridBagConstraints();
            gbcRule.anchor = GridBagConstraints.WEST;
            gbcRule.gridx = 0;
            gbcRule.gridy = 5;
            gbcRule.gridwidth = 2;
            pnTeam.add(getLblAgeRule(), gbcRule);
        }
        return pnTeam;
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
            txtName = new JTextField(20);
            txtName.setToolTipText("Name of the team, for example \"Cadete A\" (it must be unique)");
        }
        return txtName;
    }

    private JLabel getLblType() {
        if (lblType == null) {
            lblType = new JLabel("Type:");
        }
        return lblType;
    }

    private JPanel getPnType() {
        if (pnType == null) {
            pnType = new JPanel();
            pnType.setOpaque(false);
            pnType.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            typeGroup = new ButtonGroup();
            typeGroup.add(getRbProfessional());
            typeGroup.add(getRbYouth());
            pnType.add(getRbProfessional());
            pnType.add(getRbYouth());
        }
        return pnType;
    }

    private JRadioButton getRbProfessional() {
        if (rbProfessional == null) {
            rbProfessional = new JRadioButton("Professional");
            rbProfessional.setOpaque(false);
            rbProfessional.setMnemonic('P');
            rbProfessional.addActionListener(event -> teamTypeChanged());
        }
        return rbProfessional;
    }

    private JRadioButton getRbYouth() {
        if (rbYouth == null) {
            rbYouth = new JRadioButton("Youth");
            rbYouth.setOpaque(false);
            rbYouth.setMnemonic('Y');
            rbYouth.addActionListener(event -> teamTypeChanged());
        }
        return rbYouth;
    }

    private JLabel getLblProfessional() {
        if (lblProfessional == null) {
            lblProfessional = new JLabel("Team:");
            lblProfessional.setLabelFor(getCbProfessional());
        }
        return lblProfessional;
    }

    private JComboBox<TeamCategory> getCbProfessional() {
        if (cbProfessional == null) {
            cbProfessional = new JComboBox<>();
            for (TeamCategory category : TeamCategory.values()) {
                if (category.isProfessional()) {
                    cbProfessional.addItem(category);
                }
            }
            cbProfessional.addActionListener(event -> categoryChanged());
        }
        return cbProfessional;
    }

    private JLabel getLblCategory() {
        if (lblCategory == null) {
            lblCategory = new JLabel("Category:");
            lblCategory.setLabelFor(getCbYouth());
        }
        return lblCategory;
    }

    private JComboBox<TeamCategory> getCbYouth() {
        if (cbYouth == null) {
            cbYouth = new JComboBox<>();
            for (TeamCategory category : TeamCategory.values()) {
                if (!category.isProfessional()) {
                    cbYouth.addItem(category);
                }
            }
            cbYouth.addActionListener(event -> categoryChanged());
        }
        return cbYouth;
    }

    private JLabel getLblGender() {
        if (lblGender == null) {
            lblGender = new JLabel("Gender:");
            lblGender.setDisplayedMnemonic('G');
            lblGender.setLabelFor(getCbGender());
        }
        return lblGender;
    }

    private JComboBox<TeamGender> getCbGender() {
        if (cbGender == null) {
            cbGender = new JComboBox<>(TeamGender.values());
            cbGender.setSelectedIndex(-1);
            cbGender.setRenderer(placeholderRenderer("Choose the gender of the players"));
            cbGender.addActionListener(event -> categoryChanged());
        }
        return cbGender;
    }

    private JLabel getLblAgeRule() {
        if (lblAgeRule == null) {
            lblAgeRule = new JLabel();
            lblAgeRule.setForeground(Branding.TEXT_MUTED);
        }
        return lblAgeRule;
    }

    private JPanel getPnCoaches() {
        if (pnCoaches == null) {
            pnCoaches = new JPanel();
            pnCoaches.setBorder(titled("2. Coaches (required)"));
            pnCoaches.setLayout(new GridBagLayout());
            addFormRow(pnCoaches, 0, getLblFirstCoach(), getCbFirstCoach());
            addFormRow(pnCoaches, 1, getLblSecondCoach(), getCbSecondCoach());
        }
        return pnCoaches;
    }

    private JLabel getLblFirstCoach() {
        if (lblFirstCoach == null) {
            lblFirstCoach = new JLabel("First coach:");
            lblFirstCoach.setDisplayedMnemonic('F');
            lblFirstCoach.setLabelFor(getCbFirstCoach());
        }
        return lblFirstCoach;
    }

    private JComboBox<SportsEmployee> getCbFirstCoach() {
        if (cbFirstCoach == null) {
            cbFirstCoach = technicalStaffCombo("Choose the first coach");
        }
        return cbFirstCoach;
    }

    private JLabel getLblSecondCoach() {
        if (lblSecondCoach == null) {
            lblSecondCoach = new JLabel("Second coach:");
            lblSecondCoach.setDisplayedMnemonic('o');
            lblSecondCoach.setLabelFor(getCbSecondCoach());
        }
        return lblSecondCoach;
    }

    private JComboBox<SportsEmployee> getCbSecondCoach() {
        if (cbSecondCoach == null) {
            cbSecondCoach = technicalStaffCombo("Choose the second coach");
        }
        return cbSecondCoach;
    }

    /** Combo with the technical sports employees and nothing chosen at first. */
    private JComboBox<SportsEmployee> technicalStaffCombo(String placeholder) {
        JComboBox<SportsEmployee> combo = new JComboBox<>();
        for (SportsEmployee employee : controller.getTechnicalStaff()) {
            combo.addItem(employee);
        }
        combo.setSelectedIndex(-1);
        combo.setRenderer(placeholderRenderer(placeholder));
        return combo;
    }

    private JPanel getPnStaff() {
        if (pnStaff == null) {
            pnStaff = new JPanel();
            pnStaff.setBorder(titled("4. Other technical staff (optional)"));
            pnStaff.setLayout(new BorderLayout(0, 8));
            pnStaff.add(getPnStaffAdd(), BorderLayout.NORTH);
            pnStaff.add(getScrStaff(), BorderLayout.CENTER);
            pnStaff.add(getPnStaffButtons(), BorderLayout.SOUTH);
        }
        return pnStaff;
    }

    private JPanel getPnStaffAdd() {
        if (pnStaffAdd == null) {
            pnStaffAdd = new JPanel();
            pnStaffAdd.setOpaque(false);
            pnStaffAdd.setLayout(new GridBagLayout());

            GridBagConstraints gbcCombo = new GridBagConstraints();
            gbcCombo.fill = GridBagConstraints.HORIZONTAL;
            gbcCombo.weightx = 0.55;
            gbcCombo.insets = new Insets(0, 0, 0, 8);
            pnStaffAdd.add(getCbStaff(), gbcCombo);

            GridBagConstraints gbcTaskLabel = new GridBagConstraints();
            gbcTaskLabel.insets = new Insets(0, 0, 0, 6);
            JLabel lblTask = new JLabel("Task:");
            lblTask.setDisplayedMnemonic('k');
            lblTask.setLabelFor(getTxtTask());
            pnStaffAdd.add(lblTask, gbcTaskLabel);

            GridBagConstraints gbcTask = new GridBagConstraints();
            gbcTask.fill = GridBagConstraints.HORIZONTAL;
            gbcTask.weightx = 0.45;
            gbcTask.insets = new Insets(0, 0, 0, 8);
            pnStaffAdd.add(getTxtTask(), gbcTask);

            pnStaffAdd.add(getBtnAddStaff(), new GridBagConstraints());
        }
        return pnStaffAdd;
    }

    private JComboBox<SportsEmployee> getCbStaff() {
        if (cbStaff == null) {
            cbStaff = technicalStaffCombo("Technical employee");
            cbStaff.setPreferredSize(new Dimension(150, cbStaff.getPreferredSize().height));
        }
        return cbStaff;
    }

    private JTextField getTxtTask() {
        if (txtTask == null) {
            txtTask = new JTextField(10);
            txtTask.setToolTipText("What this employee does in the team, for example \"Goalkeepers\"");
            txtTask.addActionListener(event -> addStaff());
        }
        return txtTask;
    }

    private JButton getBtnAddStaff() {
        if (btnAddStaff == null) {
            btnAddStaff = new JButton("Add");
            btnAddStaff.setMnemonic('d');
            btnAddStaff.addActionListener(event -> addStaff());
        }
        return btnAddStaff;
    }

    private JScrollPane getScrStaff() {
        if (scrStaff == null) {
            scrStaff = new JScrollPane();
            scrStaff.setViewportView(getTblStaff());
            scrStaff.setPreferredSize(new Dimension(300, 90));
        }
        return scrStaff;
    }

    private JTable getTblStaff() {
        if (tblStaff == null) {
            tblStaff = new JTable(staffModel);
            configureTable(tblStaff, ListSelectionModel.SINGLE_SELECTION);
            tblStaff.getSelectionModel().addListSelectionListener(event -> updateButtons());
        }
        return tblStaff;
    }

    private JPanel getPnStaffButtons() {
        if (pnStaffButtons == null) {
            pnStaffButtons = new JPanel();
            pnStaffButtons.setOpaque(false);
            pnStaffButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnStaffButtons.add(getBtnRemoveStaff());
        }
        return pnStaffButtons;
    }

    private JButton getBtnRemoveStaff() {
        if (btnRemoveStaff == null) {
            btnRemoveStaff = new JButton("Remove");
            btnRemoveStaff.addActionListener(event -> removeStaff());
        }
        return btnRemoveStaff;
    }

    private JPanel getPnPlayers() {
        if (pnPlayers == null) {
            pnPlayers = new JPanel();
            pnPlayers.setBorder(titled("3. Players"));
            pnPlayers.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 0, 6, 0);

            gbc.gridy = 0;
            pnPlayers.add(getPnSearch(), gbc);
            gbc.gridy = 1;
            pnPlayers.add(boldLabel("Available players (double click to add)"), gbc);
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            pnPlayers.add(getScrAvailable(), gbc);
            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0;
            pnPlayers.add(getPnMoveButtons(), gbc);
            gbc.gridy = 4;
            pnPlayers.add(boldLabel("Team players"), gbc);
            gbc.gridy = 5;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            pnPlayers.add(getScrTeamPlayers(), gbc);
            gbc.gridy = 6;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0;
            gbc.insets = new Insets(0, 0, 0, 0);
            pnPlayers.add(getLblPlayerCount(), gbc);
        }
        return pnPlayers;
    }

    private JPanel getPnSearch() {
        if (pnSearch == null) {
            pnSearch = new JPanel();
            pnSearch.setOpaque(false);
            pnSearch.setLayout(new BorderLayout(8, 0));
            pnSearch.add(getLblSearch(), BorderLayout.WEST);
            pnSearch.add(getTxtSearch(), BorderLayout.CENTER);
            pnSearch.add(getChkShowAll(), BorderLayout.EAST);
        }
        return pnSearch;
    }

    private JLabel getLblSearch() {
        if (lblSearch == null) {
            lblSearch = new JLabel("Search by name:");
            lblSearch.setDisplayedMnemonic('h');
            lblSearch.setLabelFor(getTxtSearch());
        }
        return lblSearch;
    }

    private JTextField getTxtSearch() {
        if (txtSearch == null) {
            txtSearch = new JTextField();
            txtSearch.setToolTipText("Shows the players whose name contains this text (not case sensitive)");
            txtSearch.getDocument().addDocumentListener(onChange(this::refreshAvailable));
        }
        return txtSearch;
    }

    private JCheckBox getChkShowAll() {
        if (chkShowAll == null) {
            chkShowAll = new JCheckBox("Show players that can not join");
            chkShowAll.setOpaque(false);
            chkShowAll.setMnemonic('w');
            chkShowAll.addActionListener(event -> refreshAvailable());
        }
        return chkShowAll;
    }

    private JScrollPane getScrAvailable() {
        if (scrAvailable == null) {
            scrAvailable = new JScrollPane();
            scrAvailable.setViewportView(getTblAvailable());
            scrAvailable.setPreferredSize(new Dimension(500, 160));
        }
        return scrAvailable;
    }

    private JTable getTblAvailable() {
        if (tblAvailable == null) {
            tblAvailable = playersTable(availableModel, NOT_ELIGIBLE);
            onDoubleClick(tblAvailable, event -> addPlayers());
            tblAvailable.getSelectionModel().addListSelectionListener(event -> updateButtons());
        }
        return tblAvailable;
    }

    private JPanel getPnMoveButtons() {
        if (pnMoveButtons == null) {
            pnMoveButtons = new JPanel();
            pnMoveButtons.setOpaque(false);
            pnMoveButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
            pnMoveButtons.add(getBtnAddPlayer());
            pnMoveButtons.add(getBtnRemovePlayer());
        }
        return pnMoveButtons;
    }

    private JButton getBtnAddPlayer() {
        if (btnAddPlayer == null) {
            btnAddPlayer = new JButton("Add to team ↓");
            btnAddPlayer.setMnemonic('A');
            btnAddPlayer.addActionListener(event -> addPlayers());
        }
        return btnAddPlayer;
    }

    private JButton getBtnRemovePlayer() {
        if (btnRemovePlayer == null) {
            btnRemovePlayer = new JButton("↑ Remove from team");
            btnRemovePlayer.setMnemonic('R');
            btnRemovePlayer.addActionListener(event -> removePlayers());
        }
        return btnRemovePlayer;
    }

    private JScrollPane getScrTeamPlayers() {
        if (scrTeamPlayers == null) {
            scrTeamPlayers = new JScrollPane();
            scrTeamPlayers.setViewportView(getTblTeamPlayers());
            scrTeamPlayers.setPreferredSize(new Dimension(500, 160));
        }
        return scrTeamPlayers;
    }

    private JTable getTblTeamPlayers() {
        if (tblTeamPlayers == null) {
            tblTeamPlayers = playersTable(teamModel, Branding.ERROR);
            onDoubleClick(tblTeamPlayers, event -> removePlayers());
            tblTeamPlayers.getSelectionModel().addListSelectionListener(event -> updateButtons());
        }
        return tblTeamPlayers;
    }

    private JLabel getLblPlayerCount() {
        if (lblPlayerCount == null) {
            lblPlayerCount = new JLabel();
            lblPlayerCount.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        }
        return lblPlayerCount;
    }

    private JPanel getPnButtons() {
        if (pnButtons == null) {
            pnButtons = new JPanel();
            pnButtons.setOpaque(false);
            pnButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnButtons.add(getBtnCancel());
            pnButtons.add(getBtnCreate());
        }
        return pnButtons;
    }

    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton("Cancel");
            btnCancel.setMnemonic('C');
            btnCancel.addActionListener(event -> dispose());
        }
        return btnCancel;
    }

    private JButton getBtnCreate() {
        if (btnCreate == null) {
            btnCreate = new JButton("Create team");
            btnCreate.setMnemonic('t');
            btnCreate.setFont(btnCreate.getFont().deriveFont(Font.BOLD));
            btnCreate.addActionListener(event -> createTeam());
        }
        return btnCreate;
    }

    // ------------------------------------------------------------------
    // Helpers to build components
    // ------------------------------------------------------------------

    private static TitledBorder titled(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 13));
        return border;
    }

    private static JLabel boldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Branding.FONT_FAMILY, Font.BOLD, 12));
        return label;
    }

    private static void addFormRow(JPanel panel, int row, JLabel label, Component field) {
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

    /** Renderer that shows {@code placeholder} in grey while nothing is chosen. */
    private static DefaultListCellRenderer placeholderRenderer(String placeholder) {
        return new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText(placeholder);
                    setForeground(Branding.TEXT_MUTED);
                }
                return component;
            }
        };
    }

    /** Common settings of the tables: read-only, sortable, row height 24. */
    private static void configureTable(JTable table, int selectionMode) {
        table.setSelectionMode(selectionMode);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
    }

    /**
     * Table of players where the rows of players that can not join the team
     * are painted with {@code problemColor}. Several rows can be selected to
     * add or remove many players at once.
     */
    private static JTable playersTable(PlayersTableModel model, Color problemColor) {
        JTable table = new JTable(model) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    boolean problem = model.hasProblem(convertRowIndexToModel(row));
                    component.setForeground(problem ? problemColor : getForeground());
                }
                return component;
            }
        };
        configureTable(table, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(170);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(85);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(230);
        return table;
    }

    private static void onDoubleClick(JTable table, Consumer<MouseEvent> action) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && table.rowAtPoint(event.getPoint()) >= 0) {
                    action.accept(event);
                }
            }
        });
    }

    private static DocumentListener onChange(Runnable action) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                action.run();
            }
        };
    }

    // ------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------

    /** The first/subsidiary combo is used for professional teams, the category combo for youth teams. */
    private TeamCategory getSelectedCategory() {
        if (getRbProfessional().isSelected()) {
            return (TeamCategory) getCbProfessional().getSelectedItem();
        }
        if (getRbYouth().isSelected()) {
            return (TeamCategory) getCbYouth().getSelectedItem();
        }
        return null;
    }

    private TeamGender getSelectedGender() {
        return (TeamGender) getCbGender().getSelectedItem();
    }

    private void teamTypeChanged() {
        getCbProfessional().setEnabled(getRbProfessional().isSelected());
        getLblProfessional().setEnabled(getRbProfessional().isSelected());
        getCbYouth().setEnabled(getRbYouth().isSelected());
        getLblCategory().setEnabled(getRbYouth().isSelected());
        categoryChanged();
    }

    /** The category or gender changed: the age rule and which players can join change too. */
    private void categoryChanged() {
        TeamCategory category = getSelectedCategory();
        getLblAgeRule().setText(category == null
                ? "Choose the type of team to see the age rule."
                : category.describeAgeRule(controller.getReferenceYear()));
        refreshAvailable();
        teamModel.refreshStatus();
        updatePlayerCount();
    }

    private void refreshAvailable() {
        availableModel.setPlayers(controller.searchAvailablePlayers(getTxtSearch().getText(), getSelectedCategory(),
                getSelectedGender(), !getChkShowAll().isSelected()));
        updateButtons();
    }

    private void updatePlayerCount() {
        int count = controller.getTeamPlayers().size();
        if (count >= TeamRules.MIN_PLAYERS) {
            getLblPlayerCount().setForeground(Branding.SUCCESS);
            getLblPlayerCount().setText("Players: " + count + "  (minimum of " + TeamRules.MIN_PLAYERS + " reached)");
        } else {
            getLblPlayerCount().setForeground(Branding.ERROR);
            getLblPlayerCount().setText("Players: " + count + " / at least " + TeamRules.MIN_PLAYERS);
        }
        updateButtons();
    }

    private void updateButtons() {
        getBtnAddPlayer().setEnabled(getTblAvailable().getSelectedRowCount() > 0);
        getBtnRemovePlayer().setEnabled(getTblTeamPlayers().getSelectedRowCount() > 0);
        getBtnRemoveStaff().setEnabled(getTblStaff().getSelectedRow() >= 0);
    }

    private List<SportsEmployee> selectedPlayers(JTable table, PlayersTableModel model) {
        List<SportsEmployee> selected = new ArrayList<>();
        for (int viewRow : table.getSelectedRows()) {
            selected.add(model.getPlayerAt(table.convertRowIndexToModel(viewRow)));
        }
        return selected;
    }

    /** Adds the selected players; the ones that can not join are skipped and listed in a warning. */
    private void addPlayers() {
        List<String> rejected = new ArrayList<>();
        for (SportsEmployee player : selectedPlayers(getTblAvailable(), availableModel)) {
            try {
                controller.addPlayer(player, getSelectedCategory(), getSelectedGender());
            } catch (IllegalArgumentException exception) {
                rejected.add(exception.getMessage());
            }
        }
        teamModel.setPlayers(controller.getTeamPlayers());
        refreshAvailable();
        updatePlayerCount();
        if (!rejected.isEmpty()) {
            showWarning(String.join("\n", rejected));
        }
    }

    private void removePlayers() {
        for (SportsEmployee player : selectedPlayers(getTblTeamPlayers(), teamModel)) {
            controller.removePlayer(player);
        }
        teamModel.setPlayers(controller.getTeamPlayers());
        refreshAvailable();
        updatePlayerCount();
    }

    private void addStaff() {
        SportsEmployee employee = (SportsEmployee) getCbStaff().getSelectedItem();
        if (employee == null) {
            showWarning("Choose the technical employee to add.");
            return;
        }
        try {
            controller.addStaff(employee, getTxtTask().getText(), (SportsEmployee) getCbFirstCoach().getSelectedItem(),
                    (SportsEmployee) getCbSecondCoach().getSelectedItem());
        } catch (IllegalArgumentException exception) {
            showWarning(exception.getMessage());
            return;
        }
        staffModel.setStaff(controller.getTeamStaff());
        getCbStaff().setSelectedIndex(-1);
        getTxtTask().setText("");
        updateButtons();
    }

    private void removeStaff() {
        int viewRow = getTblStaff().getSelectedRow();
        if (viewRow >= 0) {
            controller.removeStaff(staffModel.getAssignmentAt(getTblStaff().convertRowIndexToModel(viewRow)).getEmployee());
            staffModel.setStaff(controller.getTeamStaff());
            updateButtons();
        }
    }

    private void createTeam() {
        NewTeam team = controller.buildTeam(getTxtName().getText(), getSelectedCategory(), getSelectedGender(),
                (SportsEmployee) getCbFirstCoach().getSelectedItem(), (SportsEmployee) getCbSecondCoach().getSelectedItem());
        try {
            List<String> problems = controller.findProblems(team);
            if (!problems.isEmpty()) {
                showProblems(problems);
                return;
            }

            int option = JOptionPane.showConfirmDialog(this,
                    "Create the team \"" + team.getName() + "\" (" + team.getCategory().getLabel() + ", "
                            + team.getGender() + ") with " + team.getPlayers().size() + " players?",
                    "Confirm team creation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
            int id = controller.create(team);
            JOptionPane.showMessageDialog(this, buildSummary(id, team), "Team created", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException exception) {
            // Something changed in the database since the window was opened.
            showProblems(List.of(exception.getMessage().split("\n")));
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, "The team could not be created:\n" + exception.getMessage(),
                    "Team creation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showProblems(List<String> problems) {
        StringBuilder html = new StringBuilder("<html><body style='width: 380px'><b>The team can not be created yet:</b><ul>");
        for (String problem : problems) {
            html.append("<li>").append(escape(problem)).append("</li>");
        }
        JOptionPane.showMessageDialog(this, html.append("</ul></body></html>").toString(), "New Sports Team",
                JOptionPane.WARNING_MESSAGE);
    }

    private static String buildSummary(int id, NewTeam team) {
        StringBuilder html = new StringBuilder("<html><body style='width: 340px'>");
        html.append("<b>Team nº ").append(id).append(": ").append(escape(team.getName())).append("</b>")
                .append("<table cellpadding='2' style='margin-top: 8px'>")
                .append(row("Type", team.getCategory().isProfessional() ? "Professional" : "Youth"))
                .append(row("Category", team.getCategory().toString()))
                .append(row("Gender", team.getGender().toString()))
                .append(row("First coach", team.getFirstCoach().getFullName()))
                .append(row("Second coach", team.getSecondCoach().getFullName()))
                .append(row("Players", String.valueOf(team.getPlayers().size())));
        for (StaffAssignment assignment : team.getStaff()) {
            html.append(row("Staff", assignment.getEmployee().getFullName() + " (" + assignment.getTask() + ")"));
        }
        return html.append("</table></body></html>").toString();
    }

    private static String row(String label, String value) {
        return "<tr><td style='color: #6B7280'>" + label + "</td><td>" + escape(value) + "</td></tr>";
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "New Sports Team", JOptionPane.WARNING_MESSAGE);
    }
}
