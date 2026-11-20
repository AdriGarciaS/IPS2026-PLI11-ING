package com.ips2026.pl11.controller.injury;

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
    }
    
    public void updateInjury(Injury injury) throws SQLException {
        injuryDAO.updateInjury(injury);
    }
    
    public void loadInjuries() throws SQLException {
        injuries = injuryDAO.getAllInjuries();
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
