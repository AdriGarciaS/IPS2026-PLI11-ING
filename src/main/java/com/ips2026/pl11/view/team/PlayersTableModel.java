package com.ips2026.pl11.view.team;

import com.ips2026.pl11.model.team.SportsEmployee;
import com.ips2026.pl11.model.team.TeamRules;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only table model of players: name, age (counted by year), birth date,
 * gender and whether they can join the team being created.
 */
public class PlayersTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Class<?>[] COLUMN_CLASSES = { String.class, Integer.class, String.class, String.class, String.class };

    private final String[] columns;
    private final int year;
    private final transient Function<SportsEmployee, Optional<String>> problemOf;
    private transient List<SportsEmployee> players = new ArrayList<>();

    /**
     * @param year      year used to count the ages
     * @param problemOf why a player can not join the team (empty if they can)
     */
    public PlayersTableModel(int year, Function<SportsEmployee, Optional<String>> problemOf) {
        this.year = year;
        this.problemOf = problemOf;
        this.columns = new String[] { "Name", "Age in " + year, "Born", "Gender", "Can join?" };
    }

    public void setPlayers(List<SportsEmployee> players) {
        this.players = new ArrayList<>(players);
        fireTableDataChanged();
    }

    public SportsEmployee getPlayerAt(int modelRow) {
        return players.get(modelRow);
    }

    /** True if the player of that row can not join the team. */
    public boolean hasProblem(int modelRow) {
        return problemOf.apply(players.get(modelRow)).isPresent();
    }

    /** Repaints the "Can join?" column after the category or gender of the team changes. */
    public void refreshStatus() {
        if (!players.isEmpty()) {
            fireTableRowsUpdated(0, players.size() - 1);
        }
    }

    @Override
    public int getRowCount() {
        return players.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return COLUMN_CLASSES[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        SportsEmployee player = players.get(row);
        return switch (column) {
            case 0 -> player.getFullName();
            case 1 -> player.getAgeIn(year);
            case 2 -> player.getBirthDate().format(DATE_FORMAT);
            case 3 -> TeamRules.genderLabel(player.getGender());
            case 4 -> problemOf.apply(player).orElse("Yes");
            default -> throw new IllegalArgumentException("Unknown column " + column);
        };
    }
}
