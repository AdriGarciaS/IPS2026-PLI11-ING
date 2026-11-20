package com.ips2026.pl11.controller.team;

import java.sql.SQLException;
import java.util.List;

import com.ips2026.pl11.data.team.TeamDAO;
import com.ips2026.pl11.data.team.TeamDAO.TeamItem;
import com.ips2026.pl11.model.employee.Employee;

public class TeamController {

    private final TeamDAO teamDAO;

    public TeamController(TeamDAO teamDAO) {
        this.teamDAO = teamDAO;
    }
    
    public List<TeamItem> getTeams() throws SQLException {
        return teamDAO.findAll();
    }
    
    public List<TeamItem> getTeamsByCoach(int coachId) throws SQLException {
        return teamDAO.findTeamsByCoach(coachId);
    }
    
    public List<Employee> getPlayersByTeam(int teamId) throws SQLException {
        return teamDAO.findPlayersByTeam(teamId);
    }
    
    
}
