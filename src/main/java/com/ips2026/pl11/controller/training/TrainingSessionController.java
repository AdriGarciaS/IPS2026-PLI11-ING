package com.ips2026.pl11.controller.training;

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
        if (session.getDate() == null || session.getDate().isBlank()) {
            throw new IllegalArgumentException("Date is required.");
        }
        if (session.getStartTime() == null || session.getStartTime().isBlank()) {
            throw new IllegalArgumentException("Start time is required.");
        }
        if (session.getEndTime() == null || session.getEndTime().isBlank()) {
            throw new IllegalArgumentException("End time is required.");
        }
        if(session.getStartTime().compareTo(session.getEndTime()) >= 0) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        trainingSessionDAO.insert(session);
    }
    
}
