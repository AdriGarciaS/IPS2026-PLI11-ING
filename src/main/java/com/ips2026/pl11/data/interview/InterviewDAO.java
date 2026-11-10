package com.ips2026.pl11.data.interview;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.interview.AvailableSlotRecord;

public class InterviewDAO {

    public List<AvailableSlotRecord> findAll() throws SQLException {
        String sql = "SELECT * FROM interview_slots";
        
        try(Connection connection = ConexionBD.obtenerConexion();
            PreparedStatement statement = connection.prepareStatement(sql)){
            
            ResultSet resultSet = statement.executeQuery();
            List<AvailableSlotRecord> slots = new ArrayList<>();
            
            while(resultSet.next()) {
                AvailableSlotRecord slot = new AvailableSlotRecord(
                    resultSet.getInt("id"),
                    resultSet.getInt("player_id"),
                    null, // player_name is not retrieved in this query
                    resultSet.getString("slot_date"),
                    resultSet.getString("start_time"),
                    resultSet.getString("end_time")
                );
                slots.add(slot);
            }
            return slots;
        } 
    }
        

}
