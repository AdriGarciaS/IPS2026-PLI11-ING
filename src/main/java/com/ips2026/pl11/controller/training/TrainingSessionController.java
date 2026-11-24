package com.ips2026.pl11.controller.training;

import com.ips2026.pl11.controller.log.SystemLog;
import com.ips2026.pl11.model.log.LogAction;
import java.util.List;
import java.util.Optional;

import com.ips2026.pl11.data.training.TrainingSessionDAO;
import com.ips2026.pl11.model.training.TrainingSession;

public class TrainingSessionController {
    
    TrainingSessionDAO trainingSessionDAO;
    
    public TrainingSessionController(TrainingSessionDAO trainingSessionDAO) {
        this.trainingSessionDAO = trainingSessionDAO;
    }
    
    public void scheduleTrainingSession(TrainingSession session) throws Exception {
        if(session.getFacility() < 0) {
            throw new IllegalArgumentException("Facility ID is required.");
        }
        if(session.getCoach() < 0) {
            throw new IllegalArgumentException("Coach ID is required.");
        }
        if(session.getTeam() < 0) {
            throw new IllegalArgumentException("Team ID is required.");
        }
        if(session.getStartTime().compareTo(session.getEndTime()) >= 0) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        trainingSessionDAO.insert(session);
        SystemLog.record(LogAction.INSERT, "Created training session for team #" + session.getTeam() + " on "
                + session.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " " + session.getStartTime() + "-"
                + session.getEndTime() + " (facility #" + session.getFacility() + ")");
    }
    
    public List<TrainingSession> getAllTrainingSessions() throws Exception {
        return trainingSessionDAO.findAll();
    }
    
    public List<TrainingSession> getTrainingSessionsByPlayer(int playerId) throws Exception {
        return trainingSessionDAO.findByPlayer(playerId);
    }
    
    public Optional<TrainingSession> findTrainingById(int trainingId) throws Exception {
        return trainingSessionDAO.findById(trainingId);
    }
    
}
