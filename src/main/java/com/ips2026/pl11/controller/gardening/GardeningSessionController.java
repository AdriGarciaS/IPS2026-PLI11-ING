package com.ips2026.pl11.controller.gardening;

import java.sql.SQLException;
import java.util.List;

import com.ips2026.pl11.data.gardening.GardeningSessionDAO;
import com.ips2026.pl11.model.gardening.GardeningSession;

public class GardeningSessionController {

    private final GardeningSessionDAO gardeningSessionDAO;

    public GardeningSessionController(GardeningSessionDAO gardeningSessionDAO) {
        this.gardeningSessionDAO = gardeningSessionDAO;
    }

    public void scheduleGardeningSession(GardeningSession session) throws Exception {
        if (session.getSessionType() == null || session.getSessionType().isBlank()) {
            throw new IllegalArgumentException("Session type is required.");
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

        gardeningSessionDAO.insert(session);
    }
    
    public List<GardeningSession> getAllGardeningSessions() throws SQLException {
        return gardeningSessionDAO.findAll();
    }

}
