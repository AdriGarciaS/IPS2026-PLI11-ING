package com.ips2026.pl11.controller.interview;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.ips2026.pl11.data.employee.EmployeeDAO;
import com.ips2026.pl11.data.interview.InterviewDAO;
import com.ips2026.pl11.model.employee.Employee;
import com.ips2026.pl11.model.interview.AvailableSlotRecord;

public class InterviewController {

    InterviewDAO interviewDAO;
    EmployeeDAO employeeDAO;

    public InterviewController(InterviewDAO interviewDAO, EmployeeDAO employeeDAO) {
        this.interviewDAO = interviewDAO;
        this.employeeDAO = employeeDAO;
    }

    public List<AvailableSlotRecord> getInterviewSlots() throws SQLException {
        List<AvailableSlotRecord> interviews = interviewDAO.findAll();
        for (AvailableSlotRecord slot : interviews) {
            Optional<Employee> op = employeeDAO.findById(slot.getPlayerId());
            if (op.isPresent()) {
                String playerName = op.get().getFirstName() + " " + op.get().getLastName();
                slot.setPlayerName(playerName);
            }
        }
        return interviews;
    }

}
