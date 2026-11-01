package com.ips2026.pl11.controller.gardening;

import java.sql.SQLException;
import java.util.List;

import com.ips2026.pl11.data.gardening.GardeningSessionDAO;
import com.ips2026.pl11.data.gardening.GardeningSessionEmployeeDAO;
import com.ips2026.pl11.model.gardening.GardeningSession;

public class GardeningSessionController {

    private final GardeningSessionDAO gardeningSessionDAO;
    private final GardeningSessionEmployeeDAO gardeningSessionEmployeeDAO;

    public GardeningSessionController(GardeningSessionDAO gardeningSessionDAO,
        GardeningSessionEmployeeDAO gardeningSessionEmployeeDAO) {
        
        this.gardeningSessionDAO = gardeningSessionDAO;
        this.gardeningSessionEmployeeDAO = gardeningSessionEmployeeDAO;
    }

    public void scheduleGardeningSession(GardeningSession session, List<Long> gardenerIds) throws Exception {
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
        for (Long gardenerId : gardenerIds) {
            gardeningSessionEmployeeDAO.insert(session.getId(), gardenerId);
        }
    }

    public List<GardeningSession> getAllGardeningSessions() throws SQLException {
        return gardeningSessionDAO.findAll();
    }

    public List<GardeningSession> getGardeningSessionsByGardenerId(int gardenerId) throws SQLException {
        return gardeningSessionDAO.findByGardenerId(gardenerId);
    }

}
