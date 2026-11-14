package com.ips2026.pl11.controller.injury;

import java.sql.SQLException;
import java.util.List;

import com.ips2026.pl11.data.injury.InjuryDAO;
import com.ips2026.pl11.model.injury.Injury;

public class InjuryController {
    
    private InjuryDAO injuryDAO;
    
    public InjuryController() {
        this.injuryDAO = new InjuryDAO();
    }
    
    public void registerInjury(Injury injury) throws SQLException {   
        injuryDAO.registerInjury(injury);
    }
    
    public List<Injury> findAllInjuries() throws SQLException {
        return injuryDAO.getAllInjuries();
    }
}
