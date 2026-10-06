package com.ips2026.pl11.data.schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.data.ConexionBD;
import com.ips2026.pl11.model.schedule.DaySchedule;

public class DayScheduleDAO {

    public void insertSchedule(DaySchedule ds) throws SQLException {
        String sql = "insert into day_schedule (work_schedule_id, date, start_time, end_time) values (?, ?, ?, ?)";
        
        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(sql)) {
            
            ps.setLong(1, ds.getWorkScheduleId());
            ps.setString(2, ds.getDate().toString());
            ps.setString(3, String.valueOf(ds.getStartTime()));
            ps.setString(4, String.valueOf(ds.getEndTime()));
            
            ps.executeUpdate();
        }
    }

    public List<DaySchedule> getSchedulesByDate(LocalDate date) throws SQLException {
        String sql = "select id, work_schedule_id, date, start_time, end_time "
            + "from day_schedule where date = ?";

        List<DaySchedule> result = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
            PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, date.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new DaySchedule(rs.getLong("id"), rs.getLong("work_schedule_id"), LocalDate.parse(rs.getString("date")),
                        LocalTime.parse(rs.getString("start_time")), LocalTime.parse(rs.getString("end_time"))));
                }
            }
        }

        return result;
    }
}
