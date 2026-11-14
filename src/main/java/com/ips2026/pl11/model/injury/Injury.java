package com.ips2026.pl11.model.injury;

import java.time.LocalDate;

public class Injury {
    private int id;
    private int playerId;
    private int matchId;
    private int trainingId;
    private String description;
    private LocalDate injuryDate;
    
    public Injury(int id, int playerId, int matchId, int trainingId, String description, LocalDate injuryDate) {
        this.id = id;
        this.playerId = playerId;
        this.matchId = matchId;
        this.trainingId = trainingId;
        this.description = description;
        this.injuryDate = injuryDate;
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
    
}
