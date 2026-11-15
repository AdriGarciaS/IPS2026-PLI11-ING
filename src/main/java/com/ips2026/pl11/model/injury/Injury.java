package com.ips2026.pl11.model.injury;

import java.time.LocalDate;
import java.time.LocalTime;

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

    public Injury(int id, int playerId, int matchId, int trainingId, String description, LocalDate injuryDate,
        boolean isHealed, int doctorId, LocalTime injuryTime) {
        this.id = id;
        this.playerId = playerId;
        this.matchId = matchId;
        this.trainingId = trainingId;
        this.description = description;
        this.injuryDate = injuryDate;
        this.isHealed = isHealed;
        this.doctorId = doctorId;
        this.injuryTime = injuryTime;
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

}
