package com.ips2026.pl11.controller.injury;

import com.ips2026.pl11.controller.log.SystemLog;
import com.ips2026.pl11.model.log.LogAction;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.injury.InjuryDAO;
import com.ips2026.pl11.model.injury.Injury;

public class InjuryController {
    
    private InjuryDAO injuryDAO;
    private List<Injury> injuries;
    
    public InjuryController() {
        this.injuryDAO = new InjuryDAO();
    }
    
    public void registerInjury(Injury injury) throws SQLException {   
        injuryDAO.registerInjury(injury);
        SystemLog.record(LogAction.INSERT, "Registered injury of player #" + injury.getPlayerId() + " on "
            + injury.getInjuryDate() + ": " + injury.getDescription());
    }
    
    public void updateInjury(Injury injury) throws SQLException {
        injuryDAO.updateInjury(injury);
        SystemLog.record(LogAction.UPDATE, "Updated injury #" + injury.getId() + " of player #" + injury.getPlayerId());
    }
    
    public void loadInjuries() throws SQLException {
        injuries = injuryDAO.getAllInjuries();
        SystemLog.record(LogAction.QUERY, "Viewed injuries (" + injuries.size() + ")");
    }

    public List<Injury> findInjuriesByDoctor(int doctorId) {
        List<Injury> result = new ArrayList<>();
        for (Injury injury : injuries) {
            if (injury.getDoctorId() == doctorId) {
                result.add(injury);
            }
        }
        return result;
    }
}
