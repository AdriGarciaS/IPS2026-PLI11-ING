package com.ips2026.pl11.model.injury;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Injury {
    private int id;
    private int playerId;
    private int doctorId;
    private int matchId;
    private int trainingId;
    private String description;
    private LocalDate injuryDate;
    private LocalTime injuryTime;
    private boolean isHealed;
    private List<String[]> evolutionNotes;

    public Injury(int id, int playerId, int matchId, int trainingId, String description, LocalDate injuryDate,
        boolean isHealed, int doctorId, LocalTime injuryTime) {

        this(id, playerId, matchId, trainingId, description, injuryDate, isHealed, doctorId, injuryTime,
            new ArrayList<>());
    }

    public Injury(int id, int playerId, int matchId, int trainingId, String description, LocalDate injuryDate,
        boolean isHealed, int doctorId, LocalTime injuryTime, String parsedEvolutionNotes) {

        this(id, playerId, matchId, trainingId, description, injuryDate, isHealed, doctorId, injuryTime,
            unparseEvolutionNotes(parsedEvolutionNotes));
    }

    public Injury(int id, int playerId, int matchId, int trainingId, String description, LocalDate injuryDate,
        boolean isHealed, int doctorId, LocalTime injuryTime, List<String[]> evolutionNotes) {
        this.id = id;
        this.playerId = playerId;
        this.matchId = matchId;
        this.trainingId = trainingId;
        this.description = description;
        this.injuryDate = injuryDate;
        this.isHealed = isHealed;
        this.doctorId = doctorId;
        this.injuryTime = injuryTime;
        this.evolutionNotes = evolutionNotes;
    }

    public void setHealed(boolean healed) {
        isHealed = healed;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getMatchId() {
        return matchId;
    }

    public int getTrainingId() {
        return trainingId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getInjuryDate() {
        return injuryDate;
    }

    public boolean isHealed() {
        return isHealed;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public LocalTime getInjuryTime() {
        return injuryTime;
    }

    public List<String[]> getEvolutionNotes() {
        return evolutionNotes;
    }

    void setEvolutionNotes(List<String[]> evolutionNotes) {
        this.evolutionNotes = evolutionNotes;
    }

    public String getParsedEvolutionNotes() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < evolutionNotes.size(); i++) {
            String[] note = evolutionNotes.get(i);
            if (i > 0) {
                sb.append("|");
            }

            sb.append("[").append(note[0]).append("&").append(note[1]).append("]");
        }
        sb.append("]");
        return sb.toString();
    }

    private static List<String[]> unparseEvolutionNotes(String parsedEvolutionNotes) {
        List<String[]> notes = new ArrayList<>();
        if (parsedEvolutionNotes == null || parsedEvolutionNotes.isBlank() || parsedEvolutionNotes.equals("[]")) {
            return notes;
        }

        String trimmed = parsedEvolutionNotes.substring(1, parsedEvolutionNotes.length() - 1);
        String[] notePairs = trimmed.split("\\|");

        for (String notePair : notePairs) {
            notePair = notePair.replace("[", "").replace("]", "");
            String[] note = notePair.split("&", 2);
            if (note.length == 2) {
                notes.add(note);
            }
        }
        return notes;
    }

}
